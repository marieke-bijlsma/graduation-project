package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.getAnnotationField;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.util.Map;

public class GetOverviewPerPatient
{
	private File samplesFile;
	private File vcfFile;
	private File mvFile;

	private Map<String, String> readSamplesFile() throws Exception
	{
		Map<String, String> samplesWithSex = newHashMap();
		for (String record : readFile(samplesFile, false))
		{
			String[] recordSplit = record.split("\t", -1);
			samplesWithSex.put(recordSplit[0], recordSplit[1]); // sample and sex
		}
		return samplesWithSex;
	}

	private void readMvFile() throws Exception
	{
		Map<String, String> samplesWithSex = readSamplesFile();
		for (String record : readFile(mvFile, true))
		{
			String[] recordSplit = record.split("\t", -1);

			String key = recordSplit[7];
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

				// append gene symbol, effect and cDNA to stringBuilder
				StringBuilder stringBuilder = buildAnnotationString(multiAnnotationField);

				if (samplesWithSex.containsKey(key))
				{
					System.out.println("Patient: " + key + " (" + samplesWithSex.get(key) + ")" + "\n"
							+ "Most likely candidates: " + stringBuilder.substring(0, stringBuilder.length() - 2)
							+ "\n" + "Inheritance: " + "de novo" + "\n" + "Evidence: " + impact + " " + "impact" + ", "
							+ caddImpact + " CADD score" + " (" + cadd + ")" + ", " + exacImpact + ", "
							+ "Allele frequency: " + exacAF + "\n");
				}
				else
				{
					System.out.println("sample ID: " + key + " not in samples");
				}
			}
		}
	}

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

	private String calculateCADD(String cadd)
	{
		String caddImpact = null;

		// impact CADD score (high, medium, low)
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

	public static void main(String[] args) throws Exception
	{
		GetOverviewPerPatient getOverviewPerPatient = new GetOverviewPerPatient();
		getOverviewPerPatient.parseCommandLineArgs(args);

		getOverviewPerPatient.readMvFile();
	}

	private void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		mvFile = new File(args[1]);
		if (!mvFile.isFile())
		{
			throw new Exception("Mendelian violations file does not exist or directory: " + mvFile.getAbsolutePath());
		}

		samplesFile = new File(args[2]);
		if (!samplesFile.isFile())
		{
			throw new Exception("Samples file does not exist or directory: " + samplesFile.getAbsolutePath());
		}
	}

}
