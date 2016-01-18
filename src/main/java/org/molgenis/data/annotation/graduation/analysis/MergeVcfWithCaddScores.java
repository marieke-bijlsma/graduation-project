package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.molgenis.data.annotation.graduation.utils.FileReadUtils;

/**
 * This class merges a VCF file with CADD scores and writes the result to a new file.
 * 
 * @author mbijlsma
 * 
 */
public class MergeVcfWithCaddScores
{
	private File vcfFile;
	private File caddFile;
	private File outputFile;

	List<String> VcfChrPos = newArrayList();
	Map<String, String> mergedVcfAndCadd = newHashMap();

	/**
	 * Reads the file with CADD scores and adds them, together with the chromosome and position combination, to a map.
	 * 
	 * @throws IOException
	 *             when input or output file is incorrect
	 */
	public void readAndProcessFile() throws IOException
	{
		FileWriter fileWriter = new FileWriter(outputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		// Write new header to new file
		bufferedWriter.append("GENE_SYMBOL" + "\t" + "EFFECT" + "\t" + "IMPACT" + "\t" + "MOTHER_GT" + "\t"
				+ "FATHER_GT" + "\t" + "CHILD_GT" + "\t" + "FAMILY_ID" + "\t" + "SAMPLE_ID" + "\t" + "pred33wNotch"
				+ "\t" + "pred38" + "\t" + "predAll82" + "\t" + "hpoGenes" + "\t" + "ExAC allele frequency" + "\t"
				+ "homozyg in ExAC" + "\t" + "hets in ExAC" + "\t" + "CHROM" + "\t" + "POS" + "\t" + "ID" + "\t"
				+ "REF" + "\t" + "ALT" + "\t" + "QUAL" + "\t" + "FILTER" + "\t" + "INFO" + "\t" + "CADD_PHRED\n");

		for (String record : FileReadUtils.readFile(caddFile, true))
		{
			String[] recordSplit = record.split("\t", -1);
			String caddChr = recordSplit[0];
			String caddPos = recordSplit[1];
			String caddPhred = recordSplit[5];

			String CaddChrPos = caddChr + "_" + caddPos;
			mergedVcfAndCadd.put(CaddChrPos, caddPhred);
		}
		printVcfFileWithCadd();
		bufferedWriter.close();
	}

	/**
	 * Reads VCF file and writes the lines with associated CADD scores to a new file.
	 * 
	 * @throws IOException
	 *             when output or VCF file is incorrect
	 */
	private void printVcfFileWithCadd() throws IOException
	{
		FileWriter fileWriter = new FileWriter(outputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		for (String record : FileReadUtils.readFile(vcfFile, true)) // true: skip header
		{
			String[] recordSplit = record.split("\t", -1);

			// if one of the keys in map equals the chromosome and position of VCF, write VCF line + CADD score to new
			// file
			if (mergedVcfAndCadd.keySet().contains(recordSplit[15] + "_" + recordSplit[16]))
			{
				bufferedWriter.append(record + "\t" + mergedVcfAndCadd.get(recordSplit[15] + "_" + recordSplit[16])
						+ "\n");
			}
		}
		bufferedWriter.close();
	}

	/**
	 * The main method, invokes parseCommandLineArgs() and readAndProcessFile().
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when one of the files is incorrect
	 */
	public static void main(String[] args) throws Exception
	{
		MergeVcfWithCaddScores mergeVcfWithCaddScores = new MergeVcfWithCaddScores();
		mergeVcfWithCaddScores.parseCommandLineArgs(args);
		mergeVcfWithCaddScores.readAndProcessFile();
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when the length of the arguments is not 3 or if one of the files in incorrect or does not exist
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or is not a directory: " + vcfFile.getAbsolutePath());
		}

		caddFile = new File(args[1]);
		if (!caddFile.isFile())
		{
			throw new Exception("Input CADD file does not exist or is not a directory: " + caddFile.getAbsolutePath());
		}

		outputFile = new File(args[2]);
		if (!outputFile.exists())
		{
			outputFile.createNewFile();
		}
		else if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or is not a directory: " + outputFile.getAbsolutePath());
		}
		else
		{
			outputFile.delete();
			outputFile.createNewFile();
		}
	}
}
