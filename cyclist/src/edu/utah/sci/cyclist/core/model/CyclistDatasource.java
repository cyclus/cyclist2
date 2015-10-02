/*******************************************************************************
 * Copyright (c) 2013 SCI Institute, University of Utah.
 * All rights reserved.
 *
 * License for the specific language governing rights and limitations under Permission
 * is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions: The above copyright notice 
 * and this permission notice shall be included in all copies  or substantial portions of the Software. 
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR 
 *  A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Yarden Livnat  
 *******************************************************************************/
package edu.utah.sci.cyclist.core.model;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.sql.DataSource;

import edu.utah.sci.cyclist.core.controller.IMemento;

public class CyclistDatasource implements DataSource, Resource {
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CyclistDatasource.class);

	private Properties _properties = new Properties();
	private transient PrintWriter _logger;
	private String _url;
	private boolean _ready = false;
	private String _id = UUID.randomUUID().toString();
	
	// SQLite hack
	static private final Semaphore _SQLiteSemaphore = new Semaphore(1, true);
	static private Connection _SQLiteConnection = null;
	static private Thread _currentThread = null;
	
	private static final String SQLITE_PREFIX = "jdbc:sqlite:/";
	
	
	public CyclistDatasource() {
//		_properties.setProperty("uid", UUID.randomUUID().toString());
	}		

	public String getUID() {
		return _id;
	}
	
	// Save this data source
	public void save(IMemento memento) {
		memento.putString("UID", _id);
		// Set the url
		memento.putString("url", getURL());
				
		Enumeration<Object> e = _properties.keys();
		while (e.hasMoreElements()){
			String key = (String) e.nextElement();
			String value = _properties.getProperty(key);
			memento.putString(key, value);
		}
	}
	
	// Restore this data source
	public void restore(IMemento memento, Context ctx){		
		_id = memento.getString("UID");
		ctx.put(_id, this);
		
		// Get all of the keys	
		String[] keys = memento.getAttributeKeys();
		for(String key: keys){

			// Get the value associated with the key
			String value = memento.getString(key);

			// If we have a url, set it
			if(key == "url")
				setURL(value);
			else	
				_properties.setProperty(key, value);
		}	
		
		// sqlite hack
		if (isSQLite()) {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				log.warn("Can not load sqlite driver", e);
			}
		}
	}

	public boolean isSQLite() {
		return "SQLite".equals(_properties.getProperty("type"));
	}
	
	@Override
    public String toString() {
        return getName();
    }
	
	public String getName() {
		return _properties.getProperty("name");
	}

	public void setName(String name) {
		_properties.setProperty("name", name);
	}

	public StringProperty getNameProperty(){
		return new SimpleStringProperty(getName());
	}
	
	public void setProperties(Properties p) {
		_properties = p;
	}

	public Properties getProperties() {
		return _properties;
	}

	
	public void setURL(String url) {
		if (_url != url) {
			_url = url;
			_ready = false;
		}
	}

	public String getURL() {
		return _url;
	}
	
	public boolean isReady() {
		return _ready;
	}

	public void setReady(boolean value) {
		_ready = value;
	}
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return _logger;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		_logger = out;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return (T) this;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		if (username != null)
            _properties.put("user", username);
        if (password != null)
            _properties.put("pass", password);
        
        Connection connection;
        
        if (isSQLite()) {
        	connection = getSQLiteConnection();
        } else {
        	connection = DriverManager.getConnection(_url, _properties);
        }
        return connection;
	}
	
	public void releaseConnection() {
		if (isSQLite()) {
			if (_currentThread != Thread.currentThread()) {
				log.debug("SQLite: wrong thread close connection: ignored");
				return;
			}
			try {
				_SQLiteConnection.close();
				_SQLiteConnection = null;
			} catch (SQLException e) {
				log.debug("Error while closing sqlite connection: ",e);
				e.printStackTrace();
			}
			_SQLiteSemaphore.release();
		}
	}
	
	private Connection getSQLiteConnection() throws SQLException {
		//If file doesn't exist the sqlite driver, creates one, and returns it without any error.
		//In order to show an error, when non-existing file is chosen - have to check the existence of the file.
		File file = new File(_url.replace(SQLITE_PREFIX, ""));
		if (!file.exists()) {
			throw new SQLException("SQLite: no such file ["+_url+"]");
		}
		
		try {		
			_SQLiteSemaphore.acquire();
		} catch (InterruptedException e) { //InterruptedException
			// ignore. Thread was waiting on the semaphore
		}
		
		try {
			_SQLiteConnection =  DriverManager.getConnection(_url, _properties);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SQLite getConnection failed", e);
		}
		_currentThread = Thread.currentThread();
		return _SQLiteConnection;
	}
 }