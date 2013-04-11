package ece5984.phase2.truerandomstudy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;

public class PseudoTest implements Test {

	Random rand;
	@Override
	public int initialize(Context context, ByteArrayOutputStream raw) 
	{
		rand = new Random();
		return 1;
	}

	@Override
	public void finish() 
	{

	}

	@Override
	public String[] describeTests() {
		return new String[]{"32 bits of Pseudo-Random"};
	}

	public boolean timeMatters()
	{
		return false;
	}

	@Override
	public ArrayList<DataPair> getData() {
		ArrayList<DataPair> data = new ArrayList<DataPair>();
		for (int i=0; i < 8; i++)//create 256 bits
			data.add(new DataPair(32,rand.nextInt()));
		return data;
	}
}
