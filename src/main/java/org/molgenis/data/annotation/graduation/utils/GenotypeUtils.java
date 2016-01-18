package org.molgenis.data.annotation.graduation.utils;

import org.molgenis.data.annotation.graduation.analysis.InheritanceAnalysis;
import org.molgenis.data.annotation.graduation.model.Trio;

/**
 * This class provides utilities for the estimation of the inheritance mode using the genotypes of the {@link Trio}s.
 * This class is invoked by the {@link InheritanceAnalysis} class.
 * 
 * @author mbijlsma
 *
 */
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
	 * Estimates if a variant belongs to the homozygous de novo inheritance mode.
	 * 
	 * @param childGenotype
	 *            the genotype of the child
	 * @param fatherGenotype
	 *            the genotype of the father
	 * @param motherGenotype
	 *            the genotype of the mother
	 * @return true if the variant belongs to this inheritance mode
	 */
	public static boolean homozygousDenovoAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
	{

		return (
		// 1/1, 0/1, 0/0
		(childGenotype.equals(homozygous_1) || childGenotype.equals(homozygous_2))
				&& (fatherGenotype.equals(heterozygous_1) || fatherGenotype.equals(heterozygous_2)
						|| fatherGenotype.equals(heterozygous_3) || fatherGenotype.equals(heterozygous_4)) && (motherGenotype
				.equals(homozygous_3) || motherGenotype.equals(homozygous_4)))

				// OR 1/1, 0/0, 0/1
				|| (childGenotype.equals(homozygous_1) || childGenotype.equals(homozygous_2))
				&& (fatherGenotype.equals(homozygous_3) || fatherGenotype.equals(homozygous_4))
				&& (motherGenotype.equals(heterozygous_1) || motherGenotype.equals(heterozygous_2)
						|| motherGenotype.equals(heterozygous_3) || motherGenotype.equals(heterozygous_4));
	}

	/**
	 * Estimates if a variant belongs to the heterozygous de novo inheritance mode.
	 * 
	 * @param childGenotype
	 *            the genotype of the child
	 * @param fatherGenotype
	 *            the genotype of the father
	 * @param motherGenotype
	 *            the genotype of the mother
	 * @return true if the variant belongs to this inheritance mode
	 */
	public static boolean heterozygousDenovoAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
	{
		// 1/0, 0/0, 0/0
		return (childGenotype.equals(heterozygous_1) || childGenotype.equals(heterozygous_2)
				|| childGenotype.equals(heterozygous_3) || childGenotype.equals(heterozygous_4))
				&& (fatherGenotype.equals(homozygous_3) || fatherGenotype.equals(homozygous_4))
				&& (motherGenotype.equals(homozygous_3) || motherGenotype.equals(homozygous_4));
	}

	/**
	 * Estimates if a variant belongs to the homozygous inheritance mode.
	 * 
	 * @param childGenotype
	 *            the genotype of the child
	 * @param fatherGenotype
	 *            the genotype of the father
	 * @param motherGenotype
	 *            the genotype of the mother
	 * @return true if the variant belongs to this inheritance mode
	 */
	public static boolean homozygousAnalysis(String childGenotype, String fatherGenotype, String motherGenotype)
	{
		// 1/1, 1/0, 1/0
		return ((childGenotype.equals(homozygous_1) || childGenotype.equals(homozygous_2)))
				&& ((fatherGenotype.equals(heterozygous_1) || fatherGenotype.equals(heterozygous_2)
						|| fatherGenotype.equals(heterozygous_3) || fatherGenotype.equals(heterozygous_4)) && (motherGenotype
						.equals(heterozygous_1)
						|| motherGenotype.equals(heterozygous_2)
						|| motherGenotype.equals(heterozygous_3) || motherGenotype.equals(heterozygous_4)));
	}

	/**
	 * Estimates if a variant belongs to the compound heterozygous inheritance mode. First the father genotype is
	 * estimated.
	 * 
	 * @param childGenotype
	 *            the genotype of the child
	 * @param fatherGenotype
	 *            the genotype of the father
	 * @param motherGenotype
	 *            the genotype of the mother
	 * @return true if the variant belongs to this inheritance mode
	 */
	public static boolean compoundHeterozygousAnalysisFather(String childGenotype, String fatherGenotype,
			String motherGenotype)
	{
		// Same gene, two or more variants
		// They can't be all heterozygous
		// They can't be all homozygous
		// Child must be heterozygous
		// One of parents must be at least one time heterozygous for variant, but not for the same variant
		// variant1: father: 0/1, mother 0/0
		// variant2: father 0/0, mother 0/1

		return (childGenotype.equals(heterozygous_1) || childGenotype.equals(heterozygous_2)
				|| childGenotype.equals(heterozygous_3) || childGenotype.equals(heterozygous_4))
				&& (fatherGenotype.equals(heterozygous_1) || fatherGenotype.equals(heterozygous_2)
						|| fatherGenotype.equals(heterozygous_3) || fatherGenotype.equals(heterozygous_4))
				&& (motherGenotype.equals(homozygous_3) || motherGenotype.equals(homozygous_4));

	}

	/**
	 * Estimates if a variant belongs to the compound heterozygous inheritance mode. The mother genotype is now
	 * estimated.
	 * 
	 * @param childGenotype
	 *            the genotype of the child
	 * @param fatherGenotype
	 *            the genotype of the father
	 * @param motherGenotype
	 *            the genotype of the mother
	 * @return true if the variant belongs to this inheritance mode
	 */
	public static boolean compoundHeterozygousAnalysisMother(String childGenotype, String fatherGenotype,
			String motherGenotype)
	{
		return (childGenotype.equals(heterozygous_1) || childGenotype.equals(heterozygous_2)
				|| childGenotype.equals(heterozygous_3) || childGenotype.equals(heterozygous_4))
				&& (fatherGenotype.equals(homozygous_3) || fatherGenotype.equals(homozygous_4))
				&& (motherGenotype.equals(heterozygous_1) || motherGenotype.equals(heterozygous_2)
						|| motherGenotype.equals(heterozygous_3) || motherGenotype.equals(heterozygous_4));
	}

	/**
	 * Contains the genotype values (reference and alternate) for the inheritance modes.
	 * 
	 * @param altIndex
	 *            the index of the alternate allele
	 */
	public static void updateHomozygousAndHeterozygousValues(int altIndex)
	{
		// Define all heterozygous and homozygous combinations (phased and unphased)
		heterozygous_1 = "0/" + altIndex;
		heterozygous_2 = altIndex + "/0";
		heterozygous_3 = "0|" + altIndex;
		heterozygous_4 = altIndex + "|0";

		// For child in homozygous analysis
		homozygous_1 = altIndex + "/" + altIndex;
		homozygous_2 = altIndex + "|" + altIndex;

		// For parents in compound heterozygous analysis
		homozygous_3 = "0/0";
		homozygous_4 = "0|0";
	}

	/**
	 * Checks whether a genotype is correct and returns a boolean.
	 * 
	 * @param childGenotype
	 *            the genotype of the child
	 * @param fatherGenotype
	 *            the genotype of the father
	 * @param motherGenotype
	 *            the genotype of the mother
	 * @return true if genotype is correct, otherwise false
	 */
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

	/**
	 * Checks whether a depth is correct and returns a boolean.
	 * 
	 * @param childDepth
	 *            the depth of the child
	 * @param fatherDepth
	 *            the depth of the father
	 * @param motherDepth
	 *            the depth of the mother
	 * @return true if depth is correct, otherwise false
	 */
	public static boolean isDepthCorrect(String childDepth, String fatherDepth, String motherDepth)
	{
		// Filter whole trio if one member has a depth below 10
		if ((Integer.parseInt(childDepth) < 10 || Integer.parseInt(fatherDepth) < 10)
				|| Integer.parseInt(motherDepth) < 10)
		{
			return false;
		}
		return true;
	}
}
