package org.molgenis.data.annotation.graduation.model;

import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.molgenis.data.Entity;

/**
 * This class stores information used to create a Trio Object.
 * 
 * @author mbijlsma
 *
 */
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

	/**
	 * This is the constructor and is invoked to create a Trio Object.
	 * 
	 * @param family_id
	 *            the family ID
	 * @param child_id
	 *            the child ID
	 * @param father_id
	 *            the father ID
	 * @param mother_id
	 *            the mother ID
	 */
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

	/**
	 * A helper method used to add genes and variants to a map.
	 * 
	 * @param gene
	 *            the gene we are currently looking at
	 * @param variant
	 *            the variant we are currently looking at
	 */
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
			variants.get(gene).add(variant);
		}
	}

	/**
	 * A helper method to add a list of samples for one gene for one child ID to a map.
	 * 
	 * @param gene
	 *            the gene we are currently looking at
	 * @param sample
	 *            {@link Entity} containing one sample
	 */
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

	/**
	 * A helper method to add a list of samples for one gene for one father ID to a map.
	 * 
	 * @param gene
	 *            the gene we are currently looking at
	 * @param sample
	 *            {@link Entity} containing one sample
	 */
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

	/**
	 * A helper method to add a list of samples for one gene for one mother ID to a map.
	 * 
	 * @param gene
	 *            the gene we are currently looking at
	 * @param sample
	 *            {@link Entity} containing one sample
	 */
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

	/**
	 * A helper method that adds a list of {@link Candidate}s for one gene for one child to a map.
	 * 
	 * @param gene
	 *            the gene we are currently looking at
	 * @param candidate
	 *            the {@link Candidate} we are currently looking at
	 */
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
