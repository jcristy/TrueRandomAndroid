package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ece5984.phase2.truerandomstudy.WiFiTest.doIt;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.util.Log;

public class SystemTest implements Test {
	int[] values = {0,0,0,0,0,0,0,0};
	int[] bits = {0,0,0,0,0,0,0,0};
	Timer t = new Timer();
	Context context;
	
	ArrayList<ArrayList<DataPair>> dataPairs;
	public SystemTest()
	{
		
	}
	@Override
	public int initialize(Context context, ByteArrayOutputStream raw) {
		this.context = context;
		
		dataPairs = new ArrayList<ArrayList<DataPair>>();
        for (int i=0; i<describeTests().length;i++)
        {
        	dataPairs.add(new ArrayList<DataPair>());
        }
		t.scheduleAtFixedRate(new doIt(), 0, 250);
		return 8;
	}

	@Override
	public ArrayList<DataPair> getData() 
	{
		int best = 0;
		double score = Double.MAX_VALUE;
		for(int i=0;i<describeTests().length;i++)
		{
			Analysis analysis = new Analysis("");
			
			analysis.runAnalysis(dataPairs.get(i));
			double new_score = analysis.getScore();
			Log.d("System","Spread for "+describeTests()[i]+":"+analysis.zeros+" "+analysis.ones+" score:"+new_score);
			if (new_score<score)
			{
				score = new_score;
				best = i;
			}
		}
		Log.d("System","Best was "+describeTests()[best]);
		if (score>2)//All Crap
			return new ArrayList<DataPair>();
		else
			return dataPairs.get(best);
	}

	
	
	@Override
	public void finish() {
		t.cancel();
	}

	@Override
	public String[] describeTests() {
		
		return new String[]{""};
	}

	@Override
	public boolean timeMatters() {
		
		return true;
	}
	public class doIt extends TimerTask
	{
		int previous = 0;
		public void run ()
		{
			Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			
			int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
			int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
			
			//Log.d("Battery",level+" "+temperature+" "+voltage);
			
			values[0] = level&0x1;
			bits[0] = 1;
			values[1] = (level&0x2)>>1;
			bits[1] = 1;
			values[2] = (level&0x3);
			bits[2] = 2;		
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
