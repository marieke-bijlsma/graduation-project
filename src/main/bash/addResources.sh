#!/bin/bash

# Add relevant resources to trio data (GoNL, ExAC, 1000G, CADD).
# Each job has to wait until previous job was successfully completed. 
# Keep track of job using log file.

module load jdk/1.8.0_25

nohup java -Xmx4g -jar CmdLineAnnotator-1.12.0.jar -a gonl -s /gcc/groups/pub/prm02/resources/gonl/release5_noContam_noChildren_with_AN_AC_GTC_stripped/ -i Joeri_exome.variant.calls.GATK.sorted.PBT.alldiploid_snpeff.vcf -o Joeri_exome.variant.calls.GATK.sorted.PBT.alldiploid.snpeff.gonl.vcf > log.txt &&

nohup java -Xmx4g -jar CmdLineAnnotator-1.12.0.jar -a exac -s /gcc/groups/pub/prm02/resources/exac/ExAC.r0.3.sites.vep.vcf.gz -i Joeri_exome.variant.calls.GATK.sorted.PBT.alldiploid.snpeff.gonl.vcf -o Joeri_exome.variant.calls.GATK.sorted.PBT.alldiploid.snpeff.gonl.exac.vcf > log.txt &&

nohup java -Xmx4g -jar CmdLineAnnotator-1.12.0.jar -a 1000g -s /gcc/groups/pub/prm02/resources/1000G/1000G.tsv.gz -i Joeri_exome.variant.calls.GATK.sorted.PBT.alldiploid.snpeff.gonl.exac.vcf -o Joeri_exome.variant.calls.GATK.sorted.PBT.alldiploid.snpeff.gonl.exac.1000g.vcf > log.txt &&

nohup java -Xmx4g -jar CmdLineAnnotator-1.12.0.jar -a cadd -s /gcc/groups/pub/prm02/resources/cadd/whole_genome_SNVs.tsv.gz -i Joeri_exome.variant.calls.GATK.sorted.PBT.alldiploid.snpeff.gonl.exac.1000g.vcf -o Joeri_exome.variant.calls.GATK.sorted.PBT.alldiploid.snpeff.gonl.exac.1000g.caddSNVs.vcf > log.txt
