package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

import org.molgenis.data.annotation.graduation.utils.FileReadUtils;

/**
 * This class reads a text file and calculates the true positive rate of all replicate trios according to the
 * transmission probability.
 * 
 * @author mbijlsma
 */
public class CalculateThreshold
{
	private final File mendelianViolationFileLocation = new File(
			"/Users/molgenis/Documents/graduation_project/mendelianViolationFiles/mendelian_violation_Xadjusted_replicates.txt");

	/**
	 * Reads the file and calculates the true positive rate according to the transmission probability and prints the
	 * result.
	 *
	 * @throws FileNotFoundException
	 *             when file does not exists
	 */
	private void calculateTransmissionProbability() throws FileNotFoundException
	{
		List<String> records = FileReadUtils.readFile(mendelianViolationFileLocation, true); // true: skip header
		HashMap<String, List<String>> recordMap = newHashMap();

		int maxMendelianViolationTransmissionProbability = 127;

		for (int transmissionProbability = 0; transmissionProbability < maxMendelianViolationTransmissionProbability; transmissionProbability++)
		{
			int total = 0;
			int pairs = 0;

			for (String record : records)
			{
				String[] split = record.split("\t");

				int variantTransmissionProbability = Integer.parseInt(split[5]);
				if (variantTransmissionProbability >= transmissionProbability)
				{
					String chromosomePositionKey = split[0] + "_" + split[1];

					if (recordMap.containsKey(chromosomePositionKey))
					{
						List<String> recordInfo = recordMap.get(chromosomePositionKey);
						String familyID = recordInfo.get(0);

						if (!split[3].equals(familyID))
						{
							String motherGt = recordInfo.get(1);
							String fatherGt = recordInfo.get(2);
							String childGt = recordInfo.get(3);

							if (split[6].equals(motherGt) && split[10].equals(fatherGt) && split[14].equals(childGt))
							{
								pairs++;
							}
						}
					}
					else
					{
						recordMap.put(chromosomePositionKey, newArrayList(split[3], split[6], split[10], split[14]));
					}
					total++;
				}
			}

			if (total != 0 && pairs != 0) // Skip '0' values, because dividing 0 by 0 gives NaN values
			{
				System.out.println("for tp = " + transmissionProbability + " we find " + total + " of which " + pairs
						+ " pairs (perc TP:" + ((double) pairs) / (double) total * 100.0 + ")");
			}
		}
	}

	/**
	 * The main method, invokes calculateTransmissionProbability().
	 * 
	 * @param args
	 *            the command line args
	 * @throws FileNotFoundException
	 *             when file does not exists
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		CalculateThreshold calculateThreshold = new CalculateThreshold();
		calculateThreshold.calculateTransmissionProbability();
	}
}
