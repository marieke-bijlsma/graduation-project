## computing a simple ROC curve (x-axis: fpr, y-axis: tpr) 
# install.packages("gplots") 
library(ROCR) 
data(ROCR.simple)

pred <- prediction( ROCR.simple$predictions, ROCR.simple$labels)
perf <- performance(pred,"tpr","fpr")
plot(perf)
## precision/recall curve (x-axis: recall, y-axis: precision)
perf1 <- performance(pred, "prec", "rec")
plot(perf1)
## sensitivity/specificity curve (x-axis: specificity,
## y-axis: sensitivity)
perf1 <- performance(pred, "sens", "spec")
plot(perf1)

data <- read.table("/Users/molgenis/Documents/graduation_project/mendelian_violation_adjusted_replicates.txt", header=T)
transPos <- as.list(data[,5])
total <- length(transPos)

minTransPos <- min(transPos)
maxTransPos <- max(transPos)

goodCount <- 0
wrongCount <- 0
# predictions <- list(0)
# labels <- list(0)

for(tp in transPos){
  if(tp >= minTransPos){
    goodCount =+1
    minTransPos <- minTransPos + 1
    # tp + 1
  }
  else{
    wrongCount =+1
    minTransPos <- minTransPos + 1
    # tp + 1
  }
  predictions <- list(goodCount, wrongCount)
  labels <- list(1,0)
}


# predictions <- c(1,2,3,4)
# labels <- c(1,0,1,0)
# pred <- prediction(predictions, labels)
# perf <- performance(pred, "tpr", "fpr")
# plot(perf)
