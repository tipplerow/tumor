
NeoPair.aggregateTrial <- function(trialFrame, binSize = 10) {
    aggFunc <- function(x) {
        data.frame(selectCoeff = x$selectCoeff[1],
                   neoRate     = x$neoRate[1],
                   trialIndex  = x$trialIndex[1],
                   arcDist     = x$arcDistMid[1],
                   neoCorr     = mean(x$neoCorr),
                   selectCorr  = mean(x$selectCorr))
    }

    trialFrame$distBin    <- floor(trialFrame$arcDist / binSize)
    trialFrame$arcDistMid <- binSize * (trialFrame$distBin + 0.5)

    frame0 <-
        data.frame(selectCoeff = trialFrame$selectCoeff[1],
                   neoRate     = trialFrame$neoRate[1],
                   trialIndex  = trialFrame$trialIndex[1],
                   arcDist     = 0,
                   neoCorr     = 1.0,
                   selectCorr  = 1.0)
    
    binList <- by(trialFrame, trialFrame$distBin, aggFunc)
    
    result <- do.call(rbind, c(list(frame0), binList))
    rownames(result) <- NULL

    result
}

NeoPair.aggregateTrials <- function(trialSetFrame) {
    aggFunc <- function(x) {
        data.frame(selectCoeff = x$selectCoeff[1],
                   neoRate     = x$neoRate[1],
                   arcDist     = x$arcDist[1],
                   neoCorr     = mean(x$neoCorr),
                   neoErr      = sd(x$neoCorr) / sqrt(nrow(x)),
                   selectCorr  = mean(x$selectCorr),
                   selectErr   = sd(x$selectCorr) / sqrt(nrow(x)))
    }
    
    result <- do.call(rbind, by(trialSetFrame, trialSetFrame$arcDist, aggFunc))
    rownames(result) <- NULL

    result
}

NeoPair.baseName <- function() {
    "surface-cell-mutation-type-pair.csv"
}

NeoPair.dirName <- function(selectCoeff, neoRate, trialIndex) {
    if (selectCoeff == 0.005)
        file.path(sprintf("S%.3f", selectCoeff), sprintf("NR%s", neoRate), sprintf("Trial%02d", trialIndex))
    else
        file.path(sprintf("S%.2f", selectCoeff), sprintf("NR%s", neoRate), sprintf("Trial%02d", trialIndex))
}

NeoPair.list <- function(dirName) {
    list.files(path = dirName, pattern = NeoPair.baseName(), full.names = TRUE, recursive = TRUE)
}

NeoPair.loadTrial <- function(selectCoeff, neoRate, trialIndex) {
    fileName <- file.path(NeoPair.dirName(selectCoeff, neoRate, trialIndex), NeoPair.baseName())

    JamLog.info("Loading [%s]...", fileName)
    trialFrame <- read.csv(fileName)
    trialFrame <- subset(trialFrame, timeStep == max(timeStep))

    trialFrame$selectCoeff <- selectCoeff
    trialFrame$neoRate     <- as.numeric(neoRate)

    neoMean <- mean(c(trialFrame$NEOANTIGEN.count1, trialFrame$NEOANTIGEN.count2))
    neoVar  <- var( c(trialFrame$NEOANTIGEN.count1, trialFrame$NEOANTIGEN.count2))
    neoEx1  <- trialFrame$NEOANTIGEN.count1 - neoMean
    neoEx2  <- trialFrame$NEOANTIGEN.count2 - neoMean
    neoCorr <- neoEx1 * neoEx2 / neoVar

    selectMean <- mean(c(trialFrame$SCALAR.count1, trialFrame$SCALAR.count2))
    selectVar  <- var( c(trialFrame$SCALAR.count1, trialFrame$SCALAR.count2))
    selectEx1  <- trialFrame$SCALAR.count1 - selectMean
    selectEx2  <- trialFrame$SCALAR.count2 - selectMean
    selectCorr <- selectEx1 * selectEx2 / selectVar

    trialFrame$neoCorr    <- neoCorr
    trialFrame$selectCorr <- selectCorr
    trialFrame
}

NeoPair.loadTrials <- function(selectCoeff, neoRate, trialIndexes) {
    trialFrames <- list()

    for (k in seq_along(trialIndexes)) {
        trialIndex <- trialIndexes[k]
        trialFrame <- NeoPair.loadTrial(selectCoeff, neoRate, trialIndex)

        trialFrames[[k]] <-
            NeoPair.aggregateTrial(trialFrame)
    }

    aggFrame <- NeoPair.aggregateTrials(do.call(rbind, trialFrames))
    aggFrame
}

NeoPair.loadMaster <- function(selectCoeffs = c(0.0, 0.01, 0.02, 0.05, 0.10, 0.20),
                               neoRates     = c("1E-4", "1E-3", "1E-2", "1E-1"),
                               trialIndexes = 0:9) {
    masterList <- list()

    for (selectCoeff in selectCoeffs)
        for (neoRate in neoRates)
            masterList[[length(masterList) + 1]] <-
                NeoPair.loadTrials(selectCoeff, neoRate, trialIndexes)

    pairMaster <- do.call(rbind, masterList)
    pairMaster
}

NeoPair.plotCorrLinear <- function(dframe, xmax) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    dframe <- subset(dframe, arcDist <= xmax)

    x   <- dframe$arcDist
    y1  <- dframe$selectCorr
    dy1 <- dframe$selectErr
    y2  <- dframe$neoCorr
    dy2 <- dframe$neoErr

    plot(x, y1,
         type = "n",
         xlim = c(0, xmax),
         ylim = c(0, 1),
         xlab = "Distance [cells]",
         ylab = "Correlation")

    lines(x, y1, col = 1)
    lines(x, y2, col = 2)

    points(x, y1, pch = 16, col = 1)
    points(x, y2, pch = 16, col = 2)

    for (k in seq_along(x)) {
        dx <- 1
        lines(c(x[k], x[k]), c(y1[k] - 2 * dy1[k], y1[k] + 2 * dy1[k]), col = 1)
        lines(c(x[k] - dx, x[k] + dx), c(y1[k] - 2 * dy1[k], y1[k] - 2 * dy1[k]), col = 1)
        lines(c(x[k] - dx, x[k] + dx), c(y1[k] + 2 * dy1[k], y1[k] + 2 * dy1[k]), col = 1)
    }

    for (k in seq_along(x)) {
        dx <- 1
        lines(c(x[k], x[k]), c(y2[k] - 2 * dy2[k], y2[k] + 2 * dy2[k]), col = 2)
        lines(c(x[k] - dx, x[k] + dx), c(y2[k] - 2 * dy2[k], y2[k] - 2 * dy2[k]), col = 2)
        lines(c(x[k] - dx, x[k] + dx), c(y2[k] + 2 * dy2[k], y2[k] + 2 * dy2[k]), col = 2)
    }

    legend("topright", bty = "n",
           legend = c("Selective", "Neoantigen"), col = c(1, 2), pch = c(16, 16))
}

NeoPair.plotCorrLog <- function(dframe, xmax, ymin) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    dframe <- subset(dframe, arcDist > 0)
    dframe <- subset(dframe, arcDist <= xmax)

    x   <- dframe$arcDist
    y1  <- dframe$selectCorr
    dy1 <- dframe$selectErr
    y2  <- dframe$neoCorr
    dy2 <- dframe$neoErr

    JamPlot.logY(xlim = c(0, xmax),
                 ylim = c(ymin, 1),
                 xlab = "Distance [cells]",
                 ylab = "Correlation")

    points(x, y1, pch = 16, col = 1)
    points(x, y2, pch = 16, col = 2)

    for (k in seq_along(x)) {
        dx <- 1
        lines(c(x[k], x[k]), c(max(1E-6, y1[k] - 2 * dy1[k]), y1[k] + 2 * dy1[k]), col = 1)
        lines(c(x[k] - dx, x[k] + dx), c(max(1E-6, y1[k] - 2 * dy1[k]), max(1E-6, y1[k] - 2 * dy1[k])), col = 1)
        lines(c(x[k] - dx, x[k] + dx), c(y1[k] + 2 * dy1[k], y1[k] + 2 * dy1[k]), col = 1)
    }

    for (k in seq_along(x)) {
        dx <- 1
        lines(c(x[k], x[k]), c(max(1E-6, y2[k] - 2 * dy2[k]), y2[k] + 2 * dy2[k]), col = 2)
        lines(c(x[k] - dx, x[k] + dx), c(max(1E-6, y2[k] - 2 * dy2[k]), max(1E-6, y2[k] - 2 * dy2[k])), col = 2)
        lines(c(x[k] - dx, x[k] + dx), c(y2[k] + 2 * dy2[k], y2[k] + 2 * dy2[k]), col = 2)
    }

    lm1 <- lm(log(y1) ~ x)
    a1  <- lm1$coeff[1]
    b1  <- lm1$coeff[2]

    lmx1 <- -10
    lmx2 <- xmax + 10
    lmy1 <- exp(a1) * exp(b1 * lmx1)
    lmy2 <- exp(a1) * exp(b1 * lmx2)

    lines(c(lmx1, lmx2), c(lmy1, lmy2), lty = 3)

    lm2 <- lm(log(y2) ~ x)
    a2  <- lm2$coeff[1]
    b2  <- lm2$coeff[2]

    lmx1 <- -10
    lmx2 <- xmax + 10
    lmy1 <- exp(a2) * exp(b2 * lmx1)
    lmy2 <- exp(a2) * exp(b2 * lmx2)

    lines(c(lmx1, lmx2), c(lmy1, lmy2), lty = 3, col = 2)

    legend("topright", bty = "n",
           legend = c("Selective", "Neoantigen"), col = c(1, 2), pch = c(16, 16))
}


NeoPair.plotNeoMean <- function(countMaster) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    JamPlot.logXY(xlim = c(1E5, 1E7),
                  ylim = c(0.001, 100.0),
                  xlab = "Tumor size [cells]",
                  ylab = "Mean neoantigen load")

    plotOne <- function(selectCoeff, neoRate, col, pch, lty) {
        slice <- countMaster[which(countMaster$selectCoeff == selectCoeff & countMaster$neoRate == neoRate),]

        x  <- slice$tumorSize
        y  <- slice$neoMean
        dy <- slice$neoErr

        lines( x, y, col = col, lty = lty)
        points(x, y, col = col, pch = pch)

        for (k in seq_along(y))
            lines(c(x[k], x[k]), c(y[k] - 2 * dy[k], y[k] + 2 * dy[k]), col = col)
    }

    plotOne(0.01, 1E-4, 1, 1, 2)
    plotOne(0.01, 1E-3, 2, 1, 2)
    plotOne(0.01, 1E-2, 3, 1, 2)
    plotOne(0.01, 1E-1, 4, 1, 2)

    plotOne(0.1, 1E-4, 1, 16, 1)
    plotOne(0.1, 1E-3, 2, 16, 1)
    plotOne(0.1, 1E-2, 3, 16, 1)
    plotOne(0.1, 1E-1, 4, 16, 1)

    legend(3.1E6, 0.04, bty = "n",
           legend = c(expression(paste(mu[n], " = ", 10^-1)),
                      expression(paste(mu[n], " = ", 10^-2)),
                      expression(paste(mu[n], " = ", 10^-3)),
                      expression(paste(mu[n], " = ", 10^-4))),
           col = c(4, 3, 2, 1),
           pch = c(16, 16, 16, 16))

    legend(4E5, 0.008, bty = "n",
           legend = c("s = 0.01", "s = 0.10"),
           col = c(1, 1),
           lty = c(2, 1))
}

NeoPair.computeCorrLength <- function(pairMaster) {
    computeCorr <- function(x) {
        neoK    <- which(x$neoCorr    < 2 * x$neoErr)[1]
        selectK <- which(x$selectCorr < 2 * x$selectErr)[1]

        data.frame(selectCoeff   = x$selectCoeff[1],
                   neoRate       = x$neoRate[1],
                   neoCorrLen    = x$arcDist[neoK],
                   selectCorrLen = x$arcDist[selectK])
    }

    result <- do.call(rbind, by(pairMaster, list(pairMaster$selectCoeff, pairMaster$neoRate), computeCorr))
    result <- result[order(result$selectCoeff, result$neoRate),]
    rownames(result) <- NULL

    result
}

NeoPair.plotSelectCorrLength <- function(corLen) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    meanCorr <- by(corLen, corLen$selectCoeff, function(x) mean(x$selectCorrLen))

    JamPlot.by(corLen,
               xkey  = "selectCoeff",
               ykey  = "selectCorrLen",
               bykey = "neoRate",
               type  = "p",
               xlab  = "Selection coefficient",
               ylab  = "Correlation length [cells]",
               ylim  = c(30, 170),
               pch   = 15:18,
               legend.loc  = "bottomright",
               legend.text = expression(10^-4, 10^-3, 10^-2, 10^-1))

    x <- seq(0.0, 0.20, 0.01)
    y <- 40 + 100 * (1 - exp(-x / 0.025))

    ##lines(x, y, lty = 2)
}

NeoPair.plotMeanSelectCorrLength <- function(corLen, ...) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    selectCorr <-
        do.call(rbind,
                by(corLen,
                   corLen$selectCoeff,
                   function(x) data.frame(selectCoeff = x$selectCoeff[1],
                                          selectMean  = mean(x$selectCorrLen),
                                          selectErr   = sd(x$selectCorrLen) / sqrt(nrow(x)),
                                          neoMean     = mean(x$neoCorrLen),
                                          neoErr      = sd(x$neoCorrLen) / sqrt(nrow(x)))))

    x   <- selectCorr$selectCoeff
    y1  <- selectCorr$selectMean
    dy1 <- selectCorr$selectErr
    y2  <- selectCorr$neoMean
    dy2 <- selectCorr$neoErr

    plot(x, y1,
         type = "p",
         xlab = "Selection coefficient",
         ylab = "Correlation length [cells]",
         col  = 1,
         pch  = 16,
         ...)

    JamPlot.err(x, y1, dy1, col = 1)

    ##points(x, y2, col = 2, pch = 16)
    ##JamPlot.err(x, y2, dy2, col = 2)

    legend("bottomright", bty = "n", legend = "Selective", col = 1, pch = 16)
}

NeoPair.plotNeoCorrLength <- function(corLen) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    JamPlot.logX(xlim = c(1E-4, 1E-1),
                 ylim = c(50, 160),
                 xlab  = "Neoantigen rate",
                 ylab  = "Correlation length [cells]",
                 tick.power = -4:-1)

    JamPlot.by(corLen,
               xkey  = "neoRate",
               ykey  = "neoCorrLen",
               bykey = "selectCoeff",
               type  = "p",
               pch   = 15:18,
               overlay = TRUE,
               legend.loc  = "topleft",
               legend.text = c("0", "0.01", "0.02", "0.05", "0.10", "0.20"))

    x <- 10 ^ seq(-5, 0, 0.1)
    y <- 144 + 8.47 * log(x)
    lines(x, y, lty = 2)
}

NeoPair.plotMeanNeoCorrLength <- function(corLen, ylim) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    neoCorr <-
        do.call(rbind,
                by(corLen,
                   corLen$neoRate,
                   function(x) data.frame(neoRate     = x$neoRate[1],
                                          selectMean  = mean(x$selectCorrLen),
                                          selectErr   = sd(x$selectCorrLen) / sqrt(nrow(x)),
                                          neoMean     = mean(x$neoCorrLen),
                                          neoErr      = sd(x$neoCorrLen) / sqrt(nrow(x)))))

    x   <- neoCorr$neoRate
    y1  <- selectCorr$selectMean
    dy1 <- selectCorr$selectErr
    y2  <- neoCorr$neoMean
    dy2 <- neoCorr$neoErr

    JamPlot.logX(xlim = c(1E-4, 1E-1),
                 ylim = ylim,
                 xlab = "Neoantigen mutation rate",
                 ylab = "Correlation length [cells]",
                 tick.power = -4:-1)

    points(x, y2, col = 2, pch = 16)
    JamPlot.err(x, y2, dy2, col = 2)

    legend("bottomright", bty = "n", legend = "Neoantigen", col = 2, pch = 16)
}
