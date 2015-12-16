package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class reads a VCF file and parses the gene symbol column.
 * 
 * @author mbijlsma
 */
public class GetGeneSymbolColumn
{
	/**
	 * Reads a VCF file and returns lines.
	 * 
	 * @return record a list containing all lines from VCF file
	 * @throws FileNotFoundException when VCF file is not found
	 */
	private ArrayList<String> readFile() throws FileNotFoundException
	{
		ArrayList<String> record = new ArrayList<String>();
		Scanner s = new Scanner(
				new File(
						"/Users/molgenis/Documents/graduation_project/output_mergePBTwithVCF/output_mergePBTwithVCF_mendViolSampleFamily_noGenotypes_copy.txt"));

		// skipping header
		s.nextLine();

		while (s.hasNextLine())
		{
			String line = s.nextLine();
			record.add(line);
		}
		s.close();
		return record;
	}

	/**
	 * Parses gene symbol column from VCF file and print the gene with associated impact.
	 * 
	 * @param record a list containing all lines from VCF file
	 */
	public void getGeneColumn(ArrayList<String> record)
	{
		// Variant can have multiple gene symbols with different impacts
		// Variant then contains multiple times the impact, followed by different gene symbols

		for (String line : record)
		{
			String[] split = line.split("\t");

			String infoCol = split[7];
			// System.out.println(infoCol);

			String infoFields[] = infoCol.split(";", -1);
			String annField = null;
			for (String infoField : infoFields)
			{
				if (infoField.startsWith("ANN="))
				{
					annField = infoField;
					break;
				}
			}

			StringBuffer sb = new StringBuffer();
			String[] multiAnn = annField.split(",");
			for (String oneAnn : multiAnn)
			{
				String[] annSplit = oneAnn.split("\\|", -1);
				sb.append(annSplit[3] + " (" + annSplit[1] + "), "); // gene symbol, impact
			}

			sb.delete(sb.length() - 2, sb.length());

			System.out.println(sb.toString().trim());
		}
	}

	/**
	 * Parses annotation field from VCF file and returns one or multiple columns.
	 * 
	 * @param annField the annotation field from the VCF file
	 * @param col the column to be parsed
	 * @return StringBuffer containing geneSymbol from specific annotation field
	 */
	public static String getColFromInfoField(String annField, int col)
	{
		StringBuffer sb = new StringBuffer();
		String[] multiAnn = annField.split(","); // for multi-gene!
		for (String oneAnn : multiAnn)
		{
			String[] annSplit = oneAnn.split("\\|", -1);
			sb.append(annSplit[col] + ", ");
		}

		sb.delete(sb.length() - 2, sb.length());

		return sb.toString().trim();
	}

	/**
	 * The main method, invokes readFile() and getGeneColumn().
	 * 
	 * @param args
	 * @throws FileNotFoundException when VCF file is not found
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		GetGeneSymbolColumn getColumn = new GetGeneSymbolColumn();
		ArrayList<String> record = getColumn.readFile();
		getColumn.getGeneColumn(record);
	}
}
