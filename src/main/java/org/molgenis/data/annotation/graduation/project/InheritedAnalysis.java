package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class InheritedAnalysis
{
	boolean printIds = false; // used in analyzeGene

	public void go(File vcfFile, File familyAndChildSamplesFile, File outputFile) throws Exception
	{
		List<Trio> trioList = readPedFile(familyAndChildSamplesFile);
		analyzeVariants(vcfFile, outputFile, trioList);
	}

	public List<Trio> readPedFile(File familyAndChildSamplesFile) throws IOException
	{

		Scanner scanPed = new Scanner(familyAndChildSamplesFile);
		String line = null;
		List<Trio> trioList = Lists.newArrayList();

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
			if (child.equals("0") || father.equals("0") || mother.equals("0"))
			{
				continue;
			}

			else
			{
				// add trios to map
				trioList.add(new Trio(family, child, father, mother));
			}

		}
		scanPed.close();
		return trioList;
	}

	public void analyzeVariants(File vcfFile, File outputFile, List<Trio> trioList) throws Exception
	{
		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		Set<String> genesSeenForPreviousVariant = new HashSet<String>();

		int count = 0;
		while (vcf.hasNext())
		{
			count++;

			// Print status after 100 lines
//			if (count % 100 == 0)
//			{
//				System.out.println("Now at line: " + count);
//			}

			Entity record = vcf.next();

			String[] multiAnn = record.getString("ANN").split(",");

			// get ann field (genes), in hashset to get unique ones
			// get samples and variants for trios
			Set<String> genesSeenForNextVariant = new HashSet<String>();
			Set<String> uniqGenes = new HashSet<String>();

			for (String annField : multiAnn)
			{
				String[] annSplit = annField.split("\\|", -1);
				// String impact = annSplit[2];
				String gene = annSplit[3];
				uniqGenes.add(gene);
			}

			for (String uniqGene : uniqGenes)
			{
				genesSeenForNextVariant.add(uniqGene);
				addSamplesToTrioForGene(record, trioList, uniqGene);

			}

			// find out which genes in the genesSeenForPreviousVariant list are NO LONGER present in the
			// genesSeenForNextVariant list
			for (String gene : genesSeenForPreviousVariant)
			{
				if (!genesSeenForNextVariant.contains(gene))
				{
					// analyse within trios the date for this gene

					analyzeGene(trioList, gene, pw);

					// if (gene.equals("MTOR") || gene.equals("ANGPTL7"))
					// {
					// System.out.println("We are done with " + gene + ", starting analyis! " +
					// genesSeenForPreviousVariant.size());
					// checkTrio(trioList);
					// }

					// then remove data for this gene
					// delete from trio: samples & variants
					for (Trio trio : trioList)
					{
						trio.getVariants().remove(gene);
						trio.getSamplesChild().remove(gene);
						trio.getSamplesFather().remove(gene);
						trio.getSamplesMother().remove(gene);
					}

				}
			}

			// and copy genes
			genesSeenForPreviousVariant.clear();
			for (String gene : genesSeenForNextVariant)
			{
				genesSeenForPreviousVariant.add(gene);
			}

		}

	}

	public void checkTrio(List<Trio> trioList) throws Exception
	{

		for (Trio trio : trioList)
		{
			for (String gene : trio.getSamplesChild().keySet())
			{
				if (!(trio.getVariants().get(gene).size() == trio.getSamplesChild().get(gene).size()
						&& trio.getVariants().get(gene).size() == trio.getSamplesFather().get(gene).size() && trio
						.getVariants().get(gene).size() == trio.getSamplesMother().get(gene).size()))
				{
					throw new Exception("Something is going wrong here...");
				}

				// if (trio.getSamplesFather().containsKey(key) && trio.getSamplesMother().containsKey(key))
				// {
				// System.out.println("trio ok:" + trio.getChild_id() + ", gene: " + gene);
				// System.out.println("Child: " + trio.getChild_id() + " gene: " + key + " sample: "
				// + trio.getSamplesChild().get(key) + "\n" + "Father: " + trio.getFather_id() + " sample: "
				// + trio.getSamplesFather().get(key) + "\n" + "Mother: " + trio.getMother_id() + " sample: "
				// + trio.getSamplesMother().get(key) + "\n" + "variant: " + trio.getVariants().get(key));
				// }
			}
		}
	}

	public void addSamplesToTrioForGene(Entity record, List<Trio> trioList, String gene)
	{
		Iterable<Entity> sampleEntities = record.getEntities(VcfRepository.SAMPLES);

		// For every trio, add all variants to map and use gene as key
		for (Trio trio : trioList)
		{
			trio.addInfoToVariantMap(gene, record);
		}

		// For every sample in VCF, add all samples to right members of trio and use gene as key
		sampleloop: for (Entity sample : sampleEntities)
		{
			for (Trio trio : trioList)
			{
				String child = trio.getChild_id();
				String father = trio.getFather_id();
				String mother = trio.getMother_id();

				String sampleID = sample.get("ORIGINAL_NAME").toString();

				if (child.equals(sampleID))
				{
					trio.addSampleToChildMap(gene, sample);
					continue sampleloop;
				}
				else if (father.equals(sampleID))
				{
					trio.addSampleToFatherMap(gene, sample);
					continue sampleloop;
				}
				else if (mother.equals(sampleID))
				{
					trio.addSampleToMotherMap(gene, sample);
					continue sampleloop;
				}
				else
				{
					continue;
				}
			}
		}
	}

	public void analyzeGene(List<Trio> trioList, String gene, PrintWriter pw) throws Exception
	{
		Map<String, List<String>> geneFamilyCandidateCounts = Maps.newHashMap();

		// look for homozygous in child && heterozygous in parents

		for (Trio trio : trioList)
		{
			// for every trio, set counts on null
			int childAndFatherHet = 0;
			int childAndMotherHet = 0;

			// temporary list. If comp het, go to addCandidates function. Otherwise, empty list (next trio)
			List<Candidate> temporaryCandidates = Lists.newArrayList();

			Map<String, List<Entity>> variants = trio.getVariants();
			Map<String, List<Entity>> childSamples = trio.getSamplesChild();
			Map<String, List<Entity>> fatherSamples = trio.getSamplesFather();
			Map<String, List<Entity>> motherSamples = trio.getSamplesMother();

			int variantIndex = 0;
			for (Entity variant : variants.get(gene))
			{
				Entity sampleChild = childSamples.get(gene).get(variantIndex);
				Entity sampleFather = fatherSamples.get(gene).get(variantIndex);
				Entity sampleMother = motherSamples.get(gene).get(variantIndex);

				String childGenotype = sampleChild.get("GT").toString();
				String fatherGenotype = sampleFather.get("GT").toString();
				String motherGenotype = sampleMother.get("GT").toString();

				// check for missing genotypes
				if ((childGenotype.equals("./.")) || (fatherGenotype.equals("./.")) || (motherGenotype.equals("./.")))
				{
					continue;
				}

				// check if genotype equals reference
				if (childGenotype.equals("0/0") || childGenotype.equals("0|0"))
				{
					continue;
				}

				// Get depth for every member of trio for the variant and filter whole trio if one member
				// has a depth below 10
				String childDepth = sampleChild.get("DP").toString();
				String fatherDepth = sampleFather.get("DP").toString();
				String motherDepth = sampleMother.get("DP").toString();

				if ((Integer.parseInt(childDepth) < 10 || Integer.parseInt(fatherDepth) < 10)
						|| Integer.parseInt(motherDepth) < 10)
				{
					continue;
				}

				// if(altsplit.length > 1)
				// {
				//
				// String[] multiAnn = variant.getString("ANN").split(",");
				// System.out.println("multiple alleles! " + altsplit.length + " number of genes: " + multiAnn.length);
				// }

				String altAlleles = variant.getString("ALT");
				String[] altsplit = altAlleles.split(",", -1);

				for (int i = 0; i < altsplit.length; i++)
				{
					// System.out.println("alt allele: " + altsplit[i]);
					// System.out.println(variant.getString("#CHROM") + ", " + variant.getString("POS"));
					// System.out.println(variant.get("ANN").toString());

					String ann = variant.getString("ANN");
					Impact impact = getImpactForGeneAlleleCombo(ann, gene, altsplit[i]);

					if (impact.equals(Impact.MODIFIER) || impact.equals(Impact.LOW))
					{
						continue;
					}

					// because alt index = 0 for the first alt add 1
					int altIndex = i + 1;

					// define all heterozygous and homozygous combinations (phased and unphased)
					String het1 = "0/" + altIndex;
					String het2 = altIndex + "/0";
					String het3 = "0|" + altIndex;
					String het4 = altIndex + "|0";

					// For child in homozygous analysis
					String hom1 = altIndex + "/" + altIndex;
					String hom2 = altIndex + "|" + altIndex;

					// For parents in compound het analysis
					String hom3 = "0/0";
					String hom4 = "0|0";

					// homozygous child (not reference) && heterozygous parents

					if (childGenotype.equals(hom1) || childGenotype.equals(hom2))
					{
						if ((fatherGenotype.equals(het1) || fatherGenotype.equals(het2) || fatherGenotype.equals(het3) || fatherGenotype
								.equals(het4))
								&& (motherGenotype.equals(het1) || motherGenotype.equals(het2)
										|| motherGenotype.equals(het3) || motherGenotype.equals(het4)))
						{
							Candidate candidate = new Candidate(Candidate.InheritanceMode.HOMOZYGOUS, altsplit[i],
									variant, sampleChild, sampleFather, sampleMother);
							trio.addCandidate(gene, candidate);
						}
					}

					// look for compound het

					// Same gene, two or more variants
					// They can't be all heterozygous
					// They can't be all homozygous
					// Child must be heterozygous
					// One of parents must be at least one time heterozygous for variant but not for the
					// same
					// variant
					// variant1: father: 0/1, mother 0/0
					// variant2: father 0/0, mother 0/1

					if ((childGenotype.equals(het1) || childGenotype.equals(het2) || childGenotype.equals(het3) || childGenotype
							.equals(het4))
							&& (fatherGenotype.equals(het1) || fatherGenotype.equals(het2)
									|| fatherGenotype.equals(het3) || fatherGenotype.equals(het4))
							&& (motherGenotype.equals(hom3) || motherGenotype.equals(hom4)))
					{

						childAndFatherHet += 1;
						Candidate candidate = new Candidate(Candidate.InheritanceMode.COMPOUND_HET, altsplit[i],
								variant, sampleChild, sampleFather, sampleMother);
						temporaryCandidates.add(candidate);
					}

					if ((childGenotype.equals(het1) || childGenotype.equals(het2) || childGenotype.equals(het3) || childGenotype
							.equals(het4))
							&& (fatherGenotype.equals(hom3) || fatherGenotype.equals(hom4))
							&& (motherGenotype.equals(het1) || motherGenotype.equals(het2)
									|| motherGenotype.equals(het3) || motherGenotype.equals(het4)))
					{

						childAndMotherHet += 1;
						Candidate candidate = new Candidate(Candidate.InheritanceMode.COMPOUND_HET, altsplit[i],
								variant, sampleChild, sampleFather, sampleMother);
						temporaryCandidates.add(candidate);
					}

					// look for de novo het
					// 1/0, 0/0, 0/0
					if ((childGenotype.equals(het1) || childGenotype.equals(het2) || childGenotype.equals(het3) || childGenotype
							.equals(het4))
							&& (fatherGenotype.equals(hom3) || fatherGenotype.equals(hom4))
							&& (motherGenotype.equals(hom3) || motherGenotype.equals(hom4)))
					{
						Candidate candidate = new Candidate(Candidate.InheritanceMode.DENOVO_HET, altsplit[i], variant,
								sampleChild, sampleFather, sampleMother);
						trio.addCandidate(gene, candidate);

					}

					// look for de novo hom
					// 1/1, 0/1, 0/0
					if (childGenotype.equals(hom1) || childGenotype.equals(hom2))
					{
						if ((fatherGenotype.equals(het1) || fatherGenotype.equals(het2) || fatherGenotype.equals(het3) || fatherGenotype
								.equals(het4)) && (motherGenotype.equals(hom3) || motherGenotype.equals(hom4)))
						{
							Candidate candidate = new Candidate(Candidate.InheritanceMode.DENOVO_HOM, altsplit[i],
									variant, sampleChild, sampleFather, sampleMother);
							trio.addCandidate(gene, candidate);
						}
					}

					// OR 1/1,0/0, 0/1
					if (childGenotype.equals(hom1) || childGenotype.equals(hom2))
					{
						if ((fatherGenotype.equals(hom3) || fatherGenotype.equals(hom4))
								&& (motherGenotype.equals(het1) || motherGenotype.equals(het2)
										|| motherGenotype.equals(het3) || motherGenotype.equals(het4)))
						{
							Candidate candidate = new Candidate(Candidate.InheritanceMode.DENOVO_HOM, altsplit[i],
									variant, sampleChild, sampleFather, sampleMother);
							trio.addCandidate(gene, candidate);
						}
					}

				}
				// next variant
				variantIndex++;
			}

			// Only if comp het found, send temporary list to addCandidates
			// if both parents are at least heterozygous for one variant (not the same one) and child is
			// heterozygous for both variants on this gene

			if ((childAndFatherHet >= 1) && (childAndMotherHet >= 1))
			{
				// TODO: Use addAll -> for every gene add all candidates at once
				for (Candidate candidate : temporaryCandidates)
				{
					trio.addCandidate(gene, candidate);
				}
			}

			if (trio.getCandidatesForChildperGene().get(gene) != null)
			{
				if (!geneFamilyCandidateCounts.containsKey(gene))
				{
					List<String> familyList = Lists.newArrayList();

					familyList.add(trio.getFamily_id() + "\t" + trio.getCandidatesForChildperGene().get(gene).size());
					geneFamilyCandidateCounts.put(gene, familyList);

				}
				else
				{
					geneFamilyCandidateCounts.get(gene).add(
							trio.getFamily_id() + "\t" + trio.getCandidatesForChildperGene().get(gene).size());
				}

				// System.out.println("\n" + "Family id: " + trio.getFamily_id() + "\n" + "Gene: " + gene + "\n"
				// + "Number of candidates: " + trio.getCandidatesForChildperGene().get(gene).size() + "\n");

				// pw.println("Family id: " + trio.getFamily_id() + "\n" + "Gene: " + gene + "\n"
				// + "Number of candidates: " + trio.getCandidatesForChildperGene().get(gene).size() + "\n");
				// pw.flush();

				printCandidates(trio.getCandidatesForChildperGene().get(gene), pw);

				// break;

				// impacts can be added to Candidate object (cDNA/ effect?)
			}
			else
			{
				if (!geneFamilyCandidateCounts.containsKey(gene))
				{
					List<String> familyList = Lists.newArrayList();
					familyList.add(trio.getFamily_id() + "\t" + 0);
					geneFamilyCandidateCounts.put(gene, familyList);
				}
				else
				{
					geneFamilyCandidateCounts.get(gene).add(trio.getFamily_id() + "\t" + 0);
				}

			}

		}

		for (String geneSymbol : geneFamilyCandidateCounts.keySet())
		{
			List<String> fam_ids = Lists.newArrayList();

			List<String> counts = Lists.newArrayList();

			for (String famAndCount : geneFamilyCandidateCounts.get(geneSymbol))
			{
				String[] split = famAndCount.split("\t");
				String fam_id = split[0];
				String count = split[1];

				fam_ids.add(fam_id + "\t");
				counts.add(count + "\t");

			}

			if (printIds == false) // print fam_ids once!
			{
				// remove brackets and commas
				String familyAsString = fam_ids.toString().replaceAll("^\\[", "").replaceAll("\\]$", "")
						.replace(",", "");
				System.out.println("\t" + familyAsString);
				printIds = true;
			}
			// remove brackets and commas
			String countsAsString = counts.toString().replaceAll("^\\[", "").replaceAll("\\]$", "").replace(",", "");
			System.out.println(geneSymbol + "\t" + countsAsString);
		}

	}

	// Get impacts for gene and allele
	private Impact getImpactForGeneAlleleCombo(String ann, String gene, String allele)
	{
		String impact = null;

		String[] multiAnn = ann.split(",", -1);

		for (String annField : multiAnn)
		{
			String[] annSplit = annField.split("\\|", -1);
			impact = annSplit[2];
		}

		return Impact.valueOf(impact);
	}

	public void printCandidates(List<Candidate> candidates, PrintWriter pw)
	{
		for (Candidate c : candidates)
		{
			// System.out.println(c.toString());
			// pw.println(c + "\n");
			// pw.flush();
		}
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

		File familyAndChildSamplesFile = new File(args[1]);
		if (!familyAndChildSamplesFile.isFile())
		{
			throw new Exception("Family and child samples file does not exist or directory: "
					+ familyAndChildSamplesFile.getAbsolutePath());
		}
		File outputFile = new File(args[2]);
		if (!outputFile.isFile())
		{
			throw new Exception("output file does not exist or directory: " + outputFile.getAbsolutePath());
		}

		InheritedAnalysis ia = new InheritedAnalysis();
		ia.go(vcfFile, familyAndChildSamplesFile, outputFile);
	}
}
