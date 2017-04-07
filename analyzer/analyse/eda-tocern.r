library(ggplot2)


speeds <- read.csv("tocern.csv")

scatterplots <- function() {
  plots <- list()
  independentVariables <- c(
    "minutesDay",
    "minutesHour",
    "morningrush",
    "weekday",
    "holidayfr",
    "holidaych",
    "schoolfr",
    "schoolch",
    "humidity",
    "pressure",
    "temperature",
    "weather"
  )
  for (i in independentVariables) {
    plots[[i]] <-
      ggplot(data = speeds, aes_string(x = i, y = "speed")) + geom_point(shape = 1)
  }
  do.call(grid.arrange, c(plots, list(ncol = 3)))
}

scatterplots()




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
