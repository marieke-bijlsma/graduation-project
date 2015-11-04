package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.elasticsearch.common.collect.Maps;

public class GetOverviewPerPatient
{
	public void go(File vcfFile, File mvFile, File samplesFile) throws Exception
	{
		Map<String, String> samplesWithSex = Maps.newHashMap();
		Map<String, List<String>> infoPerPatient = Maps.newHashMap();

		Scanner scanSamples = new Scanner(samplesFile);
		String samplesLine = null;

		while (scanSamples.hasNextLine())
		{
			samplesLine = scanSamples.nextLine();
			String[] lineSplit = samplesLine.split("\t", -1);
			String sample = lineSplit[0];
			String sex = lineSplit[1];
			samplesWithSex.put(sample, sex);
		}
		scanSamples.close();
		
		
		//TODO multiple genes: multiple CADD/ExAC
		
		Scanner scanMv = new Scanner(mvFile);
		String mvLine = null;

		scanMv.nextLine(); // skip header
		while (scanMv.hasNextLine())
		{
			mvLine = scanMv.nextLine();
			String[] lineSplit = mvLine.split("\t", -1);

			String key = lineSplit[7];
			String impact = lineSplit[2];

			String cadd = lineSplit[23];

			String caddImpact = "";

			// impact CADD score (high, medium, low
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

			String childGT = lineSplit[5];
			String alleles[] = childGT.split("/");
			String firstAllele = alleles[0];
			String secondAllele = alleles[1];

			String exacAF = lineSplit[12];
			String exacHom = lineSplit[13];
			String exacHet = lineSplit[14];

			String ExacImpact = "";
			if (exacHom.equals("") || exacHet.equals(""))
			{
				continue;
			}

			if (firstAllele.equals(secondAllele))
			{
				if (Integer.parseInt(exacHom) == 0)
				{
					ExacImpact = "homozygous, not seen in ExAC";
				}
				else
				{
					ExacImpact = "homozygous, seen in ExAC" + " (" + exacHom + ")";
				}
			}
			else
			{
				if (Integer.parseInt(exacHet) == 0)
				{
					ExacImpact = "Heterozygous, not seen in ExAC";
				}
				else
				{
					ExacImpact = "Heterozygous, seen in ExAC" + " (" + exacHet + ")";
				}
			}

			String infoField = lineSplit[22];
			// String splitInfoField[] = infoField.split("\\|");

			// Get all info fields, for multiple genes
			String infoFields[] = infoField.split(";", -1);
			String annField = null;
			for (String info : infoFields)
			{
				if (info.startsWith("ANN="))
				{
					annField = info;
					break;
				}
			}

			// append gene symbol, effect and cDNA to sb
			StringBuffer sb = new StringBuffer();
			String[] multiAnn = annField.split(",");
			for (String oneAnn : multiAnn)
			{
				String[] annSplit = oneAnn.split("\\|", -1);
				sb.append(annSplit[3] + " (" + annSplit[1] + ", " + annSplit[9] + "), ");
			}

			sb.delete(sb.length() - 2, sb.length());

			// if key already exists -> get key and add new info (entries)
			if (infoPerPatient.containsKey(key))
			{
				infoPerPatient.get(key).add(
						impact + "^" + cadd + "^" + caddImpact + "^" + exacAF + "^" + exacHom + "^" + exacHet + "^"
								+ ExacImpact);
			}
			else
			{
				List<String> entries = new ArrayList<String>();
				entries.add(impact + "^" + cadd + "^" + caddImpact + "^" + exacAF + "^" + exacHom + "^" + exacHet + "^"
						+ ExacImpact);
				infoPerPatient.put(key, entries);

				// if unique key in samples -> print patient + info
				if (samplesWithSex.containsKey(key))
				{
					for (String info : entries)
					{
						String[] oneLineInfo = info.split("\\^");

						System.out.println("Patient: " + key + " (" + samplesWithSex.get(key) + ")" + "\n"
								+ "Most likely candidates: " + sb.toString() + "\n" + "Inheritance: " + "de novo"
								+ "\n" + "Evidence: " + oneLineInfo[0].toLowerCase() + " " + "impact" + ", "
								+ oneLineInfo[2] + " CADD score" + " (" + oneLineInfo[1] + ")" + ", " + oneLineInfo[6]
								+ ", " + "Allele frequency: " + oneLineInfo[3] + "\n");
					}
				}
				else
				{
					System.out.println("sample ID: " + key + " not in samples");
				}
			}

		}
		scanMv.close();
	}

	public static void main(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		File mvFile = new File(args[1]);
		if (!mvFile.isFile())
		{
			throw new Exception("Mendelian violations file does not exist or directory: " + mvFile.getAbsolutePath());
		}

		File samplesFile = new File(args[2]);
		if (!samplesFile.isFile())
		{
			throw new Exception("Samples file does not exist or directory: " + samplesFile.getAbsolutePath());
		}

		GetOverviewPerPatient gop = new GetOverviewPerPatient();
		gop.go(vcfFile, mvFile, samplesFile);

	}

}
