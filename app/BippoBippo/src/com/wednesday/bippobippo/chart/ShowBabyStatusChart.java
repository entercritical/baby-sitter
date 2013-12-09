package com.wednesday.bippobippo.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;

import com.wednesday.bippobippo.BippoBippo;
import com.wednesday.bippobippo.DebugUtils;
import com.wednesday.bippobippo.network.NetworkCommunicator;

public class ShowBabyStatusChart extends AbstractChart 
{
  private static final long HOUR = 3600 * 1000;

  private static final long DAY = HOUR * 24;
  
  private static final int HOURS = 24;
  
  private static final int MAX_COUNT = 1000;

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
    String[] titles = new String[] { "My baby's Temprature" };
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
    double[] heat = new double[count];
    String heatValue = "-1";
    long time;
    int i = 0;    
    try{
    	while(i < MAX_COUNT && cursor.moveToNext()){
    		time = cursor.getLong(TIMESTAMP_INDEX);
    		dateValues[i] = new Date(time);
    		heatValue = cursor.getString(HEAT_INDEX);
    		heat[i] = Double.valueOf(heatValue);
    		i++;    		
        }
    }finally{
    	cursor.close();
    }
    dates.add(dateValues); 
    values.add(heat);
    
    // Get Serverdata
    Intent intent = new Intent();

    int[] colors = new int[] { Color.GREEN};
    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE};
    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
    
    setChartSettings(renderer, "Temprature", "Time", "Celcious Degrees", dates.get(0)[0].getTime(),
    		dates.get(0)[dateValues.length - 1].getTime(), 32, 43, Color.DKGRAY, Color.BLUE);

    renderer.setXLabels(0);
    renderer.setYLabels(10);
    renderer.setXLabelsAlign(Align.CENTER);
    renderer.setYLabelsAlign(Align.RIGHT);
    renderer.setMarginsColor(Color.parseColor("#FFFFFF"));

    return ChartFactory.getTimeChartView(context, buildDateDataset(titles, dates, values), renderer, "h:mm a");    
  }
}

