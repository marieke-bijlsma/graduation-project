package org.molgenis.graduation.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class getGeneSymbolColumn
{
	public static void main(String[] args) throws FileNotFoundException
	{
		readFile();
	}

	private static void readFile() throws FileNotFoundException
	{

		ArrayList<String> lines = new ArrayList<String>();
		Scanner s = new Scanner(new File(
				"/Users/molgenis/Documents/graduation_project/mendelian_violation_adjusted_vcfrecords_noGenotypes.txt"));

		while (s.hasNextLine())
		{
			String line = s.nextLine();
			// System.out.println(line);
			lines.add(line);
		}

		// Variant can have multiple gene symbols with different impacts
		// Variant then contains multiple times the impact, followed by different gene symbols

		for (String line : lines)
		{
			String[] split = line.split("\t");

			String infoCol = split[7];
			
			String infoFields[] = infoCol.split(";", -1);
			String annField = null;
			for(String infoField : infoFields)
			{
				if(infoField.startsWith("ANN="))
				{
					annField = infoField;
					break;
				}
			}
			
			StringBuffer sb = new StringBuffer();
			String[] multiAnn = annField.split(",");
			for(String oneAnn : multiAnn)
			{
				String[] annSplit = oneAnn.split("\\|", -1);
				sb.append(annSplit[3] + " (" +annSplit[1] + "), ");
			}
			
			sb.delete(sb.length() - 2, sb.length());
			
//			if (geneSb.length() >= 2)
//			{
//				geneSb.delete(geneSb.length() - 2, geneSb.length());
//
//			}
			
			System.out.println(sb.toString().trim());

//			Pattern pat = Pattern.compile("\\|(.*?)\\|(HIGH|MODERATE|LOW|MODIFIER)\\|(.*?)\\|");
//			Matcher mat = pat.matcher(impact);
//
//			StringBuffer geneSb = new StringBuffer();
//			StringBuffer impactSB = new StringBuffer();
//
//			while (mat.find())
//			{
//				geneSb.append(mat.group(3) + ", ");
//				impactSB.append(mat.group(1) + ", ");
//			}
//			
//			if (geneSb.length() >= 2)
//			{
//				geneSb.delete(geneSb.length() - 2, geneSb.length());
//
//			}
//			System.out.println(geneSb.toString() + "\t" + impactSB.toString());
//			
			
		}
	}
}
