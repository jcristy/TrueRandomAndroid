package ece5984.phase2.truerandomstudy;

import java.util.ArrayList;

import android.util.Log;

public class Analysis 
{
	int zeros = 0;
	int ones = 0;
	int[] longestStreaks = {0,0};
	int[] numStreaks = {0,0};
	int lastbit=-1;
	//double averageStreak = 0;
	long[] totalOfStreaks = {0,0};
	int currentStreak = 0;
	public ArrayList<Integer> randoms;
	public ArrayList<Byte> random_bytes;
	public StringBuilder bitStream;
	int[] pairs = new int[4];
	int[] triples = new int[8];
	int[] quads = new int[16];
	public String description;
	public Analysis(String description)
	{
		this.description = description; 
		randoms = new ArrayList<Integer>();
		random_bytes = new ArrayList<Byte>();
		bitStream = new StringBuilder();
		for (int i = 0; i<4; i++)
			pairs[i] = 0;
		for (int i = 0; i<8; i++)
			triples[i] = 0;
		for (int i=0;i<16;i++)
			quads[i] = 0;
	}
	/**
	 * Checks for streaks and other information
	 * TODO should look for repeating bits
	 * @param data
	 */
	public void runAnalysis(ArrayList<DataPair> data)
	{
		int bits = 0;
		int currentInteger = 0;
		int currentBit = 0;
		int stream = 0;
		for (int i=0; i<data.size();i++)
		{
			DataPair current = data.get(i);
			int current_value = current.value;
			if (current.bits==0) continue;
			for (int j=0; j<current.bits; j++)
			{
				currentBit = current_value & 0x1;
				stream = stream << 1;
				stream = stream | currentBit;
				bitStream.append(currentBit);
				//Construct Randoms
				currentInteger = currentInteger << 1;
				currentInteger = currentInteger | currentBit;
				bits++;
				if (bits % 8 == 0)
				{
					
					random_bytes.add(Byte.valueOf((byte) (currentInteger&0xFF)));
				}
				if (bits == 32)
				{
					randoms.add(Integer.valueOf(currentInteger));
					currentInteger = 0;
					bits = 0;
				}
				
				//Perform Analysis
				if (lastbit==-1)//First Time, so nothing should matter
				{
					currentStreak--;
					lastbit = currentBit;
				}
				if (currentBit == 0) zeros++;
				else if (currentBit == 1) ones++;
				
				//check double
				if (bits > 1 && bits%2==0)
				{
					pairs[stream&0x3]++;
				}
				//check triple
				if (bits > 2 && bits%3==0)
				{
					triples[stream&0x7]++;
				}
				//check quad
				if (bits > 3 && bits%4==0)
				{
					quads[stream&0xF]++;
				}
					
				if (currentBit == lastbit) currentStreak++;
				else if (currentBit != lastbit)
				{
					
					//averageStreak = (averageStreak * numStreaks+currentStreak)/(numStreaks+1);
					
					if (currentStreak > longestStreaks[currentBit^0x1]) longestStreaks[currentBit^0x1] = currentStreak;
					totalOfStreaks[currentBit^0x1] = totalOfStreaks[currentBit^0x1] + currentStreak;
					numStreaks[currentBit^0x1]++;
					currentStreak = 1;
				}
					
				lastbit = currentBit;
				current_value = current_value >> 1;
			}
		}
		totalOfStreaks[currentBit] = totalOfStreaks[currentBit] + currentStreak;
		numStreaks[currentBit]++;
	}
	public static String header()
	{
		return ("Test Title,Zeros,Ones,Longest Streak of Zeros,Longest Streak of Ones,Average Streak Length of Zeros, Average Streak Length of Ones," +
				"'00,'01,'10,'11,'000,'001,'010,'011,'100,'101,'110,'111,Bit Stream");
	}
	public String toString(String title) 
	{
		StringBuilder sb = new StringBuilder();
		sb.append(title+","+zeros+","+ones+","+longestStreaks[0]+","+longestStreaks[1]+","+(((double)totalOfStreaks[0])/numStreaks[0])+","+(((double)totalOfStreaks[1])/numStreaks[1])+",");
		for (int i=0; i<pairs.length;i++)	
			sb.append(pairs[i]+",");
		for (int i=0; i<triples.length;i++)
			sb.append(triples[i]+",");
				
		sb.append(getBitStream());
		return (sb.toString());
	}
	public ArrayList<Byte> getRandomBytes()
	{
		return random_bytes;
	}
	public String getRandomIntegers()
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<randoms.size();i++)	
			sb.append(randoms.get(i)+"\r\n");
		return sb.toString();
	}
	public String getBitStream()
	{
		return bitStream.toString()+"b";
	}
	public double getScore()
	{
		double score = 0;
		for (int i=1;i<=4;i++)
			score+=getScore(i);
		return score;
	}
	/**
	 * 
	 * @param bits which pattern length to check for
	 * @return the difference from the expected value
	 */
	public double getScore(int bits)
	{
		double toReturn = 0;
		if (bits==1)
		{
			double sum = zeros + ones;
			toReturn = Math.abs(.5-zeros/(sum))+Math.abs(.5-ones/(sum));
		}
		if (bits==2)
		{
			double expected = .25;
			double sum = 0;
			for (int i=0;i<4;i++)
				sum+=pairs[i];
			for (int i=0;i<4;i++)
				toReturn += Math.abs(expected-pairs[i]/sum);
		}
		if (bits==3)
		{
			double expected = .125;
			double sum = 0;
			for (int i=0;i<8;i++)
				sum+=triples[i];
			for (int i=0;i<8;i++)
				toReturn += Math.abs(expected-triples[i]/sum);
		}
		if (bits==4)
		{
			double expected = .0625;
			double sum = 0;
			for (int i=0;i<16;i++)
				sum+=quads[i];
			for (int i=0;i<16;i++)
				toReturn += Math.abs(expected-quads[i]/sum);
		}
		return toReturn;
	}
}
