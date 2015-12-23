package org.molgenis.data.annotation.graduation.analysis;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;

/**
 * This class counts the number of several impacts per patient. 
 * The output is printed in matrix form for easy pasting in Excel (for calculations and coloring).
 * Also, the output can be printed with context (for clinicians).
 * 
 * @author mbijlsma
 */
public class GetVariantsPerPatient
{
	File vcfFile;
	/**
	 * Parses a VCF file and saves relevant information in HashMaps.
	 * 
	 * @param vcfFile the file to be parsed
	 * @throws Exception if file does not exists or cannot be parsed
	 */
	public void readVCF() throws Exception
	{
		Map<String, Integer> variantCountsPerPatient = Maps.newHashMap();
		Map<String, List<String>> impactsPerPatient = Maps.newHashMap();

		Scanner s = new Scanner(vcfFile);
		String line = null;
		s.nextLine(); // skip header

		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);
			String key = lineSplit[7]; // sample ID patient
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
				List<String> entries = Lists.newArrayList();
				entries.add(impact);
				impactsPerPatient.put(key, entries);
			}
		}
		s.close();
		getImpactCountPerPatient(variantCountsPerPatient, impactsPerPatient);
	}

	/**
	 * For every patient, the number of 4 different impacts are count.
	 * 
	 * @param variantCountsPerPatient list containing number of variants per patient
	 * @param impactsPerPatient list containing impacts per patient
	 */
	public void getImpactCountPerPatient(Map<String, Integer> variantCountsPerPatient,
			Map<String, List<String>> impactsPerPatient)
	{

		Map<String, List<Integer>> impactCountsPerPatient = Maps.newHashMap();

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
				List<Integer> allImpacts = Lists.newArrayList();
				allImpacts.add(countHigh);
				allImpacts.add(countModerate);
				allImpacts.add(countLow);
				allImpacts.add(countModifier);
				impactCountsPerPatient.put(patient, allImpacts);
			}
		}
		printImpactCountsPerPatient(impactCountsPerPatient, variantCountsPerPatient);
	}
	
/**
 * Prints the number of several impacts per patient.
 * 
 * @param impactCountsPerPatient list containing impacts per patient
 * @param variantCountsPerPatient list containing number of variants per patient
 */
	public void printImpactCountsPerPatient(Map<String, List<Integer>> impactCountsPerPatient,
			Map<String, Integer> variantCountsPerPatient)
	{
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
					// output with context

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

	/**
	 * The main method.
	 * 
	 * @param args the command line arguments
	 * @throws Exception when arguments are not correct
	 */
	public static void main(String[] args) throws Exception
	{
		GetVariantsPerPatient getVariantsPerPatient = new GetVariantsPerPatient();
		getVariantsPerPatient.parseCommandLineArgs(args);
		getVariantsPerPatient.readVCF();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args the command line arguments
	 * @throws Exception when file does not exists or length of arguments is not right
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 1))
		{
			throw new Exception("Must supply 1 argument");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}
	}
}
