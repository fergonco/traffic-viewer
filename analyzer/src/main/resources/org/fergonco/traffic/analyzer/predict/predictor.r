modelFilename <- commandArgs(TRUE)[1]
predictDatasetFilename <- commandArgs(TRUE)[2]

fit <- readRDS(modelFilename)
predictDataset <- read.csv(predictDatasetFilename)

print(predict.lm(fit, newdata = predictDataset, interval = "prediction", level = 0.95))

