package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.LOW;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.MODIFIER;
import static org.molgenis.data.annotation.graduation.utils.AnnotatorUtils.annotateWithExac;
import static org.molgenis.data.annotation.graduation.utils.AnnotatorUtils.registerCommandLineAnnotator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;
import org.molgenis.data.annotation.graduation.model.Variant;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class filters a VCF file according to multiple clinical filters and prints the remaining variants with
 * associated information.
 * 
 * @author mbijlsma
 */
@Component
public class ClinicalFilters
{
	private static final String CANDIDATE_GENE_GROUP = "avm"; // aortic valve malformation

	private File vcfFile;
	private File patientGroups;
	private Map<String, String> samplePatientGroupMap = newHashMap();
	private String gtcMessage = null;

	// genes involved in AVM
	private ArrayList<String> avmGenes = newArrayList(Arrays.asList(new String[]
	{ "NELFB", "PRKAG2", "JPH2", "MFAP4", "RP5-1086D14.6", "TAZ", "PRIM1", "IRX4", "IRAK1", "SNIP1 ", "B3GALT6",
			"WDR1", "GATA6", "GATA4", "GATA5", "LZTS2", "DDX42", "ZNF777", "CSRP3", "CHST3 ", "TCF19", "ACTA2",
			"DCHS1", "DTL", "PDS5A", "SLC30A5", "COL3A1", "TNNT2", "MYPN", "ZIC3", "ANKS6", "PGM5", "ALPK3", "TMEM123",
			"SNRNP200", "NCAPD2", "THSD1", "BLM", "NFASC", "COL4A2", "COL4A1", "LEFTY2 ", "CHST3", "RPS6KA3", "LRR1",
			"ADAMTSL1 ", "CBL", "ATAD2", "TTC37", "FLNA", "EMC1", "IGFBP7", "MYH11", "GANAB", "SNIP1", "GLA", "SMAD6",
			"SMAD3", "NPHP3", "PRKCSH", "AKAP12", "SLC4A2", "HTRA1", "EXOSC10", "NR2F2", "RAD51AP1", "RBM10", "ATOH8",
			"LAMA2", "LAMA4", "ARVCF", "GSG2", "RNase_MRP", "COL18A1", "NTN4", "CDC45", "TUBGCP2", "LMOD1", "CRTAP",
			"IFIH1", "DAB2", "SMAD4 ", "HAND2", "CAD", "CCNA2", "PEAR1", "ESYT1", "KIF4A", "SKA1", "GPR124", "CDCA4",
			"MYBPC3", "JAG1", "CNOT6", "HEATR6", "LTBP4", "LTBP3", "LTBP2", "FBN2", "FBN1", "ADAMTSL2 ", "NR2F2 ",
			"HGD", "NKX2-6", "NEXN", "E2F2", "E2F1", "DKK3", "CHRNG", "MLF1IP", "EEF2", "WDR76", "RPN2", "RPL26",
			"FAM58A", "PRPF8", "eNOS", "TNNC1", "FBLN2", "FBLN5", "ESCO2 ", "CTD-2510F5.4", "SKA3", "FOXF1", "LTBP3",
			"LTBP4", "PLXND1", "HMG20B", "CENPK", "FOG2", "CENPH", "CENPE", "LEFTY2", "CENPB", "CENPA", "LDB3",
			"TEX261", "NPHP3 ", "VWF ", "KIFC1", "FN1", "TRAIP", "ACTB", "TMEM43", "CCL2", "STARD13", "ADNP", "FUBP3",
			"CRELD1", "NCSTN", "MCM10", "TMEM194A", "HEATR2", "ADAMTSL2", "PARPBP", "EXO1", "TIMELESS", "LUM", "NID2",
			"TNS1", "TXNRD2", "UBP1", "BUB1", "BUB3", "SSC5D", "SEPN1", "SMYD1", "ZWILCH", "ACTB ", "GJA1", "RNASEH2A",
			"LEPRE1", "MLTK", "RACGAP1", "DTYMK", "GAS1", "SCN5A", "GAS6", "PDGFRB", "PDGFRA", "EFNB1", "CYP24A1 ",
			"RFC5", "NODAL", "KIAA1462", "RFC3", "RFC2", "KIF14", "KIF15", "TIGD5", "KIF11", "FANCD2", "BRIP1",
			"ZNF623", "NDC80", "INF2", "MYH6", "SLX4", "RRM2", "S1PR3", "PSEN2", "PSEN1", "ERCC6L", "TPM2", "TPM1",
			"FANCG", "FANCF", "FANCE", "TTC28", "FANCB", "FANCA", "FANCC", "FANCM", "FANCL", "FANCI", "TGFBR2",
			"TGFBR1", "KDELC2", "NUP107", "FIGNL1", "LAMP2", "UHRF1", "C11orf82", "HRT2 ", "CYP24A1", "CDK2", "RAD51",
			"NRG1", "POLA2", "TGFB2", "SVIL", "CEP57", "ERCC4", "ACVR2B", "LIMS2", "ECE1", "SORBS3", "TGF-β", "MEGF6",
			"SCARA3", "DHX9", "TNNI3", "ASF1B", "ZBTB2", "INTS1", "MATN2", "TMEM248", "ZBTB4", "VCL", "STIL", "VCP",
			"C1S", "ERRB4", "LMNA", "SEC61A1", "CHST14", "TOPBP1", "NOTCH1", "NOTCH2", "LSP1 ", "SCRIB", "TGFBI",
			"MMP2", "GNPTAB", "TBX20", "BGN", "MYL2", "MYL3", "B3GAT3", "TIMP3", "UFD1L", "CAV3", "AXL", "C12orf57",
			"DNMT1", "TRIM65", "VEGF", "SCN1B ", "TTN", "BRCA1", "BRCA2", "TRIM28", "AQP1", "POSTN", "FOXH1", "MYLK",
			"NCAPH", "NCAPG", "PKP2", "ERBB2", "EMD", "DMD", "ERBB4", "HSPG2", "TMEM185B", "SEC16A", "GLB1 ", "SKI",
			"SEPT2", "KANSL1", "SRSF6", "SRSF1", "FRAS1 ", "MAML1", "JUP", "TMEM70", "GSN", "SURF4", "B4GALT7",
			"MCMBP", "KIAA0196", "SGOL1", "SGOL2", "PTPN9", "DSP", "NOS3", "LMNB2", "GINS3", "WBP1LP2", "NAA10",
			"PALB2", "ANKS6 ", "BAG3", "ACTC1", "DSN1", "MSRB3", "ADAMTS10", "TBX1", "FBXO5", "VEGFC ", "EHMT1",
			"NUSAP1", "MASTL", "SLC2A10 ", "FHL1", "DSG2", "UGGT1", "THRAP2", "COMT", "LMNB1", "RAD51D", "RAD51C",
			"TCAP", "LRP1", "SHCBP1", "LTBP2 ", "COL1A2", "PLN", "CALR3", "MOGS", "CFC1", "LMF2", "TMPO", "MMP14",
			"KIAA1524", "ZMYND11", "DCLRE1B", "ALDH1A2", "MYBL2", "CENPI", "GDF1", "TFAP2B", "TLN1", "GLT25D1", "EYA4",
			"FZD1", "PCNA", "KIAA0196 ", "LOXL1", "SUV39H1", "PIEZO1", "OSR1", "MRGPRF", "MCM7", "MCM6", "MCM5",
			"MCM3", "MCM2", "EDNRA", "VWF", "TAGLN", "FLNC", "INPPL1", "SHC4 ", "HGD ", "TENC1", "ABCC9", "COL6A1",
			"COL6A3", "COL6A2", "ABCC1", "ILF3", "PARG", "FBN1 ", "TIMP1", "PLK4", "AKT1", "D2HGDH", "SLC2A4RG",
			"RBM15B", "NKX2-5", "MRC2", "CCDC102A", "RYR2", "DSC2", "FSTL1", "GNB2L1", "SMAD4", "PDE7B", "LIG1",
			"NFATc", "PTBP1", "SOD2", "PRRC2B", "ZNF367", "BCOR ", "SLC2A10", "ATP2B4", "BMP1", "MBTPS1", "CCDC80",
			"IDUA ", "FAT4", "NCAPG2", "AFAP1L1", "ZWINT", "DES", "AC068134.10", "H6PD", "COPA", "FEN1", "CHD4",
			"GBA ", "MYH9", "GNPTG", "THBS1", "COL27A1", "TGFB1I1", "IFT122", "POFUT1", "MMP9 ", "SMC1A", "CDC25B",
			"ZCCHC24", "RNF40", "RHOJ", "PLEC", "RP11-381E24.1", "STT3B", "GATAD1", "OLFML2A", "TAF2", "FIBIN",
			"RBM20", "IPO9", "EHD2", "ACE", "SORCS2", "PXDC1", "ABL1", "FHL2", "FZD2", "OGFOD3", "SGCD", "GCN1L1",
			"GP1BB", "POLD1", "CITED2", "PLEKHG2", "ELN", "RBL1", "MYH7", "RCC1", "DAPK3 ", "PDSS1", "SYDE1", "STON1",
			"BCOR", "DTNA", "HEY2", "IDUA", "HIRA", "TACC3", "GLB1", "CTGF", "ARHGAP11A", "EMP1", "ACVR1", "CRYAB",
			"DDX58 ", "TDGF1", "RRM1", "CSTF2", "ADAMTS10 ", "ANKRD1", "HCFC1", "ESCO2", "Edn1", "PDGFC", "TRAF7",
			"PDIA4", "ORC1", "PTRF", "GNPTG ", "ACTN2", "BMP4", "ACTN4", "TAB2", "BBS2", "UNC45A", "ASXL1", "BRAF",
			"MYOZ2", "MYOZ1", "TAB1", "MAP3K7", "TAK1" }));

	/**
	 * Reads and parses a patientGroups file and adds sample ID of the patient and candidate gene group to a map.
	 * 
	 * @throws FileNotFoundException
	 *             when patientGroups does not exist
	 */
	private void readPatientGroups() throws FileNotFoundException
	{
		Scanner scanner = new Scanner(patientGroups);
		String line = null;

		while (scanner.hasNextLine())
		{
			line = scanner.nextLine();
			String[] lineSplit = line.split("\t", -1);
			samplePatientGroupMap.put(lineSplit[0], lineSplit[1]);
		}
		scanner.close();
	}

	/**
	 * Reads and parses a VCF file.
	 * 
	 * @throws Exception
	 *             when VCF file is incorrect or does not exist
	 */
	private void readVcf() throws Exception
	{
		VcfRepository vcfRepository = new VcfRepository(vcfFile, "vcf");
		Iterator<Entity> vcfWithExacIterator = annotateWithExac(vcfRepository);

		while (vcfWithExacIterator.hasNext())
		{
			Entity record = vcfWithExacIterator.next();
			String filter = record.getString(VcfRepository.FILTER);

			if (filter.equals("PASS"))
			{
				Variant variant = new Variant(record.getString(VcfRepository.CHROM),
						record.getString(VcfRepository.POS), record.getString(VcfRepository.REF), record.getString(
								VcfRepository.ALT).split(",", -1));
				analyzeVariant(record, variant);
			}
		}
	}

	/**
	 * Analyzes a variant and prints it if it meets the specific conditions.
	 * 
	 * @param record
	 *            {@link Entity} containing one line of the VCF file
	 * @param variant
	 *            {@link Variant} the variant we are currently looking at
	 */
	private void analyzeVariant(Entity record, Variant variant)
	{
		String[] exacAlleleFrequencies = record.get("EXAC_AF") == null ? null : record.getString("EXAC_AF").split(",",
				-1);

		String[] exacHomozygousAlleleCounts = record.get("EXAC_AC_HOM") == null ? null : record
				.getString("EXAC_AC_HOM").split(",", -1);

		String[] exacHeterozygousAlleleCounts = record.get("EXAC_AC_HET") == null ? null : record.getString(
				"EXAC_AC_HET").split(",", -1);

		variant.setMultiAnnotationField(record.getString("ANN").split(",", -1));

		// Iterate over alternate alleles
		for (int i = 0; i < variant.getAlternateAlleles().length; i++)
		{
			String alternateAllele = variant.getAlternateAlleles()[i];

			// if ExAC allele frequencies are known
			if (exacAlleleFrequencies != null && !exacAlleleFrequencies[i].equals("."))
			{
				// if ExAC allele frequencies are below 0.05
				Double exacAlleleFrequency = Double.parseDouble(exacAlleleFrequencies[i]);
				if (exacAlleleFrequency <= 0.05)
				{
					// if impact is HIGH or MODERATE
					Impact impact = variant.getImpact(i);
					if (!(impact.equals(MODIFIER) || impact.equals(LOW)))
					{
						String gene = variant.getGene(i);

						// check if we're looking at a gene that is part of one of the candidate lists, if not -> skip
						if (avmGenes.contains(gene))
						{
							int[] patient_GTC = countGenotypes(record, i, CANDIDATE_GENE_GROUP);
							if (!(patient_GTC == null))
							{
								// if not actually seen in patients -> skip
								if (!(patient_GTC[1] == 0 && patient_GTC[2] == 0))
								{
									int exacHomozygousAlleleCount = exacHomozygousAlleleCounts[i] == null ? 0 : Integer
											.parseInt(exacHomozygousAlleleCounts[i]);

									int exacHeterozygousAlleleCount = exacHeterozygousAlleleCounts[i] == null ? 0 : Integer
											.parseInt(exacHeterozygousAlleleCounts[i]);

									// if number of times variant is seen in ExAC is below 100
									if (!(exacHeterozygousAlleleCount > 100 || exacHomozygousAlleleCount > 100))
									{
										System.out.println("Location: " + variant.getChromosome() + ":"
												+ variant.getPosition() + "-" + variant.getPosition() + "\nGenotype: "
												+ variant.getReferenceAllele() + "/" + alternateAllele
												+ "\nGenetic information: " + variant.getCDNA(i) + ", "
												+ variant.getAminoAcidChange(i) + ", " + gene + "\nEffect: "
												+ variant.getEffect(i) + "\nImpact: " + impact + "\nExAC [allelefreq="
												+ exacAlleleFrequency + ", hets=" + exacHeterozygousAlleleCount
												+ ", homalts=" + exacHomozygousAlleleCount + "], patients [homrefs="
												+ patient_GTC[0] + ", hets=" + patient_GTC[1] + ", homalts="
												+ patient_GTC[2] + "], controls [homrefs=" + patient_GTC[3] + ", hets="
												+ patient_GTC[4] + ", homalts=" + patient_GTC[5] + "], details: ["
												+ this.gtcMessage + "]");
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Counts the number of times a particular genotype is found in the patients and control groups.
	 * 
	 * @param record
	 *            {@link Entity} containing one line of the VCF file
	 * @param altIndex
	 *            the alternate allele we are currently looking at
	 * @param candidateGeneGroup
	 *            the group we are currently looking at
	 * @return genotypeCounts genotype counts for patients + control group
	 */
	public int[] countGenotypes(Entity record, int altIndex, String candidateGeneGroup)
	{
		this.gtcMessage = "";

		// because alt index = 0, add 1 (alternate allele)
		altIndex = altIndex + 1;

		int[] genotypeCounts = new int[]
		{ 0, 0, 0, 0, 0, 0 };

		for (Entity sample : record.getEntities(VcfRepository.SAMPLES))
		{
			String sampleName = sample.getString("ORIGINAL_NAME");

			boolean controls = true;

			// if patient, control is false
			if (candidateGeneGroup.equals(samplePatientGroupMap.get(sampleName)))
			{
				controls = false;
			}

			String genotype = sample.get("GT").toString();

			// skip missing genotypes
			if (genotype.equals("./."))
			{
				continue;
			}

			int depthOfCoverage = Integer.parseInt(sample.get("DP").toString());

			// if depth above 10
			if (depthOfCoverage >= 10)
			{
				// if genotype equals reference
				if (genotype.equals("0/0") || genotype.equals("0|0"))
				{
					if (controls)
					{
						genotypeCounts[3]++;
					}
					else
					{
						genotypeCounts[0]++;
					}
				}

				// if genotype equals heterozygous
				else if (genotype.equals("0/" + altIndex) || genotype.equals(altIndex + "/0")
						|| genotype.equals("0|" + altIndex) || genotype.equals(altIndex + "|0"))
				{
					if (controls)
					{
						genotypeCounts[4]++;
						this.gtcMessage += "ctrlhet:" + sampleName + ",dp:" + depthOfCoverage + ",gt:" + genotype + " ";
					}
					else
					{
						genotypeCounts[1]++;
						this.gtcMessage += "pathet:" + sampleName + ",dp:" + depthOfCoverage + ",gt:" + genotype + " ";
					}

				}

				// if genotype equals homozygous
				else if (genotype.equals(altIndex + "/" + altIndex) || genotype.equals(altIndex + "|" + altIndex))
				{
					if (controls)
					{
						genotypeCounts[5]++;
						this.gtcMessage += "ctrlhom:" + sampleName + ",dp:" + depthOfCoverage + ",gt:" + genotype + " ";
					}
					else
					{
						genotypeCounts[2]++;
						this.gtcMessage += "pathom:" + sampleName + ",dp:" + depthOfCoverage + ",gt:" + genotype + " ";
					}

				}

				// Peforms only ref-alt ombinations (e.g. 0/0, 0|2 or 2/2), warning on 1/2, 3|2, etc.
				else if (genotype.contains(altIndex + ""))
				{
					System.out.println("WARNING: genotype " + genotype + " not counted for altindex " + altIndex
							+ " for " + record.getString("#CHROM") + ":" + record.getString("POS")
							+ " because it's not a ref-alt combination!");
				}
			}
		}

		return genotypeCounts;
	}

	/**
	 * The main method, invokes parseCommandLineArgs(), readPatientGroups(), and readVcf().
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when bean cannot be created or if one of the files is incorrect or does not exist
	 */
	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext context = registerCommandLineAnnotator();
		ClinicalFilters clinicalFilters = context.getBean(ClinicalFilters.class);

		clinicalFilters.parseCommandLineArgs(args);
		clinicalFilters.readPatientGroups();
		clinicalFilters.readVcf();

		context.close();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when length of arguments is not 2, or if one of the files is incorrect or does not exist
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}

		patientGroups = new File(args[1]);
		if (!patientGroups.isFile())
		{
			throw new Exception("Patient groups file does not exist or is not a directory: "
					+ patientGroups.getAbsolutePath());
		}
	}
}