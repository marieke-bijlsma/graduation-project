package org.molgenis.data.annotation.graduation.project;

import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.molgenis.data.Entity;

public class Trio
{
	private String family_id;
	private String child_id;
	private String father_id;
	private String mother_id;

	private Map<String, List<Entity>> variants = Maps.newHashMap();
	private Map<String, List<Entity>> samplesChild = Maps.newHashMap();
	private Map<String, List<Entity>> samplesFather = Maps.newHashMap();
	private Map<String, List<Entity>> samplesMother = Maps.newHashMap();

	private Map<String, List<Candidate>> candidatesForChildperGene = Maps.newHashMap();

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

	public Map<String, List<Entity>> getVariants()
	{
		return variants;
	}

	public void setVariants(Map<String, List<Entity>> variants)
	{
		this.variants = variants;
	}

	public Map<String, List<Entity>> getSamplesChild()
	{
		return samplesChild;
	}

	public void setSamplesChild(Map<String, List<Entity>> samplesChild)
	{
		this.samplesChild = samplesChild;
	}

	public Map<String, List<Entity>> getSamplesFather()
	{
		return samplesFather;
	}

	public void setSamplesFather(Map<String, List<Entity>> samplesFather)
	{
		this.samplesFather = samplesFather;
	}

	public Map<String, List<Entity>> getSamplesMother()
	{
		return samplesMother;
	}

	public void setSamplesMother(Map<String, List<Entity>> samplesMother)
	{
		this.samplesMother = samplesMother;
	}

	public Map<String, List<Candidate>> getCandidatesForChildperGene()
	{
		return candidatesForChildperGene;
	}

	public void setCandidatesForChildperGene(Map<String, List<Candidate>> candidatesForChildperGene)
	{
		this.candidatesForChildperGene = candidatesForChildperGene;
	}

	public void addInfoToVariantMap(String gene, Entity variant)
	{
		if (!(variants.containsKey(gene)))
		{
			List<Entity> variantList = Lists.newArrayList();
			variantList.add(variant);
			variants.put(gene, variantList);
		}
		else
		{
			//if variant already exists (in case of multiple alleles)
//			if (!(variants.get(gene).contains(variant)))
//			{
				variants.get(gene).add(variant);
//			}
		}

	}

	public void addSampleToChildMap(String gene, Entity sample)
	{
		if (!(samplesChild.containsKey(gene)))
		{
			List<Entity> childSampleList = Lists.newArrayList();
			childSampleList.add(sample);
			samplesChild.put(gene, childSampleList);
		}
		else
		{
			samplesChild.get(gene).add(sample);
		}

	}

	public void addSampleToFatherMap(String gene, Entity sample)
	{
		if (!(samplesFather.containsKey(gene)))
		{
			List<Entity> fatherSampleList = Lists.newArrayList();
			fatherSampleList.add(sample);
			samplesFather.put(gene, fatherSampleList);
		}
		else
		{
			samplesFather.get(gene).add(sample);
		}

	}

	public void addSampleToMotherMap(String gene, Entity sample)
	{
		if (!(samplesMother.containsKey(gene)))
		{
			List<Entity> motherSampleList = Lists.newArrayList();
			motherSampleList.add(sample);
			samplesMother.put(gene, motherSampleList);
		}
		else
		{
			samplesMother.get(gene).add(sample);
		}

	}

	public void addCandidate(String gene, Candidate candidate)
	{
		if (!(candidatesForChildperGene.containsKey(gene)))
		{
			List<Candidate> candidateList = Lists.newArrayList();
			candidateList.add(candidate);
			candidatesForChildperGene.put(gene, candidateList);
		}
		else
		{
			candidatesForChildperGene.get(gene).add(candidate);
		}
	}

}
