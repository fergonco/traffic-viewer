forecastFolder <- commandArgs(TRUE)[1]

print(forecastFolder)
print(paste(forecastFolder, "/dataset.csv", sep = ""))
forecastDataset <- read.csv(paste(forecastFolder, "/dataset.csv", sep = ""))

files <- list.files(forecastFolder, pattern = "*.rds");

lapply(files, function(file){
  fit <- readRDS(paste(forecastFolder, "/", file, sep = ""))
  prediction <- predict.lm(fit, newdata = forecastDataset, interval = "prediction", level = 0.95)
  id <- strsplit(file, "\\.")[[1]][1]
  print(paste("Result", id, prediction[1,1], prediction[1,2], prediction[1,3], sep="|"))
})
