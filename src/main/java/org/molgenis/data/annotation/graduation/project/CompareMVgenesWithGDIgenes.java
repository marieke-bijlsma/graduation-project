package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	/**
	 * Reads the Mendelian violation file and adds the gene column to a new ArrayList.
	 * 
	 * @param mvFile
	 *            the Mendelian violation file to be parsed
	 * @return mvGenes a list containing the genes from this file
	 * @throws IOException
	 *             when file is not correct
	 */
	public ArrayList<String> readMVfile(File mvFile) throws IOException
	{
		ArrayList<String> mvGenes = Lists.newArrayList();

		Scanner scanMv = new Scanner(mvFile);
		String mvLine = null;
		scanMv.nextLine(); // skip header

		while (scanMv.hasNextLine())
		{
			mvLine = scanMv.nextLine();
			String[] lineSplit = mvLine.split("\t", -1);
			mvGenes.add(lineSplit[0]); // gene column
		}
		scanMv.close();
		return mvGenes;
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
	public ArrayList<String> readGDIfile(File gdiFile) throws IOException
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
	public void compare(ArrayList<String> mvGenes, ArrayList<String> gdiGenes)
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
	 * @throws Exception
	 *             when the length of the arguments is not 2, or if one of the 2 files does not exists.
	 */
	public static void main(String[] args) throws Exception
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

		CompareMVgenesWithGDIgenes compareGenes = new CompareMVgenesWithGDIgenes();
		ArrayList<String> mvGenes = compareGenes.readMVfile(mvFile);
		ArrayList<String> gdiGenes = compareGenes.readGDIfile(gdiFile);
		compareGenes.compare(mvGenes, gdiGenes);
	}
}
