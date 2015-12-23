package org.molgenis.data.annotation.graduation.utils;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * Class that provides utilities to read files.
 * @author mbijlsma
 *
 */
public class FileReadUtils
{
	private static final File mendelianViolationFileLocation = new File(
			"/Users/molgenis/Documents/graduation_project/mendelianViolationFiles/mendelian_violation_Xadjusted_replicates.txt");

	/**
	 * Read file and add lines to {@link List}.
	 * 
	 * @throws FileNotFoundException
	 *             when file not found
	 * @return records {@link List} containing all lines of the mendelian violation file
	 */
	public static List<String> readMendelianViolationFile() throws FileNotFoundException
	{
		List<String> records = newArrayList();

		Scanner scanner = new Scanner(mendelianViolationFileLocation);
		String record = null;
		scanner.nextLine(); // skip header

		while (scanner.hasNextLine())
		{
			record = scanner.nextLine();
			records.add(record);
		}
		scanner.close();
		return records;
	}
}