#!/bin/bash

# Run PBT with VQSR output for phasing genotypes of trios.
# Keep track of job using log file.

module load GATK

nohup java -Xmx2g -jar $GATK_HOME/GenomeAnalysisTK.jar -R /gcc/groups/gdio/prm02/projects/aortic_valve_malformation_trios/human_g1k_v37/human_g1k_v37.fasta -T PhaseByTransmission -V ./output_VQSR/output_snps_indels_AVM_recalibrated_filtered_ENDRESULT.vcf -ped aortic_valve_malf_trios_allSamples.ped -pedValidationType STRICT -o ./output_phaseByTransmission/output_phaseByTransmission_withX.vcf --MendelianViolationsFile ./output_phaseByTransmission/mendelian_violation_withX.txt > log.txt
