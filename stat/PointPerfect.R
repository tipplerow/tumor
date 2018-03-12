
PointPerfect.plotTumorSize <- function(fileName, netRate, ylim, ...) {
    sizeFrame <- read.csv(fileName, header = FALSE)

    xlim <- c(0, ncol(sizeFrame) - 1)
    xlab <- "Time step"
    ylab <- "Tumor size"

    par(las = 1)
    par(fig = c(0.0, 1.0, 0.15, 0.85))
    JamPlot.logY(xlim, ylim, xlab, ylab, ...)

    x <- seq(0, ncol(sizeFrame) - 1)

    for (k in 1:nrow(sizeFrame))
        lines(x, sizeFrame[k,], col = k)

    lines(x, sizeFrame[1,1] * (1.0 + netRate) ^ x, lwd = 3)
}

PointPerfect.plotSizeRatio <- function(fileName, ylim, ...) {
    ratioFrame <- read.csv(fileName, header = FALSE)

    par(las = 1)
    par(fig = c(0.0, 1.0, 0.15, 0.85))

    x <- seq(0, ncol(ratioFrame) - 1)
    plot(x, ratioFrame[1,],
         type = "l",
         xlim = c(0, ncol(ratioFrame) - 1),
         ylim = ylim,
         xlab = "Time step",
         ylab = "Size ratio (actual / ideal)")

    for (k in 2:nrow(ratioFrame))
        lines(x, ratioFrame[k,], col = k)

    ##lines(x, ratioFrame[1,1] * (1.0 + netRate) ^ x, lwd = 3)
}

PointPerfect.plotSizeStat <- function(fileName) {
    statFrame <- read.csv(fileName)

    par(las = 1)
    par(fig = c(0.0, 1.0, 0.15, 0.85))

    x  <- statFrame$timeStep
    y1 <- statFrame$sizeRatioMean
    y2 <- statFrame$sizeRatioQ1
    y3 <- statFrame$sizeRatioQ3
    y4 <- statFrame$sizeRatioMedian
    
    plot(x, y1, 
         type = "l",
         lwd  = 2,
         ylim = c(0.0, 2.0),
         xlab = "Time step",
         ylab = "Size ratio (actual / ideal)")

    lines(x, y2, col = 2, lwd = 2)
    lines(x, y3, col = 3, lwd = 2)
    lines(x, y4, col = 4, lwd = 2)

    legend("topleft", bty = "n",
           legend = c("Q1", "Median", "Mean", "Q3"),
           lty = c(1, 1, 1, 1),
           col = c(2, 4, 1, 3))
}
