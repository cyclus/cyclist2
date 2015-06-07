package edu.utexas.cycic;

import java.util.ArrayList;
import java.util.Arrays;
/**
 * Generate demo dataArray for timeline display
 * it will be called at the beginning of timeline setup
 * structure 
 * [
 * 		["facilityName",startYear,endYear, 
 * 				[
 * 					["eventName",eventTime,eventDetails],
 *  				["eventName",eventTime,eventDetails],
 *   				["eventName",eventTime,eventDetails]
 *   				......
 *   			]
 *   	],
 *   
 * 		["facilityName",startYear,endYear, 
 * 				[
 * 					["eventName",eventTime,eventDetails],
 *  				["eventName",eventTime,eventDetails],
 *   				["eventName",eventTime,eventDetails]
 *   				......
 *   			]
 *   	]
 *   ......
 * ]
 * @author alfred
 *
 */
public class demoDev {
	/**
	 * update the empty arrayList for demo purpose
	 * @param a an empty arrayList needed to be updated
	 */
	public static void initializeSample(ArrayList<Object> a) {
		if (a.size()== 0) {
			ArrayList <Object> event1 = new ArrayList(Arrays.asList("event 1", "2010", "refuel"));
			ArrayList <Object> event2 = new ArrayList(Arrays.asList("event 2", "2050","emergency shutdown"));
			ArrayList <Object> data1event = new ArrayList (Arrays.asList(event1, event2));

			ArrayList <Object> event3 = new ArrayList(Arrays.asList("event 3", "1950","fire drill"));
			ArrayList <Object> data2event = new ArrayList (Arrays.asList(event3));
			ArrayList <Object> data3event = new ArrayList<>();
			ArrayList <Object> event4 = new ArrayList(Arrays.asList("event 4", "2007", "new fuel arrived"));
			ArrayList <Object> event5 = new ArrayList(Arrays.asList("event 5", "2049","refuel"));
			ArrayList <Object> event6 = new ArrayList(Arrays.asList("event 6", "2087", "facility remodel"));
			ArrayList <Object> data4event = new ArrayList (Arrays.asList(event4,event5,event6));

			ArrayList <Object> data1 = new ArrayList(Arrays.asList("sample 1", "1995", "2150",data1event));
			ArrayList <Object> data2 = new ArrayList(Arrays.asList("sample 2", "1900", "1993",data2event));
			ArrayList <Object> data3 = new ArrayList(Arrays.asList("sample 3", "1946", "2090",data3event));
			ArrayList <Object> data4 = new ArrayList(Arrays.asList("sample 4", "2000", "2100",data4event));
			a.add(data1);
			a.add(data2);
			a.add(data3);
			a.add(data4);
			ArrayList <Object> event11 = new ArrayList(Arrays.asList("event 1", "2010", "refuel"));
			ArrayList <Object> event21 = new ArrayList(Arrays.asList("event 2", "2050","emergency shutdown"));
			ArrayList <Object> data1event1 = new ArrayList (Arrays.asList(event11, event21));

			ArrayList <Object> event31 = new ArrayList(Arrays.asList("event 3", "1950","fire drill"));
			ArrayList <Object> data2event1 = new ArrayList (Arrays.asList(event31));
			ArrayList <Object> data3event1 = new ArrayList<>();
			ArrayList <Object> event41 = new ArrayList(Arrays.asList("event 4", "2007", "new fuel arrived"));
			ArrayList <Object> event51 = new ArrayList(Arrays.asList("event 5", "2049","refuel"));
			ArrayList <Object> event61 = new ArrayList(Arrays.asList("event 6", "2087", "facility remodel"));
			ArrayList <Object> data4event1 = new ArrayList (Arrays.asList(event41,event51,event61));

			ArrayList <Object> data11 = new ArrayList(Arrays.asList("sample 1", "1995", "2150",data1event1));
			ArrayList <Object> data21 = new ArrayList(Arrays.asList("sample 2", "1900", "1993",data2event1));
			ArrayList <Object> data31 = new ArrayList(Arrays.asList("sample 3", "1946", "2090",data3event1));
			ArrayList <Object> data41 = new ArrayList(Arrays.asList("sample 4", "2000", "2100",data4event1));
			a.add(data11);
			a.add(data21);
			a.add(data31);
			a.add(data41);
		}
	}

}
