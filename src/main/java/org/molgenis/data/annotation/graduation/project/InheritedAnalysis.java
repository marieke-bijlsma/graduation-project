package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class InheritedAnalysis
{
	
	public void go(File vcfFile, File exacFile, File familyAndChildSamplesFile) throws Exception
	{
		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator exacAnnotator = annotators.get("exac");
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(exacFile.getAbsolutePath());
		Iterator<Entity> vcfWithExac = exacAnnotator.annotate(vcf);

		// decimal format for double, otherwise can't do calculations
		DecimalFormat df = new DecimalFormat("#.##############################");

		Scanner scanPed = new Scanner(familyAndChildSamplesFile);
		String line = null;
		List<Trio> samples = Lists.newArrayList();

		scanPed.nextLine(); // skip header
		while (scanPed.hasNextLine())
		{
			line = scanPed.nextLine();
			String[] lineSplit = line.split("\t", -1);
			String family = lineSplit[0];
			String child = lineSplit[1];
			String father = lineSplit[2];
			String mother = lineSplit[3];

			// if one of the trio got no info, skip
			// child id 44498.Broad_reads not seen in VCF (weird id)
			if (child.equals("0") || child.equals("44498.Broad_reads") || father.equals("0") || mother.equals("0"))
			{
				continue;
			}

			else
			{
				// add trios to map
				samples.add(new Trio(family, child, father, mother));
			}

		}
		scanPed.close();

		int count = 0;
		while (vcfWithExac.hasNext())
		{
			count++;

			// print number of lines scanned
			if (count % 1000 == 0)
			{
				System.out.println("Scanned over " + count + " lines in VCF...");
			}

			// Only 1000 variants
			// if (count == 1000)
			// {
			// break;
			// }

			Entity record = vcfWithExac.next();

			// get filter column
			String filter = record.getString("FILTER");

			// Convert AFs to Doubles again to measure stuff
			// If all conditions true -> filter
			if (!(filter.equals("PASS")))
			{
				continue;
			}

			// Get ANN field and split it to get impact
			String annField = record.getString("ANN");
			String[] splitAnnField = annField.split("\\|");
			String impact = splitAnnField[2];

			if (impact.equals("LOW"))
			{
				continue;
			}

			if (impact.equals("MODIFIER"))
			{
				continue;
			}

			// First get all "not null" ExAC AF to String
			// Then, convert all "not null" to Doubles
			// Decimal format (String) for calculations
			// Also check for comma (one value contains comma (0.005493,0.013)

			String exacAFStr = record.get("EXAC_AF") == null || record.get("EXAC_AF").toString().contains(",") ? null : record
					.get("EXAC_AF").toString();
			Double exacAF = exacAFStr != null ? Double.parseDouble(exacAFStr) : null;
			String decimalExacAF = exacAF == null ? "" : df.format(exacAF);

			if (!(decimalExacAF == "") && ((Double.parseDouble(decimalExacAF) > 0.05)))
			{
				continue;
			}

			String altStr = record.getString("ALT");

			String[] altsplit = altStr.split(",", -1);

			for (int i = 0; i < altsplit.length; i++)
			{
				// String alt = altsplit[i];
				getInfo(record, i, samples);
			}
		}
	}

	public void getInfo(Entity record, int altIndex, List<Trio> samples)
	{
		
		Iterable<Entity> sampleEntities = record.getEntities(VcfRepository.SAMPLES);

		for (Entity sample : sampleEntities)
		{
			// Add sample info
			Trio.addToTrioList(samples, sample);
		}

		for (Trio trio : samples)
		{
			// Add variant info
			trio.getVariants().add(record);

			genotypeComparison(altIndex, trio);
		}
		
//		System.out.println(multipleGenes.size());

	}

	public void genotypeComparison(int altIndex, Trio trio)
	{
		// because alt index = 0 for the first alt, we add 1
		altIndex = altIndex + 1;

		List<Entity> variants = trio.getVariants();
		List<Entity> childSamples = trio.getChildSamples();
		List<Entity> fatherSamples = trio.getFatherSamples();
		List<Entity> motherSamples = trio.getMotherSamples();

		Map<String, List<Entity>> multipleGenes = Maps.newHashMap();

		for (int i = 0; i < variants.size(); i++)
		{
			String childGenotype = childSamples.get(i).get("GT").toString();
			String fatherGenotype = fatherSamples.get(i).get("GT").toString();
			String motherGenotype = motherSamples.get(i).get("GT").toString();

			// check for missing genotypes
			if ((childGenotype.equals("./.")) || (motherGenotype.equals("./.")) || (fatherGenotype.equals("./.")))
			{
				continue;
			}

			// check if equals reference
			// if (childGenotype.equals("0/0") || childGenotype.equals("0|0"))
			// {
			// continue;
			// }
			//
			// if (fatherGenotype.equals("0/0") || fatherGenotype.equals("0|0"))
			// {
			// continue;
			// }
			//
			// if (motherGenotype.equals("0/0") || motherGenotype.equals("0|0"))
			// {
			// continue;
			// }

			// homozygous child (not reference)
			if (childGenotype.equals("0/" + altIndex) || childGenotype.equals(altIndex + "/0"))
			{
				if (fatherGenotype.equals(altIndex + "/" + altIndex)
						|| fatherGenotype.equals(altIndex + "|" + altIndex))
				{
					if (motherGenotype.equals(altIndex + "/" + altIndex)
							|| motherGenotype.equals(altIndex + "|" + altIndex))
					{
						System.out.println("Both parents are homozygous and child is heterozygous!" + "child gt: "
								+ childGenotype + " father gt: " + fatherGenotype + " mother gt: " + motherGenotype);

					}
				}

			}

//			if (childGenotype.equals("0/" + altIndex) || childGenotype.equals(altIndex + "/0"))
//			{
//
//			}

			// get first gene
			String[] annSplit = variants.get(i).getString("ANN").split("\\|", -1);
			String gene = annSplit[3];

			// if gene already in map, add another variant, else put variant in list and add it together with new
			// key in map
			if (multipleGenes.containsKey(gene))
			{
				multipleGenes.get(gene).add(variants.get(i));
			}
			else
			{
				List<Entity> entities = Lists.newArrayList();
				entities.add(variants.get(i));
				multipleGenes.put(gene, entities);
			}

		}

//		// if child is heterozygous
//		for (String key : multipleGenes.keySet())
//		{
//			System.out.println(key);
//			for (Entity variant : multipleGenes.get(key))
//			{
//				System.out.println(variant.get("GT").toString());
//				if (variant.get("GT").toString().equals("0/" + altIndex)
//						|| variant.get("GT").toString().equals(altIndex + "/0"))
//				{
//					System.out.println(variant);
//				}
//			}
//		}

		// System.out.println(multipleGenes.size());

	}

	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);
		InheritedAnalysis main = ctx.getBean(InheritedAnalysis.class);
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
			throw new Exception("VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		File exacFile = new File(args[1]);
		if (!exacFile.isFile())
		{
			throw new Exception("VCF file does not exist or directory: " + exacFile.getAbsolutePath());
		}

		File familyAndChildSamplesFile = new File(args[2]);
		if (!familyAndChildSamplesFile.isFile())
		{
			throw new Exception("Family and child samples file does not exist or directory: "
					+ familyAndChildSamplesFile.getAbsolutePath());
		}

		InheritedAnalysis ia = new InheritedAnalysis();
		ia.go(vcfFile, exacFile, familyAndChildSamplesFile);

	}

}
