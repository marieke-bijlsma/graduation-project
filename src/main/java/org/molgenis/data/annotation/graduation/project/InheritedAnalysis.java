package org.molgenis.data.annotation.graduation.project;

import static com.google.common.collect.Sets.newHashSet;
import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;
import org.molgenis.data.annotation.graduation.project.Candidate.InheritanceMode;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class InheritedAnalysis
{
	private File familyAndChildSamplesFile;
	private File vcfFile;
	private File outputFile;

	private String heterozygous_1;
	private String heterozygous_2;
	private String heterozygous_3;
	private String heterozygous_4;

	private String homozygous_1;
	private String homozygous_2;
	private String homozygous_3;
	private String homozygous_4;

	boolean printIds = false; // used in analyzeGene

	public List<Trio> readPedFile() throws IOException
	{
		Scanner scanner = new Scanner(familyAndChildSamplesFile);
		List<Trio> trioList = newArrayList();

		scanner.nextLine(); // skip header
		String line = null;
		while (scanner.hasNextLine())
		{
			line = scanner.nextLine();
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
				// add trios to list
				trioList.add(new Trio(family, child, father, mother));
			}
		}
		scanner.close();
		return trioList;
	}

	@SuppressWarnings("resource")
	public void analyzeVariants() throws Exception
	{
		List<Trio> trioList = readPedFile();

		Set<String> genesSeenInVcfFile = newHashSet(); // mac shift m
		Iterator<Entity> vcfRepository = new VcfRepository(vcfFile, "vcf").iterator();
		int count = 0;

		while (vcfRepository.hasNext())
		{
			count++;
			Entity record = vcfRepository.next();
			Set<String> genesSeenInThisRecord = getGenesFromAnnField(record.getString("ANN").split(","));

			for (String gene : genesSeenInThisRecord)
			{
				addAllVariantsToTrio(trioList, record, gene);
				addSamplesToTrioForGene(record.getEntities(VcfRepository.SAMPLES), trioList, gene);
				genesSeenInVcfFile.add(gene);
			}

			if (count == 2000) break;
		}

		// find out which genes in the genesSeenForPreviousVariant list are NO LONGER present in the
		// genesSeenForNextVariant list
		for (String gene : genesSeenInVcfFile)
		{
			analyzeGene(trioList, gene);

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

	private Set<String> getGenesFromAnnField(String[] multiAnn)
	{
		// get ann field (genes), in hashset to get unique ones
		// get samples and variants for trios
		Set<String> genesSeenForNextVariant = new HashSet<>();

		for (String annField : multiAnn)
		{
			String[] annSplit = annField.split("\\|", -1);
			String gene = annSplit[3];
			genesSeenForNextVariant.add(gene);
		}
		return genesSeenForNextVariant;
	}

	private void addAllVariantsToTrio(List<Trio> trioList, Entity record, String gene)
	{
		// For every trio, add all variants to map and use gene as key
		for (Trio trio : trioList)
		{
			trio.addInfoToVariantMap(gene, record);
		}
	}

	private void addSamplesToTrioForGene(Iterable<Entity> sampleEntities, List<Trio> trioList, String gene)
	{
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

	/**
	 * 
	 * @param trioList
	 *            a list of {@link Trio}s
	 * @param gene
	 * @throws Exception
	 */
	public void analyzeGene(List<Trio> trioList, String gene) throws Exception
	{
		PrintWriter printWriter = new PrintWriter(outputFile, "UTF-8");
		String FORMAT_DP = "DP";

		// PRINT MATRIX
		// Map<String, List<String>> geneFamilyCandidateCounts = Maps.newHashMap();

		// look for homozygous in child && heterozygous in parents

		for (Trio trio : trioList)
		{
			// for every trio, set counts on null
			int childAndFatherHet = 0;
			int childAndMotherHet = 0;

			// temporary list. If comp het, go to addCandidates function. Otherwise, empty list (next trio)
			List<Candidate> temporaryCandidates = newArrayList();

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

				String childGenotype = sampleChild.getString(VcfRepository.FORMAT_GT);
				String fatherGenotype = sampleFather.getString(VcfRepository.FORMAT_GT);
				String motherGenotype = sampleMother.getString(VcfRepository.FORMAT_GT);

				String childDepth = sampleChild.getString(FORMAT_DP);
				String fatherDepth = sampleFather.getString(FORMAT_DP);
				String motherDepth = sampleMother.getString(FORMAT_DP);

				if (!isGenotypeCorrect(childGenotype, fatherGenotype, motherGenotype)) continue;
				if (!isDepthCorrect(childDepth, fatherDepth, motherDepth)) continue;

				String altAlleles = variant.getString(VcfRepository.ALT);
				String[] altsplit = altAlleles.split(",", -1);

				for (int i = 0; i < altsplit.length; i++)
				{
					Impact impact = getImpactForGeneAlleleCombo(variant.getString("ANN").split(",", -1));

					if (impact.equals(Impact.MODIFIER) || impact.equals(Impact.LOW))
					{
						continue;
					}

					InheritanceMode inheritanceMode = null;

					// because alt index = 0 for the first alt add 1
					int altIndex = i + 1;

					updateHomozygousAndHeterozygousValues(altIndex);

					if (homozygousAnalysis(childGenotype, fatherGenotype, motherGenotype))
					{
						inheritanceMode = Candidate.InheritanceMode.HOMOZYGOUS;
					}
					else if (heterozygousDenovoAnalysis(childGenotype, fatherGenotype, motherGenotype))
					{
						inheritanceMode = Candidate.InheritanceMode.DENOVO_HET;
					}
					else if (homozygousDenovoAnalysis(childGenotype, fatherGenotype, motherGenotype))
					{
						inheritanceMode = Candidate.InheritanceMode.DENOVO_HOM;
					}
					else
					{
						if (compoundHeterozygousAnalysisFather(childGenotype, fatherGenotype, motherGenotype))
						{
							childAndFatherHet += 1;
						}

						if (compoundHeterozygousAnalysisMother(childGenotype, fatherGenotype, motherGenotype))
						{
							childAndMotherHet += 1;
						}

						Candidate candidate = new Candidate(Candidate.InheritanceMode.COMPOUND_HET, altsplit[i],
								variant, sampleChild, sampleFather, sampleMother);
						temporaryCandidates.add(candidate);
					}

					Candidate candidate = new Candidate(inheritanceMode, altsplit[i], variant, sampleChild,
							sampleFather, sampleMother);
					trio.addCandidate(gene, candidate);

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
				// PRINT MATRIX
				// if (!geneFamilyCandidateCounts.containsKey(gene))
				// {
				// List<String> familyList = Lists.newArrayList();
				//
				// familyList.add(trio.getFamily_id() + "\t" + trio.getCandidatesForChildperGene().get(gene).size());
				// geneFamilyCandidateCounts.put(gene, familyList);
				//
				// }
				// else
				// {
				// geneFamilyCandidateCounts.get(gene).add(
				// trio.getFamily_id() + "\t" + trio.getCandidatesForChildperGene().get(gene).size());
				// }
				//
				System.out.println("\n" + "Family id: " + trio.getFamily_id() + "\n" + "Gene: " + gene + "\n"
						+ "Number of candidates: " + trio.getCandidatesForChildperGene().get(gene).size() + "\n");

				// pw.println("Family id: " + trio.getFamily_id() + "\n" + "Gene: " + gene + "\n"
				// + "Number of candidates: " + trio.getCandidatesForChildperGene().get(gene).size() + "\n");
				// pw.flush();

				printCandidates(trio.getCandidatesForChildperGene().get(gene), printWriter);

				// break;

				// impacts can be added to Candidate object (cDNA/impact/effect/CADD/ExAC/1000G/GoNL)
			}
			// PRINT MATRIX
			// else
			// {
			// if (!geneFamilyCandidateCounts.containsKey(gene))
			// {
			// List<String> familyList = Lists.newArrayList();
			// familyList.add(trio.getFamily_id() + "\t" + 0);
			// geneFamilyCandidateCounts.put(gene, familyList);
			// }
			// else
			// {
			// geneFamilyCandidateCounts.get(gene).add(trio.getFamily_id() + "\t" + 0);
			// }
			//
			// }

		}
		// PRINT MATRIX
		// for (String geneSymbol : geneFamilyCandidateCounts.keySet())
		// {
		// List<String> fam_ids = Lists.newArrayList();
		//
		// List<String> counts = Lists.newArrayList();
		//
		// for (String famAndCount : geneFamilyCandidateCounts.get(geneSymbol))
		// {
		// String[] split = famAndCount.split("\t");
		// String fam_id = split[0];
		// String count = split[1];
		//
		// fam_ids.add(fam_id + "\t");
		// counts.add(count + "\t");
		//
		// }

		// if (printIds == false) // print fam_ids once!
		// {
		// remove brackets and commas
		// String familyAsString = fam_ids.toString().replaceAll("^\\[", "").replaceAll("\\]$", "")
		// .replace(",", "");
		// System.out.println("\t" + familyAsString);
		// printIds = true;
		// }
		// PRINT MATRIX
		// remove brackets and commas
		// String countsAsString = counts.toString().replaceAll("^\\[", "").replaceAll("\\]$", "").replace(",", "");
		// System.out.println(geneSymbol + "\t" + countsAsString);
		// }

	}

	/**
	 * look for de novo hom
	 * 
	 * @param childGenotype
	 * @param fatherGenotype
	 * @param motherGenotype
	 * @return
	 */
	private boolean homozygousDenovoAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
	{

		return (
		// 1/1, 0/1, 0/0
		(childGenotype.equals(homozygous_1) || childGenotype.equals(homozygous_2))
				&& (fatherGenotype.equals(heterozygous_1) || fatherGenotype.equals(heterozygous_2)
						|| fatherGenotype.equals(heterozygous_3) || fatherGenotype.equals(heterozygous_4)) && (motherGenotype
				.equals(homozygous_3) || motherGenotype.equals(homozygous_4)))

				// OR 1/1,0/0, 0/1
				|| (childGenotype.equals(homozygous_1) || childGenotype.equals(homozygous_2))
				&& (fatherGenotype.equals(homozygous_3) || fatherGenotype.equals(homozygous_4))
				&& (motherGenotype.equals(heterozygous_1) || motherGenotype.equals(heterozygous_2)
						|| motherGenotype.equals(heterozygous_3) || motherGenotype.equals(heterozygous_4));
	}

	/**
	 * look for de novo het with genotypes 1/0, 0/0, 0/0
	 * 
	 * @param childGenotype
	 * @param fatherGenotype
	 * @param motherGenotype
	 * @return
	 */
	private boolean heterozygousDenovoAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
	{
		return (childGenotype.equals(heterozygous_1) || childGenotype.equals(heterozygous_2)
				|| childGenotype.equals(heterozygous_3) || childGenotype.equals(heterozygous_4))
				&& (fatherGenotype.equals(homozygous_3) || fatherGenotype.equals(homozygous_4))
				&& (motherGenotype.equals(homozygous_3) || motherGenotype.equals(homozygous_4));
	}

	/**
	 * homozygous child (not reference) && heterozygous parents
	 * 
	 * @param childGenotype
	 * @param fatherGenotype
	 * @param motherGenotype
	 * @return
	 */
	private boolean homozygousAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
	{
		return ((childGenotype.equals(homozygous_1) || childGenotype.equals(homozygous_2)))
				&& ((fatherGenotype.equals(heterozygous_1) || fatherGenotype.equals(heterozygous_2)
						|| fatherGenotype.equals(heterozygous_3) || fatherGenotype.equals(heterozygous_4)) && (motherGenotype
						.equals(heterozygous_1)
						|| motherGenotype.equals(heterozygous_2)
						|| motherGenotype.equals(heterozygous_3) || motherGenotype.equals(heterozygous_4)));
	}

	/**
	 * TODO
	 * 
	 * @param childGenotype
	 * @param fatherGenotype
	 * @param motherGenotype
	 * @return
	 */
	private boolean compoundHeterozygousAnalysisFather(String childGenotype, String fatherGenotype,
			String motherGenotype)
	{
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

		return (childGenotype.equals(heterozygous_1) || childGenotype.equals(heterozygous_2)
				|| childGenotype.equals(heterozygous_3) || childGenotype.equals(heterozygous_4))
				&& (fatherGenotype.equals(heterozygous_1) || fatherGenotype.equals(heterozygous_2)
						|| fatherGenotype.equals(heterozygous_3) || fatherGenotype.equals(heterozygous_4))
				&& (motherGenotype.equals(homozygous_3) || motherGenotype.equals(homozygous_4));

	}

	private boolean compoundHeterozygousAnalysisMother(String childGenotype, String fatherGenotype,
			String motherGenotype)
	{
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

		return (childGenotype.equals(heterozygous_1) || childGenotype.equals(heterozygous_2)
				|| childGenotype.equals(heterozygous_3) || childGenotype.equals(heterozygous_4))
				&& (fatherGenotype.equals(homozygous_3) || fatherGenotype.equals(homozygous_4))
				&& (motherGenotype.equals(heterozygous_1) || motherGenotype.equals(heterozygous_2)
						|| motherGenotype.equals(heterozygous_3) || motherGenotype.equals(heterozygous_4));
	}

	private void updateHomozygousAndHeterozygousValues(int altIndex)
	{
		// define all heterozygous and homozygous combinations (phased and unphased)

		heterozygous_1 = "0/" + altIndex;
		heterozygous_2 = altIndex + "/0";
		heterozygous_3 = "0|" + altIndex;
		heterozygous_4 = altIndex + "|0";

		// For child in homozygous analysis
		homozygous_1 = altIndex + "/" + altIndex;
		homozygous_2 = altIndex + "|" + altIndex;

		// For parents in compound het analysis
		homozygous_3 = "0/0";
		homozygous_4 = "0|0";
	}

	private boolean isGenotypeCorrect(String childGenotype, String fatherGenotype, String motherGenotype)
	{
		String MISSING_GT = "./.";

		// check for missing genotypes
		if ((childGenotype.equals(MISSING_GT)) || (fatherGenotype.equals(MISSING_GT))
				|| (motherGenotype.equals(MISSING_GT)))
		{
			return false;
		}

		// check if genotype equals reference
		if (childGenotype.equals("0/0") || childGenotype.equals("0|0"))
		{
			return false;
		}

		return true;
	}

	private boolean isDepthCorrect(String childDepth, String fatherDepth, String motherDepth)
	{
		// Get depth for every member of trio for the variant and filter whole trio if one member
		// has a depth below 10

		if ((Integer.parseInt(childDepth) < 10 || Integer.parseInt(fatherDepth) < 10)
				|| Integer.parseInt(motherDepth) < 10)
		{
			return false;
		}

		return true;
	}

	// Get impacts for gene and allele
	private Impact getImpactForGeneAlleleCombo(String[] multiAnn)
	{
		Impact impact = null;
		for (String annField : multiAnn)
		{
			String[] annSplit = annField.split("\\|", -1);
			impact = Impact.valueOf(annSplit[2]);
		}

		return impact;
	}

	public void printCandidates(List<Candidate> candidates, PrintWriter pw)
	{
		for (Candidate c : candidates)
		{
			System.out.println(c.toString());
			// pw.println(c + "\n");
			// pw.flush();
		}
	}

	public static void main(String[] args) throws Exception
	{
		// context to get Spring working
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);

		InheritedAnalysis inheritedAnalyses = ctx.getBean(InheritedAnalysis.class);
		inheritedAnalyses.parseCommandLineArgs(args);
		inheritedAnalyses.analyzeVariants();

		ctx.close();
	}

	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		familyAndChildSamplesFile = new File(args[1]);
		if (!familyAndChildSamplesFile.isFile())
		{
			throw new Exception("Family and child samples file does not exist or directory: "
					+ familyAndChildSamplesFile.getAbsolutePath());
		}
		outputFile = new File(args[2]);
		if (!outputFile.isFile())
		{
			throw new Exception("output file does not exist or directory: " + outputFile.getAbsolutePath());
		}
	}
}
