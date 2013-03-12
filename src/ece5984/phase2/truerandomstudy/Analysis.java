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
	public Analysis()
	{
		randoms = new ArrayList<Integer>();
		bitStream = new StringBuilder();
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
		for (int i=0; i<data.size();i++)
		{
			DataPair current = data.get(i);
			if (current.bits==0) continue;
			for (int j=0; j<current.bits; j++)
			{
				currentBit = current.value & 0x1;
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
		return ("Test Title,Zeros,Ones,Longest Streak of Zeros,Longest Streak of Ones,Average Streak Length of Zeros, Average Streak Length of Ones,Bit Stream");
	}
	public String toString(String title) 
	{
		return (title+","+zeros+","+ones+","+longestStreaks[0]+","+longestStreaks[1]+","+(((double)totalOfStreaks[0])/numStreaks[0])+","+(((double)totalOfStreaks[1])/numStreaks[1])+","+getBitStream());
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
