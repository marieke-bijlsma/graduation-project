package org.molgenis.data.annotation.graduation.project;

import static com.google.common.collect.Sets.newHashSet;
import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.graduation.project.GenotypeUtils.compoundHeterozygousAnalysisFather;
import static org.molgenis.data.annotation.graduation.project.GenotypeUtils.compoundHeterozygousAnalysisMother;
import static org.molgenis.data.annotation.graduation.project.GenotypeUtils.heterozygousDenovoAnalysis;
import static org.molgenis.data.annotation.graduation.project.GenotypeUtils.homozygousAnalysis;
import static org.molgenis.data.annotation.graduation.project.GenotypeUtils.homozygousDenovoAnalysis;
import static org.molgenis.data.annotation.graduation.project.GenotypeUtils.isDepthCorrect;
import static org.molgenis.data.annotation.graduation.project.GenotypeUtils.isGenotypeCorrect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
	private File candidateOutputFile;
	private File matrixOutputFile;

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

	public void readAndProcessVcfFile() throws Exception
	{
		List<Trio> trioList = readPedFile();

		Set<String> genesSeenForPreviousVariant = newHashSet(); // mac shift m

		@SuppressWarnings("resource")
		Iterator<Entity> vcfRepository = new VcfRepository(vcfFile, "vcf").iterator();

		int count = 0;

		while (vcfRepository.hasNext())
		{
			count++;
			Entity record = vcfRepository.next();
			Set<String> genesSeenForCurrentVariant = getGenesFromAnnField(record.getString("ANN").split(","));

			for (String gene : genesSeenForCurrentVariant)
			{
				addAllVariantsToTrio(trioList, record, gene);
				addSamplesToTrioForGene(record.getEntities(VcfRepository.SAMPLES), trioList, gene);
			}

			if (count % 1000 == 0)
			{
				System.out.println("scanned over " + count + " lines");
			}

			// find out which genes in the genesSeenForPreviousVariant list are NO LONGER present in the
			// genesSeenForNextVariant list
			for (String gene : genesSeenForPreviousVariant)
			{
				if (!genesSeenForCurrentVariant.contains(gene))
				{
					analyzeGene(trioList, gene);
				}
			}
			genesSeenForPreviousVariant.clear();
			genesSeenForPreviousVariant.addAll(genesSeenForCurrentVariant);
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

		// Used for creating matrix
		Map<String, List<String>> geneFamilyCandidateCounts = newHashMap();

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

				String FORMAT_DP = "DP";

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

					GenotypeUtils.updateHomozygousAndHeterozygousValues(altIndex);

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

						Candidate candidate = new Candidate(Candidate.InheritanceMode.COMPOUND_HET, childGenotype,
								fatherGenotype, motherGenotype, altAlleles, altsplit[i], impact);
						addInfomationToCandidate(candidate, variant);
						temporaryCandidates.add(candidate);
					}

					if (inheritanceMode != null)
					{
						Candidate candidate = new Candidate(inheritanceMode, childGenotype, fatherGenotype,
								motherGenotype, altAlleles, altsplit[i], impact);
						addInfomationToCandidate(candidate, variant);
						trio.addCandidate(gene, candidate);
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

			addTrioToMatrix(gene, geneFamilyCandidateCounts, trio);

			// then remove data for this gene
			// delete from trio: samples & variants
			trio.getVariants().remove(gene);
			trio.getSamplesChild().remove(gene);
			trio.getSamplesFather().remove(gene);
			trio.getSamplesMother().remove(gene);
		}

		printMatrix(geneFamilyCandidateCounts);
	}

	private void addInfomationToCandidate(Candidate candidate, Entity variant)
	{
		candidate.setChrom(variant.getString(VcfRepository.CHROM));
		candidate.setPos(variant.getString(VcfRepository.POS));

		String annField = variant.getString("ANN");
		String[] annSplit = annField.split("\\|");

		candidate.setEffect(annSplit[1]);
		candidate.setcDNA(annSplit[9]);

		candidate.setExac_af_STR(variant.get("EXAC_AF") == null ? "-" : variant.getString("EXAC_AF"));
		candidate.setExac_ac_hom_STR(variant.get("EXAC_AC_HOM") == null ? "-" : variant.getString("EXAC_AC_HOM"));
		candidate.setExac_ac_het_STR(variant.get("EXAC_AC_HET") == null ? "-" : variant.getString("EXAC_AC_HET"));

		String gonl_af_STR = null;

		if (variant.getString("GoNL_AF").equals(".") || variant.getString("GoNL_AF").equals(".|.")
				|| variant.get("GoNL_AF") == null)
		{
			gonl_af_STR = "-";
		}
		else
		{
			gonl_af_STR = variant.getString("GoNL_AF");
		}

		candidate.setGonl_af_STR(gonl_af_STR);
		candidate.setThousandG_af_STR(variant.get("Thousand_Genomes_AF") == null ? "-" : variant
				.getString("Thousand_Genomes_AF"));
		candidate.setCadd(variant.get("CADD_SCALED") == null ? "-" : variant.getString("CADD_SCALED"));
	}

	private void addTrioToMatrix(String gene, Map<String, List<String>> geneFamilyCandidateCounts, Trio trio)
			throws IOException
	{
		int size = 0;
		if (trio.getCandidatesForChildperGene().get(gene) != null)
		{
			size = trio.getCandidatesForChildperGene().get(gene).size();
			printCandidates(trio.getCandidatesForChildperGene().get(gene), trio, gene);
		}

		if (!geneFamilyCandidateCounts.containsKey(gene))
		{
			List<String> familyList = newArrayList();
			familyList.add(trio.getFamily_id() + "\t" + size);
			geneFamilyCandidateCounts.put(gene, familyList);
		}
		else
		{
			geneFamilyCandidateCounts.get(gene).add(trio.getFamily_id() + "\t" + size);
		}
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

	private void printCandidates(List<Candidate> candidates, Trio trio, String gene) throws IOException
	{
		FileWriter fileWriter = new FileWriter(candidateOutputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		bufferedWriter.append("Family id: " + trio.getFamily_id() + "\n" + "Gene: " + gene + "\n"
				+ "Number of candidates: " + trio.getCandidatesForChildperGene().get(gene).size() + "\n");

		// impacts can be added to Candidate object (cDNA/impact/effect/CADD/ExAC/1000G/GoNL)
		for (Candidate candidate : candidates)
		{
			bufferedWriter.append(candidate + "\n");
		}

		bufferedWriter.close();
	}

	private void printMatrix(Map<String, List<String>> geneFamilyCandidateCounts) throws IOException
	{
		boolean printIds = false; // used in analyzeGene
		FileWriter fileWriter = new FileWriter(matrixOutputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		for (String geneSymbol : geneFamilyCandidateCounts.keySet())
		{
			List<String> fam_ids = newArrayList();
			List<String> counts = newArrayList();

			for (String famAndCount : geneFamilyCandidateCounts.get(geneSymbol))
			{
				String[] split = famAndCount.split("\t");
				String fam_id = split[0];
				String count = split[1];

				fam_ids.add(fam_id + "\t");
				counts.add(count + "\t");

			}
			// print fam_ids once!
			if (printIds == false)
			{
				// remove brackets and commas
				String familyAsString = fam_ids.toString().replaceAll("^\\[", "").replaceAll("\\]$", "")
						.replace(",", "");
				bufferedWriter.append("\t" + familyAsString);
				printIds = true;
			}

			// remove brackets and commas
			String countsAsString = counts.toString().replaceAll("^\\[", "").replaceAll("\\]$", "").replace(",", "");
			bufferedWriter.append(geneSymbol + "\t" + countsAsString);
		}
		bufferedWriter.close();
	}

	public static void main(String[] args) throws Exception
	{
		// context to get Spring working
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);

		InheritedAnalysis inheritedAnalyses = ctx.getBean(InheritedAnalysis.class);
		inheritedAnalyses.parseCommandLineArgs(args);
		inheritedAnalyses.readAndProcessVcfFile();

		ctx.close();
	}

	private void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 4))
		{
			throw new Exception("Must supply 4 arguments");
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
		candidateOutputFile = new File(args[2]);
		if (!candidateOutputFile.isFile())
		{
			throw new Exception("output file does not exist or directory: " + candidateOutputFile.getAbsolutePath());
		}
		matrixOutputFile = new File(args[3]);
		if (!matrixOutputFile.isFile())
		{
			throw new Exception("output file does not exist or directory: " + matrixOutputFile.getAbsolutePath());
		}
	}
}
