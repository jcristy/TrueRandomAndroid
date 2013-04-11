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

	int[] values = {0,0,0};
	int[] bits = {0,0,0};
	Context context;
	
	ArrayList<ArrayList<DataPair>> dataPairs;
	public NetworkStatusTest()
	{
		
	}
	@Override
	public int initialize(Context context, ByteArrayOutputStream raw) {
		this.context = context;
		TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		tm.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS|PhoneStateListener.LISTEN_CELL_LOCATION);
		
		Timer time = new Timer();
		
		dataPairs = new ArrayList<ArrayList<DataPair>>();
        for (int i=0; i<describeTests().length;i++)
        {
        	dataPairs.add(new ArrayList<DataPair>());
        }
		final ArrayList<ArrayList<DataPair>> fDataPairs = dataPairs;
		final Context fcontext = context;
		time.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				for (int test = 0; test < describeTests().length; test++)
				{
					if (test <= 1)
					{
						DataPair toReturn = new DataPair(bits[test],values[test]);
						bits[test] = 0;
						values[test] = 0;
						dataPairs.get(test).add(toReturn);
					}
					else
					{
						TelephonyManager tm = (TelephonyManager) fcontext.getSystemService(fcontext.TELEPHONY_SERVICE);
						for( NeighboringCellInfo nc : tm.getNeighboringCellInfo())
						{
							Log.d("RSSI: ", ""+nc.getRssi());
							
						}
						dataPairs.get(test).add(new DataPair(0,0));
					}
				}
			}
			
		}, 0, 2000);
		
		return 3;
	}

	@Override
	public ArrayList<DataPair> getData() 
	{
		return dataPairs.get(0);
	}

	
	
	@Override
	public void finish() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		tm.listen(this, PhoneStateListener.LISTEN_NONE);

	}

	@Override
	public String[] describeTests() {
		
		return new String[]{"1 bit of CDMA Strength","1 bit of EVDO Strength","1 bit of RSSI of nearest neighbor"};
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
		Log.d("Data: ",signalStrength.getCdmaDbm()+", "+signalStrength.getEvdoDbm()+", "+signalStrength.getCdmaEcio()+", "+signalStrength.getEvdoEcio()+", "+signalStrength.getEvdoSnr());
	}
}
