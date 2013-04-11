package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * TrueRandomStudy creates a framework for testing various sensors for Random qualities 
 * @author John
 *
 */

public class TrueRandomStudy extends Activity {

	final ArrayList<Analysis> analyses = new ArrayList<Analysis>();
	int shown = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_true_random_study);
        SensorManager sensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        //We print out the sensors to see what is available on the device
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : deviceSensors)
        	Log.d("Devices:", s.getName()+" "+s.getPower());
        OnClickListener ocl;
        /*
        ((Button)findViewById(R.id.showNext)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) 
			{
				shown = (shown+1)%analyses.size();
				
				((ImageView) TrueRandomStudy.this.findViewById(R.id.forGraph)).setImageBitmap(Grapher.graph(analyses.get(shown)));
				((TextView) TrueRandomStudy.this.findViewById(R.id.testShown)).setText(analyses.get(shown).description);
			}
        });
        ((Button)findViewById(R.id.showPrevious)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) 
			{
				shown = (shown-1)%analyses.size();
				((ImageView) TrueRandomStudy.this.findViewById(R.id.forGraph)).setImageBitmap(Grapher.graph(analyses.get(shown)));
				((TextView) TrueRandomStudy.this.findViewById(R.id.testShown)).setText(analyses.get(shown).description);
			}
        });*/
    }
    //TODO Implement onPause, sensors should not be left running
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_true_random_study, menu);
        return true;
    }
    public void test(View view) {
        Test theTest;
        ArrayList<Test> tests = new ArrayList();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	if (((CheckBox)findViewById(R.id.accelerometer_test)).isChecked())
    	{
        	tests.add(new AccelerometerTest());
    	}
        if (((CheckBox)findViewById(R.id.gps_test)).isChecked())
        {
        	theTest = new GPSTest();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, (GPSTest)theTest);
            tests.add(theTest);
        }
        if (((CheckBox)findViewById(R.id.network_location_test)).isChecked())
        {
        	theTest = new NetworkLocationTest();
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, (NetworkLocationTest)theTest);
            tests.add(theTest);
        }
        if (((CheckBox)findViewById(R.id.CDMATest)).isChecked())
        {
        	theTest = new NetworkStatusTest();
        	tests.add(theTest);
        }
        if (((CheckBox)findViewById(R.id.test_proximity)).isChecked())
        {
        	theTest = new ProximityTest();
        	tests.add(theTest);
        }
        if (((CheckBox)findViewById(R.id.test_light)).isChecked())
        {
        	theTest = new AmbientLightTest();
        	tests.add(theTest);
        }
        if (((CheckBox)findViewById(R.id.system_stats_test)).isChecked())
        {
        	
        }
        if (((CheckBox)findViewById(R.id.pseudo)).isChecked())
        {
        	theTest = new PseudoTest();
        	tests.add(theTest);
        }
    	Thread t = new Thread(new RunTheTests(this,tests));
    	t.start();
    }
    final class RunTheTests implements Runnable
    {
    	Context context;
    	ArrayList<Test> tests;
    	public RunTheTests(Context context, ArrayList<Test> tests)
    	{
    		this.context = context;
    		this.tests = tests;
    	}
    
    	@Override
		public void run() {
			/**
			 * raw allows for the various tests to write raw data as it becomes available for debugging
			 */
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		ArrayList<ArrayList<DataPair>> data = new ArrayList<ArrayList<DataPair>>();
    		
    		for (int i=0; i<tests.size();i++)
    		{
    			tests.get(i).initialize(context, baos);
    			data.add(new ArrayList<DataPair>());
    		}
    		
    		int minutes = 6;
    		
    		for (int i=0;i<minutes;i++)
    		{
    			try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    			for (int j=0; j<tests.size();j++)
    			{
    				
    				ArrayList<DataPair> round_data = tests.get(j).getData();
    				for (int k=0;k<round_data.size();k++)
    				{
    					data.get(j).add(round_data.get(k));
    				}
    				tests.get(j).clear();
    			}
    		}
    		ArrayList<ArrayList<Byte>> all_data = new ArrayList<ArrayList<Byte>>();
    		for (int i=0; i<tests.size();i++)
    		{
    			Analysis analysis = new Analysis("");
    			analysis.runAnalysis(data.get(i));
    			ArrayList<Byte> randomBytes = analysis.getRandomBytes();
    			all_data.add(randomBytes);
    		}
    		
	    	//Now Shuffle byte by byte
    		ArrayList<DataPair> completeData = new ArrayList<DataPair>();
    		boolean stillMore = true;
    		while (stillMore)
    		{
    			stillMore = false;
    			for (int i=0;i<tests.size();i++)
    			{
    				if (all_data.get(i).size()>0)
    				{
    					Byte nextByte = all_data.get(i).remove(0);
    					completeData.add(new DataPair(8,nextByte));
    					Log.d("data","byte: "+nextByte);
    					stillMore = true;
    				}
    			}
    		}
    		final Analysis finalAnalysis = new Analysis("Total Data");
    		finalAnalysis.runAnalysis(completeData);
	    	runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	//shown = 0;
                	//shown = shown%analyses.size();
                	((ImageView) TrueRandomStudy.this.findViewById(R.id.forGraph)).setImageBitmap(Grapher.graph(finalAnalysis));
                	((TextView) TrueRandomStudy.this.findViewById(R.id.testShown)).setText(finalAnalysis.description);
                }
            });
	    	
		}
    }
}
