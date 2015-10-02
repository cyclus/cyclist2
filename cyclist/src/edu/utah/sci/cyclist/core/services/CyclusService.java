package edu.utah.sci.cyclist.core.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;

import edu.utah.sci.cyclist.core.controller.IMemento;
import edu.utah.sci.cyclist.core.model.CyclusJob;
import edu.utah.sci.cyclist.core.model.CyclusJob.Status;
import edu.utah.sci.cyclist.core.model.Preferences;

public class CyclusService {
	public static final String SUBMIT_JOB_PATH = "/api/v1/job";
	public static final String SUBMIT_PATH = "/api/v1/job-infile";
	public static final String STATUS_PATH = "/api/v1/job-stat/";
	public static final String LOAD_PATH  =  "/api/v1/job-outfiles/";
	
	private ListProperty<CyclusJob> _jobs = new SimpleListProperty<>(FXCollections.observableArrayList());
	private Map<String, ScheduledService<JobStatus>> _running = new HashMap<>();
	static Logger log = Logger.getLogger(CyclusService.class);
	Preferences _preferences = Preferences.getInstance();

	public CyclusService() {
		_jobs.addListener(new ListChangeListener<CyclusJob>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends CyclusJob> c) {
				while (c.next()) {
					for (CyclusJob job : c.getRemoved()) {
						ScheduledService<JobStatus> service = _running.remove(job.getId());
						if (service != null) {
							log.info("Job " + job.getAlias() + " canceled");
							service.cancel();
						}
					}
				}
			}
		});
	}

    public ListProperty<CyclusJob> jobs() {
        return _jobs;
    }

    public CyclusJob latestJob() {
        return _jobs.getValue().get(_jobs.getValue().size() - 1);
    }

	public void save(IMemento memento) {
		for (CyclusJob job : _jobs) {
			IMemento j_memento = memento.createChild("job");
			j_memento.putString("id", job.getId());
			j_memento.putString("status", job.getStatus().toString());
			j_memento.putString("alias", job.getAlias());
			j_memento.putString("datafile", job.getDatafilePath());
			j_memento.putString("url", job.getServerUrl());
		}
	}
	
	public void restore(IMemento memento) {
		if (memento == null) return;
		for (IMemento j_memento : memento.getChildren("job")) {
			CyclusJob job = CyclusJob.restore(j_memento.getString("id"));
			job.setStatus(j_memento.getString("status"));
			job.setAlias(j_memento.getString("alias"));
			job.setDatafilePath(j_memento.getString("datafile"));
			String serverUrl = j_memento.getString("url");
			if(serverUrl == null){
				job.setServerUrl(Preferences.CLOUDIUS_URL);
			}else{
				job.setServerUrl(serverUrl);
			}
			_jobs.add(job);
			switch (job.getStatus()) {
			case INIT:
				// can not happen
			case SUBMITTED:
				poll(job);
				break;
			case COMPLETED:
				break;
			case FAILED:
				// noting to do
				break;
			case LOADING:
				// reload
				loadData(job);
				break;
			case READY:
				// nothing to do
				break;
			}
		}
	}
	
    private CyclusJob _submit(String path, Request request, String serverUrl) {
        CyclusJob job = new CyclusJob(path,serverUrl);

        try {
            InputStream stream = request.execute().returnContent().asStream();
            JsonReader reader = Json.createReader(stream);
            JsonObject info = reader.readObject();
            job.setInfo(info);
            job.setStatus(Status.SUBMITTED);
            _jobs.add(job);
            poll(job);
        } catch (ClientProtocolException e) {
        	String msg = e.getMessage() != null ? ": "+e.getMessage() : "";
            log.error("Submit job communication error "+ msg);
            job.setStatus(Status.FAILED);
        } catch (IOException e) {
        	String msg = e.getMessage() != null ? ": "+e.getMessage() : "";
            log.error("submit job IO error"+msg);
            job.setStatus(Status.FAILED);
        }
        return job;
    }

    public CyclusJob submit(File file, String serverUrl) {
    	String path = file.getAbsolutePath();
        Request request = Request.Post(serverUrl + SUBMIT_PATH)
            .bodyFile(file, ContentType.DEFAULT_TEXT);
        return this._submit(path, request,serverUrl);
    }

    public CyclusJob submit(String cmd) {
    	return submit(cmd, Preferences.CLOUDIUS_URL);
    }
    
    public CyclusJob submit(String cmd, String url) {
        // cmd is a string of XML 
        String path = "cycic.xml";
        Request request = Request.Post(url+ SUBMIT_PATH)
            .bodyString(cmd, ContentType.DEFAULT_TEXT);
        return this._submit(path, request,url);
    }

    public CyclusJob submitCmd(String cmd, String... args) {
    	return submitCmdToRemote(Preferences.CLOUDIUS_URL, cmd, args);
    }
    
    public CyclusJob submitCmdToRemote(String url, String cmd, String... args) {
    	String name = "cmd";
        String uid = UUID.randomUUID().toString().replace("-", "");
        String reqJSON = "{\"Id\": \"" + uid + "\", \"Cmd\": [\"" + cmd + "\"";
        for (String arg : args) {
            reqJSON += ", \"" + arg + "\"";
        }
        reqJSON += "]}";
        Request request = Request.Post(url+SUBMIT_JOB_PATH)
            .bodyString(reqJSON, ContentType.APPLICATION_JSON);
        return this._submit(name, request,url);
	}

    private void poll(final CyclusJob job) {
		final PollService service = new PollService(job);

		service.setDelay(Duration.seconds(0));
		service.setPeriod(Duration.seconds(5));

		service.lastValueProperty().addListener(
				new ChangeListener<JobStatus>() {

					@Override
					public void changed(
							ObservableValue<? extends JobStatus> observable,
							JobStatus prev, JobStatus current) {
						if (current.info != null) {
							current.job.setInfo(current.info);
							current.job.setStatus(current.status);
							switch (current.job.getStatus()) {
							case COMPLETED:
								service.cancel();
								_running.remove(job.getId());
								loadData(current.job);
								break;
							case FAILED:
								service.cancel();
								_running.remove(job.getId());
								break;
							case SUBMITTED:
							case INIT:
								break;
							case LOADING:
							case READY:
								// cannot occur during this stage
								break;
							}
						}
					}
				});

		_running.put(job.getId(), service);
		service.start();
	}

	private Path createSaveDirectory(String id) throws IOException {
		String home = System.getProperty("user.home");
		Path dir = Paths.get(home, "cyclist/data/", id);
		dir.toFile().mkdirs();
		return dir;
	}
	
	private void loadData(final CyclusJob job) {
		job.setStatus("loading");
		Task<JobStatus> task = new Task<JobStatus>() {
			protected JobStatus call() {
				String msg;
				try {
					Path dir = createSaveDirectory(job.getId());

					InputStream input = Request.Get(job.getServerUrl()+ CyclusService.LOAD_PATH + job.getId())
							.connectTimeout(1000).socketTimeout(100000)
							.execute().returnContent().asStream();
					saveZipStream(input, dir);
					job.setDatafilePath(dir.resolve("cyclus.sqlite").toString());
			
					msg = "ready";
				} catch (ClientProtocolException e) {
					log.error("Communication error while loading simulation data", e);
					msg = e.getMessage();
				} catch (IOException e) {
					log.error("IO error while loading simulation data" + e);
					msg = e.getMessage();
				}
				return new JobStatus(job, msg, null);
			}
		};

		task.valueProperty().addListener(new ChangeListener<JobStatus>() {
			@Override
			public void changed(
					ObservableValue<? extends JobStatus> observable,
					JobStatus prev, JobStatus newStatus) {
				job.setStatus(newStatus.status);
			}
		});

		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}
	
	final int BUFFER = 2048;
	private void saveZipStream(InputStream input, Path dir) throws FileNotFoundException, IOException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(input));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            FileOutputStream fos = new FileOutputStream(dir.resolve(entry.getName()).toString());
            BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER))  != -1) {
               dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
         }
         zis.close();
	}
}

class PollService extends ScheduledService<JobStatus> {
	private final CyclusJob _job;
	private static Logger log = Logger.getLogger(PollService.class);

	public PollService(CyclusJob job) {
		_job = job;
	}

	@Override
	protected Task<JobStatus> createTask() {
		return new Task<JobStatus>() {
			protected JobStatus call() {
				InputStream stream;
				try {
					stream = Request
							.Get(_job.getServerUrl()+ CyclusService.STATUS_PATH + _job.getId())
							.connectTimeout(1000).socketTimeout(1000)
							.execute()
							.returnContent().asStream();

					if (isCancelled()) {
						return new JobStatus(_job, "cancelled", null);
					}
					
					JsonObject reply = Json.createReader(stream).readObject();
					return new JobStatus(_job, reply.getString("Status"), reply);
				} catch (ClientProtocolException e) {
					log.error("Communication error while polling remote execution service",	e);
					return new JobStatus(_job, e.getMessage(), null);
				} catch (IOException e) {
					log.error("IO error while polling remote execution service", e);
					return new JobStatus(_job, e.getMessage(), null);
				}
			}
		};
	}
}

class JobStatus {
	CyclusJob job;
	String status;
	JsonObject info;

	public JobStatus(CyclusJob job, String status, JsonObject info) {
		this.job = job;
		this.status = status;
		this.info = info;
	}
}
