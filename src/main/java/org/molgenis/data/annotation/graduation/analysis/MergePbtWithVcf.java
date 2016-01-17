package org.molgenis.data.annotation.graduation.analysis;

import static java.lang.Double.parseDouble;
import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.HIGH;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.LOW;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.MODERATE;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.MODIFIER;
import static org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact.valueOf;
import static org.molgenis.data.annotation.graduation.utils.AnnotatorUtils.annotateWithExac;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.utils.VcfUtils.convertToVCF;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator.Impact;
import org.molgenis.data.annotation.graduation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.graduation.utils.FileReadUtils;
import org.molgenis.data.annotation.graduation.utils.GenePanelsUtils;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class reads the PBT and VCF file and merges them by looking at the chromosome and position of the variants.
 * 
 * @author mbijlsma
 */

@Component
public class MergePbtWithVcf
{
	private File pbtFile;
	private File vcfFile;
	private File exacFile;
	private File outputFile;

	private boolean printedHeader = false;

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
		for (String record : FileReadUtils.readFile(pbtFile, false))
		{
			String[] recordSplit = record.split("\t", -1);

			String[] familySampleGenotypes =
			{ recordSplit[3], recordSplit[4], recordSplit[6], recordSplit[10], recordSplit[14] }; // [family ID, sample
																									// ID, child
			// gt, father gt, mother gt]

			String key = recordSplit[0] + "_" + recordSplit[1]; // chr_pos

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

		return pbtEntries;
	}

	/**
	 * Reads the VCF file (annotated with ExAC).
	 * 
	 * @param pbtEntries
	 *            HashMap containing chromosome and position + associated info from PBT file
	 * @throws Exception
	 */
	public void annotateVcfWithExac() throws Exception
	{
		Map<String, List<String[]>> pbtEntries = readPBT();

		VcfRepository vcfRepository = new VcfRepository(vcfFile, "vcf");
		Iterator<Entity> vcfWithExac = annotateWithExac(vcfRepository);

		int count = 0;
		while (vcfWithExac.hasNext())
		{
			Entity record = vcfWithExac.next();

			count++;
			if (count % 1000 == 0)
			{
				System.out.println("Scanned over " + count + " lines in VCF...");
			}

			String key = record.getString(CHROM) + "_" + record.getString(POS);
			if (pbtEntries.containsKey(key))
			{
				combineVCFwithPBT(record, pbtEntries.get(key));
			}
		}
	}

	/**
	 * Combines the VCF (with ExAC) file with the PBT file.
	 * 
	 * @param record
	 *            Entity containing a line from the VCF file
	 * @param list
	 *            HashMap containing chromosome and position + associated info from PBT file
	 * @param printWriter
	 *            PrintWriter to write output to file
	 * @throws Exception
	 *             when multi-allelic varaint is found
	 */
	public void combineVCFwithPBT(Entity record, List<String[]> entries) throws Exception
	{
		ArrayList<String> variantsWithHighImpact = newArrayList();
		ArrayList<String> variantsWithModerateImpact = newArrayList();
		ArrayList<String> variantsWithLowImpact = newArrayList();
		ArrayList<String> variantsWithModifierImpact = newArrayList();

		// no rounding
		DecimalFormat df = new DecimalFormat("#.##############################");

		// for every entry (famID, samID) in HashMap values, get associated VCF data (without genotypes)
		// add samID and famID after VCF data, separated by tab and print all

		if (record.getString(ALT).contains(","))
		{
			throw new Exception("WARNING !! multi allelic variant, needs manual checking: "
					+ convertToVCF(record, false));
		}

		String annField = record.getString("ANN");
		String exac_af_STR = record.get("EXAC_AF") == null ? null : record.getString("EXAC_AF");
		String exac_ac_hom_STR = record.get("EXAC_AC_HOM") == null ? "" : record.getString("EXAC_AC_HOM");
		String exac_ac_het_STR = record.get("EXAC_AC_HET") == null ? "" : record.getString("EXAC_AC_HET");

		Double exac_af = exac_af_STR != null ? parseDouble(exac_af_STR) : null;

		String gene = getColumnFromInfoField(annField, 3);
		String effect = getColumnFromInfoField(annField, 1);
		Impact impact = valueOf(getColumnFromInfoField(annField, 2));

		for (String[] entry : entries)
		{
			String vcfEntry = convertToVCF(record, false); // no genotypes

			String printReadyVariant = gene + "\t" + effect + "\t" + impact + "\t" + entry[2] + "\t" + entry[3] + "\t"
					+ entry[4] + "\t" + entry[0] + "\t" + entry[1] + "\t"
					+ containsMultiGene(gene, GenePanelsUtils.pred33wNotch) + "\t"
					+ containsMultiGene(gene, GenePanelsUtils.pred38) + "\t"
					+ containsMultiGene(gene, GenePanelsUtils.predAll82) + "\t"
					+ containsMultiGene(gene, GenePanelsUtils.hpoGenes) + "\t"
					+ (exac_af == null ? "" : df.format(exac_af)) + "\t" + exac_ac_hom_STR + "\t" + exac_ac_het_STR
					+ "\t" + vcfEntry;

			if (impact.equals(HIGH))
			{
				variantsWithHighImpact.add(printReadyVariant);
			}
			else if (impact.equals(MODERATE))
			{
				variantsWithModerateImpact.add(printReadyVariant);
			}
			else if (impact.equals(LOW))
			{
				variantsWithLowImpact.add(printReadyVariant);
			}
			else if (impact.equals(MODIFIER))
			{
				variantsWithModifierImpact.add(printReadyVariant);
			}
			else
			{
				throw new Exception("UNKNOWN IMPACT: " + impact);
			}

		}

		printSortedImpact(variantsWithHighImpact, variantsWithModerateImpact, variantsWithLowImpact,
				variantsWithModifierImpact);
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
	 * @throws IOException
	 */
	public void printSortedImpact(ArrayList<String> variantsWithHighImpact,
			ArrayList<String> variantsWithModerateImpact, ArrayList<String> variantsWithLowImpact,
			ArrayList<String> variantsWithModifierImpact) throws IOException
	{
		FileWriter fileWriter = new FileWriter(outputFile, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		if (!printedHeader)
		{
			// Print header one time
			bufferedWriter.append("GENE_SYMBOL" + "\t" + "EFFECT" + "\t" + "IMPACT" + "\t" + "MOTHER_GT" + "\t"
					+ "FATHER_GT" + "\t" + "CHILD_GT" + "\t" + "FAMILY_ID" + "\t" + "SAMPLE_ID" + "\t" + "pred33wNotch"
					+ "\t" + "pred38" + "\t" + "predAll82" + "\t" + "hpoGenes" + "\t" + "ExAC allele frequency" + "\t"
					+ "homozyg in ExAC" + "\t" + "hets in ExAC" + "\t" + "CHROM" + "\t" + "POS" + "\t" + "ID"
					+ "\t" + "REF" + "\t" + "ALT" + "\t" + "QUAL" + "\t" + "FILTER" + "\t" + "INFO\n");
			printedHeader = true;
		}

		// Impact sorted from HIGH to MODIFIER
		for (String variant : variantsWithHighImpact)
		{
			bufferedWriter.append(variant + "\n");
		}

		for (String variant : variantsWithModerateImpact)
		{
			bufferedWriter.append(variant + "\n");
		}

		for (String variant : variantsWithLowImpact)
		{
			bufferedWriter.append(variant + "\n");
		}

		for (String variant : variantsWithModifierImpact)
		{
			bufferedWriter.append(variant + "\n");
		}

		bufferedWriter.close();
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
		StringBuilder stringBuilder = new StringBuilder();
		String[] multiGene = gene.split(", ", -1);
		for (String oneGene : multiGene)
		{
			if (panel.contains(oneGene))
			{
				stringBuilder.append("yes, ");
			}
			else
			{
				stringBuilder.append("no, ");
			}
		}
		return stringBuilder.substring(0, stringBuilder.length() - 2);
	}

	/**
	 * Parses annotation field from VCF file and returns one or multiple columns.
	 * 
	 * @param annotationField
	 *            the annotation field from the VCF file
	 * @param index
	 *            the column to be parsed
	 * @return StringBuffer containing geneSymbol from specific annotation field
	 */
	private String getColumnFromInfoField(String annotationField, int index)
	{
		StringBuilder stringBuilder = new StringBuilder();
		String[] multiAnnotationField = annotationField.split(","); // for multi-gene!
		for (String oneAnnotationField : multiAnnotationField)
		{
			String[] annSplit = oneAnnotationField.split("\\|", -1);
			stringBuilder.append(annSplit[index] + ", ");
		}
		return stringBuilder.substring(0, stringBuilder.length() - 2);
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
		AnnotationConfigApplicationContext context = AnnotatorUtils.registerCommandLineAnnotator();
		MergePbtWithVcf mergePBTwithVCF = context.getBean(MergePbtWithVcf.class);

		mergePBTwithVCF.parseCommandLineArgs(args);

		mergePBTwithVCF.annotateVcfWithExac();

		context.close();
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

		vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("Input VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}

		pbtFile = new File(args[1]);
		if (!pbtFile.isFile())
		{
			throw new Exception("PBT file does not exist or directory: " + pbtFile.getAbsolutePath());
		}

		exacFile = new File(args[2]);
		if (!exacFile.isFile())
		{
			throw new Exception("exac file does not exist or directory: " + exacFile.getAbsolutePath());
		}

		outputFile = new File(args[3]);
		if (!outputFile.exists())
		{
			outputFile.createNewFile();
		}
		else
		{
			System.out.println("WARNING: output file already exists, overwriting " + outputFile);
		}
	}
}