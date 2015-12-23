package org.molgenis.data.annotation.graduation.analysis;

import static org.molgenis.data.annotation.graduation.utils.AnnotatorUtils.annotateWithCadd;
import static org.molgenis.data.annotation.graduation.utils.AnnotatorUtils.annotateWithExac;
import static org.molgenis.data.annotation.graduation.utils.AnnotatorUtils.annotateWithGoNl;
import static org.molgenis.data.annotation.graduation.utils.AnnotatorUtils.annotateWithThousandGenomes;
import static org.molgenis.data.vcf.utils.VcfUtils.convertToVCF;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.graduation.utils.AnnotatorUtils;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class combines a VCF file with a datatset (ExAC, GoNL, Thousand Genomes, or CADD) and writes the output to a new
 * file.
 * 
 * @author mbijlsma
 */
@Component
public class CombineVCF
{
	private File vcfFile;
	private File outputFile;

	/**
	 * Reads a VCF file and the associated data file, parses it, and writes it to a new file.
	 * 
	 * @param vcfFile
	 *            the VCF file to be parsed
	 * @param dataFile
	 *            the data file to be parsed
	 * @param outputFile
	 *            the file where the output will be written to
	 * @throws Exception
	 *             when output file is not correct
	 */
	public void annotateVcfFile() throws Exception
	{
		@SuppressWarnings("resource")
		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

		VcfRepository vcfRepository = new VcfRepository(vcfFile, "vcf");

		Iterator<Entity> vcfRepositoryWithExac = annotateWithExac(vcfRepository);
		Iterator<Entity> vcfRepositoryWithExacAndCadd = annotateWithCadd(vcfRepositoryWithExac);
		Iterator<Entity> vcfRepositoryWithExacAndCaddAndGoNl = annotateWithGoNl(vcfRepositoryWithExacAndCadd);
		Iterator<Entity> vcfRepositoryWithExacAndCaddAndGoNlAndThousandGenomes = annotateWithThousandGenomes(vcfRepositoryWithExacAndCaddAndGoNl);

		while (vcfRepositoryWithExacAndCaddAndGoNlAndThousandGenomes.hasNext())
		{
			Entity record = vcfRepositoryWithExac.next();

			String exac_af_STR = record.get("EXAC_AF") == null ? null : record.get("EXAC_AF").toString();
			String exac_ac_hom_STR = record.get("EXAC_AC_HOM") == null ? "" : record.get("EXAC_AC_HOM").toString();
			String exac_ac_het_STR = record.get("EXAC_AC_HET") == null ? "" : record.get("EXAC_AC_HET").toString();
			String gonl_af_STR = record.get("GoNL_AF") == null ? null : record.get("GoNL_AF").toString();
			String thousandG_af_STR = record.get("Thousand_Genomes_AF") == null ? null : record.get(
					"Thousand_Genomes_AF").toString();
			String cadd_score = record.get("CADD_SCALED") == null ? null : record.get("CADD_SCALED").toString();

			String vcfEntry = convertToVCF(record, false);

			pw.println(vcfEntry + "\t" + exac_af_STR + "\t" + exac_ac_hom_STR + "\t" + exac_ac_het_STR);
			pw.println(vcfEntry + "\t" + gonl_af_STR);
			pw.println(vcfEntry + "\t" + thousandG_af_STR);
			pw.println(vcfEntry + "\t" + cadd_score);
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when bean can't be created or file does not exists
	 */
	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext context = AnnotatorUtils.registerCommandLineAnnotator();
		CombineVCF combineVCF = context.getBean(CombineVCF.class);

		combineVCF.parseCommandLineArgs(args);
		combineVCF.annotateVcfFile();

		context.close();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when the length of the arguments is not 3, or if the VCF or data file does not exists.
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
			throw new Exception("Input VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}

		outputFile = new File(args[1]);
		if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or is not a directory: " + outputFile.getAbsolutePath());
		}
	}
}
