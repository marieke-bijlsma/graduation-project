package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.elasticsearch.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MergePBTwithVCF
{

	HashMap<String, ArrayList<String>> patientgroupToGenes = new HashMap<String, ArrayList<String>>();

	private ArrayList<String> pbtChrPos = new ArrayList<String>();

	public void go(File vcfFile, File pbtFile, File exacFile, File output) throws Exception
	{

		@SuppressWarnings("resource")
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
			String[] familySampleGenotypes =
			{ lineSplit[3], lineSplit[4], lineSplit[6], lineSplit[10], lineSplit[14] };

			// if pbtEntries already contains key (chr_pos), get that existing key again and add new family and sample
			// IDs
			// else, create new list and add the new family and sample IDs and put it together with the new chr_pos in
			// HashMap
			if (pbtEntries.containsKey(key))
			{
				pbtEntries.get(key).add(familySampleGenotypes);
			}
			else
			{
				List<String[]> entries = new ArrayList<String[]>();
				entries.add(familySampleGenotypes);
				pbtEntries.put(key, entries);
			}

		}
		s.close();

		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator exacAnnotator = annotators.get("exac");
		exacAnnotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(exacFile.getAbsolutePath());
		Iterator<Entity> vcfWithExac = exacAnnotator.annotate(vcf);
		
		pw.println("GENE_SYMBOL" + "\t" + "EFFECT" + "\t" + "IMPACT" + "\t" + "MOTHER_GT" + "\t" 
		+ "FATHER_GT" + "\t" + "CHILD_GT" + "\t" + "FAMILY_ID" + "\t" + "SAMPLE_ID" + "\t" + "pred33wNotch"
		+ "\t" + "pred38" + "\t" + "predAll82" + "\t" + "hpoGenes" + "\t" + "ExAC allele frequency" 
		+ "\t" + "homozyg in ExAC" + "\t" + "hets in ExAC" + "\t" + "\t" + "CHROM" + "\t" + "POS" 
		+ "\t" + "ID" + "\t" + "REF" + "\t" + "ALT" + "\t" + "QUAL" + "\t" + "FILTER" + "\t"  + "INFO");
	

		ArrayList<String> variantsWithHighImpact = new ArrayList<String>();
		ArrayList<String> variantsWithModerateImpact = new ArrayList<String>();
		ArrayList<String> variantsWithLowImpact = new ArrayList<String>();
		ArrayList<String> variantsWithModifierImpact = new ArrayList<String>();

		//no rounding
		DecimalFormat df = new DecimalFormat("#.##############################");
		
		int count = 0;
		while (vcfWithExac.hasNext())
		{
			count++;
			if(count%1000==0)
			{
				System.out.println("Scanned over " + count + " lines in VCF...");
			}
			
			/**
			 * DEVELOPMENT MODUS!!
			 * Stop when iterated over 5000 lines of VCF
			 */
//			if(count == 5000)
//			{
//				break;
//			}
			
			Entity record = vcfWithExac.next();
			
			// get chr_pos from VCF
			String chr = record.getString("#CHROM");
			String pos = record.getString("POS");

			String key = chr + "_" + pos;

			// if HashMap (from pbt) contains chr_pos (from VCF)
			// for every entry (famID, samID) in HashMap values, get associated VCF data (without genotypes)
			// add samID and famID after VCF data, separated by tab and print all
			if (pbtEntries.containsKey(key))
			{
				String alts = record.getString("ALT");
				if (alts.contains(","))
				{
					throw new Exception("WARNING !! multi allelic variant, needs manual checking: "
							+ VcfUtils.convertToVCF(record, false));
				}

				String annField = record.getString("ANN");
				String exac_af_STR = record.get("EXAC_AF") == null ? null : record.get("EXAC_AF").toString();
				String exac_ac_hom_STR = record.get("EXAC_AC_HOM") == null ? "" : record.get("EXAC_AC_HOM").toString();
				String exac_ac_het_STR = record.get("EXAC_AC_HET") == null ? "" : record.get("EXAC_AC_HET").toString();
				
				Double exac_af = exac_af_STR != null ? Double.parseDouble(exac_af_STR) : null;
				
				String gene = getGeneSymbolColumn.getColFromInfoField(annField, 3);
				String effect = getGeneSymbolColumn.getColFromInfoField(annField, 1);
				String impact = getGeneSymbolColumn.getColFromInfoField(annField, 2);
				for (String[] entries : pbtEntries.get(key))
				{
					String vcfEntry = VcfUtils.convertToVCF(record, false);
//					vcfEntry += entries[0] + "\t" + entries[1] + "\t";
					
					String printReadyVariant = gene + "\t" + effect + "\t" + impact + "\t" + entries[2] + "\t" + entries[3] + "\t" + entries[4] + "\t" + entries[0] + "\t"
							+ entries[1] + "\t" + containsMultiGene(gene, GenePanels.pred33wNotch) + "\t" + containsMultiGene(gene, GenePanels.pred38) + "\t" 
							+ containsMultiGene(gene, GenePanels.predAll82) + "\t" + containsMultiGene(gene, GenePanels.hpoGenes) + "\t" + (exac_af==null?"":df.format(exac_af)) + "\t" + exac_ac_hom_STR 
							+ "\t" + exac_ac_het_STR + "\t" + vcfEntry;

					if (impact.contains("HIGH"))
					{
						variantsWithHighImpact.add(printReadyVariant);
					}
					else if (impact.contains("MODERATE"))
					{
						variantsWithModerateImpact.add(printReadyVariant);
					}
					else if (impact.contains("LOW"))
					{
						variantsWithLowImpact.add(printReadyVariant);
					}
					else if (impact.contains("MODIFIER"))
					{
						variantsWithModifierImpact.add(printReadyVariant);
					}
					else
					{
						throw new Exception("UNKNOWN IMPACT: " + impact);
					}

				}
				
				//CADD scores
			}

			
		}
		
		//print, sorted HIGH to MODIFIER :)
		for(String variant : variantsWithHighImpact)
		{
			pw.println(variant);
		}
		pw.flush();
		
		for(String variant : variantsWithModerateImpact)
		{
			pw.println(variant);
		}
		pw.flush();
		
		for(String variant : variantsWithLowImpact)
		{
			pw.println(variant);
		}
		pw.flush();
		
		for(String variant : variantsWithModifierImpact)
		{
			pw.println(variant);
		}
		pw.flush();
		
		pw.close();
	}

	public static void main(String[] args) throws Exception
	{
		// configureLogging();

		// See http://stackoverflow.com/questions/4787719/spring-console-application-configured-using-annotations
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				"org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);
		MergePBTwithVCF main = ctx.getBean(MergePBTwithVCF.class);
		main.run(args);
		ctx.close();
	}
	
	
	//gene can be comma separated, e.g. "MLH1, MSH2" (2 genes on different strand)
	private String containsMultiGene(String gene, List<String> panel)
	{
		StringBuffer res = new StringBuffer();
		String[] multiGene = gene.split(", ", -1);
		for(String oneGene : multiGene)
		{
			if(panel.contains(oneGene)){ 
				res.append("yes, ");
			}else
			{
				res.append("no, ");
			}
		}
		res.delete(res.length() - 2, res.length());
		return res.toString();
	}

	public void run(String[] args) throws Exception
	{
		if (!(args.length == 4))
		{
			throw new Exception("Must supply 4 arguments");
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
		
		File exacFile = new File(args[2]);
		if (!exacFile.isFile())
		{
			throw new Exception("exac file does not exist or directory: " + exacFile.getAbsolutePath());
		}

		File output = new File(args[4]);
		if (output.exists())
		{
			System.out.println("WARNING: output file already exists, overwriting " + output);
		}

		MergePBTwithVCF cf = new MergePBTwithVCF();
		cf.go(vcfFile, pbtFile, exacFile, output);

	}

}
