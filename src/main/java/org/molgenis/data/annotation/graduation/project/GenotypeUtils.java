package org.molgenis.data.annotation.graduation.project;

public class GenotypeUtils
{
	private static String heterozygous_1;
	private static String heterozygous_2;
	private static String heterozygous_3;
	private static String heterozygous_4;

	private static String homozygous_1;
	private static String homozygous_2;
	private static String homozygous_3;
	private static String homozygous_4;
	
	
	/**
	 * look for de novo hom
	 * 
	 * @param childGenotype
	 * @param fatherGenotype
	 * @param motherGenotype
	 * @return
	 */
	public static boolean homozygousDenovoAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
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
	public static boolean heterozygousDenovoAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
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
	public static boolean homozygousAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
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
	public static boolean compoundHeterozygousAnalysisFather(String childGenotype, String fatherGenotype,
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

	public static boolean compoundHeterozygousAnalysisMother(String childGenotype, String fatherGenotype,
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

	public static void updateHomozygousAndHeterozygousValues(int altIndex)
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

	public static boolean isGenotypeCorrect(String childGenotype, String fatherGenotype, String motherGenotype)
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

	public static boolean isDepthCorrect(String childDepth, String fatherDepth, String motherDepth)
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
}
