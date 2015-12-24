package org.molgenis.data.annotation.graduation.utils;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * Class that provides utilities to read files.
 * 
 * @author mbijlsma
 *
 */
public class FileReadUtils
{
	/**
	 * Read file and add lines to {@link List}.
	 * 
	 * @throws FileNotFoundException
	 *             when file not found
	 * @return records {@link List} containing all lines of the mendelian violation file
	 */
	public static List<String> readFile(File file, boolean skipHeader) throws FileNotFoundException
	{
		List<String> records = newArrayList();

		Scanner scanner = new Scanner(file);
		String record = null;

		if (skipHeader) scanner.nextLine(); // skip header

		while (scanner.hasNextLine())
		{
			record = scanner.nextLine();
			records.add(record);
		}
		scanner.close();
		return records;
	}
	
	/**
	 * 
	 * @param record
	 * @param index
	 * @return
	 */
	public static String getAnnotationField(String record, int index)
	{
		String infoColumn = record.split("\t")[index];

		String infoFields[] = infoColumn.split(";", -1);
		String annField = null;
		for (String infoField : infoFields)
		{
			if (infoField.startsWith("ANN="))
			{
				annField = infoField;
				break;
			}
		}
		return annField;
	}
}