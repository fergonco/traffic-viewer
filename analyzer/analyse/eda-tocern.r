library(ggplot2)
library(gridExtra)

speeds <- read.csv("tocern.csv")

speedhistogram <- function() {
  speedHist <- ggplot(data=speeds, aes(x=speed)) + geom_density()
  speedPerDayHist <- ggplot(data=speeds, aes(x=speed, fill=weekday)) + geom_density()
  facetedPerDayHist <- ggplot(data=speeds, aes(x=speed, fill=weekday)) + geom_density() + facet_wrap(~weekday)
  speedPerWeatherHist <- ggplot(data=speeds, aes(x=speed, fill=weather)) + geom_histogram()+ facet_wrap(~weather)
  grid.arrange(speedHist, speedPerDayHist, facetedPerDayHist,speedPerWeatherHist, nrow=2)
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
  plots[[12]] <- ggplot(data = speeds, aes(x = factor(weather), y = speed)) + geom_boxplot()

  do.call(grid.arrange, c(plots, list(ncol = 4)))
}

speedhistogram()
# scatterplots()

# model <- lm(data = speeds, speed ~ minutesDay + minutesHour + weekday + holidayfr + holidaych + schoolfr + schoolch + humidity + pressure + temperature + weather)
# layout(matrix(c(1,2,3,4),2,2)) # optional 4 graphs/page
# plot(model)



# print(summary(model))
# print(coefficients(model))
# plot(residuals(model))
# print(summary(model)$adj.r.squared)












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
