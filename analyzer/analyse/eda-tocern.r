library(ggplot2)
library(gridExtra)
library(arm)

speeds <- read.csv("tocern.csv")

histograms <- function() {
  plots <- list()
  # response variable histogram
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed)) + geom_density()
  
  # response variable histogram coloured and faceted by a predictor variable
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed, fill=weekday)) + geom_density()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed, fill=weekday)) + geom_density() + facet_wrap(~weekday)
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed, fill=weather)) + geom_histogram(binwidth = 5)+ facet_wrap(~weather)
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed, fill=morningrush)) + geom_histogram(binwidth = 3)

  do.call(grid.arrange, c(plots, nrow=2))
  plots <- list()
  
  # scatter plots speed~minutesDay coloured by a third variable
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=morningfall, y=speed)) + geom_point()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=morningrise, y=speed)) + geom_point()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=remainingday, y=speed)) + geom_point()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=minutesDay, y=speed)) + geom_point()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=minutesDay, y=speed, color=schoolfr)) + geom_point()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=minutesDay, y=speed, color=weekday)) + geom_point()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=distortedMinutes, y=speed)) + geom_point()
  
  do.call(grid.arrange, c(plots, nrow=3))
  plots <- list()
  
  # predictor histograms
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=weather)) + geom_histogram(stat = "count")
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=temperature)) + geom_histogram(binwidth = 2)
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=humidity)) + geom_histogram(binwidth = 5)
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=pressure)) + geom_histogram(binwidth = 3)
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=weekday)) + geom_histogram(stat = "count")
  print(summary(speeds[,c("holidaych", "holidayfr", "schoolch", "schoolfr")]))
  do.call(grid.arrange, c(plots, nrow=2))
  plots <- list()
}

scatterplots <- function() {
  plots <- list()
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = morningrush, y = speed)) + geom_boxplot()
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = minutesDay, y = speed)) + geom_point(shape = 1) 
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = minutesHour, y = speed)) + geom_point(shape = 1) 
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = weekday, y = speed)) + geom_boxplot()
  do.call(grid.arrange, c(plots, list(ncol = 2)))
  plots <- list()
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = holidayfr, y = speed)) + geom_boxplot()
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = holidaych, y = speed)) + geom_boxplot()
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = schoolfr, y = speed)) + geom_boxplot()
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = schoolch, y = speed)) + geom_boxplot()
  do.call(grid.arrange, c(plots, list(ncol = 2)))
  plots <- list()
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = humidity, y = speed)) + geom_point(shape = 1) 
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = pressure, y = speed)) + geom_point(shape = 1) 
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = temperature, y = speed)) + geom_point(shape = 1) 
  plots[[length(plots)+1]] <- ggplot(data = speeds, aes(x = weather, y = speed)) + geom_boxplot()
  do.call(grid.arrange, c(plots, list(ncol = 2)))
}

calculateModel <- function(formula) {
  fit <- lm(data = speeds, formula)
  print(summary(fit))
  arm::coefplot(fit, xlim=c(-10, 10))
  return(fit)
}

residualPlots <- function(fit) {
  fittedVsResidualsPlot <- ggplot(data = fit, aes(y=.resid, x=.fitted)) + geom_point() + geom_hline(yintercept = 0) + geom_smooth(se = FALSE) + labs(x="Fitted values", y="Residuals")
  qqPlot <- ggplot(fit, aes(sample = .resid)) + geom_qq() + geom_abline(intercept = mean(fit$residuals), slope = sd(fit$residuals))
  residualHistogram <- ggplot(fit, aes(x = .resid)) + geom_density()
  grid.arrange(fittedVsResidualsPlot, qqPlot, residualHistogram, ncol = 2)
}

edaHist <- FALSE
edaScatter <- FALSE
model <- FALSE
fitAnalysis <- FALSE
predict <- TRUE
crossValidation <- TRUE

if (edaHist) {
  histograms()
}
if (edaScatter) {
  scatterplots()
}
if (model) {
  fits <- list()
  fits[[length(fits)+1]] <- calculateModel(speed ~ minutesDay + weekday + schoolfr + humidity + pressure + temperature + weather)
  fits[[length(fits)+1]] <- calculateModel(speed ~ distortedMinutes + weekday + schoolfr + humidity + pressure + temperature + weather)
  fits[[length(fits)+1]] <- calculateModel(speed ~ distortedMinutes * weekday + weather)
  fits[[length(fits)+1]] <- calculateModel(speed ~ morningrush * weekday * weather)
  fits[[length(fits)+1]] <- calculateModel(speed ~ morningrush * weekday + morningrush * weather + morningrush * schoolfr)
  print(AIC(fits[[1]],fits[[2]],fits[[3]],fits[[4]],fits[[5]]))
  print(BIC(fits[[1]],fits[[2]],fits[[3]],fits[[4]],fits[[5]]))
  if (fitAnalysis) {
    for (fit in fits) {
      residualPlots(fit)
    }
  }
}
if (predict) {
  formula <- speed ~ morningrush * weekday * weather
  fit <- lm(data = speeds, formula)
  print("morningrush = TRUE, Tuesday, clearorclouds")
  print(predict.lm(fit, newdata = data.frame(morningrush="true", weekday="tuesday", weather="clearorclouds"), interval = "prediction", level = 0.95))
  print("morningrush = TRUE, Tuesday, fog")
  print(predict.lm(fit, newdata = data.frame(morningrush="true", weekday="tuesday", weather="fog"), interval = "prediction", level = 0.95))
}
if (crossValidation) {
  formula <- speed ~ morningrush * weekday * weather
  n <- nrow(speeds)
  k <- 5
  folds <- data.frame(Fold=sample(rep(1:k, length.out=n)), Row=1:n)

  for (fold in 1:max(folds$Fold)) {
    testRows <- folds[folds$Fold==fold,]$Row
    fit <- lm(data = speeds[-testRows,], formula)
    prediction <- predict.lm(fit, newdata = speeds[testRows,], interval = "prediction", level = 0.95)
    # How many observations inside the prediction interval
    testResponse <- speeds[testRows, "speed"]
    successRatio <- sum(testResponse > prediction[,"lwr"] & testResponse < prediction[,"upr"]) / length(testRows)
    print(successRatio)
  }  
  
}


# summary (speeds$speed)
# boxplot(speeds$speed)
# hist(speeds$speed)
# 
# scatterplotSpeedMinutes <- ggplot(data = speeds, aes(x=minutesDay, y=speed)) + geom_point(shape=1)
# print(scatterplotSpeedMinutes)
# scatterplotSpeedTimestamp <- ggplot(data = speeds, aes(x=timestamp, y=speed)) + geom_point(shape=1)
# print(scatterplotSpeedTimestamp)
# scatterplotSpeedSchoolfr <- ggplot(data = speeds, aes(x=schoolfr, y=speed)) + geom_point(shape=1)
# print(scatterplotSpeedSchoolfr)
# 
# facetedHistograms <- ggplot(data = speeds, aes(x=speed)) + geom_histogram() + ggtitle("results") + facet_grid(morningrush~weekday, labeller = label_both)
# print(facetedHistograms)
# facetedScatterplots <- ggplot(data = speeds, aes(x=minutesDay, y=speed)) + geom_point(shape=1) + ggtitle("results") + facet_grid(weekday~morningrush, labeller = label_both)
# print(facetedScatterplots)
# facetedScatterplots <- ggplot(data = speeds, aes(x=temperature, y=speed)) + geom_point(shape=1) + ggtitle("results") + facet_grid(weekday~morningrush, labeller = label_both)
# print(facetedScatterplots)
