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

import edu.utah.sci.cyclist.core.tools.SimpleToolFactory;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utexas.cycic.tools.CycicToolFactory;
import edu.utexas.cycic.tools.InstitutionCorralViewToolFactory;
import edu.utexas.cycic.tools.RecipeFormToolFactory;
import edu.utexas.cycic.tools.RegionCorralViewToolFactory;
import edu.utexas.cycic.tools.TimelineDisplayToolFactory;

public class ToolsLibrary {

    public static final ToolFactory[] factories = {
		new SimpleToolFactory("edu.utah.sci.cyclist.core", 
				"Table", AwesomeIcon.LIST_ALT, 
				"ui.views.SimpleTableView", 
				"presenter.TablePresenter"),
		
		new SimpleToolFactory("edu.utah.sci.cyclist.core", 
				"Plot", AwesomeIcon.BAR_CHART_ALT, 
				"ui.views.ChartView", 
				"presenter.ChartPresenter"),
		
		new SimpleToolFactory("edu.utah.sci.cyclist.neup", 
				"Flow", AwesomeIcon.RANDOM, 
				"ui.views.flow.FlowView", 
				"presenter.NEUPPresenter"),	
		
		new SimpleToolFactory("edu.utah.sci.cyclist.neup", 
				"Inventory", AwesomeIcon.BOOK, 
				"ui.views.inventory.InventoryView", 
				"presenter.NEUPPresenter"),
		
		new SimpleToolFactory("edu.utah.sci.cyclist.core", 
				"Workspace", AwesomeIcon.DESKTOP, 
				"ui.views.Workspace", 
                "presenter.WorkspacePresenter")
	};

    public static final ToolFactory[] inputFactories = {
        new CycicToolFactory(),
        new InstitutionCorralViewToolFactory(),
        new RecipeFormToolFactory(),
        new RegionCorralViewToolFactory(),
        //new TimelineDisplayToolFactory()
    };
    
	public static ToolFactory findFactory(String name) {
		for (int i=0; i<factories.length; i++) {
			if (factories[i].getToolName().equals(name))
				return factories[i];
		}
		return null;
	}
	
	public static Tool createTool(String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Tool tool = null;
		ToolFactory factory = findFactory(name);
		if (factory != null)
			tool = factory.create();
		return tool;
	}
	
} 
