
datasetName <- commandArgs(TRUE)[1]
modelFilename <- commandArgs(TRUE)[2]

# datasetName <- "/home/fergonco/b/java/geomatico/traffic/analyzer/analyse/tocern.csv"
# modelFilename <- "/tmp/model.rds"

speeds <- read.csv(datasetName)

# fit <- lm(data = speeds, speed ~ distortedMinutes * weekday + weekday * weather)
# # fit$rank <- c()
# fit$residuals <- c()
# fit$effects <- c()
# fit$fitted.values <- c()
# fit$assign <- c()
# fit$model <- c()
# #fit$df.residual <- c()
# fit$contrasts <- c()
# fit$xlevels <- c()
# fit$call <- c()
# # fit$qr <- c()
# attr(fit$terms,".Environment") = c()
# saveRDS(fit, file = modelFilename)
# fit2 <- readRDS(modelFilename)
# prediction <- predict.lm(fit2, newdata = speeds[22,], interval = "prediction", level = 0.95)

fit <- glm(data = speeds, speed ~ distortedMinutes * weekday + weekday * weather)
fit$y = c()
fit$model = c()
fit$residuals = c()
fit$fitted.values = c()
fit$effects = c()
fit$qr$qr = c()
fit$linear.predictors = c()
fit$weights = c()
fit$prior.weights = c()
fit$data = c()
fit$family$variance = c()
fit$family$dev.resids = c()
fit$family$aic = c()
fit$family$validmu = c()
fit$family$simulate = c()
attr(fit$terms,".Environment") = c()
attr(fit$formula,".Environment") = c()
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