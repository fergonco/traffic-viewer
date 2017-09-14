library(boot)

speeds <- read.csv("/tmp/output.csv")

cv.error.10 <- rep(0, 22)
for (i in 1:22) {
  # formula <- speed ~ weekday * poly(minutesDay, i) + schoolfr * poly(minutesDay, i) + schoolfr + weekday + poly(minutesDay, i)
  formula <- speed ~ poly(minutesDay, i)
  glm.fit <- glm(formula, data=speeds)
  # glm.fit <- glm(speed ~ poly(minutesDay, i), data=speeds)
  cv.error.10[i] <- cv.glm(speeds, glm.fit, K=10)$delta[1]
}
plot(cv.error.10)
