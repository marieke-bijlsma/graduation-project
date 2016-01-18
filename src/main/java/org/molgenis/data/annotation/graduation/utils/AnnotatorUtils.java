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
 * This class provides utilities to register and use annotators.
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

	/**
	 * Provides configuration for an application and returns interface for annotators.
	 * 
	 * @param annotator
	 *            the annotator to be used
	 * @return repositoryAnnotator {@link RepositoryAnnotator} interface for annotators
	 */
	private static RepositoryAnnotator registerAnnotator(String annotator)
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator repositoryAnnotator = annotators.get(annotator);

		return repositoryAnnotator;
	}

	/**
	 * Creates interface for annotators and annotates a VCF file with ExAC, with {@link Iterator}.
	 * 
	 * @param vcfRepositoryIterator
	 *            {@link Iterator} over a collection
	 * @return exacAnnotator.annotate(vcfRepositoryIterator) the annotated VCF file
	 */
	public static Iterator<Entity> annotateWithExac(Iterator<Entity> vcfRepositoryIterator)
	{
		RepositoryAnnotator exacAnnotator = registerAnnotator(EXAC_ANNOTATOR_NAME);
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(exacFile.getAbsolutePath());
		return exacAnnotator.annotate(vcfRepositoryIterator);
	}

	/**
	 * Creates interface for annotators and annotates a VCF file with ExAC, without {@link Iterator}.
	 * 
	 * @param vcfRepository
	 *            {@link VcfRepository} repository implementation for VCF file
	 * @return exacAnnotator.annotate(vcfRepositoryIterator) the annotated VCF file
	 */
	public static Iterator<Entity> annotateWithExac(VcfRepository vcfRepository)
	{
		RepositoryAnnotator exacAnnotator = registerAnnotator(EXAC_ANNOTATOR_NAME);
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(exacFile.getAbsolutePath());
		return exacAnnotator.annotate(vcfRepository);
	}

	/**
	 * Creates interface for annotators and annotates a VCF file with CADD, with {@link Iterator}.
	 * 
	 * @param vcfRepositoryIterator
	 *            {@link Iterator} over a collection
	 * @return caddAnnotator.annotate(vcfRepositoryIterator) the annotated VCF file
	 */
	public static Iterator<Entity> annotateWithCadd(Iterator<Entity> vcfRepositoryIterator)
	{
		RepositoryAnnotator caddAnnotator = registerAnnotator(CADD_ANNOTATOR_NAME);
		caddAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(caddFile.getAbsolutePath());
		return caddAnnotator.annotate(vcfRepositoryIterator);
	}

	/**
	 * Creates interface for annotators and annotates a VCF file with CADD, without {@link Iterator}.
	 * 
	 * @param vcfRepository
	 *            {@link VcfRepository} repository implementation for VCF file
	 * @return caddAnnotator.annotate(vcfRepositoryIterator) the annotated VCF file
	 */
	public static Iterator<Entity> annotateWithCadd(VcfRepository vcfRepository)
	{
		RepositoryAnnotator caddAnnotator = registerAnnotator(CADD_ANNOTATOR_NAME);
		caddAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(caddFile.getAbsolutePath());
		return caddAnnotator.annotate(vcfRepository);
	}

	/**
	 * Creates interface for annotators and annotates a VCF file with GoNL, with {@link Iterator}.
	 * 
	 * @param vcfRepositoryIterator
	 *            {@link Iterator} over a collection
	 * @return gonlAnnotator.annotate(vcfRepositoryIterator) the annotated VCF file
	 */
	public static Iterator<Entity> annotateWithGoNl(Iterator<Entity> vcfRepositoryIterator)
	{
		RepositoryAnnotator gonlAnnotator = registerAnnotator(GONL_ANNOTATOR_NAME);
		gonlAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(gonlFile.getAbsolutePath());
		return gonlAnnotator.annotate(vcfRepositoryIterator);
	}

	/**
	 * Creates interface for annotators and annotates a VCF file with GoNL, without {@link Iterator}.
	 * 
	 * @param vcfRepository
	 *            {@link VcfRepository} repository implementation for VCF file
	 * @return gonlAnnotator.annotate(vcfRepositoryIterator) the annotated VCF file
	 */
	public static Iterator<Entity> annotateWithGoNl(VcfRepository vcfRepository)
	{
		RepositoryAnnotator gonlAnnotator = registerAnnotator(GONL_ANNOTATOR_NAME);
		gonlAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(gonlFile.getAbsolutePath());
		return gonlAnnotator.annotate(vcfRepository);
	}

	/**
	 * Creates interface for annotators and annotates a VCF file with 1000G, with {@link Iterator}.
	 * 
	 * @param vcfRepositoryIterator
	 *            {@link Iterator} over a collection
	 * @return thousandGenomesAnnotator.annotate(vcfRepositoryIterator) the annotated VCF file
	 */
	public static Iterator<Entity> annotateWithThousandGenomes(Iterator<Entity> vcfRepositoryIterator)
	{
		RepositoryAnnotator thousandGenomesAnnotator = registerAnnotator(THOUSAND_GENOMES_ANNOTATOR_NAME);
		thousandGenomesAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(
				thousandGenomesFile.getAbsolutePath());
		return thousandGenomesAnnotator.annotate(vcfRepositoryIterator);
	}

	/**
	 * Creates interface for annotators and annotates a VCF file with 1000G, without {@link Iterator}.
	 * 
	 * @param vcfRepository
	 *            {@link VcfRepository} repository implementation for VCF file
	 * @return thousandGenomesAnnotator.annotate(vcfRepositoryIterator) the annotated VCF file
	 */
	public static Iterator<Entity> annotateWithThousandGenomes(VcfRepository vcfRepository)
	{
		RepositoryAnnotator thousandGenomesAnnotator = registerAnnotator(THOUSAND_GENOMES_ANNOTATOR_NAME);
		thousandGenomesAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(
				thousandGenomesFile.getAbsolutePath());
		return thousandGenomesAnnotator.annotate(vcfRepository);
	}

	/**
	 * Registers one or more annotated classes to be processed.
	 * 
	 * @return context for annotated classes
	 */
	public static AnnotationConfigApplicationContext registerCommandLineAnnotator()
	{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"org.molgenis.data.annotation");
		context.register(CommandLineAnnotatorConfig.class);

		return context;
	}
}
