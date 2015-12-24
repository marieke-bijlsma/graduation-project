package org.molgenis.data.annotation.graduation.analysis;

import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.getAnnotationField;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * This class reads a VCF file and parses the gene symbol column.
 * 
 * @author mbijlsma
 */
public class GetGeneSymbolColumn
{
	private File file = new File(
			"/Users/molgenis/Documents/graduation_project/output_mergePBTwithVCF/output_mergePBTwithVCF_mendViolSampleFamily_noGenotypes_copy.txt");

	/**
	 * Reads a VCF file and returns lines.
	 * 
	 * @return record a list containing all lines from VCF file
	 * @throws FileNotFoundException
	 *             when VCF file is not found
	 */
	private List<String> getVcfRecords() throws FileNotFoundException
	{
		return readFile(file, true);
	}

	/**
	 * Parses gene symbol column from VCF file and print the gene with associated impact.
	 * 
	 * @param record
	 *            a list containing all lines from VCF file
	 * @throws IOException
	 */
	public void getGeneColumn(List<String> records) throws IOException
	{
		// Variant can have multiple gene symbols with different impacts
		// Variant then contains multiple times the impact, followed by different gene symbols

		for (String record : records)
		{
			StringBuilder stringBuilder = new StringBuilder();

			String[] multiAnnotationFields = getAnnotationField(record, 7).split(",");
			for (String oneAnnotationField : multiAnnotationFields)
			{
				String[] annSplit = oneAnnotationField.split("\\|", -1);
				stringBuilder.append(annSplit[3] + " (" + annSplit[1] + "), ");
			}

			System.out.println(stringBuilder.substring(0, stringBuilder.length() - 2));
		}
	}

	/**
	 * The main method, invokes readFile() and getGeneColumn().
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		GetGeneSymbolColumn getColumn = new GetGeneSymbolColumn();
		List<String> record = getColumn.getVcfRecords();
		getColumn.getGeneColumn(record);
	}
}
