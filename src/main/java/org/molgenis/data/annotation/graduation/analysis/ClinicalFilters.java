package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ClinicalFilters
{
	File vcfFile;
	File patientGroups;
	File exacFile;

	HashMap<String, ArrayList<String>> patientgroupToGenes = newHashMap();
	private HashMap<String, String> sampleToGroup;

	private String gtcMessage = null;

	public void collectGenes() throws Exception
	{
		// Candidates, these should NOT overlap!!!
		// FIXME: this is "hardcoded", could be read from a file

		ArrayList<String> avmGenes = newArrayList(Arrays.asList(new String[]
		{ "NELFB", "PRKAG2", "JPH2", "MFAP4", "RP5-1086D14.6", "TAZ", "PRIM1", "IRX4", "IRAK1", "SNIP1 ", "B3GALT6",
				"WDR1", "GATA6", "GATA4", "GATA5", "LZTS2", "DDX42", "ZNF777", "CSRP3", "CHST3 ", "TCF19", "ACTA2",
				"DCHS1", "DTL", "PDS5A", "SLC30A5", "COL3A1", "TNNT2", "MYPN", "ZIC3", "ANKS6", "PGM5", "ALPK3",
				"TMEM123", "SNRNP200", "NCAPD2", "THSD1", "BLM", "NFASC", "COL4A2", "COL4A1", "LEFTY2 ", "CHST3",
				"RPS6KA3", "LRR1", "ADAMTSL1 ", "CBL", "ATAD2", "TTC37", "FLNA", "EMC1", "IGFBP7", "MYH11", "GANAB",
				"SNIP1", "GLA", "SMAD6", "SMAD3", "NPHP3", "PRKCSH", "AKAP12", "SLC4A2", "HTRA1", "EXOSC10", "NR2F2",
				"RAD51AP1", "RBM10", "ATOH8", "LAMA2", "LAMA4", "ARVCF", "GSG2", "RNase_MRP", "COL18A1", "NTN4",
				"CDC45", "TUBGCP2", "LMOD1", "CRTAP", "IFIH1", "DAB2", "SMAD4 ", "HAND2", "CAD", "CCNA2", "PEAR1",
				"ESYT1", "KIF4A", "SKA1", "GPR124", "CDCA4", "MYBPC3", "JAG1", "CNOT6", "HEATR6", "LTBP4", "LTBP3",
				"LTBP2", "FBN2", "FBN1", "ADAMTSL2 ", "NR2F2 ", "HGD", "NKX2-6", "NEXN", "E2F2", "E2F1", "DKK3",
				"CHRNG", "MLF1IP", "EEF2", "WDR76", "RPN2", "RPL26", "FAM58A", "PRPF8", "eNOS", "TNNC1", "FBLN2",
				"FBLN5", "ESCO2 ", "CTD-2510F5.4", "SKA3", "FOXF1", "LTBP3", "LTBP4", "PLXND1", "HMG20B", "CENPK",
				"FOG2", "CENPH", "CENPE", "LEFTY2", "CENPB", "CENPA", "LDB3", "TEX261", "NPHP3 ", "VWF ", "KIFC1",
				"FN1", "TRAIP", "ACTB", "TMEM43", "CCL2", "STARD13", "ADNP", "FUBP3", "CRELD1", "NCSTN", "MCM10",
				"TMEM194A", "HEATR2", "ADAMTSL2", "PARPBP", "EXO1", "TIMELESS", "LUM", "NID2", "TNS1", "TXNRD2",
				"UBP1", "BUB1", "BUB3", "SSC5D", "SEPN1", "SMYD1", "ZWILCH", "ACTB ", "GJA1", "RNASEH2A", "LEPRE1",
				"MLTK", "RACGAP1", "DTYMK", "GAS1", "SCN5A", "GAS6", "PDGFRB", "PDGFRA", "EFNB1", "CYP24A1 ", "RFC5",
				"NODAL", "KIAA1462", "RFC3", "RFC2", "KIF14", "KIF15", "TIGD5", "KIF11", "FANCD2", "BRIP1", "ZNF623",
				"NDC80", "INF2", "MYH6", "SLX4", "RRM2", "S1PR3", "PSEN2", "PSEN1", "ERCC6L", "TPM2", "TPM1", "FANCG",
				"FANCF", "FANCE", "TTC28", "FANCB", "FANCA", "FANCC", "FANCM", "FANCL", "FANCI", "TGFBR2", "TGFBR1",
				"KDELC2", "NUP107", "FIGNL1", "LAMP2", "UHRF1", "C11orf82", "HRT2 ", "CYP24A1", "CDK2", "RAD51",
				"NRG1", "POLA2", "TGFB2", "SVIL", "CEP57", "ERCC4", "ACVR2B", "LIMS2", "ECE1", "SORBS3", "TGF-β",
				"MEGF6", "SCARA3", "DHX9", "TNNI3", "ASF1B", "ZBTB2", "INTS1", "MATN2", "TMEM248", "ZBTB4", "VCL",
				"STIL", "VCP", "C1S", "ERRB4", "LMNA", "SEC61A1", "CHST14", "TOPBP1", "NOTCH1", "NOTCH2", "LSP1 ",
				"SCRIB", "TGFBI", "MMP2", "GNPTAB", "TBX20", "BGN", "MYL2", "MYL3", "B3GAT3", "TIMP3", "UFD1L", "CAV3",
				"AXL", "C12orf57", "DNMT1", "TRIM65", "VEGF", "SCN1B ", "TTN", "BRCA1", "BRCA2", "TRIM28", "AQP1",
				"POSTN", "FOXH1", "MYLK", "NCAPH", "NCAPG", "PKP2", "ERBB2", "EMD", "DMD", "ERBB4", "HSPG2",
				"TMEM185B", "SEC16A", "GLB1 ", "SKI", "SEPT2", "KANSL1", "SRSF6", "SRSF1", "FRAS1 ", "MAML1", "JUP",
				"TMEM70", "GSN", "SURF4", "B4GALT7", "MCMBP", "KIAA0196", "SGOL1", "SGOL2", "PTPN9", "DSP", "NOS3",
				"LMNB2", "GINS3", "WBP1LP2", "NAA10", "PALB2", "ANKS6 ", "BAG3", "ACTC1", "DSN1", "MSRB3", "ADAMTS10",
				"TBX1", "FBXO5", "VEGFC ", "EHMT1", "NUSAP1", "MASTL", "SLC2A10 ", "FHL1", "DSG2", "UGGT1", "THRAP2",
				"COMT", "LMNB1", "RAD51D", "RAD51C", "TCAP", "LRP1", "SHCBP1", "LTBP2 ", "COL1A2", "PLN", "CALR3",
				"MOGS", "CFC1", "LMF2", "TMPO", "MMP14", "KIAA1524", "ZMYND11", "DCLRE1B", "ALDH1A2", "MYBL2", "CENPI",
				"GDF1", "TFAP2B", "TLN1", "GLT25D1", "EYA4", "FZD1", "PCNA", "KIAA0196 ", "LOXL1", "SUV39H1", "PIEZO1",
				"OSR1", "MRGPRF", "MCM7", "MCM6", "MCM5", "MCM3", "MCM2", "EDNRA", "VWF", "TAGLN", "FLNC", "INPPL1",
				"SHC4 ", "HGD ", "TENC1", "ABCC9", "COL6A1", "COL6A3", "COL6A2", "ABCC1", "ILF3", "PARG", "FBN1 ",
				"TIMP1", "PLK4", "AKT1", "D2HGDH", "SLC2A4RG", "RBM15B", "NKX2-5", "MRC2", "CCDC102A", "RYR2", "DSC2",
				"FSTL1", "GNB2L1", "SMAD4", "PDE7B", "LIG1", "NFATc", "PTBP1", "SOD2", "PRRC2B", "ZNF367", "BCOR ",
				"SLC2A10", "ATP2B4", "BMP1", "MBTPS1", "CCDC80", "IDUA ", "FAT4", "NCAPG2", "AFAP1L1", "ZWINT", "DES",
				"AC068134.10", "H6PD", "COPA", "FEN1", "CHD4", "GBA ", "MYH9", "GNPTG", "THBS1", "COL27A1", "TGFB1I1",
				"IFT122", "POFUT1", "MMP9 ", "SMC1A", "CDC25B", "ZCCHC24", "RNF40", "RHOJ", "PLEC", "RP11-381E24.1",
				"STT3B", "GATAD1", "OLFML2A", "TAF2", "FIBIN", "RBM20", "IPO9", "EHD2", "ACE", "SORCS2", "PXDC1",
				"ABL1", "FHL2", "FZD2", "OGFOD3", "SGCD", "GCN1L1", "GP1BB", "POLD1", "CITED2", "PLEKHG2", "ELN",
				"RBL1", "MYH7", "RCC1", "DAPK3 ", "PDSS1", "SYDE1", "STON1", "BCOR", "DTNA", "HEY2", "IDUA", "HIRA",
				"TACC3", "GLB1", "CTGF", "ARHGAP11A", "EMP1", "ACVR1", "CRYAB", "DDX58 ", "TDGF1", "RRM1", "CSTF2",
				"ADAMTS10 ", "ANKRD1", "HCFC1", "ESCO2", "Edn1", "PDGFC", "TRAF7", "PDIA4", "ORC1", "PTRF", "GNPTG ",
				"ACTN2", "BMP4", "ACTN4", "TAB2", "BBS2", "UNC45A", "ASXL1", "BRAF", "MYOZ2", "MYOZ1", "TAB1",
				"MAP3K7", "TAK1" }));

		this.patientgroupToGenes.put("avm", avmGenes);
	}

	public void readPatientGroups() throws FileNotFoundException
	{
		Scanner s = new Scanner(patientGroups);
		String line = null;

		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);
			sampleToGroup.put(lineSplit[0], lineSplit[1]);
		}
		s.close();
	}

	public void readVcf() throws Exception
	{
		VcfRepository vcf = new VcfRepository(vcfFile, "vcf");
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator exacAnnotator = annotators.get("exac");

		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(exacFile.getAbsolutePath());

		Iterator<Entity> it = exacAnnotator.annotate(vcf);

		while (it.hasNext())
		{
			Entity record = it.next();
			String filter = record.getString("FILTER");

			if (!filter.equals("PASS"))
			{
				continue;
			}

			String chr = record.getString("#CHROM");
			String pos = record.getString("POS");
			String ref = record.getString("REF");
			String altStr = record.getString("ALT");
			
			String exac_af_STR = record.get("EXAC_AF") == null ? null : record.get("EXAC_AF").toString();
			String exac_ac_hom_STR = record.get("EXAC_AC_HOM") == null ? null : record.get("EXAC_AC_HOM").toString();
			String exac_ac_het_STR = record.get("EXAC_AC_HET") == null ? null : record.get("EXAC_AC_HET").toString();
			String[] multiAnn = record.getString("ANN").split(",");

			String[] altsplit = altStr.split(",", -1);

			String[] exac_af_split = new String[altsplit.length];
			if (exac_af_STR != null)
			{
				exac_af_split = exac_af_STR.split(",", -1);
			}

			String[] exac_ac_hom_split = new String[altsplit.length];
			if (exac_ac_hom_STR != null)
			{
				exac_ac_hom_split = exac_ac_hom_STR.split(",", -1);
			}

			String[] exac_ac_het_split = new String[altsplit.length];
			if (exac_ac_het_STR != null)
			{
				exac_ac_het_split = exac_ac_het_STR.split(",", -1);
			}

			/**
			 * Iterate over alternatives, if applicable multi allelic example: 1:1148100-1148100
			 */
			for (int i = 0; i < altsplit.length; i++)
			{
				String alt = altsplit[i];

				if (exac_af_STR != null && !exac_af_split[i].equals("."))
				{
					Double exac_af = Double.parseDouble(exac_af_split[i]);
					if (exac_af > 0.05)
					{
						continue;
					}
				}

				String ann = multiAnn[i];
				String[] annSplit = ann.split("\\|", -1);
				String impact = annSplit[2];
				if (impact.equals("MODIFIER") || impact.equals("LOW"))
				{
					continue;
				}

				String effect = annSplit[1];

				String cDNA = annSplit[9];
				String aaChange = annSplit[10];

				String gene = annSplit[3];
				if (gene.isEmpty())
				{
					throw new Exception("reminder: gene can be empty??");
				}

				// check if we're looking at a gene that is part of one of the candidate lists, if not, skip it

				String candidateGeneGroup = null;
				for (String patientGroup : patientgroupToGenes.keySet())
				{
					ArrayList<String> patientGroupGenes = patientgroupToGenes.get(patientGroup);
					if (patientGroupGenes.stream().anyMatch(geneName -> geneName.equalsIgnoreCase(gene)))
					{
						candidateGeneGroup = patientGroup;
						break;
					}
				}
				if (candidateGeneGroup == null)
				{
					continue;
				}

				// TODO: add CADD scores

				int[] patient_GTC = countGTC(record, i, candidateGeneGroup);
				if (patient_GTC == null)
				{
					continue;
				}

				String ExAC_AF = exac_af_split[i] == null ? "0" : exac_af_split[i];
				int ExAC_AC_HOM = exac_ac_hom_split[i] == null ? 0 : Integer.parseInt(exac_ac_hom_split[i]);
				int ExAC_AC_HET = exac_ac_het_split[i] == null ? 0 : Integer.parseInt(exac_ac_het_split[i]);

				// if not actually seen in patients, skip it...
				if (patient_GTC[1] == 0 && patient_GTC[2] == 0)
				{
					continue;
				}

				// if the number of people in exac with het or homalt exceed our patients with this variant.. it's hard
				// to believe it :-\ exac has late-onset, common diseases, see: http://exac.broadinstitute.org/about
				// if (ExAC_AC_HET > patient_GTC[1] || ExAC_AC_HOM > patient_GTC[2])
				if (ExAC_AC_HET > 100 || ExAC_AC_HOM > 100) // changed from 10 to 100
				{
					continue;
				}

				String variantInfo = chr + ":" + pos + "-" + pos + ", " + ref + "/" + alt + ", " + cDNA + ", "
						+ aaChange + ", " + gene + ", effect: " + effect + ", impact: " + impact
						+ ", ExAC [allelefreq=" + ExAC_AF + ", hets=" + ExAC_AC_HET + ", homalts=" + ExAC_AC_HOM
						+ "], patients [homrefs=" + patient_GTC[0] + ", hets=" + patient_GTC[1] + ", homalts="
						+ patient_GTC[2] + "], controls [homrefs=" + patient_GTC[3] + ", hets=" + patient_GTC[4]
						+ ", homalts=" + patient_GTC[5] + "], details: [" + this.gtcMessage + "]";

				// System.out.println(candidateGeneGroup + " candidate: " + variantInfo);
				System.out.println(variantInfo);
			}

		}
	}

	public int[] countGTC(Entity record, int altIndex, String candidateGeneGroup)
	{
		this.gtcMessage = "";

		// because alt index = 0 for the first alt, we add 1
		altIndex = altIndex + 1;

		// for a particular ref-alt combination:
		// [homref, het, homalt]
		// can only do for ref/alt-index combinations, so e.g. 0/0, 0|2 or 2/2. print warning on 1/2, 3|2, etc.
		// warn if this happens
		// also count for "other people" in [3][4] and [5] as control reference
		int[] gtc = new int[]
		{ 0, 0, 0, 0, 0, 0 };

		Iterable<Entity> sampleEntities = record.getEntities(VcfRepository.SAMPLES);
		for (Entity sample : sampleEntities)
		{
			String sampleName = sample.get("ORIGINAL_NAME").toString();

			// GTC count applies to the 'disease gene panel' of the patient disease group
			boolean controls = true;
			if (candidateGeneGroup.equals(sampleToGroup.get(sampleName)))
			{
				controls = false;
			}

			// System.out.println("patientGroup="+candidateGeneGroup+"sampleToGroup.get(sample.get(\"ORIGINAL_NAME\").toString())="+sampleToGroup.get(sample.get("ORIGINAL_NAME").toString()));

			String genotype = sample.get("GT").toString();

			if (genotype.equals("./."))
			{
				continue;
			}

			// quality filter: we want depth X or more
			int depthOfCoverage = Integer.parseInt(sample.get("DP").toString());
			if (depthOfCoverage < 10)
			{
				continue;
			}

			if (genotype.equals("0/0") || genotype.equals("0|0"))
			{
				if (controls)
				{
					gtc[3]++;
				}
				else
				{
					gtc[0]++;
				}
			}
			else if (genotype.equals("0/" + altIndex) || genotype.equals(altIndex + "/0")
					|| genotype.equals("0|" + altIndex) || genotype.equals(altIndex + "|0"))
			{
				if (controls)
				{
					gtc[4]++;
					this.gtcMessage += "ctrlhet:" + sampleName + ",dp:" + depthOfCoverage + ",gt:" + genotype + " ";
				}
				else
				{
					gtc[1]++;
					this.gtcMessage += "pathet:" + sampleName + ",dp:" + depthOfCoverage + ",gt:" + genotype + " ";
				}

			}
			else if (genotype.equals(altIndex + "/" + altIndex) || genotype.equals(altIndex + "|" + altIndex))
			{
				if (controls)
				{
					gtc[5]++;
					this.gtcMessage += "ctrlhom:" + sampleName + ",dp:" + depthOfCoverage + ",gt:" + genotype + " ";
				}
				else
				{
					gtc[2]++;
					this.gtcMessage += "pathom:" + sampleName + ",dp:" + depthOfCoverage + ",gt:" + genotype + " ";
				}

			}
			else if (genotype.contains(altIndex + ""))
			{
				System.out.println("WARNING: genotype " + genotype + " not counted for altindex " + altIndex + " for "
						+ record.getString("#CHROM") + ":" + record.getString("POS")
						+ " because it's not a ref-alt combination!");
			}
		}

		return gtc;
	}

	public static void main(String[] args) throws Exception
	{
		// See http://stackoverflow.com/questions/4787719/spring-console-application-configured-using-annotations
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);
		ClinicalFilters clinicalFilters = ctx.getBean(ClinicalFilters.class);
		clinicalFilters.parseCommandLineArgs(args);
		clinicalFilters.collectGenes();
		clinicalFilters.readPatientGroups();
		clinicalFilters.readVcf();
		ctx.close();
	}

	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}

		File patientGroups = new File(args[1]);
		if (!patientGroups.isFile())
		{
			throw new Exception("Patient groups file does not exist or is not a directory: " + patientGroups.getAbsolutePath());
		}

		File exacFile = new File(args[2]);
		if (!exacFile.isFile())
		{
			throw new Exception("Exac file does not exists or is not a directory: " + exacFile);
		}
	}
}