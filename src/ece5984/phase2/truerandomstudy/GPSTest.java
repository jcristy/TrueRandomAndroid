package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class GPSTest implements Test, LocationListener {

	int[] bits = new int[]{0,0,0,0,0};
	int[] data = new int[]{0,0,0,0,0};
	Context context;
	ArrayList<ArrayList<DataPair>> dataPairs;
	
	@Override
	public int initialize(Context context, ByteArrayOutputStream raw) {
		this.context = context;
		dataPairs = new ArrayList<ArrayList<DataPair>>();
        for (int i=0; i<describeTests().length;i++)
        {
        	dataPairs.add(new ArrayList<DataPair>());
        }
		return 5;
	}

	@Override
	public ArrayList<DataPair> getData() {
		
		return dataPairs.get(0);
	}

	@Override
	public void finish() {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(this);
	}

	@Override
	public String[] describeTests() {
		return new String[]{"1 bit each","2 bits each","1 bit skew","2 bits skew","1 bit lat/lon/skew"};
	}

	@Override
	public boolean timeMatters() {
		return true;
	}

	long prevGPSTime = -1;
	long prevJavaTime = -1;
	@Override
	public void onLocationChanged(Location location) 
	{
		long rawLat = Double.doubleToLongBits(location.getLatitude());
		long rawLon = Double.doubleToLongBits(location.getLongitude());
		long currentGPSTime = location.getTime();
		long currentJavaTime = System.currentTimeMillis();
		long skew = (currentJavaTime-prevJavaTime)-(currentGPSTime-prevGPSTime);
		prevGPSTime = location.getTime();
		prevJavaTime = currentJavaTime;
		//Log.d("Data: ", ""+rawLat+" "+rawLon+" "+location.getAccuracy()+" "+location.getTime()+" "+skew);
		bits[0] = 2;
		data[0] = (int)(rawLat&0x1 | ((rawLon&0x1)<<1));
		bits[1] = 4;
		data[1] = (int)(rawLat&0x3 | ((rawLon&0x3)<<2));
		if (prevGPSTime != -1)
		{
			bits[2] = 1;
			data[2] = (int)(skew&0x1);
			bits[3] = 2;
			data[3] = (int)(skew&0x3);
			bits[4] = 3;
			data[4] = (int)(((rawLat&0x1)<<2) | ((rawLon&0x1)<<1)| (skew&0x1));
		}
		else
		{
			bits[2] = 0;
			bits[3] = 0;
			data[2] = 0;
			data[3] = 0;
			bits[4] = 0;
			data[4] = 0;
		}
		for (int i=0;i<5;i++)
		{
			dataPairs.get(i).add(new DataPair(bits[i],data[i]));
			bits[i] = 0;
			data[i] = 0;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	
	public void clear()
	{
		for (ArrayList<DataPair> al : dataPairs)
		{
			al.clear();
		}
	}
	
}
