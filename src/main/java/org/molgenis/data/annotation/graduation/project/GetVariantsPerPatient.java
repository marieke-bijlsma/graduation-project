package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;

public class GetVariantsPerPatient
{
	Map<String, Integer> variantCountsPerPatient = Maps.newHashMap();
	Map<String, List<String>> impactsPerPatient = Maps.newHashMap();
	Map<String, List<Integer>> impactCountsPerPatient = Maps.newHashMap();

	public void go(File vcfFile) throws Exception
	{
		Scanner s = new Scanner(vcfFile);
		String line = null;
		s.nextLine(); // skip header
		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);
			String key = lineSplit[7];
			String impacts = lineSplit[2];
			String[] impactSplit = impacts.split(",");
			String impact = impactSplit[0];

			// if patient not in map yet, add patient and count =1
			// else, add patient and count one up

			if (!variantCountsPerPatient.containsKey(key))
			{
				variantCountsPerPatient.put(key, 1);
			}
			else
			{
				variantCountsPerPatient.put(key, variantCountsPerPatient.get(key) + 1);
			}

			// if key already in map, get it and add impact
			// else, add impacts to new list and put it together in map

			if (impactsPerPatient.containsKey(key))
			{
				impactsPerPatient.get(key).add(impact);
			}
			else
			{
				List<String> entries = new ArrayList<String>();
				entries.add(impact);
				impactsPerPatient.put(key, entries);
			}
		}
		s.close();

		// for every patient
		for (Map.Entry<String, List<String>> entry : impactsPerPatient.entrySet())
		{
			// get patient
			String patient = entry.getKey();

			// next patient, count=0
			int countHigh = 0;
			int countModerate = 0;
			int countLow = 0;
			int countModifier = 0;

			// for every impact
			for (String impact : entry.getValue())
			{
				// count high/moderate/low/modifier per patient
				if (impact.equals("HIGH"))
				{
					countHigh++;
				}

				else if (impact.equals("MODERATE"))
				{
					countModerate++;
				}
				else if (impact.equals("LOW"))
				{
					countLow++;
				}
				else if (impact.equals("MODIFIER"))
				{
					countModifier++;
				}
				else
				{
					System.out.println("This is not a legal impact: " + impact);
				}

				// empty list, add counts per impact -> in map with associated patient
				List<Integer> allImpacts = new ArrayList<Integer>();
				allImpacts.add(countHigh);
				allImpacts.add(countModerate);
				allImpacts.add(countLow);
				allImpacts.add(countModifier);
				impactCountsPerPatient.put(patient, allImpacts);
			}
		}
		// print header
		System.out.println("Sample id" + "\t" + "variant count" + "\t" + "high" + "\t" + "moderate" + "\t" + "low"
				+ "\t" + "modifier");

		// Print patient + variant count + counts per impact
		for (Entry<String, Integer> entryVariantCount : variantCountsPerPatient.entrySet())
		{
			for (Entry<String, List<Integer>> entryImpactCount : impactCountsPerPatient.entrySet())
			{
				String patient = entryImpactCount.getKey();

				if (variantCountsPerPatient.containsKey(patient))
				{
					// output with annotation

					// System.out.println("Patient " + patient + " has " + variantCountsPerPatient.get(patient)
					// + " variants." + entryImpactCount.getValue().get(0) + " high impacts, "
					// + entryImpactCount.getValue().get(1) + " moderate impacts, "
					// + entryImpactCount.getValue().get(2) + " low impacts, and "
					// + entryImpactCount.getValue().get(3) + " modifier impacts.");

					// output for making graph (easy to paste in excel)
					System.out.println(patient + "\t" + variantCountsPerPatient.get(patient) + "\t"
							+ entryImpactCount.getValue().get(0) + "\t" + entryImpactCount.getValue().get(1) + "\t"
							+ entryImpactCount.getValue().get(2) + "\t" + entryImpactCount.getValue().get(3));
				}
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		GetVariantsPerPatient vp = new GetVariantsPerPatient();
		vp.run(args);

	}

	public void run(String[] args) throws Exception
	{
		if (!(args.length == 1))
		{
			throw new Exception("Must supply 1 argument");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		GetVariantsPerPatient vpp = new GetVariantsPerPatient();
		vpp.go(vcfFile);

	}

}
