package org.molgenis.data.annotation.graduation.utils;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Class to register and use annotators
 * 
 * @author mbijlsma
 *
 */
public class AnnotatorUtils
{
	private static final String THOUSAND_GENOMES_ANNOTATOR_NAME = "thousandGenomes";
	private static final String GONL_ANNOTATOR_NAME = "gonl";
	private static final String CADD_ANNOTATOR_NAME = "cadd";
	private static final String EXAC_ANNOTATOR_NAME = "exac";

	private static final File exacFile = new File("/Users/molgenis/Downloads/ExAC.r0.3.sites.vep.vcf.gz");
	private static final File caddFile = new File("");
	private static final File gonlFile = new File("");
	private static final File thousandGenomesFile = new File("");

	private static RepositoryAnnotator registerAnnotator(String annotator)
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator repositoryAnnotator = annotators.get(annotator);

		return repositoryAnnotator;
	}

	public static Iterator<Entity> annotateWithExac(Iterator<Entity> vcfRepositoryIterator)
	{
		RepositoryAnnotator exacAnnotator = registerAnnotator(EXAC_ANNOTATOR_NAME);
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(exacFile.getAbsolutePath());
		return exacAnnotator.annotate(vcfRepositoryIterator);
	}

	public static Iterator<Entity> annotateWithExac(VcfRepository vcfRepository)
	{
		RepositoryAnnotator exacAnnotator = registerAnnotator(EXAC_ANNOTATOR_NAME);
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(exacFile.getAbsolutePath());
		return exacAnnotator.annotate(vcfRepository);
	}

	public static Iterator<Entity> annotateWithCadd(Iterator<Entity> vcfRepositoryIterator)
	{
		RepositoryAnnotator caddAnnotator = registerAnnotator(CADD_ANNOTATOR_NAME);
		caddAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(caddFile.getAbsolutePath());
		return caddAnnotator.annotate(vcfRepositoryIterator);
	}

	public static Iterator<Entity> annotateWithCadd(VcfRepository vcfRepository)
	{
		RepositoryAnnotator caddAnnotator = registerAnnotator(CADD_ANNOTATOR_NAME);
		caddAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(caddFile.getAbsolutePath());
		return caddAnnotator.annotate(vcfRepository);
	}

	public static Iterator<Entity> annotateWithGoNl(Iterator<Entity> vcfRepositoryIterator)
	{
		RepositoryAnnotator gonlAnnotator = registerAnnotator(GONL_ANNOTATOR_NAME);
		gonlAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(gonlFile.getAbsolutePath());
		return gonlAnnotator.annotate(vcfRepositoryIterator);
	}

	public static Iterator<Entity> annotateWithGoNl(VcfRepository vcfRepository)
	{
		RepositoryAnnotator gonlAnnotator = registerAnnotator(GONL_ANNOTATOR_NAME);
		gonlAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(gonlFile.getAbsolutePath());
		return gonlAnnotator.annotate(vcfRepository);
	}

	public static Iterator<Entity> annotateWithThousandGenomes(Iterator<Entity> vcfRepositoryIterator)
	{
		RepositoryAnnotator thousandGenomesAnnotator = registerAnnotator(THOUSAND_GENOMES_ANNOTATOR_NAME);
		thousandGenomesAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(
				thousandGenomesFile.getAbsolutePath());
		return thousandGenomesAnnotator.annotate(vcfRepositoryIterator);
	}

	public static Iterator<Entity> annotateWithThousandGenomes(VcfRepository vcfRepository)
	{
		RepositoryAnnotator thousandGenomesAnnotator = registerAnnotator(THOUSAND_GENOMES_ANNOTATOR_NAME);
		thousandGenomesAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(
				thousandGenomesFile.getAbsolutePath());
		return thousandGenomesAnnotator.annotate(vcfRepository);
	}

	public static AnnotationConfigApplicationContext registerCommandLineAnnotator()
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"org.molgenis.data.annotation");
		context.register(CommandLineAnnotatorConfig.class);

		return context;
	}
}
