library(ggplot2)

timeSeriesCV <- function(speeds, formula) {
  n <- nrow(speeds)
  k <- 5
  
  minMonth <- min(speeds$month)
  maxMonth <- max(speeds$month)
  mseSum <- 0
  k <- 0
  for (month in (minMonth + 2):maxMonth) {
    trainingSet <- speeds[speeds$month < month,]
    testSet <- speeds[speeds$month == month,]
    # print(sprintf(
    #   "Training set for month < %d with size %d",
    #   month,
    #   nrow(trainingSet)
    # ))
    # print(sprintf("Test set for month %d with size %d", month, nrow(testSet)))
    tryCatch({
      fit <- glm(data = trainingSet, formula)
      prediction <- predict.glm(fit, newdata = testSet)
      mse <-
        sum((testSet$speed - prediction) ^ 2) / length(prediction)
      # print(sprintf("MSE = %f", mse))
      mseSum <- mseSum + mse
      k <- k + 1
    }, error = function(cond) {
      print(sprintf("skipping CV for month %d: %s", month, cond))
    })
  }
  CVk = mseSum / k
  print(sprintf ("CV(%d) = %f", k, CVk))
  CVk
}

speeds <- read.csv("/tmp/output.csv")

CV <- data.frame()
for (i in 1:20) {
  index <- nrow(CV) + 1
  formula <- speed ~ poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
for (i in 1:22) {
  index <- nrow(CV) + 1
  formula <- speed ~ weekday + poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
for (i in 1:22) {
  index <- nrow(CV) + 1
  formula <- speed ~ schoolfr + poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
for (i in 1:15) {
  index <- nrow(CV) + 1
  formula <- speed ~ schoolfr * poly(minutesDay, j) + schoolfr + poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- "speed ~ schoolfr * poly(minutesDay, j) + schoolfr + poly(minutesDay, i)"
}
for (i in 1:22) {
  index <- nrow(CV) + 1
  formula <- speed ~ weekday + schoolfr + poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
for (i in 1:22) {
  index <- nrow(CV) + 1
  formula <- speed ~ weekday + schoolfr + schoolch + poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
for (i in 1:22) {
  index <- nrow(CV) + 1
  formula <- speed ~ weekday + schoolfr + weather + poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
for (i in 1:22) {
  index <- nrow(CV) + 1
  formula <- speed ~ weekday + holidaySizefr + poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
for (i in 1:11) {
  index <- nrow(CV) + 1
  formula <- speed ~ weekday * poly(minutesDay, j) + schoolfr * poly(minutesDay, j) + weekday + schoolfr + poly(minutesDay, i)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
for (i in 1:20) {
  index <- nrow(CV) + 1
  formula <- speed ~ weekday + schoolfr + poly(minutesDay, i) + weekday * schoolfr * poly(minutesDay, j)
  CV[index, "cv"] <- timeSeriesCV(speeds, formula)
  CV[index, "degree"] <- i
  CV[index, "formula"] <- as.character(formula)[3]
}
plot <- ggplot(data = CV, aes(x=degree, y=cv, color=factor(formula))) + geom_point()  + geom_line()

levels <- levels(as.factor(CV$formula))
for(level in levels) {
  formulaDataset <- CV[CV$formula == level,]
  stdError <- sd(formulaDataset[, "cv"]) / sqrt(nrow(formulaDataset))
  validModels <- formulaDataset[formulaDataset$cv<min(formulaDataset$cv) + stdError,]
  selected <- validModels[1,]
  plot <- plot +
    geom_point(data=selected, shape=18, size = 7)
}
print(plot)


