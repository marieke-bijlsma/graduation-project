#!/bin/bash

# Run VR and AR of VQSR (SNPs & Indels) for recalibrating variants.
# Each job has to wait until previous job was successfully completed. 
# Keep track of job using log file.

module load GATK

nohup java -Xmx4g -jar $GATK_HOME/GenomeAnalysisTK.jar -T VariantRecalibrator -R /gcc/groups/gdio/prm02/projects/aortic_valve_malformation_trios/human_g1k_v37/human_g1k_v37.fasta -input /gcc/groups/gcc/prm02/projects/Joeri_exome/test01/Joeri_exome.variant.calls.GATK.sorted.vcf.gz -recalFile output_snps_variantRecalibrator_AVM.recal -tranchesFile output_snps_variantRecalibrator_AVM.tranches -rscriptFile output_snps_variantRecalibrator_AVM_plots.R -nt 4 -resource:hapmap,known=false,training=true,truth=true,prior=15.0 /gcc/resources/b37/snp/hapmap/hapmap_3.3.b37.vcf -resource:omni,known=false,training=true,truth=true,prior=12.0 /gcc/resources/b37/snp/1000G/1000G_omni2.5.b37.vcf -resource:1000G,known=false,training=true,truth=false,prior=10.0 /gcc/resources/b37/snp/1000G/1000G_phase1.snps.high_confidence.b37.vcf -resource:dbsnp,known=true,training=false,truth=false,prior=2.0 /gcc/resources/b37/snp/dbSNP/dbsnp_138.b37.vcf -an QD -an MQ -an MQRankSum -an ReadPosRankSum -an FS -an InbreedingCoeff -mode SNP > log.txt &&

nohup java -Xmx3g -jar $GATK_HOME/GenomeAnalysisTK.jar -T ApplyRecalibration -R /gcc/groups/gdio/prm02/projects/aortic_valve_malformation_trios/human_g1k_v37/human_g1k_v37.fasta -input /gcc/groups/gcc/prm02/projects/Joeri_exome/test01/Joeri_exome.variant.calls.GATK.sorted.vcf.gz -recalFile ./output_VQSR_AVM/output_snps_variantRecalibrator_AVM.recal -tranchesFile ./output_VQSR_AVM/output_snps_variantRecalibrator_AVM.tranches -o ./output_VQSR_AVM/output_snps_AVM_recalibrated_filtered.vcf -ts_filter_level 99.5 -mode SNP && > log.txt

nohup java -Xmx4g -jar $GATK_HOME/GenomeAnalysisTK.jar -T VariantRecalibrator -R /gcc/groups/gdio/prm02/projects/aortic_valve_malformation_trios/human_g1k_v37/human_g1k_v37.fasta -input /gcc/groups/gcc/prm02/projects/Joeri_exome/test01/Joeri_exome.variant.calls.GATK.sorted.vcf.gz -recalFile output_indels_variantRecalibrator_AVM.recal -tranchesFile output_indels_variantRecalibrator_AVM.tranches --maxGaussians 4 -resource:dbsnp,known=true,training=false,truth=false,prior=2.0 /gcc/resources/b37/snp/dbSNP/dbsnp_138.b37.vcf -resource:mills,known=false,training=true,truth=true,prior=12.0 /gcc/resources/b37/sv/1000G/Mills_and_1000G_gold_standard.indels.b37.vcf -an QD -an FS -an ReadPosRankSum -an MQRankSum -an InbreedingCoeff -mode INDEL > log.txt &&

nohup java -Xmx3g -jar $GATK_HOME/GenomeAnalysisTK.jar -T ApplyRecalibration -R /gcc/groups/gdio/prm02/projects/aortic_valve_malformation_trios/human_g1k_v37/human_g1k_v37.fasta -input ./output_VQSR_AVM/output_snps_AVM_recalibrated_filtered.vcf -recalFile ./output_VQSR_AVM/output_indels_variantRecalibrator_AVM.recal -tranchesFile ./output_VQSR_AVM/output_indels_variantRecalibrator_AVM.tranches -o output_indels_AVM_recalibrated_filtered_ENDRESULT.vcf -ts_filter_level 99.0 -mode INDEL > log.txt

