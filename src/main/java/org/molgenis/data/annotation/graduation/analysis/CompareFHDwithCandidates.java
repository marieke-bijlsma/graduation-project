package org.molgenis.data.annotation.graduation.analysis;

import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CompareFHDwithCandidates
{
	private File convertedIDsFile;
	private File FHDFile;
	private File candidatesFile;
	
	public HashMap<String, String> readConvertedFile() throws IOException
	{
		HashMap<String, String> geneIdsAndSymbols = new HashMap<>();

		for (String record : readFile(convertedIDsFile, true))
		{
			String[] recordSplit = record.split("\t", -1);
			geneIdsAndSymbols.put(recordSplit[0], recordSplit[1]); // [Ensembl ID, gene symbol]
		}

		return geneIdsAndSymbols;
	}

	public HashMap<String, String> readFHD(HashMap<String, String> geneIdsAndSymbols) throws IOException
	{
		HashMap<String, String> geneSymbolsAndExpression = new HashMap<String, String>();
		
		for(String record : readFile(FHDFile, false)){
			String[] recordSplit = record.split("\t", -1);

			String id = recordSplit[0];
			String expression = recordSplit[1];

			if (geneIdsAndSymbols.keySet().contains(id))
			{
				geneSymbolsAndExpression.put(geneIdsAndSymbols.get(id), expression);
			}
		}
		return geneSymbolsAndExpression;
	}

	public void compareFHDandCandidates(HashMap<String, String> geneSymbolsAndExpression)
			throws IOException
	{
		int countTotal = 0;
		int countRemaining = 0;
		int countExpressionAboveZero = 0;

		List<String> candidates = readFile(candidatesFile, false);

		for (String candidate : candidates)
		{
			if (geneSymbolsAndExpression.keySet().toString().toUpperCase().contains(candidate.toUpperCase()))
			{
				countTotal++;

				if (!geneSymbolsAndExpression.get(candidate).contains("-"))
				{
					double expr = Double.parseDouble(geneSymbolsAndExpression.get(candidate));

					if (expr >= 0)
					{
						countExpressionAboveZero++;
						System.out.println(candidate + "\t" + expr);
					}
				}
			}
			else
			{
				countRemaining++;
			}
		}
		System.out.println("total candidates found: " + countTotal);
		System.out.println("total remaining candidates: " + countRemaining);
		System.out.println("Number of candidates with expression above 0: " + countExpressionAboveZero);
		// 192 - DAVID
		// 202 - BioMart
	}

	public static void main(String[] args) throws Exception
	{
		CompareFHDwithCandidates compareFHDwithCandidates = new CompareFHDwithCandidates();
		compareFHDwithCandidates.parseCommandLineArgs(args);
		
		HashMap<String, String> geneIdsAndSymbols = compareFHDwithCandidates.readConvertedFile();
		HashMap<String, String> geneSymbolsAndExpression = compareFHDwithCandidates.readFHD(geneIdsAndSymbols);
		compareFHDwithCandidates.compareFHDandCandidates(geneSymbolsAndExpression);
	}

	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 2 arguments");
		}

		convertedIDsFile = new File(args[0]);
		if (!convertedIDsFile.isFile())
		{
			throw new Exception("Input converted IDs file does not exist or is not a directory: "
					+ convertedIDsFile.getAbsolutePath());
		}

		FHDFile = new File(args[1]);
		if (!FHDFile.isFile())
		{
			throw new Exception("FHD file does not exist or is not a directory: " + FHDFile.getAbsolutePath());
		}

		candidatesFile = new File(args[2]);
		if (!candidatesFile.isFile())
		{
			throw new Exception("candidates file does not exist or is not a directory: "
					+ candidatesFile.getAbsolutePath());
		}
	}
}
