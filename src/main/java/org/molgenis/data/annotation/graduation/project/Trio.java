package org.molgenis.data.annotation.graduation.project;

import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.Entity;

public class Trio
{
	private String family_id;
	private String child_id;
	private String father_id;
	private String mother_id;
	
	// index of variants correspond to index of samples
	// variant[0] has samples childSamples[0], fatherSamples[0], motherSamples[0]
	// variant[1] has samples childSamples[1], fatherSamples[1], motherSamples[1]
	private List<Entity> variants = Lists.newArrayList();
	private List<Entity> childSamples = Lists.newArrayList();
	private List<Entity> fatherSamples = Lists.newArrayList();
	private List<Entity> motherSamples = Lists.newArrayList();

	public Trio(String family_id, String child_id, String father_id, String mother_id)
	{
		this.family_id = family_id;
		this.child_id = child_id;
		this.father_id = father_id;
		this.mother_id = mother_id;
	}

	public String getFamily_id()
	{
		return family_id;
	}

	public String getChild_id()
	{
		return child_id;
	}

	public String getFather_id()
	{
		return father_id;
	}

	public String getMother_id()
	{
		return mother_id;
	}

	public List<Entity> getVariants()
	{
		return variants;
	}

	public void setVariants(List<Entity> variants)
	{
		this.variants = variants;
	}

	public List<Entity> getChildSamples()
	{
		return childSamples;
	}

	public void setChildSamples(List<Entity> childSamples)
	{
		this.childSamples = childSamples;
	}

	public List<Entity> getFatherSamples()
	{
		return fatherSamples;
	}

	public void setFatherSamples(List<Entity> fatherSamples)
	{
		this.fatherSamples = fatherSamples;
	}

	public List<Entity> getMotherSamples()
	{
		return motherSamples;
	}

	public void setMotherSamples(List<Entity> motherSamples)
	{
		this.motherSamples = motherSamples;
	}
	
	public static void addToTrioList(List<Trio> trios, Entity sample)
	{
		boolean childFound = false;
		boolean fatherFound = false;
		boolean  motherFound = false;
		
		trio_loop: 
		for (Trio trio : trios)
		{
			String child = trio.getChild_id();
			String father = trio.getFather_id();
			String mother = trio.getMother_id();
			
			//if sample.oridinalid == trio id
			//do stuff

			if (childFound && fatherFound && motherFound)
			{
				childFound = false;
				fatherFound = false;
				motherFound = false;
				
				continue trio_loop;
			}
			
			String sampleID = sample.get("ORIGINAL_NAME").toString();

			if (child.equals(sampleID))
			{
				childFound = true;
				trio.getChildSamples().add(sample);
			}
			else if (father.equals(sampleID))
			{
				fatherFound = true;
				trio.getFatherSamples().add(sample);
			}
			else if (mother.equals(sampleID))
			{
				motherFound = true;
				trio.getMotherSamples().add(sample);
			}
			else
			{
				continue;
			}
		}
	}

}
