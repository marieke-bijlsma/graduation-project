package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MergeVCFwithCADD
{

	public static void main(String[] args) throws Exception
	{

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		File caddFile = new File(args[1]);
		if (!caddFile.isFile())
		{
			throw new Exception("Input CADD file does not exist or directory: " + caddFile.getAbsolutePath());
		}

		File output_mergeVCFwithCADD = new File(args[2]);
		if (!output_mergeVCFwithCADD.isFile())
		{
			throw new Exception("Output file does not exist or directory: " + output_mergeVCFwithCADD.getAbsolutePath());
		}

		MergeVCFwithCADD mvc = new MergeVCFwithCADD();
		mvc.run(vcfFile, caddFile, output_mergeVCFwithCADD);

	}

	ArrayList<String> VcfChrPos = new ArrayList<String>();

	HashMap<String, String> mergedVcfAndCadd = new HashMap<String, String>();

	public void run(File vcfFile, File caddFile, File outputFile) throws FileNotFoundException,
			UnsupportedEncodingException
	{
		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

		// Write new header to file
		pw.println("GENE_SYMBOL" + "\t" + "EFFECT" + "\t" + "IMPACT" + "\t" + "MOTHER_GT" + "\t" + "FATHER_GT" + "\t"
				+ "CHILD_GT" + "\t" + "FAMILY_ID" + "\t" + "SAMPLE_ID" + "\t" + "pred33wNotch" + "\t" + "pred38" + "\t"
				+ "predAll82" + "\t" + "hpoGenes" + "\t" + "ExAC allele frequency" + "\t" + "homozyg in ExAC" + "\t"
				+ "hets in ExAC" + "\t" + "CHROM" + "\t" + "POS" + "\t" + "ID" + "\t" + "REF" + "\t" + "ALT" + "\t"
				+ "QUAL" + "\t" + "FILTER" + "\t" + "INFO" + "\t" + "CADD_PHRED");

		Scanner scanCadd = new Scanner(caddFile);
		String caddLine = null;
		scanCadd.nextLine(); // skip header
		while (scanCadd.hasNextLine())
		{
			caddLine = scanCadd.nextLine();
			String[] lineSplit = caddLine.split("\t", -1);

			// get chr, pos and cadd score and add to map
			String caddChr = lineSplit[0];
			String caddPos = lineSplit[1];
			String caddPhred = lineSplit[5];

			String CaddChrPos = caddChr + "_" + caddPos;
			mergedVcfAndCadd.put(CaddChrPos, caddPhred);
		}
		scanCadd.close();

		Scanner scanVcf = new Scanner(vcfFile);
		String vcfLine = null;

		scanVcf.nextLine(); // skip header
		while (scanVcf.hasNextLine())
		{
			vcfLine = scanVcf.nextLine();
			String[] lineSplit = vcfLine.split("\t", -1);

			// if one of the keys in map equals the chr and pos of vcf, print vcf line to new file and add cadd score
			if (mergedVcfAndCadd.keySet().contains(lineSplit[15] + "_" + lineSplit[16]))
			{
				pw.println(vcfLine + "\t" + mergedVcfAndCadd.get(lineSplit[15] + "_" + lineSplit[16]));
			}
		}
		scanVcf.close();

		pw.flush();
		pw.close();
	}

}
