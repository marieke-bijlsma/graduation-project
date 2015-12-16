 #!/bin/bash
 
 # Run vcf-fix-ploidy for fixing X chromosome of trio data.
 # Concatenate output with input data and write to new file.
 
 cat Joeri_exome.variant.calls.GATK.sorted.alldiploid.vcf | vcf-fix-ploidy -a F -s 282exomes_sample_geslachten.tsv > Joeri_exome.variant.calls.GATK.sorted.fixedPloidy.vcf
