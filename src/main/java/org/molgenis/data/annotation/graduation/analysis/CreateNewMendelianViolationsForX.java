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
 * This class creates a new Mendelian Violation file by analyzing the X chromosome of males. It prints the result to a
 * new file.
 * 
 * @author mbijlsma
 */
public class CreateNewMendelianViolationsForX
{
	private File pedFile;
	private File mvFile;
	private File outputFile;

	// PAR regions, start and end positions
	private static final int PAR1_START = 60001;
	private static final int PAR1_END = 2699520;
	private static final int PAR2_START = 154931044;
	private static final int PAR2_END = 155270560;

	/**
	 * Reads and parses the PED file.
	 * 
	 * @return pedFamilyAndSex a map containing the family ID and sex of patient
	 * @throws FileNotFoundException
	 *             when file does not exist
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
	 * Reads the Mendelian Violation file and writes output to a new file.
	 * 
	 * @return Xchromosomes a list containing the X chromosome lines from the Mendelian violation file
	 * @throws IOException
	 *             when one of the files is incorrect or cannot be parsed
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
	 * Parses the Mendelian violation file.
	 * 
	 * @param bufferedWriter
	 *            to write output to new file
	 * @param Xchromosomes
	 *            a list containing the X chromosome lines from the Mendelian violation file
	 * @param record
	 *            the line of the Mendelian violation file we are currently looking at
	 * @throws IOException
	 *             when mvFile or outputFile is incorrect or cannot be parsed
	 */
	private void writeAutosomalChromosomesToFile(BufferedWriter bufferedWriter, List<String> Xchromosomes, String record)
			throws IOException
	{
		// delete empty fields and missing genotypes
		if (record.contains(".\t.\t.\t.") || record.contains("./."))
		{
			return;
		}

		if (record.startsWith("X"))
		{
			// If line contains X chromosome, add to list
			Xchromosomes.add(record);
		}
		else
		{
			// If line contains autosomal chromosome, write to file
			bufferedWriter.append(record + "\n");
		}
	}

	/**
	 * Parses male patients and filters the variants that do not meet the specific conditions, otherwise they are written
	 * to a new file.
	 * 
	 * @param Xchromosomes
	 *            a list containing the X chromosome lines from the Mendelian violation file
	 * @throws IOException
	 *             when pedFile or outputFile is incorrect or cannot be parsed
	 */
	public void getSexOfPatient(List<String> Xchromosomes) throws IOException
	{
		FileWriter fileWriter = new FileWriter(outputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		Map<String, String> familyAndSexPedMap = readPedFile();
		List<String> listOfPatientSexes = getListOfPatientSexes(Xchromosomes, familyAndSexPedMap);

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
	 * Estimates if a variant has to be filtered according to specific conditions and returns boolean.
	 * 
	 * @param splittedLine
	 *            a line of the Mendelian violation file, splitted on tab
	 * @return true if variant meets conditions, else false
	 */
	private boolean isFixedPloidy(String[] splittedLine)
	{
		String sex = splittedLine[17];
		String position = splittedLine[1];
		String childGT = splittedLine[13];
		String motherGT = splittedLine[5];

		String[] childAlleles = childGT.split("/");
		String firstChildAllele = childAlleles[0];
		String secondChildAllele = childAlleles[1];

		String[] motherAlleles = motherGT.split("/");
		String firstMotherAllele = motherAlleles[0];
		String secondMotherAllele = motherAlleles[1];

		// if male AND no PAR1 regio AND no PAR2 regio AND child is hemizygous AND mother has allele -> return true
		// if one or more conditions are false -> return false
		return sex.equals("1")
				&& (!((Integer.parseInt(position) > PAR1_START) && (Integer.parseInt(position) < PAR1_END)))
				&& (!((Integer.parseInt(position) > PAR2_START) && (Integer.parseInt(position) < PAR2_END)))
				&& firstChildAllele.equals(secondChildAllele)
				&& (firstMotherAllele.equals(firstChildAllele) || secondMotherAllele.equals(firstChildAllele));
	}

	/**
	 * Estimates the sex of each patient.
	 * 
	 * @param Xchromosomes
	 *            a list containing the X chromosome lines from the Mendelian violation file
	 * @param familyAndSexPedMap
	 *            a map containing the family ID and sex of patient
	 * @return listOfPatientSexes a list containing patient sample ID and sex
	 */
	private List<String> getListOfPatientSexes(List<String> Xchromosomes, Map<String, String> familyAndSexPedMap)
	{
		List<String> listOfPatientSexes = Lists.newArrayList();
		for (String Xchromosome : Xchromosomes)
		{
			String[] mendelianViolationColumns = Xchromosome.split("\t");
			String mendelianViolationFamilyID = mendelianViolationColumns[3];

			// If family ID matches with PED family ID -> get sex of patient
			if (familyAndSexPedMap.containsKey(mendelianViolationFamilyID))
			{
				listOfPatientSexes.add(Xchromosome + "\t" + familyAndSexPedMap.get(mendelianViolationFamilyID));
			}
		}
		return listOfPatientSexes;
	}

	/**
	 * The main method. Invokes parseCommandLineArgs(), parseXchromosomesFromMendelianViolationFile(), and
	 * getSexOfPatient().
	 * 
	 * @param args
	 *            the command line args
	 * @throws Exception
	 *             when Mendelian violation file is incorrect or does not exist
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
	 *            the command line args
	 * @throws Exception
	 *             when one of the files does not exist or is incorrect
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
	{
		pedFile = new File(args[0]);
		if (!pedFile.isFile())
		{
			throw new Exception("Ped file does not exist or is not a directory: " + pedFile.getAbsolutePath());
		}

		mvFile = new File(args[1]);
		if (!mvFile.isFile())
		{
			throw new Exception("Mendelian violation file does not exist or is not a directory: " + mvFile.getAbsolutePath());
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
