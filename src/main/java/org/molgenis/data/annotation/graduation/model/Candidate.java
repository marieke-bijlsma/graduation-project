package org.molgenis.data.annotation.graduation.model;

import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;

/**
 * This class stores information used to create a Candidate Object.
 * 
 * @author mbijlsma
 *
 */
public class Candidate
{
	public enum InheritanceMode
	{
		COMPOUND_HET, HOMOZYGOUS, DENOVO_HET, DENOVO_HOM
	}

	private InheritanceMode inheritanceMode;
	private String chromosome;
	private String pos;
	private String altAlleles;
	private String affectedAllele;
	private Impact impact;
	private String effect;
	private String cDNA;
	private String exac_af_STR;
	private String exac_ac_hom_STR;
	private String exac_ac_het_STR;
	private String gonl_af_STR;
	private String thousandG_af_STR;
	private String cadd;
	private String childGenotype;
	private String fatherGenotype;
	private String motherGenotype;

	/**
	 * This is the constructor and is invoked to create a Candidate Object.
	 * 
	 * @param inheritanceMode
	 *            the mode of inheritance
	 * @param childGenotype
	 *            the child genotype
	 * @param fatherGenotype
	 *            the father genotype
	 * @param motherGenotype
	 *            the mother genotype
	 * @param altAlleles
	 *            the alternate alleles
	 * @param affectedAllele
	 *            the allele that is affected
	 * @param impact
	 *            the {@link Impact} of the variant
	 */
	public Candidate(InheritanceMode inheritanceMode, String childGenotype, String fatherGenotype,
			String motherGenotype, String altAlleles, String affectedAllele, Impact impact)
	{
		this.inheritanceMode = inheritanceMode;
		this.childGenotype = childGenotype;
		this.fatherGenotype = fatherGenotype;
		this.motherGenotype = motherGenotype;
		this.altAlleles = altAlleles;
		this.affectedAllele = affectedAllele;
		this.impact = impact;
	}

	@Override
	public String toString()
	{
		return "Inheritance mode: " + inheritanceMode + "\n" + "Chromosome and position: " + chromosome + ":" + pos
				+ "\n" + "Alt allele(s): " + altAlleles + "\n" + "Affected allele: " + affectedAllele + "\n"
				+ "Child, father, mother genotypes: " + childGenotype + ", " + fatherGenotype + ", " + motherGenotype
				+ "\n" + "Impact: " + impact + " (" + effect + ", " + cDNA + ") " + "\n" + "ExAC AF: " + exac_af_STR
				+ ", ExAC HOM: " + exac_ac_hom_STR + ", ExAC HET: " + exac_ac_het_STR + "\n" + "GoNL: " + gonl_af_STR
				+ "\n" + "1000G: " + thousandG_af_STR + "\n" + "CADD: " + cadd + "\n";
	}

	public void setChrom(String chromosome)
	{
		this.chromosome = chromosome;
	}

	public void setInheritanceMode(InheritanceMode inheritanceMode)
	{
		this.inheritanceMode = inheritanceMode;
	}

	public void setPos(String pos)
	{
		this.pos = pos;
	}

	public void setEffect(String effect)
	{
		this.effect = effect;
	}

	public void setcDNA(String cDNA)
	{
		this.cDNA = cDNA;
	}

	public void setExac_af_STR(String exac_af_STR)
	{
		this.exac_af_STR = exac_af_STR;
	}

	public void setExac_ac_hom_STR(String exac_ac_hom_STR)
	{
		this.exac_ac_hom_STR = exac_ac_hom_STR;
	}

	public void setExac_ac_het_STR(String exac_ac_het_STR)
	{
		this.exac_ac_het_STR = exac_ac_het_STR;
	}

	public void setGonl_af_STR(String gonl_af_STR)
	{
		this.gonl_af_STR = gonl_af_STR;
	}

	public void setThousandG_af_STR(String thousandG_af_STR)
	{
		this.thousandG_af_STR = thousandG_af_STR;
	}

	public void setCadd(String cadd)
	{
		this.cadd = cadd;
	}

}
