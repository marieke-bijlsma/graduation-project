package org.molgenis.data.annotation.graduation.project;

import org.molgenis.data.Entity;

public class Candidate
{
	public enum InheritanceMode
	{
		COMPOUND_HET, // its.. complicated :)
		MULTIGENIC, // DON'T DO - like compound but over 2+ genes... VERY tricky, don't do

		HETEROZYGOUS, // DON'T DO - e.g. parent 0/1 + 0/0 = child 0/1 -> pathogenic due to low-penetrance
		HOMOZYGOUS, // parents 0/1 + 0/1 = child 1/1

		DENOVO_HET, // parents 0/0 + 0/0 = child 0/1
		DENOVO_HOM // parent e.g. 0/1 + 0/0, child 1/1
	}

	@Override
	public String toString()
	{
		String annField = variant.getString("ANN");
		String[] annSplit = annField.split("\\|");

		String effect = annSplit[1];
		String impact = annSplit[2];
		String cDNA = annSplit[9];

		String exac_af_STR = variant.get("EXAC_AF") == null ? "-" : variant.get("EXAC_AF").toString();
		String exac_ac_hom_STR = variant.get("EXAC_AC_HOM") == null ? "-" : variant.get("EXAC_AC_HOM").toString();
		String exac_ac_het_STR = variant.get("EXAC_AC_HET") == null ? "-" : variant.get("EXAC_AC_HET").toString();

		String gonl_af_STR = null;

		if (variant.get("GoNL_AF").toString().equals(".") || variant.get("GoNL_AF").toString().equals(".|.") || variant.get("GoNL_AF") == null)
		{
			gonl_af_STR = "-";
		}
		else
		{
			gonl_af_STR = variant.get("GoNL_AF").toString();
		}

		String thousandG_af_STR = variant.get("Thousand_Genomes_AF") == null ? "-" : variant.get("Thousand_Genomes_AF")
				.toString();
		String cadd = variant.get("CADD_SCALED") == null ? "-" : variant.get("CADD_SCALED").toString();

		return "Inheritance mode: " + inheritanceMode + "\n" + "Chromosome and position: "
				+ variant.getString("#CHROM") + ":" + variant.getString("POS") + "\n" + "Alt allele(s): "
				+ variant.getString("ALT") + "\n" + "Affected allele: " + affectedAlelle + "\n"
				+ "Child, father, mother genotypes: " + childGenotype.get("GT").toString() + ", "
				+ fatherGenotype.get("GT").toString() + ", " + motherGenotype.get("GT").toString() + "\n" + "Impact: "
				+ impact + " (" + effect + ", " + cDNA + ") " + "\n" + "ExAC AF: " + exac_af_STR + ", ExAC HOM: "
				+ exac_ac_hom_STR + ", ExAC HET: " + exac_ac_het_STR + "\n" + "GoNL: " + gonl_af_STR + "\n" + "1000G: "
				+ thousandG_af_STR + "\n" + "CADD: " + cadd + "\n";
	}

	private InheritanceMode inheritanceMode;

	// select only the variants that apply to this particular candidate
	private String affectedAlelle;
	private Entity variant;
	private Entity childGenotype;
	private Entity fatherGenotype;
	private Entity motherGenotype;

	public Candidate(InheritanceMode inheritanceMode, String affectedAllele, Entity variant, Entity childGenotype,
			Entity fatherGenotype, Entity motherGenotype)
	{
		super();
		this.inheritanceMode = inheritanceMode;
		this.affectedAlelle = affectedAllele;
		this.variant = variant;
		this.childGenotype = childGenotype;
		this.fatherGenotype = fatherGenotype;
		this.motherGenotype = motherGenotype;

	}

	public InheritanceMode getInheritanceMode()
	{
		return inheritanceMode;
	}

	public void setInheritanceMode(InheritanceMode inheritanceMode)
	{
		this.inheritanceMode = inheritanceMode;
	}

	public Entity getVariant()
	{
		return variant;
	}

	public Entity getFatherGenotype()
	{
		return fatherGenotype;
	}

	public Entity getMotherGenotype()
	{
		return motherGenotype;
	}

	public Entity getChildGenotype()
	{
		return childGenotype;
	}

	public String getAffectedAlelle()
	{
		return affectedAlelle;
	}

}
