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
		return "Candidate [inheritanceMode=" + inheritanceMode + ", " + "affected allele: " + affectedAlelle + ", " + "childGenotype="
				+ childGenotype.get("GT").toString() + ", fatherGenotype=" + fatherGenotype.get("GT").toString()
				+ ", motherGenotype=" + motherGenotype.get("GT").toString() + " chrom + pos: "
				+ variant.getString("#CHROM") + " + " + variant.getString("POS") + "]";
	}

	private InheritanceMode inheritanceMode;

	// select only the variants that apply to this particular candidate
	private String affectedAlelle;
	private Entity variant;
	private Entity childGenotype;
	private Entity fatherGenotype;
	private Entity motherGenotype;

	public Candidate(InheritanceMode inheritanceMode, String affectedAllele, Entity variant, Entity childGenotype, Entity fatherGenotype,
			Entity motherGenotype)
	{
		super();
		this.inheritanceMode = inheritanceMode;
		this.affectedAlelle  = affectedAllele;
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

	public void setVariant(Entity variant)
	{
		this.variant = variant;
	}

	public Entity getFatherGenotype()
	{
		return fatherGenotype;
	}

	public void setFatherGenotype(Entity fatherGenotype)
	{
		this.fatherGenotype = fatherGenotype;
	}

	public Entity getMotherGenotype()
	{
		return motherGenotype;
	}

	public void setMotherGenotype(Entity motherGenotype)
	{
		this.motherGenotype = motherGenotype;
	}

	public Entity getChildGenotype()
	{
		return childGenotype;
	}

	public void setChildGenotype(Entity childGenotype)
	{
		this.childGenotype = childGenotype;
	}

	public String getAffectedAlelle()
	{
		return affectedAlelle;
	}

	public void setAffectedAlelle(String affectedAlelle)
	{
		this.affectedAlelle = affectedAlelle;
	}

}
