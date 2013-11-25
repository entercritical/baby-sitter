package com.wendesday.bippobippo.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import com.wendesday.bippobippo.BippoBippo;
import com.wendesday.bippobippo.DebugUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;

public class ShowBabyStatusChart extends AbstractChart 
{
  private static final long HOUR = 3600 * 1000;

  private static final long DAY = HOUR * 24;
  
  private static final int HOURS = 24;

  public String[] DATA_PROJECTION = new String[] {BippoBippo.SensorData.HEAT, 
		                                          BippoBippo.SensorData.TIMESTAMP};
  private final int HEAT_INDEX = 0;
  private final int TIMESTAMP_INDEX = 1;
  
  public String getName() 
  {
    return "Temprature ";
  }
  public String getDesc() 
  {
    return "The temprature status across several days (time chart)";
  }
  public View execute(Context context) 
  {
    String[] titles = new String[] { "Temprature" };
    List<Date[]> dates = new ArrayList<Date[]>();
    List<double[]> values = new ArrayList<double[]>();
    
    // get time and temprature values from db    
    Cursor cursor = context.getContentResolver().query(BippoBippo.SensorData.CONTENT_URI, 
    		DATA_PROJECTION, null, null, null);
    
    int count = cursor.getCount();
    if(cursor !=null && count <= 0){
    	DebugUtils.ErrorLog(" There are no values in db");
    	return null;
    }
    
    if(count > 10){
    	count = 10; // max arrary count is 10
    }
    
    Date[] dateValues = new Date[count];
    double[] heat = new double[count];
    String heatValue = "-1";
    int i = 0;    
    try{
    	while(cursor.moveToNext()){
    		dateValues[i] = new Date(cursor.getLong(TIMESTAMP_INDEX));
    		heatValue = cursor.getString(HEAT_INDEX);
    		heat[i] = Double.valueOf(heatValue);
    		if(i>10)
    			break;
    		i++;    		
        }
    }finally{
    	cursor.close();
    }
    dates.add(dateValues);    
    values.add(heat);

    int[] colors = new int[] { Color.GREEN };
    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };
    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
    
    setChartSettings(renderer, "Temprature", "Time", "Celsius degrees", dateValues[0].getTime(),
    		dateValues[dateValues.length - 1].getTime(), 32, 42, Color.LTGRAY, Color.BLUE);
    
    DebugUtils.Log(dateValues[0].getTime()+"");

    renderer.setXLabels(10);
    renderer.setYLabels(10);
    renderer.setXLabelsAlign(Align.CENTER);
    renderer.setYLabelsAlign(Align.RIGHT);

    return ChartFactory.getTimeChartView(context, buildDateDataset(titles, dates, values), renderer, "h:mm a");    
  }
}

