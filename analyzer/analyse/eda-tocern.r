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
  
  # scatter plots speed~minutesDay coloured by a third variable
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=minutesDay, y=speed)) + geom_point()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=minutesDay, y=speed, color=schoolfr)) + geom_point()
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=minutesDay, y=speed, color=weekday)) + geom_point()
  
  # predictor histograms
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=weather)) + geom_histogram(stat = "count")
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=temperature)) + geom_histogram(binwidth = 2)
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=humidity)) + geom_histogram(binwidth = 5)
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=pressure)) + geom_histogram(binwidth = 3)
  plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=weekday)) + geom_histogram(stat = "count")
  print(summary(speeds[,c("holidaych", "holidayfr", "schoolch", "schoolfr")]))
  
  do.call(grid.arrange, c(plots, ncol=4))
}

scatterplots <- function() {
  plots <- list()
  plots[[1]] <- ggplot(data = speeds, aes(x = morningrush, y = speed)) + geom_boxplot()
  plots[[2]] <- ggplot(data = speeds, aes(x = minutesDay, y = speed)) + geom_point(shape = 1) 
  plots[[3]] <- ggplot(data = speeds, aes(x = minutesHour, y = speed)) + geom_point(shape = 1) 
  plots[[4]] <- ggplot(data = speeds, aes(x = weekday, y = speed)) + geom_boxplot()
  plots[[5]] <- ggplot(data = speeds, aes(x = holidayfr, y = speed)) + geom_boxplot()
  plots[[6]] <- ggplot(data = speeds, aes(x = holidaych, y = speed)) + geom_boxplot()
  plots[[7]] <- ggplot(data = speeds, aes(x = schoolfr, y = speed)) + geom_boxplot()
  plots[[8]] <- ggplot(data = speeds, aes(x = schoolch, y = speed)) + geom_boxplot()
  plots[[9]] <- ggplot(data = speeds, aes(x = humidity, y = speed)) + geom_point(shape = 1) 
  plots[[10]] <- ggplot(data = speeds, aes(x = pressure, y = speed)) + geom_point(shape = 1) 
  plots[[11]] <- ggplot(data = speeds, aes(x = temperature, y = speed)) + geom_point(shape = 1) 
  plots[[12]] <- ggplot(data = speeds, aes(x = weather, y = speed)) + geom_boxplot()
  
  do.call(grid.arrange, c(plots, list(ncol = 4)))
}

residualPlots <- function() {
  fittedVsResidualsPlot <- ggplot(data = fit, aes(y=.resid, x=.fitted)) + geom_point() + geom_hline(yintercept = 0) + geom_smooth(se = FALSE) + labs(x="Fitted values", y="Residuals")
  qqPlot <- ggplot(fit, aes(sample = .resid)) + geom_qq() + geom_abline(intercept = mean(fit$residuals), slope = sd(fit$residuals))
  residualHistogram <- ggplot(fit, aes(x = .resid)) + geom_histogram(binwidth = 1)
  grid.arrange(fittedVsResidualsPlot, qqPlot, residualHistogram, ncol = 2)
}

histograms()
scatterplots()

fit <- lm(data = speeds, speed ~ minutesDay + weekday + schoolfr + humidity + pressure + temperature + weather)
print(summary(fit))
arm::coefplot(model, xlim=c(-10, 10))

residualPlots()


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
