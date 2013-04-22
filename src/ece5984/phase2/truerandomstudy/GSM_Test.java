package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class GSM_Test extends PhoneStateListener implements Test {
	int[] values = {0,0,0,0,0,0};
	int[] bits = {0,0,0,0,0,0};
	Context context;
	
	ArrayList<ArrayList<DataPair>> dataPairs;
	public GSM_Test()
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
		Log.d("GSM Status","Best was "+describeTests()[best]);
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
		
		return new String[]{"0x1 - GSM","0x1 - GSM ER","0x2 - GSM","0x2 - GSM ER","0x3 - GSM","0x3 - GSM ER"};
	}

	@Override
	public boolean timeMatters() {
		
		return true;
	}
	public void onSignalStrengthsChanged (SignalStrength signalStrength)
	{
		signalStrength.getGsmSignalStrength();
		values[0] = signalStrength.getGsmSignalStrength()&0x1;
		bits[0] = 1;
		values[1] = signalStrength.getGsmBitErrorRate()&0x1;
		bits[1] = 1;
		values[2] = (signalStrength.getGsmSignalStrength()&0x2)>>1;
		bits[2] = 1;
		values[3] = (signalStrength.getGsmBitErrorRate()&0x2)>>1;
		bits[3] = 1;
		values[4] = (signalStrength.getGsmSignalStrength()&0x3);
		bits[4] = 2;
		values[5] = (signalStrength.getGsmBitErrorRate()&0x3);
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
