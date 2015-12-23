package org.molgenis.data.annotation.graduation.model;

import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.valueOf;

import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;

/**
 * Class to store often used vcf record information
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
