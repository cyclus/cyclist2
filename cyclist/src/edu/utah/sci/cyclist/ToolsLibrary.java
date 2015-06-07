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
package edu.utah.sci.cyclist;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.utah.sci.cyclist.core.tools.SimpleToolFactory;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utexas.cycic.tools.CycicToolFactory;
import edu.utexas.cycic.tools.InstitutionCorralViewToolFactory;
//import edu.utexas.cycic.tools.InstitutionCorralViewToolFactory;
import edu.utexas.cycic.tools.RecipeFormToolFactory;
import edu.utexas.cycic.tools.RegionCorralViewToolFactory;

public class ToolsLibrary {
	public static final String VIS_TOOL = "vis_tool";
	public static final String SCENARIO_TOOL = "scenario_tool";
	
    private static List<ToolFactory> factories = new ArrayList<>();
 
    public static void register(ToolFactory... list) {
    	for (ToolFactory factory : list) 
    		factories.add(factory);
    }
    
    
	public static ToolFactory getFactory(String name) {
		return factories
				.stream()
				.filter(f -> f.getToolName().equals(name))
				.findFirst()
				.get();
	}
	
	public static List<ToolFactory> getFactoriesOfType(String type) {
		return factories
			.stream()
			.filter(f -> f.getToolType().equals(type))
			.collect(Collectors.toList());
	}
	
	public static Tool createTool(String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Tool tool = null;
		ToolFactory factory = getFactory(name);
		if (factory != null)
			tool = factory.create();
		return tool;
	}
	
	
	static {
		register(
			new SimpleToolFactory("edu.utah.sci.cyclist.core", 
					"Table", VIS_TOOL, AwesomeIcon.LIST_ALT, 
					"ui.views.SimpleTableView", 
					"presenter.TablePresenter"),
			
			new SimpleToolFactory("edu.utah.sci.cyclist.core", 
					"Plot", VIS_TOOL, AwesomeIcon.BAR_CHART_ALT, 
					"ui.views.ChartView", 
					"presenter.ChartPresenter"),
			
			new SimpleToolFactory("edu.utah.sci.cyclist.neup", 
					"Flow", VIS_TOOL, AwesomeIcon.RANDOM, 
					"ui.views.flow.FlowView", 
					"presenter.NEUPPresenter"),	
			
			new SimpleToolFactory("edu.utah.sci.cyclist.neup", 
					"Inventory", VIS_TOOL, AwesomeIcon.BOOK, 
					"ui.views.inventory.InventoryView", 
					"presenter.NEUPPresenter"),
			
			new SimpleToolFactory("edu.utah.sci.cyclist.core", 
					"Workspace", VIS_TOOL, AwesomeIcon.DESKTOP, 
					"ui.views.Workspace", 
	              "presenter.WorkspacePresenter")
          );
		
		register(
	      new CycicToolFactory(),
	      new InstitutionCorralViewToolFactory(),
	      new RecipeFormToolFactory(),
	      new RegionCorralViewToolFactory()
		);
	}

} 
