package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class compares the family IDs from both the PED file as the PBT file, to get the right child IDs associated with
 * the family IDs and prints them. They can be used to add as a new column to the PBT file.
 * 
 * @author mbijlsma
 */
public class GetChildSampleIds
{
	private File pedFile;
	private File pbtFile;

	/**
	 * Reads the PED file and adds the family ID and child ID to a new HashMap.
	 * 
	 * @param pedFile
	 *            the PED file to be parsed
	 * @return pedFamilyAndChildId a HashMap containing the family IDs and associated child IDs
	 * @throws IOException
	 *             when the input file is not correct
	 */
	public Map<String, String> readPed() throws IOException
	{
		Map<String, String> pedFamilyAndChildIds = newHashMap();
		for (String record : readFile(pedFile, false))
		{

			String[] recordSplit = record.split("\t", -1);
			pedFamilyAndChildIds.put(recordSplit[0], recordSplit[1]); // [family ID, child ID]
		}
		return pedFamilyAndChildIds;
	}

	/**
	 * Reads the PBT file and adds the family ID to a new list.
	 * 
	 * @param pbtFile
	 *            the PBT file to be parsed
	 * @return pbtFamilyId a list containing the family IDs
	 * @throws IOException
	 *             when input file is not correct
	 */
	public List<String> getFamilyIdentifiersFromPbtFile() throws IOException
	{
		List<String> pbtFamilyIds = newArrayList();
		for (String record : readFile(pbtFile, false))
		{
			String[] recordSplit = record.split("\t", -1);
			pbtFamilyIds.add(recordSplit[3]); // [family ID]
		}
		return pbtFamilyIds;
	}

	/**
	 * Merges the right family ID with the right child ID and prints both tab separated.
	 * 
	 * @param pedFamilyAndChildIds
	 *            a HashMap containing family and child IDs
	 * @param pbtFamilyIds
	 *            an ArrayList containing family IDs
	 * @throws IOException
	 */
	public void mergeChildAndFamilyId() throws IOException
	{
		Map<String, String> pedFamilyAndChildIds = readPed();
		List<String> pbtFamilyIds = getFamilyIdentifiersFromPbtFile();

		for (String pbtFamilyId : pbtFamilyIds)
		{
			if (pedFamilyAndChildIds.containsKey(pbtFamilyId))
			{
				String sampleIdentifier = pedFamilyAndChildIds.get(pbtFamilyId);
				System.out.println(pbtFamilyId + "\t" + sampleIdentifier);
			}
		}
	}

	/**
	 * The main method, invokes mergeChildAndFamilyId().
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when length of arguments is not 2 or when PED or PBT file does not exists.
	 */
	public static void main(String[] args) throws Exception
	{
		GetChildSampleIds getChildSampleIds = new GetChildSampleIds();
		getChildSampleIds.parseCommandLineArgs(args);

		getChildSampleIds.mergeChildAndFamilyId();
	}

	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		pedFile = new File(args[0]);
		if (!pedFile.isFile())
		{
			throw new Exception("Input ped file does not exist or is not a directory: " + pedFile.getAbsolutePath());
		}

		pbtFile = new File(args[1]);
		if (!pbtFile.isFile())
		{
			throw new Exception("PBT file does not exist or is not a directory: " + pbtFile.getAbsolutePath());
		}
	}
}
