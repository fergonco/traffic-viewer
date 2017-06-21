
datasetName <- commandArgs(TRUE)[1]
datasetName <- "/home/fergonco/b/java/geomatico/traffic/analyzer/analyse/tocern.csv"
speeds <- read.csv(datasetName)
fit <- lm(data = speeds, speed ~ distortedMinutes * weekday + weekday * weather)
fitSummary <- summary(fit)
coefficients<-fitSummary$coefficients

significant <- coefficients[coefficients[, "Pr(>|t|)"] < 0.05, ]
#print(significant[,"Estimate"])
coefficientNames <- rownames(significant)
for(name in coefficientNames) {
  print(paste(name, fit$coefficients[name]))
}