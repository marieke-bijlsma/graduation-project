package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import autovalue.shaded.com.google.common.common.collect.Lists;

@Component
public class GetVariantsPerPatient
{
	Map<String, ArrayList<String>> sampleChrPos = Maps.newHashMap();
	List<String> chrPos = Lists.newArrayList();
	List<String> allSamples = Lists.newArrayList();
	String sampleName;

	public void go(File vcfFile) throws Exception
	{

		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		while (vcf.hasNext())
		{
			Entity record = vcf.next();

			String chr = record.getString("#CHROM");
			String pos = record.getString("POS");
			// System.out.println("processing chromosome: " + chr + ", position: " + pos);
			String val = chr + "_" + pos;
			chrPos.add(val);

			Iterable<Entity> sampleEntities = record.getEntities(VcfRepository.SAMPLES);
			for (Entity sample : sampleEntities)
			{

				sampleName = sample.get("ORIGINAL_NAME").toString();

				// sample_id, [chr_pos, chr_pos]
				allSamples.add(sampleName);
			}
		}

		for (String sam_id : allSamples)
		{
			for (String cp : chrPos)
			{
				if (sampleChrPos.containsKey(sampleName))
				{
					sampleChrPos.get(sampleName).add(cp);
				}
				else
				{
					ArrayList<String> entries = Lists.newArrayList();
					entries.addAll(chrPos);
					sampleChrPos.put(sam_id, entries);
				}
				System.out.println(sampleChrPos);
			}
//			System.out.println(sampleChrPos);
			
		}
//		System.out.println(sampleChrPos);
	}

	public static void main(String[] args) throws Exception
	{
		// configureLogging();

		// See http://stackoverflow.com/questions/4787719/spring-console-application-configured-using-annotations
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				"org.molgenis.data.annotation.graduation");
		ctx.register(CommandLineAnnotatorConfig.class);
		GetVariantsPerPatient main = ctx.getBean(GetVariantsPerPatient.class);
		main.run(args);
		ctx.close();
	}

	public void run(String[] args) throws Exception
	{
		if (!(args.length == 1))
		{
			throw new Exception("Must supply 1 arguments");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		GetVariantsPerPatient vpp = new GetVariantsPerPatient();
		vpp.go(vcfFile);

	}

}
