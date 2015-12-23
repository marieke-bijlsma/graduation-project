package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readMendelianViolationFile;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

/**
 * This class calculates the true positive rate of all replicate trios according to the transmission probability.
 * 
 * @author mbijlsma
 */
public class CalculateThreshold
{
	/**
	 * Calculates the true positive rate according to the transmission probability and prints the result.
	 * 
	 * @param records
	 *            {@link List} containing all lines of file
	 * @throws FileNotFoundException
	 *             when file is not found
	 */
	private void calculateTransmissionProbability() throws FileNotFoundException
	{
		List<String> records = readMendelianViolationFile();

		// Map to keep track of chromosome - position combinations so we can quickly scan the file for duplicate
		// genotypes from samples with different family identifiers
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

			// Skip 0 values because dividing 0 by 0 gives NaN values
			if (total != 0 && pairs != 0)
			{
				System.out.println("for tp = " + transmissionProbability + " we find " + total + " of which " + pairs
						+ " pairs (perc TP:" + ((double) pairs) / (double) total * 100.0 + ")");
			}
		}
	}

	/**
	 * The main method, calculateTransmissionProbability().
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 *             when given file is not found
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		CalculateThreshold calculateThreshold = new CalculateThreshold();
		calculateThreshold.calculateTransmissionProbability();
	}
}
