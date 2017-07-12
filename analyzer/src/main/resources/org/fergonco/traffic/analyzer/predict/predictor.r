forecastFolder <- commandArgs(TRUE)[1]

forecastDataset <- read.csv(paste(forecastFolder, "/dataset.csv", sep = ""))

files <- list.files(forecastFolder, pattern = "*.rds");

lapply(files, function(file){
  fileName <- paste(forecastFolder, "/", file, sep = "")
  fit <- readRDS(fileName)
  predictions <- predict.lm(fit, newdata = forecastDataset, interval = "prediction", level = 0.95)
  matches <- regmatches(file, regexec("(\\d*).rds", file))
  id <- matches[[1]][2]
  for (i in 1:nrow(forecastDataset)){
    print(paste("Result", id, forecastDataset[i, "timestamp"], predictions[i, 1], predictions[i, 2], predictions[i, 3], sep="|"))
  }
})
