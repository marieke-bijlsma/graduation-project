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
 * This class counts the number of several impacts per patient. The output is printed in the form of a matrix for easy
 * pasting in Excel (for calculations and coloring). Also, the output can be printed with context (for clinicians).
 * 
 * @author mbijlsma
 */
public class GetVariantsPerPatient
{
	private File vcfFile;

	/**
	 * Reads and parses a VCF file and adds the variant count per patient and the impact count per patient to a map.
	 * 
	 * @throws Exception
	 *             when vcfFile is incorrect or cannot be parsed
	 */
	public void readVcf() throws Exception
	{
		Map<String, Integer> variantCountsPerPatient = newHashMap();
		Map<String, List<Impact>> impactsPerPatient = newHashMap();

		for (String record : readFile(vcfFile, true))
		{
			String[] recordSplit = record.split("\t", -1);
			String sampleIdentifier = recordSplit[7];
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
	 * Gets the number of the four different impacts for each patient.
	 * 
	 * @param variantCountsPerPatient
	 *            list containing number of variants per patient
	 * @param impactsPerPatient
	 *            list containing number of the different impacts per patient
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
	 * Calculates the number of the four different impacts per patient and returns the result.
	 * 
	 * @param entry
	 *            {@link Entry} containing sample IDs of patients with {@link Impact}s
	 * @param patient
	 *            the patient we are currently looking at
	 * @return allImpacts a list containing all {@link Impact} counts
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

		// Add impact counts to list in the right order: High, Moderate, Low, Modifier
		allImpacts.add(countHigh);
		allImpacts.add(countModerate);
		allImpacts.add(countLow);
		allImpacts.add(countModifier);

		return allImpacts;
	}

	/**
	 * Prints the number of the four different impacts per patient.
	 * 
	 * @param impactCountsPerPatient
	 *            list containing {@link Impact}s per patient
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
	 * The main method, invokes parseCommandLineArgs() and readVcf().
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when VCF file is incorrect or does not exist
	 */
	public static void main(String[] args) throws Exception
	{
		GetVariantsPerPatient getVariantsPerPatient = new GetVariantsPerPatient();
		getVariantsPerPatient.parseCommandLineArgs(args);
		getVariantsPerPatient.readVcf();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when length of arguments is not 1, or the file does not exist or is incorrect
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
