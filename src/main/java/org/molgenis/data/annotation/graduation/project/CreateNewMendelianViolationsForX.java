package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Scanner;

import org.elasticsearch.common.collect.Maps;

/**
 * This class creates a new Mendelian Violation file by analyzing the X chromosome of boys. It prints the result to a
 * new file.
 * 
 * @author mbijlsma
 */
public class CreateNewMendelianViolationsForX
{
	/**
	 * Parses the PED file.
	 * 
	 * @param pedFile
	 *            the file to be parsed
	 * @return pedFamilyAndSex a map containing the family ID and sex of patient
	 * @throws FileNotFoundException
	 *             when file does not exists
	 */
	public Map<String, String> readPedFile(File pedFile) throws FileNotFoundException
	{
		Map<String, String> pedFamilyAndSex = Maps.newHashMap();
		Scanner s = new Scanner(pedFile);
		String pedLine = null;

		while (s.hasNextLine())
		{
			pedLine = s.nextLine();
			String[] PedLineSplit = pedLine.split("\t", -1);
			pedFamilyAndSex.put(PedLineSplit[0], PedLineSplit[4]);
		}
		s.close();
		return pedFamilyAndSex;
	}

	/**
	 * Parses the Mendelian Violation file.
	 * 
	 * @param mvFile
	 *            the file to be parsed
	 * @param outputFile
	 *            the file where the "non-X" chromsomes are printed to
	 * @param pedFamilyAndSex
	 *            a map containing the family ID and sex of patient
	 * @throws FileNotFoundException
	 *             when file does not exists
	 * @throws UnsupportedEncodingException
	 *             when output file is not encoded correctly
	 */
	public void readMvFile(File mvFile, File outputFile, Map<String, String> pedFamilyAndSex)
			throws FileNotFoundException, UnsupportedEncodingException
	{

		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

		Scanner s = new Scanner(mvFile);
		String mvLine = "";
		StringBuilder onlyX = new StringBuilder();

		while (s.hasNextLine())
		{
			mvLine = s.nextLine();

			// delete empty fields
			if (mvLine.contains(".\t.\t.\t."))
			{
				continue;
			}
			// delete missing genotypes
			if (mvLine.contains("./."))
			{
				continue;
			}
			// get all lines from X chromosome and add to onlyX
			if (mvLine.startsWith("X"))
			{
				onlyX.append(mvLine + "\n");
			}
			else
			{
				pw.println(mvLine);
				pw.flush();
			}
		}
		s.close();
		getSexOfPatient(pw, onlyX, pedFamilyAndSex);
	}

	/**
	 * Gets the right sex with the right patient.
	 * 
	 * @param pw
	 *            printwriter where output is written to
	 * @param onlyX
	 *            a list containing only the lines from the MV file of X chromsome
	 * @param pedFamilyAndSex
	 *            a map containing the family ID and sex of patient
	 */
	public void getSexOfPatient(PrintWriter pw, StringBuilder onlyX, Map<String, String> pedFamilyAndSex)
	{
		String[] Xlines = null;
		StringBuilder onlyXwithSex = new StringBuilder();

		Xlines = onlyX.toString().split("\n");

		// for every X line, get fam ID
		// If fam_id matches with ped fam_id -> get sex of patient
		for (String line : Xlines)
		{
			String[] mvColumns = line.split("\t");
			String mvFamID = mvColumns[3];

			if (pedFamilyAndSex.containsKey(mvFamID))
			{
				onlyXwithSex.append(line + "\t" + pedFamilyAndSex.get(mvFamID) + "\n");
			}
		}

		// PAR regions:
		// The pseudoautosomal regions 60,001-2,699,520 and 154,931,044-155,270,560 with the ploidy 2
		// { from=>1, to=>60_000, M=>1 }
		// { from=>2_699_521, to=>154_931_043, M=>1 }

		// | chrom_2 | start_2 | end_2 |
		// +---------+----------+-----------+
		// | X | 60001 | 2699520 |
		// | X | 154931044| 155270560 |

		int PAR1_start = 60001;
		int PAR1_end = 2699520;
		int PAR2_start = 154931044;
		int PAR2_end = 155270560;

		// Read per line and get sex, pos and gt
		String[] onlyXwithSexLines = onlyXwithSex.toString().split("\n");
		int count = 0;
		for (String Xline : onlyXwithSexLines)
		{
			String[] splittedLine = Xline.split("\t");

			String sex = splittedLine[17];
			String pos = splittedLine[1];
			String childGT = splittedLine[13];
			String motherGT = splittedLine[5];

			// Get alleles of child
			String[] childAlleles = childGT.split("/");
			String firstChildAllele = childAlleles[0];
			String secondChildAllele = childAlleles[1];

			// get alleles of mother
			String[] motherAlleles = motherGT.split("/");
			String firstMotherAllele = motherAlleles[0];
			String secondMotherAllele = motherAlleles[1];

			// if male AND no PAR1 regio AND no PAR2 regio AND child is hemizygous AND mother has allele
			// everything has to be true, if one thing is false, don't filter
			if (sex.equals("1") && (!((Integer.parseInt(pos) > PAR1_start) && (Integer.parseInt(pos) < PAR1_end)))
					&& (!((Integer.parseInt(pos) > PAR2_start) && (Integer.parseInt(pos) < PAR2_end)))
					&& firstChildAllele.equals(secondChildAllele)
					&& (firstMotherAllele.equals(firstChildAllele) || secondMotherAllele.equals(firstChildAllele)))
			{
				continue;
			}
			else
			{
				count++;
				// System.out.println(Xline);
				// print every line to new mendelian violations file, except sex
				String withoutSex = Xline.replaceAll(splittedLine[17], "");
				// remove last tab

				pw.println(Xline);
				pw.flush();
			}
		}
		// System.out.println(count);
		pw.close();
	}

	/**
	 * The main method. Invokes run().
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when args are incorrect
	 */
	public static void main(String[] args) throws Exception
	{
		CreateNewMendelianViolationsForX cmv = new CreateNewMendelianViolationsForX();
		cmv.run(args);

	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when file does not exist or length of arguments is incorrect
	 */
	public void run(String[] args) throws Exception
	{
		File pedFile = new File(args[0]);
		if (!pedFile.isFile())
		{
			throw new Exception("Ped file does not exist or directory: " + pedFile.getAbsolutePath());
		}

		File mvFile = new File(args[1]);
		if (!mvFile.isFile())
		{
			throw new Exception("Mendelian violation file does not exist or directory: " + mvFile.getAbsolutePath());
		}
		File outputFile = new File(args[2]);
		if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or directory: " + outputFile.getAbsolutePath());
		}

		CreateNewMendelianViolationsForX cmv = new CreateNewMendelianViolationsForX();
		Map<String, String> pedFamilyAndSex = cmv.readPedFile(pedFile);
		cmv.readMvFile(mvFile, outputFile, pedFamilyAndSex);

	}
}
