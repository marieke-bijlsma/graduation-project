package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readMendelianViolationFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.elasticsearch.common.collect.Lists;

/**
 * This class compares the genes from the variants in the Mendelian violation file with the genes from the Gene Damage
 * Index file and prints the number of non-overlapping genes.
 * 
 * @author mbijlsma
 */
public class CompareMVgenesWithGDIgenes
{
	File mvFile;
	File gdiFile;

	/**
	 * Reads the Mendelian violation file and adds the gene column to a new ArrayList.
	 * 
	 * @param mvFile
	 *            the Mendelian violation file to be parsed
	 * @return mvGenes a list containing the genes from this file
	 * @throws FileNotFoundException 
	 * 
	 */
	public List<String> readMVfile() throws FileNotFoundException
	{
		List<String> mendelianViolationGenes = newArrayList();

		for (String record : readMendelianViolationFile())
		{
			String[] lineSplit = record.split("\t", -1);
			mendelianViolationGenes.add(lineSplit[0]); // gene column
		}

		return mendelianViolationGenes;
	}

	/**
	 * Reads the Gene Damage Index file and adds the gene column to a new ArrayList.
	 * 
	 * @param gdiFile
	 *            the the Gene Damage Index file to be parsed
	 * @return gdiGenes a list containing the genes from this file
	 * @throws IOException
	 *             when file is not correct
	 */
	public ArrayList<String> readGDIfile() throws IOException
	{
		ArrayList<String> gdiGenes = Lists.newArrayList();

		Scanner scanGDI = new Scanner(gdiFile);
		String gdiLine = null;

		while (scanGDI.hasNextLine())
		{
			gdiLine = scanGDI.nextLine();
			String[] lineSplit = gdiLine.split("\t", -1);
			gdiGenes.add(lineSplit[0]); // gene column
		}
		scanGDI.close();
		return gdiGenes;
	}

	/**
	 * Compares the mvGenes with gdiGenes and prints the number of non-overlapping genes.
	 * 
	 * @param mvGenes
	 *            a list containing the genes from the mvFile
	 * @param gdiGenes
	 *            a list containing the genes from the gdiFile
	 */
	public void compare(List<String> mvGenes, List<String> gdiGenes)
	{
		int count = 0;

		for (String geneSymbol : mvGenes)
		{
			if (gdiGenes.contains(geneSymbol))
			{
				continue;
			}
			else
			// if genes don't overlap
			{
				count++;
			}
		}
		System.out.println(count);
	}

	/**
	 * The main method, invokes readMVfile(), readGDIfile() and compare().
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when file does not exists
	 * 
	 */
	public static void main(String[] args) throws Exception
	{
		CompareMVgenesWithGDIgenes compareGenes = new CompareMVgenesWithGDIgenes();
		compareGenes.parseCommandLineArgs(args);
		List<String> mvGenes = compareGenes.readMVfile();
		ArrayList<String> gdiGenes = compareGenes.readGDIfile();
		compareGenes.compare(mvGenes, gdiGenes);
	}

	/**
	 * Parse command line arguments.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when file does not exist or when length of arguments is incorrect
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		File mvFile = new File(args[0]);
		if (!mvFile.isFile())
		{
			throw new Exception("Input Mendelian violation file does not exist or is not a directory: "
					+ mvFile.getAbsolutePath());
		}
		File gdiFile = new File(args[1]);
		if (!gdiFile.isFile())
		{
			throw new Exception("Input GDI file does not exist or is not a directory: " + gdiFile.getAbsolutePath());
		}
	}
}
