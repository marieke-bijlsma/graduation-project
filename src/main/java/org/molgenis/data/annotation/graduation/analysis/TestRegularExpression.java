package org.molgenis.data.annotation.graduation.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class tests whether a line matches the regex pattern or not and prints the matching lines.
 * 
 * @author mbijlsma
 */

public class TestRegularExpression
{
	/**
	 * Compares given line with a regex pattern and prints the result.
	 * 
	 */
	private static void testPattern()
	{
		String testLine = "AC=158;AF=0.28;AN=564;BaseQRankSum=-1.41;ClippingRankSum=0.019;DB;DP=59189;FS=15.633;GQ_MEAN=636.49;GQ_STDDEV=1037.13;InbreedingCoeff=-0.3891;MLEAC=158;MLEAF=0.28;MQ=57.01;MQ0=0;MQRankSum=-5.725;NCC=0;NEGATIVE_TRAIN_SITE;POSITIVE_TRAIN_SITE;QD=4.32;ReadPosRankSum=-0.166;SOR=1.175;VQSLOD=-1.976;culprit=MQRankSum;ANN=T|stop_gained|HIGH|TAS2R31|TAS2R31|transcript|NM_176885.2|Coding|1/1|c.900G>A|p.Trp300*|972/1021|900/930|300/309||,T|intron_variant|MODIFIER|PRH1-PRR4|PRH1-PRR4|transcript|NR_037918.2|Noncoding|3/9|n.477+4346G>A||||||,T|intron_variant|MODIFIER|PRH1|PRH1|transcript|NM_001291315.1|Coding|2/5|c.36+16584G>A||||||;";

		Pattern pattern = Pattern.compile("(HIGH|MODERATE|LOW|MODIFIER)(\\|)([\\w]*[-]*[\\w]*)(\\|)");

		Matcher matcher = pattern.matcher(testLine); // match gene

		while (matcher.find())
		{
			System.out.println(matcher.group(3));
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the command line args
	 */
	public static void main(String[] args)
	{
		testPattern();
	}
}
