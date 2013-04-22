package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ProximityTest implements Test, SensorEventListener 
{
	public String[] plannedTests = new String[]{"LSb","LSB","LS4","LS0x3C","XOR LS4","XOR LSB"};
	int[] bits;
	int[] randoms;
	SensorManager sensorManager;
	float value;
	ArrayList<ArrayList<DataPair>> dataPairs; 
	@Override
	public int initialize(Context context, ByteArrayOutputStream raw) 
	{
		
		bits = new int[plannedTests.length];
		randoms = new int[plannedTests.length];
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
		
        dataPairs = new ArrayList<ArrayList<DataPair>>();
        for (int i=0; i<plannedTests.length;i++)
        {
        	dataPairs.add(new ArrayList<DataPair>());
        }
        
        return plannedTests.length;
	}

	@Override
	public ArrayList<DataPair> getData() {
		int best = 0;
		double score = Double.MAX_VALUE;
		for(int i=0;i<plannedTests.length;i++)
		{
			Analysis analysis = new Analysis("");
			
			analysis.runAnalysis(dataPairs.get(i));
			double new_score = analysis.getScore();
			Log.d("Proximity","Spread for "+plannedTests[i]+":"+analysis.zeros+" "+analysis.ones+" score:"+new_score);
			if (new_score<score)
			{
				score = new_score;
				best = i;
			}
		}
		Log.d("Proximity","Best was "+plannedTests[best]);
		if (score>2)//All Crap
			return new ArrayList<DataPair>();
		else
			return dataPairs.get(best);

	}

	@Override
	public void finish() {
		sensorManager.unregisterListener(this);
	}

	@Override
	public String[] describeTests() {
		return plannedTests;
	}

	@Override
	public boolean timeMatters() {

		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		value = event.values[0];
		int x = Float.floatToIntBits(value);
		for (int i=0;i<plannedTests.length;i++)
		{
			switch(i)
			{
			case 0://LSb
				bits[i] = 0;
				randoms[i] = x & 0x1;
				break;
			case 1://LSB
				bits[i] = 8;
				randoms[i] = x & 0xFF;
				break;
			case 2://LS4
				bits[i] = 4;
				randoms[i] = x & 0xF;
				break;
			case 3://LS 0x3C
				bits[i] = 4;
				randoms[i] = (x>>2) & 0xF;
				break;
			case 4://XOR LS4
				bits[i] = 4;
				randoms[i] = 0;
				for (int j=0;j<4;j++)
					randoms[i] = randoms[i]^((x>>j)&0x1);
				break;
			case 5:// XOR LSB
				bits[i] = 8;
				randoms[i] = 0;
				for (int j=0;j<8;j++)
					randoms[i] = randoms[i]^((x>>j)&0x1);
				break;
			}
			DataPair toReturn = new DataPair(bits[i],bits[i]);
			dataPairs.get(i).add(toReturn);
		}
	}
	public void clear()
	{
		for (ArrayList<DataPair> al : dataPairs)
		{
			al.clear();
		}
	}
	
}
