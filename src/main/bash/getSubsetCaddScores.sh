#!/bin/bash

# Get a subset of the CADD scores to test Java code with, before running the whole script on the command line.
# Each job has to wait until previous job was successfully completed.
# Append all outputs to same file. 

module load vcftools

tabix /gcc/groups/pub/prm02/resources/cadd/whole_genome_SNVs.tsv.gz 1:100000-110000 > subsetCaddScores.txt &&
tabix /gcc/groups/pub/prm02/resources/cadd/whole_genome_SNVs.tsv.gz 4:100000-110000 >> subsetCaddScores.txt &&
tabix /gcc/groups/pub/prm02/resources/cadd/whole_genome_SNVs.tsv.gz 10:100000-110000 >> subsetCaddScores.txt &&
tabix /gcc/groups/pub/prm02/resources/cadd/whole_genome_SNVs.tsv.gz 16:10000-100000 >> subsetCaddScores.txt &&
tabix /gcc/groups/pub/prm02/resources/cadd/whole_genome_SNVs.tsv.gz 18:100000-110000 >> subsetCaddScores.txt &&
tabix /gcc/groups/pub/prm02/resources/cadd/whole_genome_SNVs.tsv.gz X:10000-100000 >> subsetCaddScores.txt &&