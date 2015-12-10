package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.vcf.VcfRepository;

/**
 * The purpose of this class is to get a better look at a specific VCF file. It reads all lines and prints the specific
 * information that is requested.
 * 
 * @author mbijlsma
 */
public class ReadVCFlines
{
	/**
	 * The main method, invokes readFile().
	 * 
	 * @param args
	 * @throws IOException when input file is not correct
	 */
	public static void main(String[] args) throws IOException
	{
		readFile();
	}

	/**
	 * Reads the given file per line and prints the requested information.
	 * 
	 * @throws IOException when input file is not correct
	 */
	private static void readFile() throws IOException
	{
		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(
				new File(
						"/Users/molgenis/Documents/graduation_project/data/Joeri_exome.variant.calls.GATK.sorted.alldiploid.vqsr.snpeff.vcf.gz"),
				"vcf").iterator();

		int count = 0;
		while (vcf.hasNext())
		{
			count++;
			Entity record = vcf.next();

			System.out.println(record);
			// System.out.println(record.getString("ALT"));
			// System.out.println(record.get("GT").toString());
			// System.out.println(record.getEntity("SAMPLES_ENTITIES").getString("ORIGINAL_NAME"));

			if (count == 100) // only read the first 100 lines of the VCF file
			{
				break;
			}

		}

	}
}