package org.molgenis.data.annotation.graduation.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class CompareFHDwithCandidates
{
	public HashMap<String, String> readConvertedFile(File convertedIDsFile) throws IOException
	{
		// ArrayList<String> ids = new ArrayList<>();
		// ArrayList<String> symbols = new ArrayList<>();
		// HashSet<String> test = new HashSet<>();
		HashMap<String, String> geneIdsAndSymbols = new HashMap<>();

		// int count = 0;
		Scanner s = new Scanner(convertedIDsFile);
		String line = null;
		s.nextLine(); // skip header
		while (s.hasNextLine())
		{
			// count++;
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);
			// ids.add(lineSplit[0]);
			// symbols.add(lineSplit[1]);

			geneIdsAndSymbols.put(lineSplit[0], lineSplit[1]); // [Ensembl ID, gene symbol]
		}
		s.close();
		// test.addAll(ids);
		// System.out.println(count);
		// System.out.println(ids.size());
		// System.out.println(symbols.size());
		// System.out.println(geneIdsAndSymbols.size());
		// System.out.println(test.size());
		return geneIdsAndSymbols;
	}

	public HashMap<String, String> readFHD(File FHDFile, HashMap<String, String> geneIdsAndSymbols) throws IOException
	{
		HashMap<String, String> geneSymbolsAndExpression = new HashMap<String, String>();
		// HashMap<String, String> geneIdsAndExpression = new HashMap<String, String>();

		Scanner s = new Scanner(FHDFile);
		String line = null;
		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);

			String id = lineSplit[0];
			String expression = lineSplit[1];

			if (geneIdsAndSymbols.keySet().contains(id))
			{
				geneSymbolsAndExpression.put(geneIdsAndSymbols.get(id), expression);
			}
		}
		s.close();
		return geneSymbolsAndExpression;
	}

	public void compareFHDandCandidates(HashMap<String, String> geneSymbolsAndExpression, File candidatesFile)
			throws IOException
	{
		int count = 0;
		int count2 = 0;
		int countExpr = 0;

		ArrayList<String> candidates = new ArrayList<>();
		Scanner s = new Scanner(candidatesFile);
		String line = null;
		while (s.hasNextLine())
		{
			line = s.nextLine();
			candidates.add(line);
		}
		s.close();

		// System.out.println(candidates.size());

		for (String candidate : candidates)
		{
			if (geneSymbolsAndExpression.keySet().toString().toUpperCase().contains(candidate.toUpperCase()))
			{
				count++;

				if (!geneSymbolsAndExpression.get(candidate).contains("-"))
				{
					double expr = Double.parseDouble(geneSymbolsAndExpression.get(candidate));

					if (expr >= 0)
					{
						countExpr++;	
						System.out.println(candidate + "\t" + expr);
//						System.out.println("expression above 0: " + candidate + " " + expr);
					}
				}
			}
			else
			{
				count2++;
//				System.out.println("no expression found for candidate: " + candidate);
			}
		}
//		System.out.println("total candidates found: " + count);
//		System.out.println("total remaining candidates: " + count2);
//		System.out.println("Number of candidates with expression above 0: " + countExpr);
		// 192 - DAVID
		// 202 - BioMart
	}

	public static void main(String[] args) throws Exception
	{
		CompareFHDwithCandidates compareGenes = new CompareFHDwithCandidates();
		compareGenes.parseCommandLineArgs(args);
	}
	
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 2 arguments");
		}

		File convertedIDsFile = new File(args[0]);
		if (!convertedIDsFile.isFile())
		{
			throw new Exception("Input converted IDs file does not exist or is not a directory: "
					+ convertedIDsFile.getAbsolutePath());
		}

		File FHDFile = new File(args[1]);
		if (!FHDFile.isFile())
		{
			throw new Exception("FHD file does not exist or is not a directory: " + FHDFile.getAbsolutePath());
		}

		File candidatesFile = new File(args[2]);
		if (!candidatesFile.isFile())
		{
			throw new Exception("candidates file does not exist or is not a directory: "
					+ candidatesFile.getAbsolutePath());
		}

		CompareFHDwithCandidates compareGenes = new CompareFHDwithCandidates();
		HashMap<String, String> geneIdsAndSymbols = compareGenes.readConvertedFile(convertedIDsFile);
		HashMap<String, String> geneSymbolsAndExpression = compareGenes.readFHD(FHDFile, geneIdsAndSymbols);
		compareGenes.compareFHDandCandidates(geneSymbolsAndExpression, candidatesFile);
	}
}
