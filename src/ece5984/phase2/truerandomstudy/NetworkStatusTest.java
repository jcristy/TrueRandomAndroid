package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.content.Context;

public class NetworkStatusTest extends PhoneStateListener implements Test {

	int[] values = {0,0,0,0,0,0};
	int[] bits = {0,0,0,0,0,0};
	Context context;
	
	ArrayList<ArrayList<DataPair>> dataPairs;
	public NetworkStatusTest()
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
		TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		tm.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS|PhoneStateListener.LISTEN_CELL_LOCATION);
		return 6;
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
			Log.d("Network Status","Spread for "+describeTests()[i]+":"+analysis.zeros+" "+analysis.ones+" score:"+new_score);
			if (new_score<score)
			{
				score = new_score;
				best = i;
			}
		}
		Log.d("Network Status","Best was "+describeTests()[best]);
		if (score>2.5)//All Crap
			return new ArrayList<DataPair>();
		else
			return dataPairs.get(best);
	}

	
	
	@Override
	public void finish() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		tm.listen(this, PhoneStateListener.LISTEN_NONE);

	}

	@Override
	public String[] describeTests() {
		
		return new String[]{"0x1 - CDMA","0x1 - EVDO","0x2 - CDMA","0x2 - EVDO","0x3 - CDMA","0x3 - EVDO"};
	}

	@Override
	public boolean timeMatters() {
		
		return true;
	}
	public void onSignalStrengthsChanged (SignalStrength signalStrength)
	{
		values[0] = signalStrength.getCdmaDbm()&0x1;
		bits[0] = 1;
		values[1] = signalStrength.getEvdoDbm()&0x1;
		bits[1] = 1;
		values[2] = (signalStrength.getCdmaDbm()&0x2)>>1;
		bits[2] = 1;
		values[3] = (signalStrength.getEvdoDbm()&0x2)>>1;
		bits[3] = 1;
		values[4] = (signalStrength.getEvdoDbm()&0x3);
		bits[4] = 2;
		values[5] = (signalStrength.getEvdoDbm()&0x3);
		bits[5] = 2;
		for (int i=0;i<6;i++)
			dataPairs.get(i).add(new DataPair(bits[i],values[i]));
		//Log.d("Data: ",signalStrength.getCdmaDbm()+", "+signalStrength.getEvdoDbm()+", "+signalStrength.getCdmaEcio()+", "+signalStrength.getEvdoEcio()+", "+signalStrength.getEvdoSnr());
		
	}
	
	public void clear()
	{
		for (ArrayList<DataPair> al : dataPairs)
		{
			al.clear();
		}
	}
	
}
