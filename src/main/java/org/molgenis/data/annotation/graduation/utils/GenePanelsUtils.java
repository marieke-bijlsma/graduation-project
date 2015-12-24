package org.molgenis.data.annotation.graduation.utils;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains multiple gene lists that are invoked by the MergePBTwithVCF class.
 * 
 * @author mbijlsma
 */
public class GenePanelsUtils
{
	public static List<String> hpoGenes = Arrays.asList(new String[]
	{ "ABCC9", "ACTB", "ADAMTS10", "ADAMTSL2", "ARVCF", "B3GALT6", "B3GAT3", "B4GALT7", "BBS2", "BRAF", "BRCA2",
			"BRIP1", "CBL", "CHRNG", "COMT", "EHMT1", "ELN", "ERCC4", "FAM58A", "FANCA", "FANCB", "FANCC", "FANCD2",
			"FANCE", "FANCF", "FANCG", "FANCI", "FANCL", "FANCM", "FBLN5", "FBN1", "FBN2", "FLNA", "FOXF1", "GBA",
			"GLA", "GNPTAB", "GNPTG", "GP1BB", "HGD", "HIRA", "IDUA", "IFT122", "KANSL1", "LTBP2", "MYH11", "MYH7",
			"NAA10", "NOTCH1", "NOTCH2", "PALB2", "RAD51C", "RPL26", "RPS6KA3", "SKI", "SLX4", "SMAD6", "SNIP1",
			"TAB2", "TAF2", "TBX1", "TGFB2", "TGFBR1", "TGFBR2", "TMEM70", "UFD1L" });

	public static List<String> pred38 = Arrays.asList(new String[]
	{ "RBL1", "ZNF367", "MCM2", "MCM6", "FIGNL1", "TIMELESS", "POLD1", "WDR76", "RP5-1086D14.6", "CDCA4", "POLA2",
			"C11orf82", "MCM5", "TMPO", "CDC45", "CSTF2", "ZWILCH", "FEN1", "CENPI", "NCAPG2", "UHRF1", "MCM3",
			"FBXO5", "RRM2", "PRIM1", "TACC3", "RNASEH2A", "PARPBP", "MYBL2", "RFC3", "BLM", "NCAPH", "MCM7", "TRAIP",
			"NUP107", "ZWINT", "GSG2", "LRR1", "SHCBP1", "MCM10", "RAD51AP1", "ORC1", "ASF1B", "KIFC1", "DNMT1",
			"BUB3", "KIF11", "NDC80", "KIF15", "LIG1", "RRM1", "SUV39H1", "DCLRE1B", "BRCA1", "CENPK", "ARHGAP11A",
			"DSN1", "KIF14", "ATAD2", "NCAPD2", "RCC1", "LMNB1", "DTYMK", "TCF19", "RAD51D", "TMEM194A", "SRSF1",
			"E2F2", "SGOL1", "NCAPG", "E2F1", "PCNA", "RP11-381E24.1", "TOPBP1", "EXO1", "RFC5", "NUSAP1", "MASTL",
			"DTL", "BUB1", "RAD51", "CCNA2", "KIAA1524", "SKA3", "CTD-2510F5.4", "RFC2", "GINS3", "PARG", "PLK4",
			"CENPE", "RACGAP1", "CDK2", "SGOL2", "CENPH", "STIL", "MLF1IP", "SKA1", "ERCC6L", "KIF4A", "CENPA",
			"B4GALT7", "UFD1L", "ACTB", "TAF2", "PALB2", "NAA10", "HIRA", "RAD51C", "CBL", "FANCL", "FANCE", "GLA",
			"BRIP1", "BRCA2", "FANCD2", "FAM58A", "B3GAT3", "PDSS1", "FANCI", "BRAF", "RPL26", "FANCC", "SNIP1",
			"KIAA0196", "CEP57", "ESCO2", "TMEM70", "ERCC4", "D2HGDH", "FANCB", "B3GALT6", "EHMT1", "FANCF", "FANCA",
			"FANCM", "SLX4", "TTC37", "FANCG" });

	public static List<String> predAll82 = Arrays.asList(new String[]
	{ "GANAB", "CCDC102A", "CHST14", "ABL1", "SEC61A1", "TRIM28", "POFUT1", "PRPF8", "FZD2", "RPN2", "SNRNP200",
			"INPPL1", "GLT25D1", "FUBP3", "PTPN9", "CNOT6", "PTBP1", "VCP", "PRKCSH", "SCRIB", "CDC25B", "RBM10",
			"DHX9", "SLC2A4RG", "MAML1", "SEC16A", "SEPT2", "COPA", "TLN1", "HCFC1", "TMEM248", "STT3B", "HEATR2",
			"OGFOD3", "SLC30A5", "RNF40", "ADNP", "SRSF6", "IPO9", "RBM15B", "UBP1", "LMNB2", "LMF2", "SURF4", "RBL1",
			"PDS5A", "ASXL1", "POLD1", "RNase_MRP", "ZNF777", "EXOSC10", "DDX42", "GNB2L1", "MBTPS1", "CAD", "AKT1",
			"KDELC2", "TIGD5", "PLEKHG2", "WDR1", "GCN1L1", "ZNF623", "MCMBP", "ABCC1", "PRRC2B", "ILF3", "TEX261",
			"EEF2", "MLTK", "ACTN4", "TRIM65", "IRAK1", "TRAF7", "NELFB", "ZBTB2", "TMEM123", "TMEM185B", "NCAPD2",
			"PIEZO1", "LEPRE1", "MOGS", "SLC4A2", "BCOR", "SMC1A", "HEATR6", "NCSTN", "CHD4", "AC068134.10", "EMC1",
			"WBP1LP2", "HMG20B", "TUBGCP2", "UNC45A", "CENPB", "ESYT1", "MYH9", "PDIA4", "CRTAP", "UGGT1", "INTS1",
			"CYP24A1", "ELN", "B4GALT7", "TAB2", "UFD1L", "ABCC9", "ACTB", "TAF2", "TGFB2", "COMT", "PALB2", "MYH7",
			"NAA10", "GNPTG", "FOXF1", "HIRA", "RAD51C", "ARVCF", "CBL", "GNPTAB", "VWF", "C12orf57", "HGD", "TGFBR1",
			"FANCL", "FANCE", "LTBP2", "KANSL1", "CHST3", "NPHP3", "BBS2", "MYH11", "GLA", "SMAD6", "BRIP1", "FBN2",
			"NOTCH2", "FBLN5", "BRCA2", "SMAD4", "FANCD2", "FAM58A", "NOTCH1", "B3GAT3", "PDSS1", "FANCI", "BRAF",
			"RPL26", "FANCC", "SNIP1", "IDUA", "COL1A2", "IFT122", "KIAA0196", "TGFBR2", "ANKS6", "CEP57", "GLB1",
			"SKI", "ESCO2", "FBN1", "TMEM70", "SMAD3", "ERCC4", "D2HGDH", "FANCB", "RPS6KA3", "B3GALT6", "EHMT1",
			"TBX1", "FANCF", "NKX2-5", "FANCA", "FANCM", "SLX4", "FLNA", "CHRNG", "ADAMTSL2", "TTC37", "SLC2A10",
			"NR2F2", "FANCG" });

	public static List<String> pred33wNotch = Arrays.asList(new String[]
	{ "LTBP3", "SORBS3", "GPR124", "COL6A2", "MRC2", "TPM2", "HSPG2", "COL3A1", "SSC5D", "FBLN2", "FHL1", "STON1",
			"EMP1", "BGN", "COL6A1", "MMP2", "TNS1", "TGFB1I1", "ATOH8", "TIMP3", "MEGF6", "COL18A1", "CTGF", "LTBP4",
			"PLXND1", "LRP1", "PEAR1", "IGFBP7", "ZCCHC24", "LOXL1", "TGFBI", "CCDC80", "ZBTB4", "MATN2", "SEPN1",
			"FAT4", "MFAP4", "GSN", "PDGFRB", "RHOJ", "EFNB1", "PLEC", "LAMA4", "SORCS2", "TTC28", "EHD2", "KIAA1462",
			"MYLK", "PTRF", "COL27A1", "GAS6", "OSR1", "MSRB3", "SYDE1", "POSTN", "TAGLN", "BMP1", "FIBIN", "LMOD1",
			"ATP2B4", "COL6A3", "AKAP12", "COL4A2", "AXL", "PGM5", "FSTL1", "MMP14", "CCL2", "H6PD", "TENC1", "FZD1",
			"GAS1", "AQP1", "HTRA1", "COL4A1", "PDGFC", "DKK3", "AFAP1L1", "NTN4", "PXDC1", "INF2", "ACTA2", "PDE7B",
			"LIMS2", "NID2", "LZTS2", "THBS1", "SVIL", "C1S", "ADAMTS10", "S1PR3", "OLFML2A", "DCHS1", "DAB2",
			"SCARA3", "ZMYND11", "THSD1", "LUM", "MRGPRF", "LAMA2", "CYP24A1", "ELN", "ABCC9", "TGFB2", "MYH7",
			"GNPTG", "FOXF1", "ARVCF", "VWF", "LTBP2", "CHST3", "NPHP3", "BBS2", "MYH11", "SMAD6", "FBN2", "NOTCH2",
			"FBLN5", "NOTCH1", "IDUA", "COL1A2", "IFT122", "TGFBR2", "ANKS6", "SKI", "FBN1", "SMAD3", "RPS6KA3",
			"TBX1", "FLNA", "ADAMTSL2", "SLC2A10", "NR2F2" });
}
