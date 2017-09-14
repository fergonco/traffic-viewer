library(ggplot2)
library(gridExtra)
library(arm)

speeds <- read.csv("/tmp/output.csv")

calculateModel <- function(formula) {
  fit <- lm(data = speeds, formula)
  print(summary(fit))
  arm::coefplot(fit, xlim=c(-10, 10))
  return(fit)
}

residualPlots <- function(fit) {
  fittedVsResidualsPlot <- ggplot(data = fit, aes(y=.resid, x=.fitted)) + geom_point(shape=1) + geom_hline(yintercept = 0) + geom_smooth(se = FALSE, aes(color="red")) + labs(x="Fitted values", y="Residuals")
  print(fittedVsResidualsPlot)
  #qqPlot <- ggplot(fit, aes(sample = .resid)) + geom_qq() + geom_abline(intercept = mean(fit$residuals), slope = sd(fit$residuals))
  #residualHistogram <- ggplot(fit, aes(x = .resid)) + geom_density()
  #grid.arrange(fittedVsResidualsPlot, qqPlot, residualHistogram, ncol = 2)
}

testPolynomialResidual <- FALSE
model <- FALSE
fitAnalysis <- FALSE
predict <- FALSE
crossValidation <- FALSE

if (testPolynomialResidual) {
  print(ggplot(data=speeds[speeds$weekday=='tuesday' & speeds$schoolfr == "true", ], aes(x=minutesDay, y=speed)) + geom_point() + geom_smooth(method = "lm", formula = y ~ poly(x,9), colour = "red"))
  formulas <- c(
    speed ~ weekday * minutesDay + schoolfr * minutesDay + schoolfr + weekday + minutesDay,
    speed ~ weekday * poly(minutesDay, 2) + schoolfr * poly(minutesDay, 2) + schoolfr + weekday + poly(minutesDay, 2),
    speed ~ weekday * poly(minutesDay, 3) + schoolfr * poly(minutesDay, 3) + schoolfr + weekday + poly(minutesDay, 3),
    speed ~ weekday * poly(minutesDay, 4) + schoolfr * poly(minutesDay, 4) + schoolfr + weekday + poly(minutesDay, 4),
    speed ~ weekday * poly(minutesDay, 5) + schoolfr * poly(minutesDay, 5) + schoolfr + weekday + poly(minutesDay, 5),
    speed ~ weekday * poly(minutesDay, 6) + schoolfr * poly(minutesDay, 6) + schoolfr + weekday + poly(minutesDay, 6)
  )
  for (formula in formulas) {
    fit <- lm(data = speeds, formula)
    residualPlots(fit)
    # print(summary(fit))
    # print(ggplot(data=speeds, aes(x=minutesDay, y=speed)) + geom_point() + geom_smooth(method = "lm", formula = y ~ poly(x,6), colour = "red"))
  }
  # arm::coefplot(fit, xlim=c(-10, 10))
}
if (model) {
  fits <- list()
  fits[[length(fits)+1]] <- calculateModel(speed ~ minutesDay + weekday + schoolfr + humidity + pressure + temperature + weather)
  fits[[length(fits)+1]] <- calculateModel(speed ~ distortedMinutes + weekday + schoolfr + humidity + pressure + temperature + weather)
  fits[[length(fits)+1]] <- calculateModel(speed ~ distortedMinutes * weekday + weekday * weather)
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
