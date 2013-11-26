package com.wendesday.bippobippo.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;

import com.wendesday.bippobippo.BippoBippo;
import com.wendesday.bippobippo.DebugUtils;

public class ShowBabyBpmChart extends AbstractChart {
	
	  private static final long HOUR = 3600 * 1000;

	  private static final long DAY = HOUR * 24;
	  
	  private static final int HOURS = 24;
	  
	  private static final int MAX_COUNT = 100;

	  public String[] DATA_PROJECTION = new String[] {BippoBippo.SensorData.BPM, 
			                                          BippoBippo.SensorData.TIMESTAMP};
	  private final int BPM_INDEX = 0;
	  private final int TIMESTAMP_INDEX = 1;
	  
	  public String getName() 
	  {
	    return "BPM ";
	  }
	  public String getDesc() 
	  {
	    return "The bpm status across several days (time chart)";
	  }
	  public View execute(Context context) 
	  {
	    String[] titles = new String[] { "BPM" };
	    List<Date[]> dates = new ArrayList<Date[]>();
	    List<double[]> values = new ArrayList<double[]>();
	    
	    // get time and temprature values from db    
	    Cursor cursor = context.getContentResolver().query(BippoBippo.SensorData.CONTENT_URI, 
	    		DATA_PROJECTION, null, null, null);
	    
	    int count = cursor.getCount();
	    if(cursor !=null && count <= 0){
	    	DebugUtils.ErrorLog(" Database data is empty");
	    	return null;
	    }
	    
	    if(count > MAX_COUNT){
	    	count = MAX_COUNT; // max arrary count is 10
	    }
	    
	    Date[] dateValues = new Date[count];
	    double[] bpm = new double[count];
	    String bpmValue = "-1";
	    int i = 0;    
	    try{
	    	while(cursor.moveToNext()){
	    		dateValues[i] = new Date(cursor.getLong(TIMESTAMP_INDEX));
	    		bpmValue = cursor.getString(BPM_INDEX);
	    		bpm[i] = Double.valueOf(bpmValue);
	    		if(i > MAX_COUNT)
	    			break;
	    		i++;    		
	        }
	    }finally{
	    	cursor.close();
	    }
	    dates.add(dateValues);    
	    values.add(bpm);

	    int[] colors = new int[] { Color.RED };
	    PointStyle[] styles = new PointStyle[] { PointStyle.TRIANGLE };
	    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
	    
	    setChartSettings(renderer, "BPM", "Time", "degrees", dateValues[0].getTime(),
	    		dateValues[dateValues.length - 1].getTime(), 80, 185, Color.DKGRAY, Color.BLUE);
	    
	    DebugUtils.Log(dateValues[0].getTime()+"");

	    renderer.setXLabels(7);
	    renderer.setYLabels(10);
	    renderer.setXLabelsAlign(Align.CENTER);
	    renderer.setYLabelsAlign(Align.RIGHT);
	    renderer.setMarginsColor(Color.parseColor("#FFFFFF"));

	    return ChartFactory.getTimeChartView(context, buildDateDataset(titles, dates, values), renderer, "h:mm a");    
	  }

}
