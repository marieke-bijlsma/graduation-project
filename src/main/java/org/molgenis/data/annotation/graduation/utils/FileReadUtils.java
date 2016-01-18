package org.molgenis.data.annotation.graduation.utils;

import static org.elasticsearch.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * This class provides utilities for the reading of files.
 * 
 * @author mbijlsma
 *
 */
public class FileReadUtils
{
	/**
	 * Reads a file and adds lines to a list.
	 * 
	 * @param file
	 *            the file to be read
	 * @param skipHeader
	 *            boolean, true if header must be skipped, otherwise false
	 * @return records list containing all lines of the file
	 * @throws FileNotFoundException
	 *             when file does not exist
	 */
	public static List<String> readFile(File file, boolean skipHeader) throws FileNotFoundException
	{
		List<String> records = newArrayList();

		Scanner scanner = new Scanner(file);
		String record = null;

		if (skipHeader) scanner.nextLine();

		while (scanner.hasNextLine())
		{
			record = scanner.nextLine();
			records.add(record);
		}
		scanner.close();
		return records;
	}

	/**
	 * Parses the annotation field from a file.
	 * 
	 * @param record
	 *            one line from a file
	 * @param index
	 *            the index of column to be parsed
	 * @return annField the parsed annotation field
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