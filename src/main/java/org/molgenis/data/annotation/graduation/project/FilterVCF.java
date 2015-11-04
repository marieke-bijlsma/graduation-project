package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.cmd.CommandLineAnnotatorConfig;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class FilterVCF
{	
	public void go(File vcfFile, File outputFile) throws Exception
	{
		@SuppressWarnings("resource")
		PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
		
		@SuppressWarnings("resource")
		Iterator<Entity> vcf = new VcfRepository(vcfFile, "vcf").iterator();

		// decimal format for double, otherwise can't do calculations
		DecimalFormat df = new DecimalFormat("#.##############################");

		while (vcf.hasNext())
		{
			Entity record = vcf.next();

			// get filter column and filter all that are not PASS
			String filter = record.getString("FILTER");

			if (!(filter.equals("PASS")))
			{
				continue;
			}

			// Get ANN field and split it to get impact and filter low and modifier
			String annField = record.getString("ANN");
			String[] splitAnnField = annField.split("\\|");
			String impact = splitAnnField[2];

			if (impact.equals("LOW"))
			{
				continue;
			}

			if (impact.equals("MODIFIER"))
			{
				continue;
			}

			// First get all "not null" ExAC AF to String
			// Then, convert all "not null" to Doubles
			// Decimal format (String) for calculations
			// Also check for comma (one value contains comma (0.005493,0.013)

			String exacAFStr = record.get("EXAC_AF") == null || record.get("EXAC_AF").toString().contains(",") ? null : record
					.get("EXAC_AF").toString();
			Double exacAF = exacAFStr != null ? Double.parseDouble(exacAFStr) : null;
			String decimalExacAF = exacAF == null ? "" : df.format(exacAF);

			if (!(decimalExacAF == "") && ((Double.parseDouble(decimalExacAF) > 0.05)))
			{
				continue;
			}
			
			//Filter CADD score <10?
			//Filter GoNL and 1000G
			
			//convert to VCF entry and print to new file
			String vcfEntry = VcfUtils.convertToVCF(record, false);			
			pw.println(vcfEntry);
		}
	}

	public static void main(String[] args) throws Exception
	{
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		ctx.register(CommandLineAnnotatorConfig.class);
		FilterVCF main = ctx.getBean(FilterVCF.class);
		main.run(args);
		ctx.close();
	}

	public void run(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		File vcfFile = new File(args[0]);
		if (!vcfFile.isFile())
		{
			throw new Exception("VCF file does not exist or directory: " + vcfFile.getAbsolutePath());
		}
		
		File outputFile = new File(args[1]);
		if (!outputFile.isFile())
		{
			throw new Exception("Output file does not exist or directory: " + outputFile.getAbsolutePath());
		}

		FilterVCF fv = new FilterVCF();
		fv.go(vcfFile, outputFile);

	}

}
