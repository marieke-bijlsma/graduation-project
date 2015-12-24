package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.molgenis.data.annotation.graduation.utils.FileReadUtils;

public class MergeVCFwithCADD
{
	private File vcfFile;
	private File caddFile;
	private File output_mergeVCFwithCADD;

	List<String> VcfChrPos = newArrayList();
	Map<String, String> mergedVcfAndCadd = newHashMap();

	public void run() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw = new PrintWriter(output_mergeVCFwithCADD, "UTF-8");

		// Write new header to file
		pw.println("GENE_SYMBOL" + "\t" + "EFFECT" + "\t" + "IMPACT" + "\t" + "MOTHER_GT" + "\t" + "FATHER_GT" + "\t"
				+ "CHILD_GT" + "\t" + "FAMILY_ID" + "\t" + "SAMPLE_ID" + "\t" + "pred33wNotch" + "\t" + "pred38" + "\t"
				+ "predAll82" + "\t" + "hpoGenes" + "\t" + "ExAC allele frequency" + "\t" + "homozyg in ExAC" + "\t"
				+ "hets in ExAC" + "\t" + "CHROM" + "\t" + "POS" + "\t" + "ID" + "\t" + "REF" + "\t" + "ALT" + "\t"
				+ "QUAL" + "\t" + "FILTER" + "\t" + "INFO" + "\t" + "CADD_PHRED");

		for (String record : FileReadUtils.readFile(caddFile, true))
		{
			String[] recordSplit = record.split("\t", -1);

			// get chromosome, pos and cadd score and add to map
			String caddChr = recordSplit[0];
			String caddPos = recordSplit[1];
			String caddPhred = recordSplit[5];

			String CaddChrPos = caddChr + "_" + caddPos;
			mergedVcfAndCadd.put(CaddChrPos, caddPhred);
		}

		printVcfFileWithCadd(pw);

		pw.flush();
		pw.close();
	}

	private void printVcfFileWithCadd(PrintWriter pw) throws FileNotFoundException
	{
		for (String record : FileReadUtils.readFile(vcfFile, true))
		{
			String[] recordSplit = record.split("\t", -1);

			// if one of the keys in map equals the chromosome and pos of vcf, print vcf line to new file and add cadd
			// score
			if (mergedVcfAndCadd.keySet().contains(recordSplit[15] + "_" + recordSplit[16]))
			{
				pw.println(record + "\t" + mergedVcfAndCadd.get(recordSplit[15] + "_" + recordSplit[16]));
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		MergeVCFwithCADD mergeVCFwithCADD = new MergeVCFwithCADD();
		mergeVCFwithCADD.parseCommandLineArgs(args);
		mergeVCFwithCADD.run();
	}

	public void parseCommandLineArgs(String[] args) throws Exception
	{
		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		caddFile = new File(args[1]);
		if (!caddFile.isFile())
		{
			throw new Exception("Input CADD file does not exist or directory: " + caddFile.getAbsolutePath());
		}

		output_mergeVCFwithCADD = new File(args[2]);
		if (!output_mergeVCFwithCADD.isFile())
		{
			throw new Exception("Output file does not exist or directory: " + output_mergeVCFwithCADD.getAbsolutePath());
		}
	}
}
