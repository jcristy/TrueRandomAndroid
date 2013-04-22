package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WiFiTest implements Test {
	int[] values = {0,0,0,0,0,0,0,0};
	int[] bits = {0,0,0,0,0,0,0,0};
	Timer t = new Timer();
	Context context;
	
	ArrayList<ArrayList<DataPair>> dataPairs;
	public WiFiTest()
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
			Log.d("WiFi","Spread for "+describeTests()[i]+":"+analysis.zeros+" "+analysis.ones+" score:"+new_score);
			if (new_score<score)
			{
				score = new_score;
				best = i;
			}
		}
		Log.d("WiFi","Best was "+describeTests()[best]);
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
		
		return new String[]{"0x1 - rssi","0x2 - rssi","0x3 - rssi","0x 3F Sum","0x 3F XOR","0x 3F SUM^XOR","0x3 Sum","0x3 Xor"};
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
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);    
			wifi.startScan();
			int rssi = wifi.getConnectionInfo().getRssi(); // for geting RSSI
			if (rssi == previous) return;
			previous = rssi;
			
			values[0] = rssi&0x1;
			bits[0] = 1;
			values[1] = (rssi&0x2)>>1;
			bits[1] = 1;
			values[2] = (rssi&0x3);
			bits[2] = 2;

			List<ScanResult> results = wifi.getScanResults();
			int sum = 0;
			int xor = 0;
			for (ScanResult result : results)
			{
				int level = result.level;
				sum += level;
				xor = level ^ sum;
			}
			//level is -100 - 0
			values[3] = sum;
			bits[3] = 6;
			values[4] = xor;
			bits[4] = 6;
			values[5] = sum ^ xor; 
			bits[5] = 6;
			values[6] = sum & 0x3;
			bits[6] = 2;
			values[7] = xor & 0x3;
			bits[7] = 2;
					
			for (int i=0;i<8;i++)
				dataPairs.get(i).add(new DataPair(bits[i],values[i]));
			//Log.d("Data: ",signalStrength.getCdmaDbm()+", "+signalStrength.getEvdoDbm()+", "+signalStrength.getCdmaEcio()+", "+signalStrength.getEvdoEcio()+", "+signalStrength.getEvdoSnr());
			
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
