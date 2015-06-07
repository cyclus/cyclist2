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
package edu.utah.sci.cyclist.core.ui.views;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.SplitPane;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mo.closure.v1.Closure;

import edu.utah.sci.cyclist.ToolsLibrary;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.event.ui.CyclistDropEvent;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.ui.CyclistView;
import edu.utah.sci.cyclist.core.ui.View;
import edu.utah.sci.cyclist.core.ui.components.Console;
import edu.utah.sci.cyclist.core.ui.components.CyclistViewBase;
import edu.utah.sci.cyclist.core.ui.components.InfinitPane;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;

public class BaseWorkspace extends ViewBase  {
	public static final String WORKSPACE_ID = "base-workspace";
	
	static final Logger log = LogManager.getLogger(BaseWorkspace.class.getName());
	
	private Pane _pane;
	
	private ViewBase _maximizedView = null;
	
	private Closure.V3<Tool, Double, Double> _onToolDrop = null;
	private Closure.V4<Tool, Table, Double, Double> _onShowTable = null;
	
	public void setOnToolDrop(Closure.V3<Tool, Double, Double> action) {
		_onToolDrop = action;
	}
	
	public void setOnShowTable(Closure.V4<Tool, Table, Double, Double> action) {
		_onShowTable = action;
	}
	
	// -- Properties
	
	// OnToolDrop
	private ObjectProperty<EventHandler<CyclistDropEvent>> _propertyOnToolDrop = new SimpleObjectProperty<EventHandler<CyclistDropEvent>>();
	
	public final ObjectProperty<EventHandler<CyclistDropEvent>> onToolDropPropery() {
		return _propertyOnToolDrop;
	}
	
	public final void setOnToolDrop(EventHandler<CyclistDropEvent> eventHandler) {
		_propertyOnToolDrop.set(eventHandler);
	}
	
	public final EventHandler<CyclistDropEvent> getOnToolDrop() {
		return _propertyOnToolDrop.get();
	}
	
	/**
	 * Constructor
	 */
	
	public BaseWorkspace() {
		this(false);
	}
	
	public BaseWorkspace(boolean toplevel) {
		super(toplevel);
		build(toplevel);
		enableDragging(!toplevel);
	}
	
	private void build(boolean toplevel) {
		getStyleClass().add("workspace");
		setTitle("Workspace");
		setPadding(new Insets(5, 10, 5, 10));
		
		InfinitPane ipane = new InfinitPane();
		_pane = ipane.getPane();
		_pane.getStyleClass().add("workspace-pane");
		
		Console console = new Console();
		
		// arrange console bellow the infinite workspace
		SplitPane sp1 = new SplitPane();
		sp1.setOrientation(Orientation.VERTICAL);
		sp1.setDividerPosition(0, 0.9);
		sp1.getItems().addAll(ipane, console);

		SplitPane.setResizableWithParent(ipane, true);
		SplitPane.setResizableWithParent(console, false);;
	
		SplitPane.setResizableWithParent(ipane, true);
		setContent(sp1, !toplevel);
		
		/*
		 *  set up listeners
		 */
		
		_pane.widthProperty().addListener(e->{
			if (_maximizedView != null) {
				_maximizedView.setPrefWidth(_pane.getWidth());
			}
		});
		
		_pane.heightProperty().addListener(e->{
			if (_maximizedView != null) {
				_maximizedView.setPrefHeight(_pane.getHeight());
			}
		});
		
		setOnDragOver(event->{
			if (event.getTarget() == _pane) {
				DnD.LocalClipboard clipboard = getLocalClipboard();
				if (clipboard.hasContent(DnD.TOOL_FORMAT) || clipboard.hasContent(DnD.TABLE_FORMAT)) 
				{
					event.acceptTransferModes(TransferMode.COPY);
					event.consume();
				} else if (clipboard.hasContent(DnD.FIELD_FORMAT)) {
					event.acceptTransferModes(TransferMode.MOVE);
					event.consume();
				}
			}
		});	
		
		setOnDragDropped(event->{
			boolean accept = false;
			DnD.LocalClipboard clipboard = getLocalClipboard();
			if (event.getGestureSource() != this) {
				if ( clipboard.hasContent(DnD.TOOL_FORMAT)) {
					Tool tool =  clipboard.get(DnD.TOOL_FORMAT, Tool.class);
					if (_onToolDrop != null) {
						Point2D p = _pane.sceneToLocal(event.getSceneX(), event.getSceneY());
						_onToolDrop.call(tool, p.getX(), p.getY());
					if(getOnToolDrop() != null){
							getOnToolDrop().handle(new CyclistDropEvent(CyclistDropEvent.DROP, tool, null, p.getX(),p.getY()));
						}
					}
					accept = true;
				} else if (clipboard.hasContent(DnD.TABLE_FORMAT)) {
					Table table = clipboard.get(DnD.TABLE_FORMAT, Table.class);
					Tool tool;
					try {
						tool = ToolsLibrary.createTool("Table");
						if (_onShowTable != null) {
							Point2D p = _pane.sceneToLocal(event.getSceneX(), event.getSceneY());
							_onShowTable.call(tool, table, p.getX(), p.getY());
							if(getOnToolDrop() != null){
								getOnToolDrop().handle(new CyclistDropEvent(CyclistDropEvent.DROP_DATASOURCE, tool, table, p.getX(), p.getY()));
							}
							accept = true;
						}
					} catch (Exception e) {
						log.error("Can not create a TableView", e);
					}
					
				} 
//					else if (clipboard.hasContent(DnD.FIELD_FORMAT)) {
//						// HACK: accept but don't do anything. 
//						// This allows fields to be remove from other places without causing the DnD to fail
//						status = true;
//					}
			}
			if (accept) {
				event.setDropCompleted(accept);
				event.consume();
			}
		});
	}
	
	@Override
	public void setTitle(String title) {
		super.setTitle(title);
	}
	
	public void selectView(View view) {
		ViewBase node = (ViewBase) view;
		node.toFront();
	}
	/**
	 * add a new view to the workspace
	 * @param view
	 */
	public void addView(final ViewBase view) {
		_pane.getChildren().add(view);
		
		view.setOnSelect(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {	
				view.toFront();
			}
		});
		
		view.setOnMinmax(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (view.isMaximized()) {
					view.setLayoutX(_viewPos.x);
					view.setLayoutY(_viewPos.y);
					view.setPrefSize(_viewPos.width, _viewPos.height);
					
					view.setMaximized(false);
					_maximizedView = null;					
				} else {
					_viewPos.x = view.getLayoutX();
					_viewPos.y = view.getLayoutY();
					_viewPos.width = view.getWidth();
					_viewPos.height = view.getHeight();
		
					view.setLayoutX(0);
					view.setLayoutY(0);
					Bounds b = _pane.getLayoutBounds();
					view.setPrefSize(b.getWidth(), b.getHeight());
					
					view.toFront();
					
					view.setMaximized(true);
					_maximizedView = view;
					
					view.select();
				}
			}
		});
	}
	

	/**
	 * will be called by the workspace presenter
	 * @param view
	 */
	public void removeView(ViewBase view) {
		view.setOnSelect(null);
		_pane.getChildren().remove(view);
		if(getOnToolDrop() != null){
			getOnToolDrop().handle(new CyclistDropEvent(CyclistDropEvent.REMOVE, view));
		}
	}
	
	private ViewPos _viewPos = new ViewPos();
}

class ViewPos {
	public double x;
	public double y;
	public double width;
	public double height;
}