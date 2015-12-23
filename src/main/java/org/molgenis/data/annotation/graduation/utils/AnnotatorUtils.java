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

public class AnnotatorUtils
{
	private static final File exacFile = new File("/Users/molgenis/Downloads/ExAC.r0.3.sites.vep.vcf.gz");

	public static Iterator<Entity> annotateWithExac(VcfRepository vcfRepository)
	{
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator exacAnnotator = annotators.get("exac");
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(exacFile.getAbsolutePath());
		return exacAnnotator.annotate(vcfRepository);
	}

	public static AnnotationConfigApplicationContext registerCommandLineAnnotator() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		context.register(CommandLineAnnotatorConfig.class);
		
		return context;
	}
	
}
