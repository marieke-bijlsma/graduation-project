package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.getAnnotationField;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.util.Map;

/**
 * This class creates and prints a candidate variant overview per patient.
 * 
 * @author mbijlsma
 *
 */
public class GetOverviewPerPatient
{
	private File samplesFile;
	private File vcfFile;
	private File mvFile;

	/**
	 * Reads and parses a samples file.
	 * 
	 * @return samplesWithSex a map containing the sample ID and the sex of the patient
	 * @throws Exception
	 *             when samplesFile is incorrect or cannot be parsed
	 */
	private Map<String, String> readSamplesFile() throws Exception
	{
		Map<String, String> samplesWithSex = newHashMap();
		for (String record : readFile(samplesFile, false))
		{
			String[] recordSplit = record.split("\t", -1);
			samplesWithSex.put(recordSplit[0], recordSplit[1]);
		}
		return samplesWithSex;
	}

	/**
	 * Reads and parses the Mendelian violation file and prints an overview of candidate variants per patient.
	 * 
	 * @throws Exception
	 *             when mvFile is incorrect or cannot be parsed
	 */
	private void readMvFile() throws Exception
	{
		Map<String, String> samplesWithSex = readSamplesFile();
		for (String record : readFile(mvFile, true))
		{
			String[] recordSplit = record.split("\t", -1);

			String sampleID = recordSplit[7];
			String impact = recordSplit[2];
			String cadd = recordSplit[23];

			String caddImpact = calculateCADD(cadd);

			String exacAF = recordSplit[12];
			String exacHom = recordSplit[13];
			String exacHet = recordSplit[14];

			if (!(exacHom.equals("") || exacHet.equals("")))
			{
				String exacImpact = getExacImpact(recordSplit);
				String[] multiAnnotationField = getAnnotationField(record, 22).split(",");

				StringBuilder stringBuilder = buildAnnotationString(multiAnnotationField);

				// if sample IDs match
				if (samplesWithSex.containsKey(sampleID))
				{
					System.out.println("Patient: " + sampleID + " (" + samplesWithSex.get(sampleID) + ")" + "\n"
							+ "Most likely candidates: " + stringBuilder.substring(0, stringBuilder.length() - 2)
							+ "\n" + "Inheritance: " + "de novo" + "\n" + "Evidence: " + impact + " " + "impact" + ", "
							+ caddImpact + " CADD score" + " (" + cadd + ")" + ", " + exacImpact + ", "
							+ "Allele frequency: " + exacAF + "\n");
				}
				else
				{
					System.out.println("sample ID: " + sampleID + " not in samples");
				}
			}
		}
	}

	/**
	 * Gets the gene symbol, effect and cDNA from each annotation field and adds it to stringBuilder, which will be
	 * returned.
	 * 
	 * @param multiAnnotationField
	 *            string array containing multiple annotation fields
	 * @return {@link StringBuilder} containing for each annotation field the gene symbol, effect and cDNA
	 */
	private StringBuilder buildAnnotationString(String[] multiAnnotationField)
	{
		StringBuilder stringBuilder = new StringBuilder();

		for (String oneAnnotationField : multiAnnotationField)
		{
			String[] annSplit = oneAnnotationField.split("\\|", -1);
			stringBuilder.append(annSplit[3] + " (" + annSplit[1] + ", " + annSplit[9] + "), ");
		}
		return stringBuilder;
	}

	/**
	 * Calculates for each variant how many times it is seen in ExAC and returns this impact.
	 * 
	 * @param recordSplit
	 *            string array containing the columns of the Mendelian violations file
	 * @return exacImpact how many times a variant is seen in ExAC
	 */
	private String getExacImpact(String[] recordSplit)
	{
		String exacHom = recordSplit[13];
		String exacHet = recordSplit[14];
		String childGT = recordSplit[5];
		String alleles[] = childGT.split("/");
		String firstAllele = alleles[0];
		String secondAllele = alleles[1];

		String exacImpact = "";
		if (firstAllele.equals(secondAllele))
		{
			if (Integer.parseInt(exacHom) == 0)
			{
				exacImpact = "homozygous, not seen in ExAC";
			}
			else
			{
				exacImpact = "homozygous, seen in ExAC" + " (" + exacHom + ")";
			}
		}
		else
		{
			if (Integer.parseInt(exacHet) == 0)
			{
				exacImpact = "Heterozygous, not seen in ExAC";
			}
			else
			{
				exacImpact = "Heterozygous, seen in ExAC" + " (" + exacHet + ")";
			}
		}
		return exacImpact;
	}

	/**
	 * Calculates for each variant the impact of the CADD score (high, medium, or low) and returns this impact.
	 * 
	 * @param cadd
	 *            the CADD score
	 * @return caddImpact the impact of the CADD score (high, medium, or low)
	 */
	private String calculateCADD(String cadd)
	{
		String caddImpact = null;

		if (Double.parseDouble(cadd) >= 20)
		{
			caddImpact = "high";
		}
		else if (Double.parseDouble(cadd) > 10 && Double.parseDouble(cadd) < 20)
		{
			caddImpact = "medium";
		}
		else if (Double.parseDouble(cadd) <= 10)
		{
			caddImpact = "low";
		}

		return caddImpact;
	}

	/**
	 * The main method, invokes parseCommandLineArgs() and readMvFile().
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when mvFile does not exist or is incorrect
	 */
	public static void main(String[] args) throws Exception
	{
		GetOverviewPerPatient getOverviewPerPatient = new GetOverviewPerPatient();
		getOverviewPerPatient.parseCommandLineArgs(args);

		getOverviewPerPatient.readMvFile();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when length of arguments is not 3, or if one of the files does not exist or is incorrect
	 */
	private void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}

		mvFile = new File(args[1]);
		if (!mvFile.isFile())
		{
			throw new Exception("Mendelian violations file does not exist or is not a directory: "
					+ mvFile.getAbsolutePath());
		}

		samplesFile = new File(args[2]);
		if (!samplesFile.isFile())
		{
			throw new Exception("Samples file does not exist or is not a directory: " + samplesFile.getAbsolutePath());
		}
	}

}
