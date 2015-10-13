package org.molgenis.data.annotation.graduation.project;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

public class getChildSampleId
{

	public static void main(String[] args) throws Exception
	{

		File pedFile = new File(args[0]);
		if (!pedFile.isFile())
		{
			throw new Exception("Input ped file does not exist or directory: " + pedFile.getAbsolutePath());
		}

		File pbtFile = new File(args[1]);
		if (!pbtFile.isFile())
		{
			throw new Exception("PBT file does not exist or directory: " + pbtFile.getAbsolutePath());
		}

		getChildSampleId cs = new getChildSampleId();
		cs.run(pedFile, pbtFile);

	}

	ArrayList<String> pbtFamilyId = new ArrayList<String>();
	HashMap<String, String> pedFamilyAndSampleId = new HashMap<String, String>();
	HashMap<Integer, HashMap<String, String>> mergedChildAndFamilyId = new HashMap<Integer, HashMap<String, String>>();

	public void run(File pedFile, File pbtFile) throws FileNotFoundException
	{
		Scanner s = new Scanner(pbtFile);
		String line = null;
		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);
			pbtFamilyId.add(lineSplit[3]);
		}
		s.close();
		
//		System.out.println(pbtFamilyId);
		Scanner scan = new Scanner(pedFile);
		String linePed = null;
		while (scan.hasNextLine())
		{
			linePed = scan.nextLine();
			String[] lineSplit = linePed.split("\t", -1);
			pedFamilyAndSampleId.put(lineSplit[0], lineSplit[1]);
		}
		scan.close();

//		 System.out.println(pbtFamilyId.size());
//		 System.out.println(pedFamilyAndSampleId.size());

		int count = 0;

		for (String id : pbtFamilyId)
		{
			if (pedFamilyAndSampleId.keySet().contains(id))
			{
				count += 1;

				mergedChildAndFamilyId.put(count, new HashMap<String, String>());
				mergedChildAndFamilyId.get(count).put(id, pedFamilyAndSampleId.get(id));
				
				// sampleIDs
//				System.out.println(mergedChildAndFamilyId.get(count).get(id)); 

			}
//			else
//			{
//				System.out.println("not in keyset: " + id);
//			}

		}
		
//		 System.out.println("count: " + count);
//		 System.out.println(mergedChildAndFamilyId);
//		 System.out.println(mergedChildAndFamilyId.size());

		// print both values separately to paste in column in pbt file
		for (Entry<Integer, HashMap<String, String>> CountEntry : mergedChildAndFamilyId.entrySet())
		{

			for (Entry<String, String> IDEntry : CountEntry.getValue().entrySet())
			{
//				String family = IDEntry.getKey();
				String sample = IDEntry.getValue();

//				System.out.println(family);
				System.out.println(sample);
			}

		}

	}
}
