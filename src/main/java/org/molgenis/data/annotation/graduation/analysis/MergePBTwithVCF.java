package org.molgenis.data.annotation.graduation.analysis;

import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.annotation.graduation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.graduation.utils.GenePanelsUtils;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class reads the PBT and VCF file and merges them by looking at the chromosome and position of the variants.
 * 
 * @author mbijlsma
 */

@Component
public class MergePBTwithVCF
{
	File pbtFile;
	File vcfFile;
	File exacFile;
	File outputFile;

	/**
	 * Reads the PBT file and adds the information to a new string array, which is added to a HashMap containing the
	 * associated chromsome and postion as key.
	 *
	 * @return pbtEntries a HashMap containing chromosome and position with associated PBT information
	 * @throws IOException
	 *             when input file is not correct
	 */
	public HashMap<String, List<String[]>> readPBT() throws IOException
	{
		// new HashMap with String chr_pos as key and String[] family ID, sampleID as values
		HashMap<String, List<String[]>> pbtEntries = newHashMap();

		Scanner s = new Scanner(pbtFile);
		String line = null;
		while (s.hasNextLine())
		{
			line = s.nextLine();
			String[] lineSplit = line.split("\t", -1);

			String[] familySampleGenotypes =
			{ lineSplit[3], lineSplit[4], lineSplit[6], lineSplit[10], lineSplit[14] }; // [family ID, sample ID, child
																						// gt, father gt, mother gt]

			String key = lineSplit[0] + "_" + lineSplit[1]; // chr_pos

			// if pbtEntries already contains key, get existing key and add new family and sample IDs
			// else, create new list and add the new family and sample IDs and put it together with the new chr_pos
			if (pbtEntries.containsKey(key))
			{
				pbtEntries.get(key).add(familySampleGenotypes);
			}
			else
			{
				List<String[]> entries = newArrayList();
				entries.add(familySampleGenotypes);
				pbtEntries.put(key, entries);
			}
		}
		s.close();
		return pbtEntries;
	}

	/**
	 * Reads the VCF file (annotated with ExAC).
	 * 
	 * @param pbtEntries
	 *            HashMap containing chromosome and position + associated info from PBT file
	 * @throws IOException
	 *             when output file or VCF file is not correct
	 */
	public void readVCFwithExAC(Map<String, List<String[]>> pbtEntries) throws IOException
	{
		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

		VcfRepository vcfRepository = new VcfRepository(vcfFile, "vcf");
		Iterator<Entity> vcfWithExac = AnnotatorUtils.annotateWithExac(vcfRepository);

		// Print header one time
		pw.println("GENE_SYMBOL" + "\t" + "EFFECT" + "\t" + "IMPACT" + "\t" + "MOTHER_GT" + "\t" + "FATHER_GT" + "\t"
				+ "CHILD_GT" + "\t" + "FAMILY_ID" + "\t" + "SAMPLE_ID" + "\t" + "pred33wNotch" + "\t" + "pred38" + "\t"
				+ "predAll82" + "\t" + "hpoGenes" + "\t" + "ExAC allele frequency" + "\t" + "homozyg in ExAC" + "\t"
				+ "hets in ExAC" + "\t" + "\t" + "CHROM" + "\t" + "POS" + "\t" + "ID" + "\t" + "REF" + "\t" + "ALT"
				+ "\t" + "QUAL" + "\t" + "FILTER" + "\t" + "INFO");

		int count = 0;
		while (vcfWithExac.hasNext())
		{
			count++;
			if (count % 1000 == 0)
			{
				System.out.println("Scanned over " + count + " lines in VCF...");
			}

			Entity record = vcfWithExac.next();
			try
			{
				combineVCFwithPBT(record, pbtEntries, pw);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Combines the VCF (with ExAC) file with the PBT file.
	 * 
	 * @param record
	 *            Entity containing a line from the VCF file
	 * @param pbtEntries
	 *            HashMap containing chromosome and position + associated info from PBT file
	 * @param pw
	 *            PrintWriter to write output to file
	 * @throws Exception
	 *             when multi-allelic varaint is found
	 */
	public void combineVCFwithPBT(Entity record, Map<String, List<String[]>> pbtEntries, PrintWriter pw)
			throws Exception
	{
		ArrayList<String> variantsWithHighImpact = newArrayList();
		ArrayList<String> variantsWithModerateImpact = newArrayList();
		ArrayList<String> variantsWithLowImpact = newArrayList();
		ArrayList<String> variantsWithModifierImpact = newArrayList();

		// no rounding
		DecimalFormat df = new DecimalFormat("#.##############################");

		String chromosome = record.getString("#CHROM");
		String pos = record.getString("POS");
		String key = chromosome + "_" + pos;

		// if HashMap (from PBT) contains chr_pos (from VCF)
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

			String gene = getColFromInfoField(annField, 3);
			String effect = getColFromInfoField(annField, 1);
			String impact = getColFromInfoField(annField, 2);

			for (String[] entries : pbtEntries.get(key))
			{
				String vcfEntry = VcfUtils.convertToVCF(record, false); // no genotypes

				String printReadyVariant = gene + "\t" + effect + "\t" + impact + "\t" + entries[2] + "\t" + entries[3]
						+ "\t" + entries[4] + "\t" + entries[0] + "\t" + entries[1] + "\t"
						+ containsMultiGene(gene, GenePanelsUtils.pred33wNotch) + "\t"
						+ containsMultiGene(gene, GenePanelsUtils.pred38) + "\t"
						+ containsMultiGene(gene, GenePanelsUtils.predAll82) + "\t"
						+ containsMultiGene(gene, GenePanelsUtils.hpoGenes) + "\t"
						+ (exac_af == null ? "" : df.format(exac_af)) + "\t" + exac_ac_hom_STR + "\t" + exac_ac_het_STR
						+ "\t" + vcfEntry;

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
		}
		printSortedImpact(variantsWithHighImpact, variantsWithModerateImpact, variantsWithLowImpact,
				variantsWithModifierImpact, pw);
	}

	/**
	 * Writes impact to output file, sorted from high to modifier.
	 * 
	 * @param variantsWithHighImpact
	 *            a list containing variants with a high impact
	 * @param variantsWithModerateImpact
	 *            a list containing variants with a moderate impact
	 * @param variantsWithLowImpact
	 *            a list containing variants with a low impact
	 * @param variantsWithModifierImpact
	 *            a list containing variants with a modifier impact
	 * @param pw
	 *            PrintWriter to write output to file
	 */
	public void printSortedImpact(ArrayList<String> variantsWithHighImpact,
			ArrayList<String> variantsWithModerateImpact, ArrayList<String> variantsWithLowImpact,
			ArrayList<String> variantsWithModifierImpact, PrintWriter pw)
	{
		// Impact sorted from HIGH to MODIFIER
		for (String variant : variantsWithHighImpact)
		{
			pw.println(variant);
		}
		pw.flush();

		for (String variant : variantsWithModerateImpact)
		{
			pw.println(variant);
		}
		pw.flush();

		for (String variant : variantsWithLowImpact)
		{
			pw.println(variant);
		}
		pw.flush();

		for (String variant : variantsWithModifierImpact)
		{
			pw.println(variant);
		}
		pw.flush();
		pw.close();
	}

	/**
	 * Returns yes or no according to multiple genes that are found for one variant.
	 * 
	 * @param gene
	 *            String holding the specific gene
	 * @param panel
	 *            List with genes from gene panels
	 * @return res the resulting stringbuffer
	 */
	private String containsMultiGene(String gene, List<String> panel)
	{
		// gene can be comma separated, e.g. "MLH1, MSH2" (2 genes on different strand)

		StringBuffer res = new StringBuffer();
		String[] multiGene = gene.split(", ", -1);
		for (String oneGene : multiGene)
		{
			if (panel.contains(oneGene))
			{
				res.append("yes, ");
			}
			else
			{
				res.append("no, ");
			}
		}
		res.delete(res.length() - 2, res.length());
		return res.toString();
	}
	
	/**
	 * Parses annotation field from VCF file and returns one or multiple columns.
	 * 
	 * @param annField
	 *            the annotation field from the VCF file
	 * @param col
	 *            the column to be parsed
	 * @return StringBuffer containing geneSymbol from specific annotation field
	 */
	private static String getColFromInfoField(String annField, int col)
	{
		StringBuffer sb = new StringBuffer();
		String[] multiAnn = annField.split(","); // for multi-gene!
		for (String oneAnn : multiAnn)
		{
			String[] annSplit = oneAnn.split("\\|", -1);
			sb.append(annSplit[col] + ", ");
		}

		sb.delete(sb.length() - 2, sb.length());

		return sb.toString().trim();
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when files do not exist
	 */
	public static void main(String[] args) throws Exception
	{
		// See http://stackoverflow.com/questions/4787719/spring-console-application-configured-using-annotations
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);
		MergePBTwithVCF mergePBTwithVCF = ctx.getBean(MergePBTwithVCF.class);
		mergePBTwithVCF.parseCommandLineArgs(args);
		Map<String, List<String[]>> pbtEntries = mergePBTwithVCF.readPBT();
		mergePBTwithVCF.readVCFwithExAC(pbtEntries);
		ctx.close();
	}

	/**
	 * Parses command line arguments.
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws Exception
	 *             when the length of the arguments is not 4, or if one of the files does not exists.
	 */
	public void parseCommandLineArgs(String[] args) throws Exception
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

		File outputFile = new File(args[3]);
		if (outputFile.exists())
		{
			System.out.println("WARNING: output file already exists, overwriting " + outputFile);
		}
	}
}
