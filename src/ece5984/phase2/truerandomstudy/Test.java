package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;

import android.content.Context;

public interface Test 
{
	/**
	 * 
	 * @param context The Context
	 * @param raw A ByteArrayOutputStream to write raw data to
	 * @return The number of tests that will be run at once.
	 */
	public int initialize(Context context, ByteArrayOutputStream raw);
	/**
	 * Requests data from the sensors
	 * @param test which test is to be collected
	 * @return the number of bits that are relevant
	 */
	public DataPair getData(int test);
	public void finish();
	public String[] describeTests();
	public boolean timeMatters();
}
