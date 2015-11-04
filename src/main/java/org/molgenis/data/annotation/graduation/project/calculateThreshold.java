package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.elasticsearch.common.collect.Lists;

public class calculateThreshold
{

	public static void main(String[] args) throws FileNotFoundException
	{
		readFile();

	}

	private static void readFile() throws FileNotFoundException
	{
		ArrayList<String> transmissionProbability = Lists.newArrayList();
		@SuppressWarnings("resource")
		Scanner s = new Scanner(new File("/Users/molgenis/Documents/graduation_project/mendelianViolationFiles/mendelian_violation_Xadjusted_replicates.txt"));
		
		s.nextLine(); //header
		while (s.hasNextLine())
		{
			String line = s.nextLine();
//			System.out.println(line);
			transmissionProbability.add(line);
		}

		for (int tp = 0; tp < 127; tp++) //max is 126
		{
			int total = 0;
			int pairs = 0;

			for (String line : transmissionProbability)
			{
				String[] split = line.split("\t");

				int variantTP = Integer.parseInt(split[5]);

				if (variantTP < tp)
				{
//					System.out.println(tp + " " + variantTP);
					continue;
				}
				for (String line1 : transmissionProbability)
				{
					String[] split1 = line1.split("\t");
					
					int variantTP1 = Integer.parseInt(split1[5]);
					
					if (variantTP1 < tp)
					{
						continue;
					}

					if (split1[3].equals(split[3]))
					{
						continue;
					}
					if (split1[0].equals(split[0]) && split1[1].equals(split[1]) && split1[6].equals(split[6])
							&& split1[10].equals(split[10]) && split1[14].equals(split[14]))
					{
						pairs++;
					}
				}

			}
			System.out.println("for tp = " + tp + " we find " + total + " of which " + pairs + " pairs (perc TP:" + ((double)pairs)/(double)total*100.0 + ")");
		}
	}
}