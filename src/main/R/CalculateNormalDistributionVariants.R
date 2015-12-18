##Create normal distribution plot of number of variants per patient.

data <- read.table("/Users/molgenis/Documents/graduation_project/output_variantCountPerPatient.txt", header=T)

min <- min(data$variant_count)
max <- max(data$variant_count)
xseq <- seq(-50, 100, 1)

mean <- mean(data$variant_count)
sd <- sd(data$variant_count)
densities <- dnorm(xseq, mean, sd)

plot(xseq, densities, col="red", xlab="variant count", ylab = "density", type="l", lwd=2, main="normal distribution over variant counts per patient")