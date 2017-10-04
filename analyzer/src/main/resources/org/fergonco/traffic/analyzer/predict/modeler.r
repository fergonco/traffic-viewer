
datasetName <- commandArgs(TRUE)[1]
modelFilename <- commandArgs(TRUE)[2]

# datasetName <- "/home/fergonco/b/java/geomatico/traffic/analyzer/analyse/tocern.csv"
# modelFilename <- "/tmp/model.rds"

speeds <- read.csv(datasetName)

fit <- glm(data = speeds, speed ~ weekday + schoolfr + poly(minutesDay, 14) + weekday * minutesDay)
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
#attr(fit$terms,".Environment") = c() # It wont accept poly at prediction
attr(fit$formula,".Environment") = c()
saveRDS(fit, file = modelFilename)

