package org.molgenis.graduation.project;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.elasticsearch.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MergePBTwithVCF
{

	HashMap<String, ArrayList<String>> patientgroupToGenes = new HashMap<String, ArrayList<String>>();

	private ArrayList<String> pbtChrPos = new ArrayList<String>();

	public void go(File vcfFile, File pbtFile, File output) throws Exception
	{

		PrintWriter pw = new PrintWriter(output, "UTF-8");

		// new HashMap with String chr_pos as key and String[] family ID, sampleID as values
		Map<String, List<String[]>> pbtEntries = Maps.newHashMap();

		Scanner s = new Scanner(pbtFile);
		String line = null;
		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);

			// get chr_pos from pbtFile
			String key = lineSplit[0] + "_" + lineSplit[1];
			pbtChrPos.add(key);

			// get family and sample IDs from pbtFile
			String[] familyAndSample =
			{ lineSplit[3], lineSplit[4] };

			// if pbtEntries already contains key (chr_pos), get that existing key again and add new family and sample IDs
			// else, create new list and add the new family and sample IDs and put it together with the new chr_pos in HashMap
			if (pbtEntries.containsKey(key))
			{
				pbtEntries.get(key).add(familyAndSample);
			}
			else
			{
				List<String[]> entries = new ArrayList<String[]>();
				entries.add(familyAndSample);
				pbtEntries.put(key, entries);
			}

		}
		s.close();

		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		pw.println("CHROM" + "\t" + "POS" + "\t" + "ID" + "\t" + "REF" + "\t" + "ALT" + "\t" + "INFO" + "\t" + "FAMILY_ID" + "\t" + "SAMPLE_ID");
		
		while (vcf.hasNext())
		{
			Entity record = vcf.next();

			// get chr_pos from VCF
			String chr = record.getString("#CHROM");
			String pos = record.getString("POS");

			String key = chr + "_" + pos;

			// if HashMap (from pbt) contains chr_pos (from VCF)
			// for every entry (famID, samID) in HashMap values, get associated VCF data (without genotypes)
			// add samID and famID after VCF data, separated by tab and print all
			if (pbtEntries.containsKey(key))
			{
				for (String[] entries : pbtEntries.get(key))
				{
					String vcfEntry = VcfUtils.convertToVCF(record, false);
					vcfEntry += entries[0] + "\t" + entries[1] + "\t";
					pw.println(vcfEntry);
				}
			}

			pw.flush();
		}
		pw.close();
	}

	public static void main(String[] args) throws Exception
	{
		// configureLogging();

		// See http://stackoverflow.com/questions/4787719/spring-console-application-configured-using-annotations
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				"org.molgenis.graduation.project");
		ctx.register(CommandLineAnnotatorConfig.class);
		MergePBTwithVCF main = ctx.getBean(MergePBTwithVCF.class);
		main.run(args);
		ctx.close();
	}

	public void run(String[] args) throws Exception
	{
		if (!(args.length == 3))
		{
			throw new Exception("Must supply 3 arguments");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		File pbtFile = new File(args[1]);
		if (!pbtFile.isFile())
		{
			throw new Exception("PBT file does not exist or directory: " + pbtFile.getAbsolutePath());
		}

		File output = new File(args[2]);
		if (output.exists())
		{
			System.out.println("WARNING: output file already exists, overwriting " + output);
		}

		MergePBTwithVCF cf = new MergePBTwithVCF();
		cf.go(vcfFile, pbtFile, output);

	}

}
