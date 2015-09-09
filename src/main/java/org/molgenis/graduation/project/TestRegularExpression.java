package org.molgenis.graduation.project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegularExpression
{

	public static void main(String[] args)
	{
		String testLIne = "AC=158;AF=0.28;AN=564;BaseQRankSum=-1.41;ClippingRankSum=0.019;DB;DP=59189;FS=15.633;GQ_MEAN=636.49;GQ_STDDEV=1037.13;InbreedingCoeff=-0.3891;MLEAC=158;MLEAF=0.28;MQ=57.01;MQ0=0;MQRankSum=-5.725;NCC=0;NEGATIVE_TRAIN_SITE;POSITIVE_TRAIN_SITE;QD=4.32;ReadPosRankSum=-0.166;SOR=1.175;VQSLOD=-1.976;culprit=MQRankSum;ANN=T|stop_gained|HIGH|TAS2R31|TAS2R31|transcript|NM_176885.2|Coding|1/1|c.900G>A|p.Trp300*|972/1021|900/930|300/309||,T|intron_variant|MODIFIER|PRH1-PRR4|PRH1-PRR4|transcript|NR_037918.2|Noncoding|3/9|n.477+4346G>A||||||,T|intron_variant|MODIFIER|PRH1|PRH1|transcript|NM_001291315.1|Coding|2/5|c.36+16584G>A||||||;";

		Pattern pat = Pattern.compile("(HIGH|MODERATE|LOW|MODIFIER)(\\|)([\\w]*[-]*[\\w]*)(\\|)");

		Matcher matcher = pat.matcher(testLIne);

		while (matcher.find())
		{
			System.out.println(matcher.group(3));
		}

	}

}
