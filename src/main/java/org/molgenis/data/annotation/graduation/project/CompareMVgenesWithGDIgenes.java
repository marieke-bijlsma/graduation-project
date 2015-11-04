package org.molgenis.data.annotation.graduation.project;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import org.elasticsearch.common.collect.Lists;

public class CompareMVgenesWithGDIgenes
{
	public void go(File mvFile, File gdiFile) throws Exception
	{
		List<String> gdiGenes = Lists.newArrayList();
		
		Scanner scanGdi = new Scanner(gdiFile);
		String gdiLine = null;
		
		while (scanGdi.hasNextLine())
		{
			gdiLine = scanGdi.nextLine();
			String[] lineSplit = gdiLine.split("\t", -1);
			gdiGenes.add(lineSplit[0]);
		}
		scanGdi.close();
		
		Scanner scanMv = new Scanner(mvFile);
		String mvLine = null;
		scanMv.nextLine(); // skip header
		
		int count=0;
		while (scanMv.hasNextLine())
		{
			mvLine = scanMv.nextLine();
			String[] lineSplit = mvLine.split("\t", -1);
			String geneSymbol = lineSplit[0];
			
			if(gdiGenes.contains(geneSymbol)){
				continue;
			}
			else{
				count++;
			}
		}
		System.out.println(count);
		scanMv.close();

	}

	public static void main(String[] args) throws Exception
	{
		if (!(args.length == 2))
		{
			throw new Exception("Must supply 2 arguments");
		}

		File mvFile = new File(args[0]);
		if (!mvFile.isFile())
		{
			throw new Exception("Input mendelian violation file does not exist or directory: " + mvFile.getAbsolutePath());
		}
		File gdiFile = new File(args[1]);
		if (!gdiFile.isFile())
		{
			throw new Exception("Input GDI file does not exist or directory: " + gdiFile.getAbsolutePath());
		}

		CompareMVgenesWithGDIgenes cmg = new CompareMVgenesWithGDIgenes();
		cmg.go(mvFile, gdiFile);

	}

}
