package org.molgenis.data.annotation.graduation.analysis;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class filters a VCF file to receive a smaller VCF file which can be analyzed further.
 * 
 * @author mbijlsma
 */
@Component
public class FilterVCF
{
	
	File vcfFile;
	File outputFile;
	
	/**
	 * Filters the VCF file according to soe specified thresholds.
	 * 
	 * @throws Exception
	 *             when printwriter or itarator cannot perfom correctly
	 */
	public void readAndFilterVcf() throws Exception
	{
		@SuppressWarnings("resource")
		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		int count = 0;

		// loop through VCF
		while (vcf.hasNext())
		{
			count++;
			Entity record = vcf.next();

			// print number of lines scanned
			if (count % 1000 == 0)
			{
				System.out.println("Scanned over " + count + " lines in VCF...");
			}

			// get filter column and filter all that are not PASS
			String filter = record.getString("FILTER");

			if (!(filter.equals("PASS")))
			{
				continue;
			}

			// get alt for iterating over alt alleles and get exac af, gonl af and 1000G af
			String altStr = record.getString("ALT");
			String exac_af_STR = record.get("EXAC_AF") == null ? null : record.get("EXAC_AF").toString();
			String gonl_af_STR = record.get("GoNL_AF") == null ? null : record.get("GoNL_AF").toString();
			String thousandG_af_STR = record.get("Thousand_Genomes_AF") == null ? null : record.get(
					"Thousand_Genomes_AF").toString();

			String[] multiAnn = record.getString("ANN").split(",");
			String[] altsplit = altStr.split(",", -1);

			// if multiple alternate alleles, multiple AFs -> split AFs and analyze all
			// ExAC is separated with comma, GoNL with pipe and 1000G aslo with comma

			// ExAC get all alternate alleles
			String[] exac_af_split = new String[altsplit.length];
			if (exac_af_STR != null)
			{
				exac_af_split = exac_af_STR.split(",", -1);
			}

			// GoNL get all alternate alleles
			String[] gonl_af_split = new String[altsplit.length];
			if (gonl_af_STR != null)
			{
				gonl_af_split = gonl_af_STR.split("\\|", -1);
			}

			// 1000G get all alternate alleles
			String[] thousandG_af_split = new String[altsplit.length];
			if (thousandG_af_STR != null)
			{
				thousandG_af_split = thousandG_af_STR.split(",", -1);
			}

			// iterate over alternate alleles
			for (int i = 0; i < altsplit.length; i++)
			{
				// ExAC (AF must be lower than 0.05)
				if (exac_af_STR != null && !exac_af_split[i].equals("."))
				{
					Double exac_af = Double.parseDouble(exac_af_split[i]);
					if (exac_af > 0.05)
					{
						continue;
					}
				}

				// GoNL (AF must be lower than 0.05)
				if (gonl_af_STR != null && !gonl_af_split[i].equals("."))
				{
					Double gonl_af = Double.parseDouble(gonl_af_split[i]);
					if (gonl_af > 0.05)
					{
						continue;
					}
				}

				// 1000G (AF must be lower than 0.05)
				if (thousandG_af_STR != null && !thousandG_af_split[i].equals("."))
				{
					Double thousandG_af = Double.parseDouble(thousandG_af_split[i]);
					if (thousandG_af > 0.05)
					{
						continue;
					}
				}

				// Get ANN field (or multiple ANN fields) and split it to get impact and filter LOW and MODIFIER
				// Get only 2 ANN fields (for every allele), could be more than 2 ANN fields!!!
				String ann = multiAnn[i];
				String[] annSplit = ann.split("\\|", -1);
				String impact = annSplit[2];
				if (impact.equals("MODIFIER") || impact.equals("LOW"))
				{
					continue;
				}

				// convert to VCF entry and print to new file (true, all genotypes must be printed too)
				String vcfEntry = VcfUtils.convertToVCF(record, true);

				pw.println(vcfEntry);
				pw.flush();

				break;
			}
		}
	}

	/**
	 * The main method. Invokes run().
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when arguments are incorrect
	 */
	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);
		FilterVCF filterVCF = ctx.getBean(FilterVCF.class);
		filterVCF.parseCommandLineArgs(args);
		filterVCF.readAndFilterVcf();
		ctx.close();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when file does not exists or length of arguments is incorrect
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		File outputFile = new File(args[1]);
		if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or directory: " + outputFile.getAbsolutePath());
		}		
	}
}
