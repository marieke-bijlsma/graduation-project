package org.molgenis.data.annotation.graduation.analysis;

import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.getAnnotationField;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * This class reads a combined text file (VCF records + Mendelian violations) and parses and prints the gene symbol
 * column.
 * 
 * @author mbijlsma
 */
public class GetGeneSymbolColumn
{
	private File file = new File(
			"/Users/molgenis/Documents/graduation_project/output_mergePBTwithVCF/output_mergePBTwithVCF_mendViolSampleFamily_noGenotypes_copy.txt");

	/**
	 * Reads a text file and returns all lines.
	 * 
	 * @return record a list containing all lines of the file
	 * @throws FileNotFoundException
	 *             when text file does not exist
	 */
	private List<String> getVcfRecords() throws FileNotFoundException
	{
		return readFile(file, true); // true: skip header
	}

	/**
	 * Parses gene symbol column from file and prints the genes with associated impacts.
	 * 
	 * @param record
	 *            a list containing all lines of the file
	 * @throws IOException
	 *             when input list is incorrect
	 */
	public void getGeneColumn(List<String> records) throws IOException
	{
		// Variant can have multiple gene symbols with different impacts
		// Variant then contains multiple times the impact, followed by different gene symbols

		for (String record : records)
		{
			StringBuilder stringBuilder = new StringBuilder();

			String[] multiAnnotationFields = getAnnotationField(record, 7).split(","); // 7th column from file

			for (String oneAnnotationField : multiAnnotationFields)
			{
				String[] annSplit = oneAnnotationField.split("\\|", -1);
				stringBuilder.append(annSplit[3] + " (" + annSplit[1] + "), ");
			}

			System.out.println(stringBuilder.substring(0, stringBuilder.length() - 2)); // delete last comma
		}
	}

	/**
	 * The main method, invokes getVcfRecords() and getGeneColumn().
	 * 
	 * @param args
	 *            the command line args
	 * @throws IOException
	 *             when input file is incorrect
	 */
	public static void main(String[] args) throws IOException
	{
		GetGeneSymbolColumn getGeneSymbolColumn = new GetGeneSymbolColumn();
		List<String> record = getGeneSymbolColumn.getVcfRecords();
		getGeneSymbolColumn.getGeneColumn(record);
	}
}
