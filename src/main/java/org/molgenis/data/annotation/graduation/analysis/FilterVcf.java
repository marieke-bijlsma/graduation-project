package org.molgenis.data.annotation.graduation.analysis;

import static java.lang.Double.parseDouble;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.LOW;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.MODIFIER;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.valueOf;
import static org.molgenis.data.vcf.utils.VcfUtils.convertToVCF;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;
import org.molgenis.data.vcf.VcfRepository;

/**
 * This class filters a VCF file to receive a smaller VCF file which can be analyzed further.
 * 
 * @author mbijlsma
 */
public class FilterVcf
{
	private File vcfFile;
	private File outputFile;

	/**
	 * Filters the VCF file according to soe specified thresholds.
	 * 
	 * @throws Exception
	 *             when printwriter or itarator cannot perfom correctly
	 */
	public void readAndFilterVcf() throws Exception
	{
		FileWriter fileWriter = new FileWriter(outputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		VcfRepository vcfRepository = new VcfRepository(vcfFile, "vcf");

		int count = 0;

		for (Entity record : vcfRepository)
		{
			count++;

			// print number of lines scanned
			if (count % 1000 == 0)
			{
				System.out.println("Scanned over " + count + " lines in VCF...");
			}

			// get filter column and filter all that are not PASS
			if (record.getString(VcfRepository.FILTER).equals("PASS"))
			{
				// if multiple alternate alleles, multiple AFs -> split AFs and analyze all
				// ExAC is separated with comma, GoNL with pipe and 1000G aslo with comma

				// get alt for iterating over alt alleles and get exac af, gonl af and 1000G af
				String alternateAlleles = record.getString(VcfRepository.ALT);
				String[] exacAlleleFrequencies = record.get("EXAC_AF") == null ? null : record.getString("EXAC_AF")
						.split(",", -1);
				String[] gonlAlleleFrequencies = record.get("GoNL_AF") == null ? null : record.getString("GoNL_AF")
						.split("\\|", -1);
				String[] thousandGenomesAlleleFrequencies = record.get("Thousand_Genomes_AF") == null ? null : record
						.getString("Thousand_Genomes_AF").split(",", -1);

				String[] multiAnnotationField = record.getString("ANN").split(",");

				// iterate over alternate alleles
				for (int i = 0; i < alternateAlleles.split(",", -1).length; i++)
				{
					if (isFrequencyHigherThanThreshold(exacAlleleFrequencies, i)
							|| isFrequencyHigherThanThreshold(gonlAlleleFrequencies, i)
							|| isFrequencyHigherThanThreshold(thousandGenomesAlleleFrequencies, i))
					{
						continue;
					}

					// Get ANN field (or multiple ANN fields) and split it to get impact and filter LOW and MODIFIER
					// Get only 2 ANN fields (for every allele), could be more than 2 ANN fields!!!
					String[] annSplit = multiAnnotationField[i].split("\\|", -1);
					Impact impact = valueOf(annSplit[2]);
					if (impact.equals(MODIFIER) || impact.equals(LOW))
					{
						continue;
					}

					// convert to VCF entry and print to new file (true, all genotypes must be printed too)
					bufferedWriter.append(convertToVCF(record, true));

					// If we have a good variant for one of the alternate alleles, go to the next line in the VCF
					break;
				}
			}
		}

		bufferedWriter.close();
		vcfRepository.close();
	}

	/**
	 * Calculates if the allele frequencies of one of the annotators is below the threshold and return the result in the
	 * form of a {@link Boolean}.
	 * 
	 * @param annotationFrequency
	 *            the allele frequency of a specific annotator
	 * @param index
	 *            the index of the allele we are looking at
	 * @return {@link Boolean} true if frequency is above threshold, otherwise false
	 */
	private boolean isFrequencyHigherThanThreshold(String[] annotationFrequency, int index)
	{
		double threshold = 0.05;
		if (annotationFrequency != null && !annotationFrequency[index].equals("."))
		{
			if (parseDouble(annotationFrequency[index]) > threshold)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * The main method, invokes parseCommandLineArgs() and readAndFilterVcf().
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when one of the files is incorrect
	 */
	public static void main(String[] args) throws Exception
	{
		FilterVcf filterVcf = new FilterVcf();

		filterVcf.parseCommandLineArgs(args);
		filterVcf.readAndFilterVcf();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when length of arguments is not 2, or if one of the files does not exists or is incorrect
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}

		outputFile = new File(args[1]);
		if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or is not a directory: " + outputFile.getAbsolutePath());
		}
	}
}
