package org.molgenis.data.annotation.graduation.model;

import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.valueOf;

import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;

/**
 * This class stores information used to create a Variant Object.
 * 
 * @author mbijlsma
 *
 */
public class Variant
{
	String chromosome;
	String position;
	String referenceAllele;
	String[] alternateAlleles;

	String[] multiAnnotationField;

	/**
	 * This is the constructor and is invoked to create a Variant Object.
	 * 
	 * @param chromosome the chromosome
	 * @param position the position
	 * @param referenceAllele the reference allele
	 * @param alternateAlleles string array containing the alternate alleles
	 */
	public Variant(String chromosome, String position, String referenceAllele, String[] alternateAlleles)
	{
		this.chromosome = chromosome;
		this.position = position;
		this.referenceAllele = referenceAllele;
		this.alternateAlleles = alternateAlleles;
	}

	public String getChromosome()
	{
		return chromosome;
	}

	public String getPosition()
	{
		return position;
	}

	public String getReferenceAllele()
	{
		return referenceAllele;
	}

	public String[] getAlternateAlleles()
	{
		return alternateAlleles;
	}

	public String[] getMultiAnnotationField()
	{
		return multiAnnotationField;
	}

	public void setMultiAnnotationField(String[] multiAnnotationField)
	{
		this.multiAnnotationField = multiAnnotationField;
	}

	public String getEffect(int index)
	{
		return multiAnnotationField[index].split("\\|", -1)[1];
	}

	public String getCDNA(int index)
	{
		return multiAnnotationField[index].split("\\|", -1)[9];
	}

	public Impact getImpact(int index)
	{
		return valueOf(multiAnnotationField[index].split("\\|", -1)[2]);
	}

	public String getGene(int index)
	{
		return multiAnnotationField[index].split("\\|", -1)[3];
	}

	public String getAminoAcidChange(int index)
	{
		return multiAnnotationField[index].split("\\|", -1)[10];
	}
}
