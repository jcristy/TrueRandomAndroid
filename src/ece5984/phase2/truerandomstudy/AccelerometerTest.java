package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class AccelerometerTest implements Test, SensorEventListener
{
	float[] values = {0,0,0};
	boolean change = false;
	ArrayList<ArrayList<DataPair>> dataPairs;
	SensorManager sensorManager;
	ByteArrayOutputStream baos;
	/**
	 * All of tests that will be run by the framework
	 */
	static String[] planned_tests = {"1 bit cycle between axis", "1 bit X axis","1 bit Y axis","1 bit Z axis", "1 bit per axis", "2 bit cycle","2 bit per axis","3 bit cycle","3 bit per axis","2nd LSB","X Y","Y Z","2 bits per axis 0x6","XOR 1 bit per axis","XOR 2 bits per axis","Bit Change"};
	
	int[] bits;
	int[] random;
	@Override
	public int initialize(Context context, ByteArrayOutputStream raw) 
	{
		Log.d("Accel_test","Initialize Accelerometer");
		this.baos = raw;
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        bits = new int[planned_tests.length];
        random = new int[planned_tests.length];
        
        dataPairs = new ArrayList<ArrayList<DataPair>>();
        for (int i=0; i<planned_tests.length;i++)
        {
        	dataPairs.add(new ArrayList<DataPair>());
        }
        	
        return planned_tests.length;
	}
	int cycle=0;
	@Override
	public ArrayList<DataPair> getData() 
	{
		int best = 0;
		double score = Double.MAX_VALUE;
		for(int i=0;i<planned_tests.length;i++)
		{
			Analysis analysis = new Analysis("");
			
			analysis.runAnalysis(dataPairs.get(i));
			double new_score = analysis.getScore();
			Log.d("Accel","Spread for "+planned_tests[i]+":"+analysis.zeros+" "+analysis.ones+" score:"+new_score);
			if (new_score<score)
			{
				score = new_score;
				best = i;
			}
		}
		Log.d("Accel","Best was "+planned_tests[best]);
		if (score>2)//All Crap
			return new ArrayList<DataPair>();
		else
			return dataPairs.get(best);
	}

	@Override
	public void finish() 
	{
		sensorManager.unregisterListener(this);
	}

	long time = 0;
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {}
	
	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		values = event.values;
		try {
			baos.write((values[0]+","+values[1]+","+values[2]).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int x,y,z;
		for (int type=0; type<planned_tests.length;type++)
		{
			switch (type)
			{
			/*
			 * It is worth noting that the mantissa's LSB is the floats LSB
			 */
			case 0://1 bit cycle
				cycle = (cycle + 1) % 3;
				bits[type] = 1;
				x = Float.floatToIntBits(values[cycle]);
				random[type] = x & 0x1;
				break;
			case 1://1 bit one axis
				bits[type] = 1;
				x = Float.floatToIntBits(values[0]);
				random[type] = (x & 0x2)>>1;
				break;
			case 2:
				bits[type] = 1;
				x = Float.floatToIntBits(values[1]);
				random[type] = (x & 0x2)>>1;
				break;
			case 3:
				bits[type] = 1;
				x = Float.floatToIntBits(values[2]);
				random[type] = (x & 0x2)>>1;
				break;
			case 4://1 bit per axis
				bits[type] = 3;
				x = Float.floatToIntBits(values[0])&0x1;
				y = Float.floatToIntBits(values[1])&0x1;
				z = Float.floatToIntBits(values[2])&0x1;
				random[type] = (x<<2)|(y<<1)|z;
				break;
			case 5://2 bit cycle
				bits[type] = 2;
				x = Float.floatToIntBits(values[cycle]);
				random[type] = x & 0x3;
				break;
			case 6://2 bit per axis
				bits[type] = 6;
				x = Float.floatToIntBits(values[0])&0x3;
				y = Float.floatToIntBits(values[1])&0x3;
				z = Float.floatToIntBits(values[2])&0x3;
				random[type] = (x<<4)|(y<<2)|z;
				break;
			case 7://3 bit cycle
				bits[type] = 3;
				x = Float.floatToIntBits(values[cycle]);
				random[type] = 0;//x & 0x7;
				break;
			case 8://3 bit per axis
				bits[type] = 9;
				x = Float.floatToIntBits(values[0])&0x7;
				y = Float.floatToIntBits(values[1])&0x7;
				z = Float.floatToIntBits(values[2])&0x7;
				random[type] = 0;//(x<<6)|(y<<3)|z;
				break;
			case 9://2nd LSB
				bits[type] = 3;
				x = Float.floatToIntBits(values[0])&0x2;
				y = Float.floatToIntBits(values[1])&0x2;
				z = Float.floatToIntBits(values[2])&0x2;
				random[type] = (x<<1)|(y)|(z>>1);
				break;
			case 10://X Y
				bits[type] = 2;
				x = Float.floatToIntBits(values[0])&0x1;
				y = Float.floatToIntBits(values[1])&0x1;
				//z = Float.floatToIntBits(values[2])&0xF0;
				random[type] = (x<<1)|y;
				break;
			case 11://Y Z
				bits[type] = 2;
				//x = Float.floatToIntBits(values[0])&0xFF0;
				y = Float.floatToIntBits(values[1])&0xFF0;
				z = Float.floatToIntBits(values[2])&0xFF0;
				random[type] = (y<<1)|z;
				break;
			case 12://2 bits per axis, ignore LSb = 0x6
				bits[type] = 6;
				x = Float.floatToIntBits(values[0])&0x6;
				y = Float.floatToIntBits(values[1])&0x6;
				z = Float.floatToIntBits(values[2])&0x6;
				random[type] = (x<<3)|(y<<1)|(z>>1);
				break;
			case 13://XOR of 1 bit per axis
				bits[type] = 1;
				x = Float.floatToIntBits(values[0])&0x1;
				y = Float.floatToIntBits(values[1])&0x1;
				z = Float.floatToIntBits(values[2])&0x1;
				random[type] = (x)^(y)^(z);
				break;
			case 14://XOR of 2 bits per axis
				bits[type] = 1;
				x = Float.floatToIntBits(values[0])&0x3;
				y = Float.floatToIntBits(values[1])&0x3;
				z = Float.floatToIntBits(values[2])&0x3;
				int temp = (x)^(y)^(z);
				random[type] = (temp==0x3 || temp==0?0:1);
				break;
			case 15://Which bit changed?!?!?
				bits[type] = 3;
				x = Float.floatToIntBits(values[0]);
				y = Float.floatToIntBits(values[1]);
				z = Float.floatToIntBits(values[2]);
				temp = 0;
				if (prevX!=x)
					temp = temp | 0x1;
				if (prevY!=y)
					temp = temp | 0x2;
				if (prevZ!=z)
					temp = temp | 0x4;
				prevX=x;
				prevY=y;
				prevZ=z;
				random[type] = 0;// temp;
			}
			DataPair toReturn = new DataPair(bits[type],random[type]);
			dataPairs.get(type).add(toReturn); 
		}
		
		x = Float.floatToIntBits(values[0]);
		y = Float.floatToIntBits(values[1]);
		z = Float.floatToIntBits(values[2]);
		/*
		baos.write((int)System.currentTimeMillis());
		baos.write(x);
		baos.write(y);
		baos.write(z);*/
		//Log.d("Data: ", ""+x+" "+y+" "+z+" time: "+(System.currentTimeMillis()-time));
		time = System.currentTimeMillis();
	}
		int prevX=0;
		int prevY=0;
		int prevZ=0;
	@Override
	public String[] describeTests() {
		return planned_tests;
	}

	public boolean timeMatters() { return true;}
	
	public void clear()
	{
		for (ArrayList<DataPair> al : dataPairs)
		{
			al.clear();
		}
	}
	
}
