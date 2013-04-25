package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
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
        	theTest = new SystemTest();
        	tests.add(theTest);
        }
        if (((CheckBox)findViewById(R.id.pseudo)).isChecked())
        {
        	theTest = new PseudoTest();
        	tests.add(theTest);
        }
        if (((CheckBox)findViewById(R.id.gsm)).isChecked())
        {
        	theTest = new GSM_Test();
        	tests.add(theTest);
        }        	
        if (((CheckBox)findViewById(R.id.wifi)).isChecked())
        {
        	theTest = new WiFiTest();
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
    		int PERIOD = 10000;
    		int ROUNDS = 6*1;
    		Date d = new Date();
    		for (int i=0;i<ROUNDS;i++)
    		{
    			try {
					Thread.sleep(PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    			//Take data from all of the sources for this round
    			for (int j=0; j<tests.size();j++)
    			{
    				
    				ArrayList<DataPair> round_data = tests.get(j).getData();
    				for (int k=0;k<round_data.size();k++)
    				{
    					data.get(j).add(round_data.get(k));
    				}
    				tests.get(j).clear();
    			}
    			Log.d("Time:","Started at: "+d.toString()+" "+i+"/"+ROUNDS);
    		}
    		
    		//Timestamp used for filenames
    		Date date = new Date();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyMMddkkmm");
    		String timestamp = sdf.format(date);
    		
    		//All data is a list of lists of the data from each source from all rounds
    		ArrayList<ArrayList<Byte>> all_data = new ArrayList<ArrayList<Byte>>();
    		
    		//Create the folder to hold everything
    		File folder = new File("/sdcard/random_"+timestamp);
    		if (folder.mkdir())
    			Log.d("Random","Created the folder");
    		
    		//Put everything into lists of bytes
    		for (int i=0; i<tests.size();i++)
    		{
    			tests.get(i).finish();
    			Analysis analysis = new Analysis("");
    			analysis.runAnalysis(data.get(i));
    			ArrayList<Byte> randomBytes = analysis.getRandomBytes();
    			//Add this test's randomBytes list to the all data  
    			all_data.add(randomBytes);
    			try {
    				//Write it out to keep track of it
    	 		       FileOutputStream out = new FileOutputStream("/sdcard/random_"+timestamp+"/"+tests.get(i).getClass().getSimpleName()+".raw");
    	 		       DataOutputStream dos = new DataOutputStream(out);
    	 		       for (int j=0; j<randomBytes.size();j++)
    	 		    	   dos.writeByte(randomBytes.get(j).byteValue());
    	 		       //dos.write(random_bytes);
    	 		       out.close();
    		 		} catch (Exception e) {
    		 		       e.printStackTrace();
    		 		}
    		}
	    	
    		ArrayList<DataPair> completeDataBitByBit = new ArrayList<DataPair>();
    		ArrayList<DataPair> completeDataByteByByte = new ArrayList<DataPair>();
    		ArrayList<DataPair> completeDataBitByBitLimited = new ArrayList<DataPair>();

    		
    		//The shuffling destroys the all_data_copy
    		ArrayList<ArrayList<Byte>> all_data_copy = makeCopy(all_data);
    		//Now Shuffle byte by byte
    		boolean stillMore = true;
    		while (stillMore)
    		{
    			stillMore = false;
    			for (int i=0;i<tests.size();i++)
    			{
    				if (all_data_copy.get(i).size()>0)
    				{
    					Byte nextByte = all_data_copy.get(i).remove(0);
    					completeDataByteByByte.add(new DataPair(8,nextByte));
    					Log.d("data","byte: "+nextByte);
    					stillMore = true;
    				}
    			}
    		}
    		
    		
    		all_data_copy = makeCopy(all_data);
    		//Shuffle data together bit by bit
    		//int h=0;
    		stillMore = true;
    		while (stillMore)
    		{
    			ArrayList<Byte> currentBytes = new ArrayList<Byte>();
    			for (int j=0; j<tests.size();j++)
    				if (all_data_copy.get(j).size()>0)
    					currentBytes.add(all_data_copy.get(j).remove(0));
    			for (int i=0;i<8;i++)//the bit we want
    			{
    				int x = 0;
    				for (int j=0;j<currentBytes.size();j++)//the test we want
    					x = x | (((currentBytes.get(j).byteValue() & (1 << i))>> i) << (j));
    				
    				completeDataBitByBit.add(new DataPair(currentBytes.size(),x));
    			}
    			
    			if (currentBytes.size()==0) stillMore = false;
    		}
    		
    		
    		all_data_copy = makeCopy(all_data);
    		//Bit by bit stop at 2 complete streams (unless there was 1 bit)
    		stillMore = true;
    		while (stillMore)
    		{
    			ArrayList<Byte> currentBytes = new ArrayList<Byte>();
    			for (int j=0; j<tests.size();j++)
    				if (all_data_copy.get(j).size()>0)
    					currentBytes.add(all_data_copy.get(j).remove(0));
    			for (int i=0;i<8;i++)//the bit we want
    			{
    				int x = 0;
    				for (int j=0;j<currentBytes.size();j++)//the test we want
    					x = x | (((currentBytes.get(j).byteValue() & (1 << i))>> i) << (j));
    				
    				completeDataBitByBitLimited.add(new DataPair(currentBytes.size(),x));
    			}
    			
    			if (currentBytes.size()==0 || (tests.size()>=2 && currentBytes.size()==1)) stillMore = false;
    		}
    		
    		final Analysis finalAnalysis[] = new Analysis[3];
    		finalAnalysis[0] = new Analysis("ByteByByte");
    		finalAnalysis[0].runAnalysis(completeDataByteByByte);
    		finalAnalysis[1] = new Analysis("BitByBit");
    		finalAnalysis[1].runAnalysis(completeDataBitByBit);
    		finalAnalysis[2] = new Analysis("BitByBitLimited");
    		finalAnalysis[2].runAnalysis(completeDataBitByBitLimited);
    		File myFile = new File("/sdcard/random_"+timestamp+"/testinformation.csv");
    		FileOutputStream fOut;
    		OutputStreamWriter myOutWriter;
    		try{
	    		myFile.createNewFile();
	    		fOut = new FileOutputStream(myFile);
	    		myOutWriter = new OutputStreamWriter(fOut);
	    		myOutWriter.append("Timestamp,"+timestamp+"\r\n");
	    		for (int i=0; i<tests.size();i++)
	    		{
	    			myOutWriter.append(tests.get(i).getClass().getSimpleName()+","+all_data.get(i).size()+"\r\n");
	    		}
	    		fOut.close();
    		}catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    		for (int i=0; i<3;i++)
    		{
	    		ArrayList<Byte> random_bytes = finalAnalysis[i].getRandomBytes();
	    		
	    		try{
		    		myFile = new File("/sdcard/random_"+timestamp+"/"+finalAnalysis[i].description+".csv");
		            myFile.createNewFile();
		            fOut = new FileOutputStream(myFile);
		            myOutWriter = new OutputStreamWriter(fOut);
		            
		            myOutWriter.append("Period,"+PERIOD+"\r\n");
		            myOutWriter.append("Rounds,"+ROUNDS+"\r\n");
		            for (Test test : tests)
		            	myOutWriter.append(test.getClass().getSimpleName()+"\r\n");
		            for (int j=0;j<random_bytes.size();j++)
		            	myOutWriter.append(""+random_bytes.get(j)+"\r\n");
		            myOutWriter.close();
	    		}catch(Exception e){}
	    		try {
	 		       FileOutputStream out = new FileOutputStream("/sdcard/random_"+timestamp+"/"+finalAnalysis[i].description+".raw");
	 		       DataOutputStream dos = new DataOutputStream(out);
	 		       for (int j=0; j<random_bytes.size();j++)
	 		    	   dos.writeByte(random_bytes.get(j).byteValue());
	 		       //dos.write(random_bytes);
	 		       out.close();
		 		} catch (Exception e) {
		 		       e.printStackTrace();
		 		}
	    		try {
	    		       FileOutputStream out = new FileOutputStream("/sdcard/random_"+timestamp+"/"+finalAnalysis[i].description+".png");
	    		       Grapher.graph(finalAnalysis[i]).compress(CompressFormat.PNG, 90, out);
	    		       out.close();
	    		} catch (Exception e) {
	    		       e.printStackTrace();
	    		}
    		}
	    	runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	//shown = 0;
                	//shown = shown%analyses.size();
                	((ImageView) TrueRandomStudy.this.findViewById(R.id.forGraph)).setImageBitmap(Grapher.graph(finalAnalysis[0]));
                	((TextView) TrueRandomStudy.this.findViewById(R.id.testShown)).setText(finalAnalysis[0].description);
                }
            });
	    	
		}
    }
	public ArrayList<ArrayList<Byte>> makeCopy(ArrayList<ArrayList<Byte>> all_data) {
		ArrayList<ArrayList<Byte>> copy = new ArrayList<ArrayList<Byte>>();
		for (int i=0; i<all_data.size();i++)
		{
			copy.add(new ArrayList<Byte>());
			for (int j=0; j<all_data.get(i).size();j++)
				copy.get(i).add(all_data.get(i).get(j));
		}
		return copy;
	}
}
