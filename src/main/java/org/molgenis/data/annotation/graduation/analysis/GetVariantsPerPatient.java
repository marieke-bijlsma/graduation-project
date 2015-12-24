package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.HIGH;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.LOW;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.MODERATE;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.MODIFIER;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.valueOf;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;

/**
 * This class counts the number of several impacts per patient. The output is printed in matrix form for easy pasting in
 * Excel (for calculations and coloring). Also, the output can be printed with context (for clinicians).
 * 
 * @author mbijlsma
 */
public class GetVariantsPerPatient
{
	private File vcfFile;

	/**
	 * Parses a VCF file and saves relevant information in HashMaps.
	 * 
	 * @param vcfFile
	 *            the file to be parsed
	 * @throws Exception
	 *             if file does not exists or cannot be parsed
	 */
	public void readVCF() throws Exception
	{
		Map<String, Integer> variantCountsPerPatient = newHashMap();
		Map<String, List<Impact>> impactsPerPatient = newHashMap();

		for (String record : readFile(vcfFile, true))
		{
			String[] recordSplit = record.split("\t", -1);
			String sampleIdentifier = recordSplit[7]; // sample ID patient
			Impact impact = valueOf(recordSplit[2].split(",")[0]);

			// if patient not in map yet, add patient and count =1
			// else, add patient and count one up
			if (!variantCountsPerPatient.containsKey(sampleIdentifier))
			{
				variantCountsPerPatient.put(sampleIdentifier, 1);
			}
			else
			{
				variantCountsPerPatient.put(sampleIdentifier, variantCountsPerPatient.get(sampleIdentifier) + 1);
			}

			// if key already in map, get it and add impact
			// else, add impacts to new list and put it together in map
			if (impactsPerPatient.containsKey(sampleIdentifier))
			{
				impactsPerPatient.get(sampleIdentifier).add(impact);
			}
			else
			{
				impactsPerPatient.put(sampleIdentifier, newArrayList(impact));
			}
		}
		getImpactCountPerPatient(variantCountsPerPatient, impactsPerPatient);
	}

	/**
	 * For every patient, the number of 4 different impacts are count.
	 * 
	 * @param variantCountsPerPatient
	 *            list containing number of variants per patient
	 * @param impactsPerPatient
	 *            list containing impacts per patient
	 */
	private void getImpactCountPerPatient(Map<String, Integer> variantCountsPerPatient,
			Map<String, List<Impact>> impactsPerPatient)
	{
		Map<String, List<Integer>> impactCountsPerPatient = newHashMap();

		for (Map.Entry<String, List<Impact>> entry : impactsPerPatient.entrySet())
		{
			String patient = entry.getKey();
			List<Integer> allImpacts = getImpactCountsPerPatient(patient, entry);

			impactCountsPerPatient.put(patient, allImpacts);
		}

		printImpactCountsPerPatient(impactCountsPerPatient, variantCountsPerPatient);
	}

	/**
	 * 
	 * @param entry
	 * @param patient
	 * @param impact
	 * @return
	 */
	private List<Integer> getImpactCountsPerPatient(String patient, Entry<String, List<Impact>> entry)
	{
		List<Integer> allImpacts = newArrayList();

		int countHigh = 0;
		int countModerate = 0;
		int countLow = 0;
		int countModifier = 0;

		for (Impact impact : entry.getValue())
		{
			// count high/moderate/low/modifier per patient
			if (impact.equals(HIGH))
			{
				countHigh++;
			}
			else if (impact.equals(MODERATE))
			{
				countModerate++;
			}
			else if (impact.equals(LOW))
			{
				countLow++;
			}
			else if (impact.equals(MODIFIER))
			{
				countModifier++;
			}
			else
			{
				System.out.println("This is not a known impact: " + impact.toString());
			}
		}

		// Add impact counts to list in the order: High, Moderate, Low, Modifier
		allImpacts.add(countHigh);
		allImpacts.add(countModerate);
		allImpacts.add(countLow);
		allImpacts.add(countModifier);

		return allImpacts;
	}

	/**
	 * Prints the number of several impacts per patient.
	 * 
	 * @param impactCountsPerPatient
	 *            list containing impacts per patient
	 * @param variantCountsPerPatient
	 *            list containing number of variants per patient
	 */
	public void printImpactCountsPerPatient(Map<String, List<Integer>> impactCountsPerPatient,
			Map<String, Integer> variantCountsPerPatient)
	{
		// print header
		System.out.println("Sample id" + "\t" + "variant count" + "\t" + "high" + "\t" + "moderate" + "\t" + "low"
				+ "\t" + "modifier");

		// Print patient + variant count + counts per impact
		for (Entry<String, List<Integer>> entryImpactCount : impactCountsPerPatient.entrySet())
		{
			String patient = entryImpactCount.getKey();
			if (variantCountsPerPatient.containsKey(patient))
			{
				// output for making graph (easy to paste in excel)
				System.out.println(patient + "\t" + variantCountsPerPatient.get(patient) + "\t"
						+ entryImpactCount.getValue().get(0) + "\t" + entryImpactCount.getValue().get(1) + "\t"
						+ entryImpactCount.getValue().get(2) + "\t" + entryImpactCount.getValue().get(3));
			}
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when arguments are not correct
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
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when file does not exists or length of arguments is not right
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 1))
		{
			throw new Exception("Must supply 1 argument");
		}

		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}
	}
}
