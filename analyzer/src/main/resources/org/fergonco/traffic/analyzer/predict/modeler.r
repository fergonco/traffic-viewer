
datasetName <- commandArgs(TRUE)[1]
modelFilename <- commandArgs(TRUE)[2]

# datasetName <- "/home/fergonco/b/java/geomatico/traffic/analyzer/analyse/tocern.csv"
# modelFilename <- "/tmp/model.rda"

speeds <- read.csv(datasetName)
fit <- lm(data = speeds, speed ~ distortedMinutes * weekday + weekday * weather)

saveRDS(fit, file = modelFilename)

## Shows a easily parsable list of coefficients
# fitSummary <- summary(fit)
# coefficients<-fitSummary$coefficients
# significant <- coefficients[coefficients[, "Pr(>|t|)"] < 0.05, ]
# #print(significant[,"Estimate"])
# coefficientNames <- rownames(significant)
# for(name in coefficientNames) {
#   print(paste(name, fit$coefficients[name]))
# }