package org.molgenis.data.annotation.graduation.analysis;

import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * This class compares genes involved in fetal heart development with the candidate genes for aortic valve malformations
 * and prints the result.
 * 
 * @author mbijlsma
 *
 */
public class CompareFhdGenesWithCandidateGenes
{
	private File convertedIdsFile;
	private File fhdFile;
	private File candidatesFile;

	/**
	 * Reads and parses the convertedIDsFile and adds both Ensembl IDs and gene symbols to a map.
	 * 
	 * @return ensemblIdsAndGeneSymbols a map containing Ensembl IDs and gene symbols
	 * @throws IOException
	 *             when convertedIDsFile is incorrect or cannot be parsed
	 */
	public HashMap<String, String> readConvertedFile() throws IOException
	{
		HashMap<String, String> ensemblIdsAndGeneSymbols = new HashMap<>();

		for (String record : readFile(convertedIdsFile, true))
		{
			String[] recordSplit = record.split("\t", -1);
			ensemblIdsAndGeneSymbols.put(recordSplit[0], recordSplit[1]);
		}
		return ensemblIdsAndGeneSymbols;
	}

	/**
	 * Reads and parses the fhdFile and adds gene symbol with associated expression to a map.
	 * 
	 * @param geneIdsAndSymbols
	 *            a map containing Ensembl IDs and gene symbols
	 * @return geneSymbolsAndExpression a map containing gene symbols and associated expression
	 * @throws IOException
	 *             when fhdFile is incorrect or cannot be parsed
	 */
	public HashMap<String, String> readFhd(HashMap<String, String> geneIdsAndSymbols) throws IOException
	{
		HashMap<String, String> geneSymbolsAndExpression = new HashMap<String, String>();

		for (String record : readFile(fhdFile, false))
		{
			String[] recordSplit = record.split("\t", -1);

			String id = recordSplit[0];
			String expression = recordSplit[1];

			// if Ensembl ID of convertedIdsFile is the same as the Ensembl ID from fhdFile, add gene symbol together
			// with expression to a map
			if (geneIdsAndSymbols.keySet().contains(id))
			{
				geneSymbolsAndExpression.put(geneIdsAndSymbols.get(id), expression);
			}
		}
		return geneSymbolsAndExpression;
	}

	/**
	 * Reads and parses the candidatesFile and compares the gene symbols with the gene symbols from FHD and prints result.
	 * 
	 * @param geneSymbolsAndExpression
	 *            a map containing gene symbols and expression
	 * @throws IOException
	 *             when candidatesFile is incorrect or cannot be parsed
	 */
	public void compareFhdAndCandidates(HashMap<String, String> geneSymbolsAndExpression) throws IOException
	{
		int countTotal = 0;
		int countRemaining = 0;
		int countExpressionAboveZero = 0;

		List<String> candidates = readFile(candidatesFile, false);

		for (String candidate : candidates)
		{
			// if gene symbol of FhdFile is the same as the candidate gene, add to map
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
	}

	/**
	 * The main method, invokes parseCommandLineArgs(), readConvertedFile(), readFhd(), and compareFhdAndCandidates()
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when one of the files is incorrect or does not exist
	 */
	public static void main(String[] args) throws Exception
	{
		CompareFhdGenesWithCandidateGenes compareFhdGenesWithCandidateGenes = new CompareFhdGenesWithCandidateGenes();
		compareFhdGenesWithCandidateGenes.parseCommandLineArgs(args);

		HashMap<String, String> geneIdsAndSymbols = compareFhdGenesWithCandidateGenes.readConvertedFile();
		HashMap<String, String> geneSymbolsAndExpression = compareFhdGenesWithCandidateGenes.readFhd(geneIdsAndSymbols);
		compareFhdGenesWithCandidateGenes.compareFhdAndCandidates(geneSymbolsAndExpression);
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when length of arguments is not 2, or when one of the files is incorrect or does not exists
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 2 arguments");
		}

		convertedIdsFile = new File(args[0]);
		if (!convertedIdsFile.isFile())
		{
			throw new Exception("Input converted IDs file does not exist or is not a directory: "
					+ convertedIdsFile.getAbsolutePath());
		}

		fhdFile = new File(args[1]);
		if (!fhdFile.isFile())
		{
			throw new Exception("FHD file does not exist or is not a directory: " + fhdFile.getAbsolutePath());
		}

		candidatesFile = new File(args[2]);
		if (!candidatesFile.isFile())
		{
			throw new Exception("candidates file does not exist or is not a directory: "
					+ candidatesFile.getAbsolutePath());
		}
	}
}
