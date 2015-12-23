package org.molgenis.data.annotation.graduation.analysis;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
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
	File vcfFile;
	File dataFile;
	File outputFile;
	
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
	public void go() throws Exception
	{
		@SuppressWarnings("resource")
		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator exacAnnotator = annotators.get("exac");
		// RepositoryAnnotator caddAnnotator = annotators.get("cadd");
		// RepositoryAnnotator gonlAnnotator = annotators.get("gonl");
		// RepositoryAnnotator thousandGenomesAnnotator = annotators.get("thousandGenomes");
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(dataFile.getAbsolutePath());
		// caddAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(dataFile.getAbsolutePath());
		// gonlAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(dataFile.getAbsolutePath());
		// thousandGenomesAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(dataFile.getAbsolutePath());
		Iterator<Entity> vcfWithExac = exacAnnotator.annotate(vcf);
		// Iterator<Entity> vcfWithCadd = caddAnnotator.annotate(vcf);
		// Iterator<Entity> vcfWithGonl = gonlAnnotator.annotate(vcf);
		// Iterator<Entity> vcfWithThousandGenomes = thousandGenomesAnnotator.annotate(vcf);

		while (vcfWithExac.hasNext())
		{
			Entity record = vcfWithExac.next();

			String exac_af_STR = record.get("EXAC_AF") == null ? null : record.get("EXAC_AF").toString();
			String exac_ac_hom_STR = record.get("EXAC_AC_HOM") == null ? "" : record.get("EXAC_AC_HOM").toString();
			String exac_ac_het_STR = record.get("EXAC_AC_HET") == null ? "" : record.get("EXAC_AC_HET").toString();
			// String gonl_af_STR = record.get("GoNL_AF") == null ? null : record.get("GoNL_AF").toString();
			// String thousandG_af_STR = record.get("Thousand_Genomes_AF") == null ? null : record.get(
			// "Thousand_Genomes_AF").toString();
			// String cadd_score = record.get("CADD_SCALED") == null ? null : record.get("CADD_SCALED").toString();

			String vcfEntry = VcfUtils.convertToVCF(record, false);

			pw.println(vcfEntry + "\t" + exac_af_STR + "\t" + exac_ac_hom_STR + "\t" + exac_ac_het_STR);
			// pw.println(vcfEntry + "\t" + gonl_af_STR
			// pw.println(vcfEntry + "\t" + thousandG_af_STR
			// pw.println(vcfEntry + "\t" + cadd_score
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
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);
		CombineVCF main = ctx.getBean(CombineVCF.class);
		main.parseCommandLineArgs(args);
		CombineVCF combineVCF = new CombineVCF();
		combineVCF.go();
		ctx.close();
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
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}

		File dataFile = new File(args[1]);
		if (!dataFile.isFile())
		{
			throw new Exception("Data file does not exist or is not a directory: " + dataFile.getAbsolutePath());
		}

		File outputFile = new File(args[2]);
		if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or is not a directory: " + outputFile.getAbsolutePath());
		}
	}
}
