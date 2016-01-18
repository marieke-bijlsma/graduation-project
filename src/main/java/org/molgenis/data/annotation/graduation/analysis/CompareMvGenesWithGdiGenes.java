package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.annotation.graduation.utils.FileReadUtils;

/**
 * This class compares the genes from the variants in the Mendelian violation file with the genes from the Gene Damage
 * Index file and prints the number of non-overlapping genes.
 * 
 * @author mbijlsma
 */
public class CompareMvGenesWithGdiGenes
{
	private File gdiFile;
	private File mendelianViolationFile = new File(
			"/Users/molgenis/Documents/graduation_project/mendelianViolationFiles/mendelian_violation_Xadjusted_replicates.txt");

	/**
	 * Reads and parses the Mendelian violation file and adds the gene column to a new list.
	 * 
	 * @return mendelianViolationGenes a list containing the gene column from this file
	 * @throws FileNotFoundException
	 *             when file does not exist
	 * 
	 */
	public List<String> readMVfile() throws FileNotFoundException
	{
		List<String> mendelianViolationGenes = newArrayList();

		for (String record : FileReadUtils.readFile(mendelianViolationFile, true))
		{
			String[] lineSplit = record.split("\t", -1);
			mendelianViolationGenes.add(lineSplit[0]);
		}
		return mendelianViolationGenes;
	}

	/**
	 * Reads and parses the Gene Damage Index file and adds the gene column to a new list.
	 *
	 * @return gdiGenes a list containing the gene column from this file
	 * @throws IOException
	 *             when gdiFile is incorrect or cannot be parsed
	 */
	public List<String> readGDIfile() throws IOException
	{
		List<String> gdiGenes = Lists.newArrayList();
		List<String> records = FileReadUtils.readFile(gdiFile, false);

		for (String record : records)
		{
			String[] recordSplit = record.split("\t", -1);
			gdiGenes.add(recordSplit[0]); // gene column
		}
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
			if (gdiGenes.contains(geneSymbol)) // if genes overlap
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
	 * The main method, invokes parseCommandLineArgs(), readMVfile(), readGDIfile() and compare().
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when one of the files is incorrect or does not exist
	 * 
	 */
	public static void main(String[] args) throws Exception
	{
		CompareMvGenesWithGdiGenes compareMvGenesWithGdiGenes = new CompareMvGenesWithGdiGenes();
		compareMvGenesWithGdiGenes.parseCommandLineArgs(args);
		List<String> mvGenes = compareMvGenesWithGdiGenes.readMVfile();
		List<String> gdiGenes = compareMvGenesWithGdiGenes.readGDIfile();
		compareMvGenesWithGdiGenes.compare(mvGenes, gdiGenes);
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when length of arguments is not 2, or if one of the files does not exist or is incorrect
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		mendelianViolationFile = new File(args[0]);
		if (!mendelianViolationFile.isFile())
		{
			throw new Exception("Input Mendelian violation file does not exist or is not a directory: "
					+ mendelianViolationFile.getAbsolutePath());
		}
		gdiFile = new File(args[1]);
		if (!gdiFile.isFile())
		{
			throw new Exception("Input GDI file does not exist or is not a directory: " + gdiFile.getAbsolutePath());
		}
	}
}
