package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.vcf.VcfRepository;

public class ReadVCFlines
{
	public static void main(String[] args) throws IOException
	{
		readFile();
	}

	// Get a better look at what the VCF file looks like, unfortunately the sample_ids are not included (only as header)
	// :(
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
			// System.out.println(record.getEntity("SAMPLES_ENTITIES").getString("NAME"));
			// System.out.println(record.getEntity("SAMPLES_ENTITIES").getString("ORIGINAL_NAME"));

//			if (count > 670 && count < 690)
//			{
//				System.out.println(record + "\n" + count);
//			}
			if (count == 100)
			{
				break;
			}

		}

	}
}