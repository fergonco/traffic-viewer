forecastFolder <- commandArgs(TRUE)[1]

forecastDataset <- read.csv(paste(forecastFolder, "/dataset.csv", sep = ""))

files <- list.files(forecastFolder, pattern = "*.rds");

lapply(files, function(file){
  fit <- readRDS(paste(forecastFolder, "/", file, sep = ""))
  predictions <- predict.lm(fit, newdata = forecastDataset, interval = "prediction", level = 0.95)
  id <- strsplit(file, "\\.")[[1]][1]
  for (i in 1:nrow(forecastDataset)){
    print(paste("Result", id, forecastDataset[i, "timestamp"], predictions[i, 1], predictions[i, 2], predictions[i, 3], sep="|"))
  }
})
