library(ggplot2);

speeds <-read.csv("tocern.csv", colClasses = c("numeric", "numeric", "numeric", "logical", "logical", "logical", "logical", "logical", "logical", "numeric", "numeric", "numeric", "numeric", "numeric", "character"))
summary (speeds$speed)
boxplot(speeds$speed)
hist(speeds$speed)

scatterplotSpeedMinutes <- ggplot(data = speeds, aes(x=minutes, y=speed)) + geom_point(shape=1)
print(scatterplotSpeedMinutes)
scatterplotSpeedTimestamp <- ggplot(data = speeds, aes(x=timestamp, y=speed)) + geom_point(shape=1)
print(scatterplotSpeedTimestamp)
scatterplotSpeedSchoolfr <- ggplot(data = speeds, aes(x=schoolfr, y=speed)) + geom_point(shape=1)
print(scatterplotSpeedSchoolfr)


speedsRush <- speeds[,]
speedsRush$rush <- apply(speeds, 1, function(speed){return (speed["minutes"]>420 & speed["minutes"]<570)})
speedsRush$workday <- !speeds$sunday & !speeds$saturday & speeds$schoolfr & !speeds$holidayfr & speeds$schoolch & !speeds$holidaych
facetedHistograms <- ggplot(data = speedsRush, aes(x=speed)) + geom_histogram() + ggtitle("results") + facet_grid(rush~workday, labeller = label_both)
print(facetedHistograms)
facetedScatterplots <- ggplot(data = speedsRush, aes(x=minutes, y=speed)) + geom_point(shape=1) + ggtitle("results") + facet_grid(workday~rush, labeller = label_both)
print(facetedScatterplots)
facetedScatterplots <- ggplot(data = speedsRush, aes(x=temperature, y=speed)) + geom_point(shape=1) + ggtitle("results") + facet_grid(workday~rush, labeller = label_both)
print(facetedScatterplots)
