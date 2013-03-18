package ece5984.phase2.truerandomstudy;

import java.util.ArrayList;

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
	public StringBuilder bitStream;
	int[] pairs = new int[4];
	int[] triples = new int[8];
	public Analysis()
	{
		randoms = new ArrayList<Integer>();
		bitStream = new StringBuilder();
		for (int i = 0; i<4; i++)
			pairs[i] = 0;
		for (int i = 0; i<8; i++)
			triples[i] = 0;
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
			if (current.bits==0) continue;
			for (int j=0; j<current.bits; j++)
			{
				currentBit = current.value & 0x1;
				stream = stream << 1;
				stream = stream | currentBit;
				bitStream.append(currentBit);
				//Construct Randoms
				currentInteger = currentInteger << 1;
				currentInteger = currentInteger | currentBit;
				bits++;
				if (bits == 32)
				{
					randoms.add(new Integer(currentInteger));
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
				if (bits > 1)
				{
					pairs[stream&0x3]++;
				}
				//check triple
				if (bits > 2)
				{
					triples[stream&0x7]++;
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
				current.value = current.value >> 1;
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
}
