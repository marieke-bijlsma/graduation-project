package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.graduation.utils.FileReadUtils.readFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.collect.Lists;

/**
 * This class creates a new Mendelian Violation file by analyzing the X chromosome of boys. It prints the result to a
 * new file.
 * 
 * @author mbijlsma
 */
public class CreateNewMendelianViolationsForX
{
	private File pedFile;
	private File mvFile;
	private File outputFile;

	// PAR regions:
	// The pseudoautosomal regions 60,001-2,699,520 and 154,931,044-155,270,560 with the ploidy 2
	// { from=>1, to=>60_000, M=>1 }
	// { from=>2_699_521, to=>154_931_043, M=>1 }

	// | chrom_2 | start_2 | end_2 |
	// +---------+----------+-----------+
	// | X | 60001 | 2699520 |
	// | X | 154931044| 155270560 |

	// PAR regions
	private static final int PAR1_START = 60001;
	private static final int PAR1_END = 2699520;
	private static final int PAR2_START = 154931044;
	private static final int PAR2_END = 155270560;

	/**
	 * Parses the PED file.
	 * 
	 * @param pedFile
	 *            the file to be parsed
	 * @return pedFamilyAndSex a map containing the family ID and sex of patient
	 * @throws FileNotFoundException
	 *             when file does not exists
	 */
	private Map<String, String> readPedFile() throws FileNotFoundException
	{
		Map<String, String> pedFamilyAndSex = newHashMap();

		for (String record : readFile(pedFile, false))
		{
			String[] recordSplit = record.split("\t", -1);
			pedFamilyAndSex.put(recordSplit[0], recordSplit[4]);
		}
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
	 * @throws IOException
	 */
	private List<String> parseXchromosomesFromMendelianViolationFile() throws IOException
	{
		List<String> Xchromosomes = newArrayList();

		FileWriter fileWriter = new FileWriter(outputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		for (String record : readFile(mvFile, false))
		{
			writeAutosomalChromosomesToFile(bufferedWriter, Xchromosomes, record);
		}

		bufferedWriter.close();
		return Xchromosomes;
	}

	/**
	 * 
	 * @param bufferedWriter
	 * @param Xchromosomes
	 * @param record
	 * @throws IOException
	 */
	private void writeAutosomalChromosomesToFile(BufferedWriter bufferedWriter, List<String> Xchromosomes, String record)
			throws IOException
	{
		// delete empty fields and delete missing genotypes
		if (record.contains(".\t.\t.\t.") || record.contains("./."))
		{
			return;
		}

		if (record.startsWith("X"))
		{
			// If it is an X chromosome, add it to the list
			Xchromosomes.add(record);
		}
		else
		{
			// If it is an autosomal chromosome, write it to file
			bufferedWriter.append(record + "\n");
		}
	}

	/**
	 * Gets the right sex with the right patient.
	 * 
	 * @param pw
	 *            printwriter where output is written to
	 * @param Xchromosomes
	 *            a list containing only the lines from the MV file of X chromsome
	 * @param pedFamilyAndSex
	 *            a map containing the family ID and sex of patient
	 * @throws IOException
	 */
	public void getSexOfPatient(List<String> Xchromosomes) throws IOException
	{
		FileWriter fileWriter = new FileWriter(outputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		Map<String, String> familyAndSexPedMap = readPedFile();
		List<String> listOfPatientSexes = getListOfPatientSexes(Xchromosomes, familyAndSexPedMap);

		// Read per line and get sex, pos and gt
		for (String patientSex : listOfPatientSexes)
		{
			String[] splittedLine = patientSex.split("\t");
			if (isFixedPloidy(splittedLine))
			{
				continue;
			}
			else
			{
				bufferedWriter.append(patientSex + "\n");
			}
		}
		bufferedWriter.close();
	}

	/**
	 * 
	 * @param splittedLine
	 * @return
	 */
	private boolean isFixedPloidy(String[] splittedLine)
	{
		String sex = splittedLine[17];
		String position = splittedLine[1];
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
		return sex.equals("1")
				&& (!((Integer.parseInt(position) > PAR1_START) && (Integer.parseInt(position) < PAR1_END)))
				&& (!((Integer.parseInt(position) > PAR2_START) && (Integer.parseInt(position) < PAR2_END)))
				&& firstChildAllele.equals(secondChildAllele)
				&& (firstMotherAllele.equals(firstChildAllele) || secondMotherAllele.equals(firstChildAllele));
	}

	/**
	 * for every X line, get family ID If family ID matches with ped family ID -> get sex of patient
	 * 
	 * @param Xchromosomes
	 * @param familyAndSexPedMap
	 * @return
	 */
	private List<String> getListOfPatientSexes(List<String> Xchromosomes, Map<String, String> familyAndSexPedMap)
	{
		List<String> listOfPatientSexes = Lists.newArrayList();
		for (String Xchromosome : Xchromosomes)
		{
			String[] mendelianViolationColumns = Xchromosome.split("\t");
			String mendelianViolationFamilyID = mendelianViolationColumns[3];

			if (familyAndSexPedMap.containsKey(mendelianViolationFamilyID))
			{
				listOfPatientSexes.add(Xchromosome + "\t" + familyAndSexPedMap.get(mendelianViolationFamilyID));
			}
		}
		return listOfPatientSexes;
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
		CreateNewMendelianViolationsForX createNewMendelianViolationsForX = new CreateNewMendelianViolationsForX();
		createNewMendelianViolationsForX.parseCommandLineArgs(args);

		List<String> Xchromosomes = createNewMendelianViolationsForX.parseXchromosomesFromMendelianViolationFile();
		createNewMendelianViolationsForX.getSexOfPatient(Xchromosomes);
	}

	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when file does not exist or length of arguments is incorrect
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		pedFile = new File(args[0]);
		if (!pedFile.isFile())
		{
			throw new Exception("Ped file does not exist or directory: " + pedFile.getAbsolutePath());
		}

		mvFile = new File(args[1]);
		if (!mvFile.isFile())
		{
			throw new Exception("Mendelian violation file does not exist or directory: " + mvFile.getAbsolutePath());
		}
		outputFile = new File(args[2]);
		if (!outputFile.exists())
		{
			outputFile.createNewFile();
		}
		else if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or directory: " + pedFile.getAbsolutePath());
		}
		else
		{
			outputFile.delete();
			outputFile.createNewFile();
		}
	}
}
