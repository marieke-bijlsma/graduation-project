package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class compares the family IDs from both the PED file and the PBT file, to get the right family IDs and the
 * associated the child IDs and prints them. They can be used to add as a new column to the PBT file.
 * 
 * @author mbijlsma
 */
public class GetChildSampleIds
{
	private File pedFile;
	private File pbtFile;

	/**
	 * Reads the PED file and adds the family ID and child ID to a new map.
	 * 
	 * @return pedFamilyAndChildIds a HashMap containing the family IDs and associated child IDs
	 * @throws IOException
	 *             when the pedFile is incorrect
	 */
	public Map<String, String> readPed() throws IOException
	{
		Map<String, String> pedFamilyAndChildIds = newHashMap();
		for (String record : readFile(pedFile, false)) // false: no header
		{
			String[] recordSplit = record.split("\t", -1);
			pedFamilyAndChildIds.put(recordSplit[0], recordSplit[1]);
		}
		return pedFamilyAndChildIds;
	}

	/**
	 * Reads the PBT file and adds the family ID to a new list.
	 * 
	 * @return pbtFamilyIds a list containing the family IDs
	 * @throws IOException
	 *             when pbtFile is incorrect
	 */
	public List<String> getFamilyIdentifiersFromPbtFile() throws IOException
	{
		List<String> pbtFamilyIds = newArrayList();
		for (String record : readFile(pbtFile, false))
		{
			String[] recordSplit = record.split("\t", -1);
			pbtFamilyIds.add(recordSplit[3]);
		}
		return pbtFamilyIds;
	}

	/**
	 * Merges the right family IDs with the right child IDs and prints both tab-separated.
	 * 
	 * @throws IOException
	 *             when one of the files is incorrect
	 */
	public void mergeChildAndFamilyId() throws IOException
	{
		Map<String, String> pedFamilyAndChildIds = readPed();
		List<String> pbtFamilyIds = getFamilyIdentifiersFromPbtFile();

		for (String pbtFamilyId : pbtFamilyIds)
		{
			// if family IDs match
			if (pedFamilyAndChildIds.containsKey(pbtFamilyId))
			{
				String sampleIdentifier = pedFamilyAndChildIds.get(pbtFamilyId);
				System.out.println(pbtFamilyId + "\t" + sampleIdentifier);
			}
		}
	}

	/**
	 * The main method, invokes parseCommandLineArgs() and mergeChildAndFamilyId().
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when length of arguments is not 2, or when PED or PBT file does not exist or is incorrect.
	 */
	public static void main(String[] args) throws Exception
	{
		GetChildSampleIds getChildSampleIds = new GetChildSampleIds();
		getChildSampleIds.parseCommandLineArgs(args);

		getChildSampleIds.mergeChildAndFamilyId();
	}

	/**
	 * Parses the command line arguments
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when the length of the arguments is not 2, or if one of the files if incorrect or does not exist
	 */
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
