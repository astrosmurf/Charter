/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.chartdemo.demo.chart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chartdemo.demo.R;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class XYChartBuilder extends Activity {
  /** The main dataset that includes all the series that go into a chart. */
  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
  /** The main renderer that includes all the renderers customizing a chart. */
  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
  /** The most recently added series. */
  private XYSeries mCurrentSeries;
  /** The most recently created renderer, customizing the current series. */
  private XYSeriesRenderer mCurrentRenderer;
  /** Button for creating a new series of data. */
  private Button mNewSeries;
  /** Button for adding entered data to the current series. */
  private Button mAdd;
  /** Edit text field for entering the X value of the data to be added. */
  private EditText mX;
  
  /** Edit text field for entering the Y value of the data to be added. */
  private EditText mY;
  /** The chart view that displays the data. */
  private GraphicalView mChartView;
  /** Variable to keep track on time for first data point **/
  private double mFirstx;
  private double mSP;
  private int tidsSkala; //0 sekunder, 1 minuter, 2 timmar, 3 dygn, 4 veckor, 
  private XYSeries mPVSeries;
  private XYSeries mSPSeries;
//  private String filename = "myfile";
//  private String stringtofile = "Hello world!";
  
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // save the current data, for instance when changing screen orientation
    outState.putSerializable("dataset", mDataset);
    outState.putSerializable("renderer", mRenderer);
    outState.putSerializable("current_series", mCurrentSeries);
    outState.putSerializable("current_renderer", mCurrentRenderer);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedState) {
    super.onRestoreInstanceState(savedState);
    // restore the current data, for instance when changing the screen
    // orientation
    mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
    mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
    mCurrentSeries = (XYSeries) savedState.getSerializable("current_series");
    mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.xy_chart);
//    checkExternalMedia();


//    public final double firstx = 0;

    // the top part of the UI components for adding new data points
    mX = (EditText) findViewById(R.id.xValue);
    mY = (EditText) findViewById(R.id.yValue);
    mAdd = (Button) findViewById(R.id.add);
    mNewSeries = (Button) findViewById(R.id.new_series);
    mSP=0;
    tidsSkala=0;

    // set some properties on the main renderer
    mRenderer.setApplyBackgroundColor(true);
    mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
    mRenderer.setAxisTitleTextSize(24);
    mRenderer.setChartTitleTextSize(30);
    mRenderer.setLabelsTextSize(30);
    mRenderer.setShowLegend(false);
//    mRenderer.setLegendTextSize(30);
    mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
    mRenderer.setZoomButtonsVisible(false);
    mRenderer.setPointSize(15);
    mRenderer.setXTitle("Time / s");
    
    //PV skapas on Create
    String seriesTitle ="PV";// = "Series " + (mDataset.getSeriesCount() + 1);
    // create a new series of data
    XYSeries series = new XYSeries(seriesTitle);
    mDataset.addSeries(series);
    mCurrentSeries = series;
   	mPVSeries=mCurrentSeries;

    // create a new renderer for the new series
    XYSeriesRenderer renderer = new XYSeriesRenderer();
    mRenderer.addSeriesRenderer(renderer);
    // set some renderer properties
    renderer.setPointStyle(PointStyle.CIRCLE);
    renderer.setLineWidth(4);
    renderer.setColor(-65536); //red
    renderer.setFillPoints(true);
    renderer.setDisplayChartValues(true);
    renderer.setDisplayChartValuesDistance(10);
    mCurrentRenderer = renderer;
    setSeriesWidgetsEnabled(true);
 //   mChartView.repaint();
  
    //Testa spara fil
//    FileOutputStream outputStream;
//
//    try {
//      outputStream = openFileOutput(filename, Context.MODE_WORLD_READABLE);
//      outputStream.write(stringtofile.getBytes());
//      outputStream.close();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }


    // the button that handles the new series of data creation
    //När denna anropas bör SP begära fokus och Y-max för graf sättas strax över SP
    mNewSeries = (Button) findViewById(R.id.new_series);
    mNewSeries.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        String seriesTitle ="";// = "Series " + (mDataset.getSeriesCount() + 1);
        // create a new series of data
//        if (mDataset.getSeriesCount()==0){
//        	seriesTitle = "PV";
//        }
        if (mDataset.getSeriesCount()==1){
        	seriesTitle = "SP";
        	mX.requestFocus();     
        	
        }
        XYSeries series = new XYSeries(seriesTitle);
        mDataset.addSeries(series);
        mCurrentSeries = series;
//        if (mDataset.getSeriesCount()==1){
//        	mPVSeries=mCurrentSeries;
////        	mPVSeries=series;
//        }
        if (mDataset.getSeriesCount()==2){
        	mSPSeries=mCurrentSeries;
//        	mSPSeries=series;
        }

        // create a new renderer for the new series
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // set some renderer properties
//        renderer.setPointStyle(PointStyle.CIRCLE);
//               if (mDataset.getSeriesCount()==1){
//        
//        renderer.setPointStyle(PointStyle.X);
//        } 
        renderer.setLineWidth(4);
//        renderer.setFillPoints(true);
        renderer.setDisplayChartValues(true);
        renderer.setDisplayChartValuesDistance(10);
        mCurrentRenderer = renderer;
        setSeriesWidgetsEnabled(true);
        mChartView.repaint();
      }
    });

    mAdd.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        double x = 0;
        double y = 0;
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        try {
        	if (mDataset.getSeriesCount()==2) if (mX.getText()!=null) mSP = Double.parseDouble(mX.getText().toString());
            x = Double.parseDouble(ts); //aktuell tidsstämpel
            if (mFirstx == 0 && mDataset.getSeriesCount()==1) {mFirstx = x;}
            x = x - mFirstx;
        } catch (NumberFormatException e) {
        	mX.requestFocus();
          return;
        }
        try {
          y = Double.parseDouble(mY.getText().toString());
        } catch (NumberFormatException e) {
          mY.requestFocus();
          return;
        }
        // add a new data point to the current series
        if (x>120){ //ändra alla tidigare data till minutdata för x första gången, och för alla nya punkter
        	if (tidsSkala==0){ //konvertera alla tidigare data till minuter
        		double tempX, tempY;
        		for(int i = 0; i < mPVSeries.getItemCount(); i++)  
        		{  
        		    tempX=mPVSeries.getX(i);
        		    tempY=mPVSeries.getY(i);
        		    tempX/=60;
        		    mPVSeries.remove(i);
        		    mPVSeries.add(i, tempX, tempY);
        		} 
        		for(int i = 0; i < mSPSeries.getItemCount(); i++)  
        		{  
        		    tempX=mSPSeries.getX(i);
        		    tempY=mSPSeries.getY(i);
        		    tempX/=60;
        		    mSPSeries.remove(i);
        		    mSPSeries.add(i, tempX, tempY);
        		} 
        		tidsSkala=1;
        	    mRenderer.setXTitle("Time / min");

        	}
        	x/=60;
        }
        mPVSeries.add(x, y);
        mX.setText(Double.toString(mSP)); //skriver aktuellt värde som default
        mY.setText(""); //rensar PV-värdet varje gång
        mY.requestFocus(); //Fokus till PV
    	double Xmax=x*1.1;
    	mRenderer.setXAxisMax(Xmax);
        //Här ett litet test
        if (mSP !=0){
        	mSPSeries.add(x, mSP);
        	double Ymax=mSP*1.1;
        	mRenderer.setYAxisMax(Ymax);
        }
        // repaint the chart such as the newly added point to be visible
        mChartView.repaint();
        writeToSDFile();

        String filename = "myInternalFile";
        String string2 = "Hello world!";
        FileOutputStream outputStream;

        try {
          outputStream = openFileOutput(filename, Context.MODE_WORLD_READABLE);
          outputStream.write(string2.getBytes());
          outputStream.close();
        } catch (Exception e) {
          e.printStackTrace();
        }

      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mChartView == null) {
      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
      mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
      // enable the chart click events
      mRenderer.setClickEnabled(true);
      mRenderer.setSelectableBuffer(10);
      mChartView.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          // handle the click event on the chart
          SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
          if (seriesSelection == null) {
//            Toast.makeText(XYChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
          } else {
            // display information of the clicked point
            Toast.makeText(
                XYChartBuilder.this,
                "Chart element in series index " + seriesSelection.getSeriesIndex()
                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                    + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
          }
        }
      });
      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      boolean enabled = mDataset.getSeriesCount() > 0;
      setSeriesWidgetsEnabled(enabled);
    } else {
      mChartView.repaint();
    }
  }

  /**
   * Enable or disable the add data to series widgets
   * 
   * @param enabled the enabled state
   */
  private void setSeriesWidgetsEnabled(boolean enabled) {
	  if (mDataset.getSeriesCount()>=2) {
		  mX.setEnabled(enabled);
		  mNewSeries.setEnabled(false);
	  }
    mY.setEnabled(enabled);
    mAdd.setEnabled(enabled);
  }
  
  private void checkExternalMedia(){
      boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    String state = Environment.getExternalStorageState();

    if (Environment.MEDIA_MOUNTED.equals(state)) {
        // Can read and write the media
        mExternalStorageAvailable = mExternalStorageWriteable = true;
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
        // Can only read the media
        mExternalStorageAvailable = true;
        mExternalStorageWriteable = false;
    } else {
        // Can't read or write
        mExternalStorageAvailable = mExternalStorageWriteable = false;
    }   
//    tv.append("\n\nExternal Media: readable="
//            +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
}

/** Method to write ascii text characters to file on SD card. Note that you must add a 
   WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
   a FileNotFound Exception because you won't have write permission. */

private void writeToSDFile(){

    // Find the root of the external storage.
    // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

//    File root = Environment.getExternalStorageDirectory(); 
//    tv.append("\nExternal file system root: "+root);

    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

//    File dir = new File (root.getAbsolutePath());// + "/download");
//    dir.mkdirs();
    File file = new File(Environment.getExternalStorageDirectory(), "myData.txt");


    try {
//    	//till internminne
    	FileOutputStream fos = openFileOutput("MyInternalFileX",
                Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
        fos.write("hej".toString().getBytes());
        fos.close();
//        //till SD
        FileOutputStream f = new FileOutputStream(file);
        PrintWriter pw = new PrintWriter(f);
        pw.println("Hi , How are you");
        pw.println("Hello");
        pw.flush();
        pw.close();
        f.close();
    } catch (FileNotFoundException e) {
        e.printStackTrace();
        Log.i("hej", "******* File not found. Did you" +
                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
    } catch (IOException e) {
        e.printStackTrace();
    }   
//    tv.append("\n\nFile written to "+file);
}
  
  
}