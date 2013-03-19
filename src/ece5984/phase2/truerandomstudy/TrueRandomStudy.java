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
        });
    }
    //TODO Implement onPause, sensors should not be left running
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_true_random_study, menu);
        return true;
    }
    public void test(View view) {
        Test theTest = new PseudoTest();
        
    	switch (view.getId())
        {
        case R.id.accelerometer_test:
        	theTest = new AccelerometerTest();
        	break;
        case R.id.gps_test:
        	theTest = new GPSTest();
        	
    		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, (GPSTest)theTest);
        	break;
        case R.id.test_proximity:
        	theTest = new ProximityTest();
        	break;
        case R.id.test_light:
        	theTest = new AmbientLightTest();
        	break;
        case R.id.system_stats_test:
        	
        	break;
        case R.id.pseudo:
        default:
        	theTest = new PseudoTest();
        }
    	Thread t = new Thread(new RunTheTests(this,theTest));
    	t.start();
    }
    public class RunTheTests implements Runnable
    {
    	Context context;
    	Test theTest;
    	public RunTheTests(Context context, Test theTest)
    	{
    		this.context = context;
    		this.theTest = theTest;
    	}
    
    	@Override
		public void run() {
			/**
			 * raw allows for the various tests to write raw data as it becomes available for debugging
			 */
    		ByteArrayOutputStream raw = new ByteArrayOutputStream();
	    	int numTests = theTest.initialize(context, raw);
	    	ArrayList<ArrayList<DataPair>> allData = new ArrayList<ArrayList<DataPair>>();
	    	for (int i=0; i<numTests;i++)
	    	{
	    		allData.add(new ArrayList<DataPair>());
	    	}
	    	/**
	    	 * Change these values to change the testing parameters
	    	 * TODO Add to UI so that users can do this on a test by test basis
	    	 */
	    	int timeInSeconds = 60;
	    	int checksPerSecond = 2;
	    	for (int i=0; i<timeInSeconds*checksPerSecond; i++)
	    	{
	    		if ((i & 0xF) == 0x8) Log.d("Testing","On "+i+" of 120");
	    		try {
					if (theTest.timeMatters()) Thread.sleep(1000/checksPerSecond);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		for (int j=0;j<numTests;j++)
	    		{
	    			DataPair data = theTest.getData(j);
	    			//Log.d("DataAtTRS",i+": "+j+" :"+Integer.toHexString(data.value));
	    			allData.get(j).add(data);
	    		}
	    	}
	    	theTest.finish();
	    	//Now Analyze
	    	Log.d("Testing","Analysis");
	    	String forWebView = "<html><body>";
	    	try {
	    		File myRawFile = new File("/sdcard/random_test_raw.csv");
	    		myRawFile.createNewFile();
	    		FileOutputStream fRawOut = new FileOutputStream(myRawFile);
	    		fRawOut.write(raw.toByteArray());
	    		fRawOut.close();
	    	} catch (Exception e) {
	            e.printStackTrace();
	        }
	    	analyses.clear();
	    	try{
	            File myFile = new File("/sdcard/random_test_data.csv");
	            myFile.createNewFile();
	            FileOutputStream fOut = new FileOutputStream(myFile);
	            OutputStreamWriter myOutWriter = 
	                                    new OutputStreamWriter(fOut);
	            myOutWriter.append(Analysis.header()+"\r\n");
	            for (int i=0; i<numTests;i++)
	        	{
	        		ArrayList<DataPair> current = allData.get(i);
	        		Analysis analysis = new Analysis(theTest.describeTests()[i]);
	        		analysis.runAnalysis(current);
	        		String output = analysis.toString(theTest.describeTests()[i]);
	        		forWebView = forWebView + output;
	        		myOutWriter.append(output+"\r\n");
	        		analyses.add(analysis);
	        	}
	            
	            
	            myOutWriter.close();
	            fOut.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    	
	    	forWebView = "</body></html>";
	    	Log.d("Testing","Updating Web View");
	    	
	    	runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	//shown = 0;
                	shown = shown%analyses.size();
                	((ImageView) TrueRandomStudy.this.findViewById(R.id.forGraph)).setImageBitmap(Grapher.graph(analyses.get(shown)));
                	((TextView) TrueRandomStudy.this.findViewById(R.id.testShown)).setText(analyses.get(shown).description);
                }
            });
	    	
		}
    }
}
