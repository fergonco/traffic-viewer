library(gridExtra)

speeds <- read.csv("/tmp/output.csv")

fit <- glm(data = speeds, speed ~ weekday + schoolfr + poly(minutesDay, 10) + weekday *  schoolfr * poly(minutesDay, 4))

fittedVsResidualsPlot <- ggplot(data = fit, aes(y=.resid, x=.fitted)) + geom_point(shape=1) + geom_hline(yintercept = 0) + geom_smooth(se = FALSE, aes(color="red")) + labs(x="Fitted values", y="Residuals")
qqPlot <- ggplot(fit, aes(sample = .resid)) + geom_qq() + geom_abline(intercept = mean(fit$residuals), slope = sd(fit$residuals))
residualHistogram <- ggplot(fit, aes(x = .resid)) + geom_density()
grid.arrange(fittedVsResidualsPlot, qqPlot, residualHistogram, ncol = 2)