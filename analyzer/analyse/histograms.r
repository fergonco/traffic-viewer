speeds <- read.csv("/tmp/output.csv")

plots <- list()
# response variable histogram
plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed)) + geom_histogram(binwidth = 3)

# response variable histogram coloured and faceted by a predictor variable
plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed, fill=weekday)) + geom_histogram(binwidth = 3)
plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed, fill=weekday)) + geom_histogram(binwidth = 3) + facet_wrap(~weekday)
plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed, fill=weather)) + geom_histogram(binwidth = 3) + facet_wrap(~weather)
plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed, fill=holidaySizefr)) + geom_histogram(binwidth = 3) + facet_wrap(~holidaySizefr)
plots[[length(plots)+1]] <- ggplot(data=speeds, aes(x=speed)) + geom_histogram(binwidth = 2) + facet_wrap(~schoolfr)

do.call(grid.arrange, c(plots, nrow=2))
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