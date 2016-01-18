package org.molgenis.data.annotation.graduation.analysis;

import static com.google.common.collect.Sets.newHashSet;
import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.graduation.utils.GenotypeUtils.compoundHeterozygousAnalysisFather;
import static org.molgenis.data.annotation.graduation.utils.GenotypeUtils.compoundHeterozygousAnalysisMother;
import static org.molgenis.data.annotation.graduation.utils.GenotypeUtils.heterozygousDenovoAnalysis;
import static org.molgenis.data.annotation.graduation.utils.GenotypeUtils.homozygousAnalysis;
import static org.molgenis.data.annotation.graduation.utils.GenotypeUtils.homozygousDenovoAnalysis;
import static org.molgenis.data.annotation.graduation.utils.GenotypeUtils.isDepthCorrect;
import static org.molgenis.data.annotation.graduation.utils.GenotypeUtils.isGenotypeCorrect;
import static org.molgenis.data.annotation.graduation.utils.GenotypeUtils.updateHomozygousAndHeterozygousValues;

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
import org.molgenis.data.annotation.graduation.model.Candidate;
import org.molgenis.data.annotation.graduation.model.Trio;
import org.molgenis.data.annotation.graduation.model.Candidate.InheritanceMode;
import org.molgenis.data.annotation.graduation.model.Variant;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class performs an inheritance analysis and estimates to which inheritance mode each variant belongs to. Patient
 * overviews and a matrix are printed.
 * 
 * @author mbijlsma
 */
@Component
public class InheritanceAnalysis
{
	private File familyAndChildSamplesFile;
	private File vcfFile;
	private File candidateOutputFile;
	private File matrixOutputFile;

	boolean printIds = false; // used in analyzeGene

	/**
	 * Reads and parses a PED file and adds family ID, child ID, father ID, and mother ID to a {@link Trio} list
	 * 
	 * @return trioList a list of {@link Trio}s
	 * @throws IOException
	 *             when familyAndChildSamplesFile is incorrect or cannot be parsed
	 */
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

			// if one of the members of the trio is missing
			if (child.equals("0") || father.equals("0") || mother.equals("0"))
			{
				continue;
			}
			else
			{
				trioList.add(new Trio(family, child, father, mother));
			}
		}
		scanner.close();
		return trioList;
	}

	/**
	 * Reads and parses a VCF file.
	 * 
	 * @throws Exception
	 *             when VCF file is incorrect or cannot be parsed
	 */
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

	/**
	 * Parses genes from annotation field of VCF and adds them to a list.
	 * 
	 * @param multiAnn
	 *            multi-annotation field
	 * @return genesSeenForNextVariant a list containing the genes from this annotation field(s)
	 */
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

	/**
	 * Adds all variants to the {@link Trio}s.
	 * 
	 * @param trioList
	 *            a list of {@link Trio}s
	 * @param record
	 *            {@link Entity} a line from the VCF file
	 * @param gene
	 *            the gene we are currently looking at
	 */
	private void addAllVariantsToTrio(List<Trio> trioList, Entity record, String gene)
	{
		// For every trio, add all variants to map and use gene as key
		for (Trio trio : trioList)
		{
			trio.addInfoToVariantMap(gene, record);
		}
	}

	/**
	 * For every gene, add the right samples to the right member of the {@link Trio}s.
	 * 
	 * @param sampleEntities
	 *            {@link Iterable} containing all sample {@link Entity}
	 * @param trioList
	 *            a list of {@link Trio}s
	 * @param gene
	 *            the gene we are currently looking at
	 */
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
	 * Analyzes gene and estimates which inheritance mode complies to a {@link Trio}.
	 * 
	 * @param trioList
	 *            a list of {@link Trio}s
	 * @param gene
	 *            the gene we are currently looking at
	 * @throws Exception
	 *             when candidateOutputFile or matrixOutputFile is incorrect
	 */
	public void analyzeGene(List<Trio> trioList, String gene) throws Exception
	{

		// Used for creating matrix
		Map<String, List<String>> geneFamilyCandidateCounts = newHashMap();

		for (Trio trio : trioList)
		{
			// for every trio, set counts on null
			int childAndFatherHet = 0;
			int childAndMotherHet = 0;

			// temporary list. If compound heterozygous, go to addCandidates function. Otherwise, empty list (next trio)
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

				// iterate over alternate alleles
				for (int i = 0; i < altsplit.length; i++)
				{
					Impact impact = getImpactForGeneAlleleCombo(variant.getString("ANN").split(",", -1));

					// if impact is not HIGH or MODERATE
					if (impact.equals(Impact.MODIFIER) || impact.equals(Impact.LOW))
					{
						continue;
					}

					InheritanceMode inheritanceMode = null;

					// because alt index = 0, add 1 (alternate alleles)
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

			// Only if compound heterozygous found, send temporary list to addCandidates
			// if both parents are at least heterozygous for one variant (not the same one) and child is
			// heterozygous for both variants on this gene

			if ((childAndFatherHet >= 1) && (childAndMotherHet >= 1))
			{
				for (Candidate candidate : temporaryCandidates)
				{
					trio.addCandidate(gene, candidate);
				}
			}

			addTrioToMatrix(gene, geneFamilyCandidateCounts, trio);

			// for this gene: delete samples & variants from trio
			trio.getVariants().remove(gene);
			trio.getSamplesChild().remove(gene);
			trio.getSamplesFather().remove(gene);
			trio.getSamplesMother().remove(gene);
		}
		printMatrix(geneFamilyCandidateCounts);
	}

	/**
	 * Sets all parameters for the {@link Candidate} Object.
	 * 
	 * @param candidate
	 *            {@link Candidate} the candidate variant we are currently looking at
	 * @param variant
	 *            {@link Variant} the variant we are currently looking at
	 */
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

	/**
	 * Adds variant counts per trio and gene to a matrix.
	 * 
	 * @param gene
	 *            the gene we are currently looking at
	 * @param geneFamilyCandidateCounts
	 *            list containing the number of variant candidates per gene
	 * @param trio
	 *            a list of {@link Trio}s
	 * @throws IOException
	 *             when candidateOutputFile is incorrect
	 */
	private void addTrioToMatrix(String gene, Map<String, List<String>> geneFamilyCandidateCounts, Trio trio)
			throws IOException
	{
		int size = 0;
		if (trio.getCandidatesForChildperGene().get(gene) != null)
		{
			size = trio.getCandidatesForChildperGene().get(gene).size();
			printCandidates(trio.getCandidatesForChildperGene().get(gene), trio, gene);
		}

		// if geneFamilyCandidateCounts does not contain gene, add gene together with family ID and size list to map,
		// otherwise get gene and add family ID and size list to map.
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

	/**
	 * Estimates the impact for each gene and allele and returns this {@link Impact}.
	 * 
	 * @param multiAnn
	 *            string array containing multi-annotation field
	 * @return impact the {@link Impact}
	 */
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

	/**
	 * Writes the {@link Candidate} Objects to a new file.
	 * 
	 * @param candidates
	 *            the {@link Candidate} we are currently looking at
	 * @param trio
	 *            a list of {@link Trio}s
	 * @param gene
	 *            the gene we are currently looking at
	 * @throws IOException
	 *             when candidateOutputFile is incorrect or cannot be written to
	 */
	private void printCandidates(List<Candidate> candidates, Trio trio, String gene) throws IOException
	{
		FileWriter fileWriter = new FileWriter(candidateOutputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		bufferedWriter.append("Family id: " + trio.getFamily_id() + "\n" + "Gene: " + gene + "\n"
				+ "Number of candidates: " + trio.getCandidatesForChildperGene().get(gene).size() + "\n");

		for (Candidate candidate : candidates)
		{
			bufferedWriter.append("\n" + candidate + "\n");
		}

		bufferedWriter.close();
	}

	/**
	 * Prints the matrix containing candidate variant counts per gene per {@link Trio}.
	 * 
	 * @param geneFamilyCandidateCounts
	 *            list containing the number of variant candidates per gene
	 * @throws IOException
	 *             when matrixOutputFile is incorrect or cannot be written to
	 */
	private void printMatrix(Map<String, List<String>> geneFamilyCandidateCounts) throws IOException
	{

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
			// print family IDs once!
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
			bufferedWriter.append("\n" + geneSymbol + "\t" + countsAsString);
		}
		bufferedWriter.close();
	}

	/**
	 * The main method, invokes parseCommandLineArgs() and readAndProcessVcfFile().
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when bean cannot be created or if vcfFile is incorrect or does not exist
	 */
	public static void main(String[] args) throws Exception
	{
		// context to get Spring working
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);

		InheritanceAnalysis inheritanceAnalysis = ctx.getBean(InheritanceAnalysis.class);
		inheritanceAnalysis.parseCommandLineArgs(args);
		inheritanceAnalysis.readAndProcessVcfFile();

		ctx.close();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when length of arguments is not 4, or if one of the files is incorrect or does not exist
	 */
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
