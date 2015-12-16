#!/bin/bash

# Run SnpEff for annotating variants.

module load snpEff

java -Xmx4g -jar $SNPEFF_HOME/snpEff.jar hg19 -noStats -noLog -lof -canon -ud 0 Joeri_exome.variant.calls.GATK.sorted.alldiploid.vcf > Joeri_exome.variant.calls.GATK.sorted.alldiploid_snpeff.vcf