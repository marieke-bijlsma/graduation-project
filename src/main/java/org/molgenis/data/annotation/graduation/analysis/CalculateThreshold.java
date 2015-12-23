package org.molgenis.data.annotation.graduation.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.elasticsearch.common.collect.Lists;

/**
 * This class calculates the true positive rate of all replicate trios according to the transmission probability.
 * 
 * @author mbijlsma
 */
public class CalculateThreshold
{
	/**
	 * Read file and add lines to ArrayList.
	 * 
	 * @throws FileNotFoundException
	 *             when file not found
	 * @return record list containing all lines of file
	 */
	private static ArrayList<String> readFile() throws FileNotFoundException
	{
		ArrayList<String> record = Lists.newArrayList();
		@SuppressWarnings("resource")
		Scanner s = new Scanner(
				new File(
						"/Users/molgenis/Documents/graduation_project/mendelianViolationFiles/mendelian_violation_Xadjusted_replicates.txt"));

		s.nextLine(); // skip header
		while (s.hasNextLine())
		{
			String line = s.nextLine();
			record.add(line);
		}

		return record;
	}

	/**
	 * Calculates the true positive rate according to the transmission probability and prints the result.
	 * 
	 * @param record
	 *            list containing all lines of file
	 */
	private static void calculateTransmissionProbability(ArrayList<String> record)
	{

		for (int tp = 0; tp < 127; tp++) // max is 126
		{
			int total = 0;
			int pairs = 0;

			for (String line : record)
			{
				String[] split = line.split("\t");

				int variantTP = Integer.parseInt(split[5]);

				if (variantTP < tp)
				{
					continue;
				}

				for (String replicateLine : record) // go through lines 2 times
				{
					String[] replicateSplit = replicateLine.split("\t");

					int replicateVariantTP = Integer.parseInt(replicateSplit[5]);

					if (replicateVariantTP < tp)
					{
						continue;
					}

					if (replicateSplit[3].equals(split[3])) // matches itself
					{
						continue;
					}

					if (replicateSplit[0].equals(split[0]) && replicateSplit[1].equals(split[1])
							&& replicateSplit[6].equals(split[6]) && replicateSplit[10].equals(split[10])
							&& replicateSplit[14].equals(split[14])) // if pair
					{
						pairs++;
					}
				}
				total++;
			}
			System.out.println("for tp = " + tp + " we find " + total + " of which " + pairs + " pairs (perc TP:"
					+ ((double) pairs) / (double) total * 100.0 + ")");
		}
	}

	/**
	 * The main method, invokes readFile() and calculateTransmissionProbability().
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 *             when given file is not found
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		ArrayList<String> record = readFile();
		calculateTransmissionProbability(record);
	}
}
