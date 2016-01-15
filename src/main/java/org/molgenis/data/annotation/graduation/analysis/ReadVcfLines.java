package org.molgenis.data.annotation.graduation.analysis;

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
public class ReadVcfLines
{
	/**
	 * Reads the given file per line and prints the requested information, in this case the sample IDs.
	 * 
	 * @throws IOException
	 *             when input file is incorrect
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
			System.out.println(record.getEntity("SAMPLES_ENTITIES").getString("ORIGINAL_NAME"));

			if (count == 100) // only read the first 100 lines of the VCF file
			{
				break;
			}
		}
	}

	/**
	 * The main method, invokes readFile().
	 * 
	 * @param args
	 *            the command line args
	 * @throws IOException
	 *             when input file is incorrect
	 */
	public static void main(String[] args) throws IOException
	{
		readFile();
	}
}