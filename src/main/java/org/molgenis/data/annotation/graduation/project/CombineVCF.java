package org.molgenis.data.annotation.graduation.project;

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

@Component
public class CombineVCF
{

	public void go(File vcfFile, File dataFile, File outputFile) throws Exception
	{
		@SuppressWarnings("resource")
		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator exacAnnotator = annotators.get("exac");
		//RepositoryAnnotator caddAnnotator = annotators.get("cadd");
		//RepositoryAnnotator gonlAnnotator = annotators.get("gonl");
		//RepositoryAnnotator thousandGenomesAnnotator = annotators.get("thousandGenomes");
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(dataFile.getAbsolutePath());
		//caddAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(dataFile.getAbsolutePath());
		//gonlAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(dataFile.getAbsolutePath());
		//thousandGenomesAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(dataFile.getAbsolutePath());
		Iterator<Entity> vcfWithExac = exacAnnotator.annotate(vcf);
		//Iterator<Entity> vcfWithCadd = caddAnnotator.annotate(vcf);
		//Iterator<Entity> vcfWithGonl = gonlAnnotator.annotate(vcf);
		//Iterator<Entity> vcfWithThousandGenomes = thousandGenomesAnnotator.annotate(vcf);

		while (vcfWithExac.hasNext())
		{
			Entity record = vcfWithExac.next();

			String exac_af_STR = record.get("EXAC_AF") == null ? null : record.get("EXAC_AF").toString();
			String exac_ac_hom_STR = record.get("EXAC_AC_HOM") == null ? "" : record.get("EXAC_AC_HOM").toString();
			String exac_ac_het_STR = record.get("EXAC_AC_HET") == null ? "" : record.get("EXAC_AC_HET").toString();
			
			String vcfEntry = VcfUtils.convertToVCF(record, false);
			
			pw.println(vcfEntry + "\t" + exac_af_STR + "\t" + exac_ac_hom_STR + "\t" + exac_ac_het_STR);

		}
	}

	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);
		CombineVCF main = ctx.getBean(CombineVCF.class);
		main.run(args);
		ctx.close();
	}

	public void run(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		File dataFile = new File(args[1]);
		if (!dataFile.isFile())
		{
			throw new Exception("Data file does not exist or directory: " + dataFile.getAbsolutePath());
		}
		
		File outputFile = new File(args[2]);
		if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or directory: " + outputFile.getAbsolutePath());
		}

		CombineVCF cv = new CombineVCF();
		cv.go(vcfFile, dataFile, outputFile);

	}
}
