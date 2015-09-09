package org.molgenis.graduation.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class calculateThreshold
{

	public static void main(String[] args) throws FileNotFoundException
	{
		readFile();

	}

	private static void readFile() throws FileNotFoundException
	{
		ArrayList<String> transmissionProbability = new ArrayList();
		Scanner s = new Scanner(new File("/Users/molgenis/Documents/graduation_project/mendelian_violation_adjusted_replicates.txt"));
		
		s.nextLine(); //header
		while (s.hasNextLine())
		{
			String line = s.nextLine();
//			System.out.println(line);
			transmissionProbability.add(line);
		}

		for (int tp = 2; tp < 53; tp++)
		{
			int total = 0;
			int pairs = 0;

			for (String line : transmissionProbability)
			{
				String[] split = line.split("\t");

				int variantTP = Integer.parseInt(split[4]);
				System.out.println(split[10]);

				if (variantTP < tp)
				{
					continue;
				}
				total++;
				for (String line1 : transmissionProbability)
				{
					String[] split1 = line1.split("\t");
					
					int variantTP1 = Integer.parseInt(split1[4]);
					
					if (variantTP1 < tp)
					{
						continue;
					}

					if (split1[3].equals(split[3]))
					{
						continue;
					}
					if (split1[0].equals(split[0]) && split1[1].equals(split[1]) && split1[5].equals(split[5])
							&& split1[9].equals(split[9]) && split1[13].equals(split[13]))
					{
						pairs++;
					}
				}

			}
			System.out.println("for tp = " + tp + " we find " + total + " of which " + pairs + " pairs (perc TP:" + ((double)pairs)/(double)total*100.0 + ")");
		}
	}
}
