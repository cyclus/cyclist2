package edu.utah.sci.cyclist.core.model;

import java.util.Arrays;
import java.util.List;

import edu.utah.sci.cyclist.core.controller.IMemento;
import edu.utah.sci.cyclist.core.presenter.VisWorkspacePresenter;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;

public class Perspective {
	public String name;
	public String type;
	public List<String> tools;
	public ViewBase workspace;
	public VisWorkspacePresenter presenter;
	public Boolean initialized = false;
	public int id;
	public double[] toolsPositions = new double[0];
	private IMemento _memento = null;
	private Context _ctx = null;
	
	public Perspective(int id, String name, String type, List<String> tools) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.tools = tools;
	}
	
	public void init() {
		if (_memento != null) {
			presenter.restore(_memento.getChild("workspace"), _ctx);
		}
		initialized = true;
	}
	
	public void setToolsPositions(double[] pos) {
		boolean changed = pos.length != toolsPositions.length;
		if (!changed) {
			int n = Math.min(pos.length, toolsPositions.length);
			for (int i=0; i<n && !changed; i++) {
				changed |= pos[i] != toolsPositions[i];
			}
		}
		if (changed) {
			toolsPositions = pos;
			presenter.setDirtyFlag(true);
		}
	}
	
	public void save(IMemento memento) {	
		memento.putString("name", name);
		memento.putInteger("id", id);
		presenter.save(memento.createChild("workspace"));
		memento.createChild("tools-pos").putString("values", Arrays.toString(toolsPositions));
	}
	
	public void restore(IMemento memento, Context ctx) {	
		if (memento == null) return;
		if (memento.getChild("tools-pos") != null )
			toolsPositions = parse(memento.getChild("tools-pos").getString("values"));
		_memento = memento;
		_ctx = ctx;
		
	}
	
	private double[] parse(String str) {
		String items[] = str.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
		double values[] = new double[items.length];
		for (int i=0; i<items.length; i++) {
			try {
				values[i] = Double.parseDouble(items[i]);
			} catch(NumberFormatException e) {}
		}
		return values;
	}
	
}
