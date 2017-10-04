forecastFolder <- commandArgs(TRUE)[1]

forecastDataset <- read.csv(paste(forecastFolder, "/dataset.csv", sep = ""))

files <- list.files(forecastFolder, pattern = "*.rds");

lapply(files, function(file){
  fileName <- paste(forecastFolder, "/", file, sep = "")
  fit <- readRDS(fileName)
  predictions <- predict.glm(fit, newdata = forecastDataset) # was valid for predict.lm, not anymore: , interval = "prediction", level = 0.95)
  matches <- regmatches(file, regexec("(\\d*).rds", file))
  id <- matches[[1]][2]
  for (i in 1:nrow(forecastDataset)){
    print(paste("Result", id, forecastDataset[i, "timestamp"], predictions[i], sep="|"))
  }
})
