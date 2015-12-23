package org.molgenis.data.annotation.graduation.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * This class compares the family IDs from both the PED file as the PBT file, to get the right child IDs associated with
 * the family IDs and prints them. They can be used to add as a new column to the PBT file.
 * 
 * @author mbijlsma
 */
public class GetChildSampleIds
{
	File pedFile;
	File pbtFile;
	
	/**
	 * Reads the PED file and adds the family ID and child ID to a new HashMap.
	 * 
	 * @param pedFile
	 *            the PED file to be parsed
	 * @return pedFamilyAndChildId a HashMap containing the family IDs and associated child IDs
	 * @throws IOException
	 *             when the input file is not correct
	 */
	public HashMap<String, String> readPed() throws IOException
	{
		HashMap<String, String> pedFamilyAndChildIds = new HashMap<String, String>();

		Scanner s = new Scanner(pedFile);
		String line = null;
		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);
			pedFamilyAndChildIds.put(lineSplit[0], lineSplit[1]); // [family ID, child ID]
		}
		s.close();
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
	public ArrayList<String> readPbt() throws IOException
	{
		ArrayList<String> pbtFamilyIds = new ArrayList<String>();

		Scanner s = new Scanner(pbtFile);
		String line = null;
		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);
			pbtFamilyIds.add(lineSplit[3]); // [family ID]
		}
		s.close();
		return pbtFamilyIds;
	}

	/**
	 * Merges the right family ID with the right child ID and prints both separately.
	 * 
	 * @param pedFamilyAndChildIds
	 *            a HashMap containing family and child IDs
	 * @param pbtFamilyIds
	 *            an ArrayList containing family IDs
	 */
	public void mergeChildAndFamilyId(HashMap<String, String> pedFamilyAndChildIds, ArrayList<String> pbtFamilyIds)
	{
		HashMap<Integer, HashMap<String, String>> mergedChildAndFamilyId = new HashMap<Integer, HashMap<String, String>>();

		int count = 0;
		for (String id : pbtFamilyIds)
		{
			if (pedFamilyAndChildIds.keySet().contains(id))
			{
				count += 1;

				mergedChildAndFamilyId.put(count, new HashMap<String, String>());
				mergedChildAndFamilyId.get(count).put(id, pedFamilyAndChildIds.get(id));
			}
		}

		// print both values separately to paste in column in pbt file
		for (Entry<Integer, HashMap<String, String>> CountEntry : mergedChildAndFamilyId.entrySet())
		{
			for (Entry<String, String> IDEntry : CountEntry.getValue().entrySet())
			{
				// String family = IDEntry.getKey();
				String sample = IDEntry.getValue();

				// System.out.println(family);
				System.out.println(sample);
			}
		}
	}

	/**
	 * The main method, invokes readPed(), readPbt(), and mergeChildAndFamilyId().
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
		HashMap<String, String> pedFamilyAndChildIds = getChildSampleIds.readPed();
		ArrayList<String> pbtFamilyIds = getChildSampleIds.readPbt();
		getChildSampleIds.mergeChildAndFamilyId(pedFamilyAndChildIds, pbtFamilyIds);
	}

	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		File pedFile = new File(args[0]);
		if (!pedFile.isFile())
		{
			throw new Exception("Input ped file does not exist or is not a directory: " + pedFile.getAbsolutePath());
		}

		File pbtFile = new File(args[1]);
		if (!pbtFile.isFile())
		{
			throw new Exception("PBT file does not exist or is not a directory: " + pbtFile.getAbsolutePath());
		}
	}
}
