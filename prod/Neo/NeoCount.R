
NeoCount.aggregateTrial <- function(countFrame) {
    aggFunc <- function(x) {
        data.frame(selectCoeff = x$selectCoeff[1],
                   neoRate     = x$neoRate[1],
                   trialIndex  = x$trialIndex[1],
                   tumorSize   = x$tumorSize[1],
                   anyNeo      = mean(x$NEOANTIGEN.count > 0.01),
                   neoMean     = mean(x$NEOANTIGEN.count),
                   neoErr      = sd(x$NEOANTIGEN.count) / sqrt(nrow(x)),
                   selectMean  = mean(x$SCALAR.count),
                   selectErr   = sd(x$SCALAR.count) / sqrt(nrow(x)))
    }
    
    result <- do.call(rbind, by(countFrame, countFrame$tumorSize, aggFunc))
    rownames(result) <- NULL

    result
}

NeoCount.aggregateTrials <- function(aggTrialFrame) {
    aggFunc <- function(x) {
        data.frame(selectCoeff = x$selectCoeff[1],
                   neoRate     = x$neoRate[1],
                   tumorSize   = x$tumorSize[1],
                   anyNeoMean  = mean(x$anyNeo),
                   anyNeoErr   = sd(x$anyNeo) / sqrt(nrow(x)),
                   neoMean     = mean(x$neoMean),
                   neoErr      = sd(x$neoMean) / sqrt(nrow(x)),
                   selectMean  = mean(x$selectMean),
                   selectErr   = sd(x$selectMean) / sqrt(nrow(x)))
    }
    
    result <- do.call(rbind, by(aggTrialFrame, aggTrialFrame$tumorSize, aggFunc))
    rownames(result) <- NULL

    result
}

NeoCount.baseName <- function() {
    "surface-cell-mutation-type-count.csv"
}

NeoCount.dirName <- function(selectCoeff, neoRate, trialIndex) {
    file.path(sprintf("S%.2f", selectCoeff), sprintf("NR%s", neoRate), sprintf("Trial%02d", trialIndex))
}

NeoCount.list <- function(dirName) {
    list.files(path = dirName, pattern = NeoCount.baseName(), full.names = TRUE, recursive = TRUE)
}

NeoCount.loadTrial <- function(selectCoeff, neoRate, trialIndex) {
    fileName <- file.path(NeoCount.dirName(selectCoeff, neoRate, trialIndex), NeoCount.baseName())

    JamLog.info("Loading [%s]...", fileName)
    trialFrame <- read.csv(fileName)

    trialFrame$selectCoeff <- selectCoeff
    trialFrame$neoRate     <- as.numeric(neoRate)
    trialFrame$tumorSize   <- round(trialFrame$tumorCellCount, -floor(log10(trialFrame$tumorCellCount)))

    trialFrame
}

NeoCount.loadTrials <- function(selectCoeff, neoRate, trialIndexes) {
    trialFrames <- list()

    for (k in seq_along(trialIndexes)) {
        trialIndex <- trialIndexes[k]
        countFrame <- NeoCount.loadTrial(selectCoeff, neoRate, trialIndex)

        trialFrames[[k]] <-
            NeoCount.aggregateTrial(countFrame)
    }

    trialFrame <- do.call(rbind, trialFrames)

    aggFrame <- NeoCount.aggregateTrials(trialFrame)
    aggFrame
}

NeoCount.meanTypeCount <- function(countFrame) {
    data.frame(anyNeo      = mean(countFrame$NEOANTIGEN.count > 0.01),
               neoCount    = mean(countFrame$NEOANTIGEN.count),
               scalarCount = mean(countFrame$SCALAR.count))
}

NeoCount.aggregateTypeCountBySize <- function(selectCoeff, neoRate) {
    rawFrame <- NeoCount.load(selectCoeff, neoRate)

    aggFunc <- function(x) {
        selectCoeff <- x$selectCoeff[1]
        neoRate     <- x$neoRate[1]
        tumorSize   <- x$tumorSize[1]

        data.frame(selectCoeff = selectCoeff,
                   neoRate     = neoRate,
                   tumorSize   = tumorSize,
                   anyNeo      = mean(x$NEOANTIGEN.count > 0.01),
                   meanNeo     = mean(x$NEOANTIGEN.count),
                   meanSelect  = mean(x$SCALAR.count))
    }

    result <- do.call(rbind, by(rawFrame, rawFrame$tumorSize, aggFunc))
    rownames(result) <- NULL

    result
}

NeoCount.loadMaster <- function(selectCoeffs = c(0.0, 0.01, 0.02, 0.05, 0.10, 0.20),
                               neoRates     = c("1E-4", "1E-3", "1E-2", "1E-1"),
                               trialIndexes = 0:9) {
    masterList <- list()

    for (selectCoeff in selectCoeffs)
        for (neoRate in neoRates)
            masterList[[length(masterList) + 1]] <-
                NeoCount.loadTrials(selectCoeff, neoRate, trialIndexes)

    countMaster <- do.call(rbind, masterList)
    countMaster
}

NeoCount.plotAny <- function(countMaster) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    JamPlot.logXY(xlim = c(1E5, 1E7),
                  ylim = c(0.001, 1.0),
                  xlab = "Tumor size [cells]",
                  ylab = "Immunogenic fraction")

    plotOne <- function(selectCoeff, neoRate, col, pch, lty) {
        slice <- countMaster[which(countMaster$selectCoeff == selectCoeff & countMaster$neoRate == neoRate),]

        x  <- slice$tumorSize
        y  <- slice$anyNeoMean
        dy <- slice$anyNeoErr

        lines( x, y, col = col, lty = lty)
        points(x, y, col = col, pch = pch)

        for (k in seq_along(y))
            lines(c(x[k], x[k]), c(y[k] - 2 * dy[k], y[k] + 2 * dy[k]), col = col)
    }

    plotOne(0.01, 1E-4, 1, 1, 2)
    plotOne(0.01, 1E-3, 2, 1, 2)
    plotOne(0.01, 1E-2, 4, 1, 2)

    plotOne(0.1, 1E-4, 1, 16, 1)
    plotOne(0.1, 1E-3, 2, 16, 1)
    plotOne(0.1, 1E-2, 4, 16, 1)

    legend(4E6, 0.005, bty = "n",
           legend = c(expression(paste(mu[n], " = ", 10^-2)),
                      expression(paste(mu[n], " = ", 10^-3)),
                      expression(paste(mu[n], " = ", 10^-4))),
           col = c(4, 2, 1),
           pch = c(16, 16, 16))

    legend(6E5, 0.005, bty = "n",
           legend = c("s = 0.01", "s = 0.10"),
           col = c(1, 1),
           lty = c(2, 1))
}

NeoCount.plotAnyLast <- function(countMaster) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    dframe <- subset(countMaster, tumorSize > 9E6)

    JamPlot.logY(xlim = c(0.0, 0.20),
                 ylim = c(0.001, 1.0),
                 xlab = "Selection coefficient",
                 ylab = "Immunogenic fraction")

    plotOne <- function(neoRate, col, pch, lty) {
        slice <- dframe[which(dframe$neoRate == neoRate),]

        x  <- slice$selectCoeff
        y  <- slice$anyNeoMean
        dy <- slice$anyNeoErr

        lines( x, y, col = col, lty = lty)
        points(x, y, col = col, pch = pch)

        for (k in seq_along(y))
            lines(c(x[k], x[k]), c(y[k] - 2 * dy[k], y[k] + 2 * dy[k]), col = col)
    }

    plotOne(1E-4, 1, 15, 1)
    plotOne(1E-3, 2, 16, 2)
    plotOne(1E-2, 3, 17, 3)
    plotOne(1E-1, 4, 18, 4)
    return(0)
    plotOne(0.01, 1E-3, 2, 1, 2)
    plotOne(0.01, 1E-2, 4, 1, 2)

    plotOne(0.1, 1E-4, 1, 16, 1)
    plotOne(0.1, 1E-3, 2, 16, 1)
    plotOne(0.1, 1E-2, 4, 16, 1)

    legend(4E6, 0.005, bty = "n",
           legend = c(expression(paste(mu[n], " = ", 10^-2)),
                      expression(paste(mu[n], " = ", 10^-3)),
                      expression(paste(mu[n], " = ", 10^-4))),
           col = c(4, 2, 1),
           pch = c(16, 16, 16))

    legend(6E5, 0.005, bty = "n",
           legend = c("s = 0.01", "s = 0.10"),
           col = c(1, 1),
           lty = c(2, 1))
}

NeoCount.plotNeoMean <- function(countMaster) {
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

NeoCount.plotMeanLast <- function(countMaster) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    dframe <- subset(countMaster, tumorSize > 9E6)

    JamPlot.logY(xlim = c(0.0, 0.20),
                 ylim = c(0.001, 100.0),
                 xlab = "Selection coefficient",
                 ylab = "Neoantigen load")

    plotOne <- function(neoRate, col, pch, lty) {
        slice <- dframe[which(dframe$neoRate == neoRate),]

        x  <- slice$selectCoeff
        y  <- slice$neoMean
        dy <- slice$neoErr

        lines( x, y, col = col, lty = lty)
        points(x, y, col = col, pch = pch)

        for (k in seq_along(y))
            lines(c(x[k], x[k]), c(y[k] - 2 * dy[k], y[k] + 2 * dy[k]), col = col)
    }

    plotOne(1E-4, 1, 15, 1)
    plotOne(1E-3, 2, 16, 2)
    plotOne(1E-2, 3, 17, 3)
    plotOne(1E-1, 4, 18, 4)
    return(0)
}

NeoCount.plotSelectMeanLast <- function(countMaster) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    dframe <- subset(countMaster, tumorSize > 9E6)

    JamPlot.by(dframe,
               xkey  = "selectCoeff",
               ykey  = "selectMean",
               bykey = "neoRate",
               xlab  = "Selection coefficient",
               ylab  = "Selective mutation load",
               ylim  = c(0, 2.5),
               pch   = 15:18,
               legend.loc = "topleft",
               legend.text = expression(10^-4, 10^-3, 10^-2, 10^-1))

    errOne <- function(neoRate, col) {
        slice <- dframe[which(dframe$neoRate == neoRate),]

        x  <- slice$selectCoeff
        y  <- slice$selectMean
        dy <- slice$selectErr

        for (k in seq_along(y))
            lines(c(x[k], x[k]), c(y[k] - 2 * dy[k], y[k] + 2 * dy[k]), col = col)
    }

    errOne(1E-4, 1)
    errOne(1E-3, 2)
    errOne(1E-2, 3)
    errOne(1E-1, 4)
}

NeoCount.plotNeoLast <- function(countMaster) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    dframe <- subset(countMaster, tumorSize > 9E6)

    JamPlot.logXY(xlim = c(1E-4, 1E-1),
                  ylim = c(1E-2, 1E2),
                  xlab = "Neoantigen rate",
                  ylab = "Neoantigen mutation load")

    JamPlot.by(dframe,
               xkey  = "neoRate",
               ykey  = "neoMean",
               bykey = "selectCoeff",
               pch   = 15:20,
               overlay = TRUE,
               legend.loc = "topleft",
               legend.text = c("0", "0.01", "0.02", "0.05", "0.10", "0.20"))

    errOne <- function(selectCoeff, col) {
        slice <- dframe[which(dframe$selectCoeff == selectCoeff),]

        x  <- slice$neoRate
        y  <- slice$neoMean
        dy <- slice$neoErr

        for (k in seq_along(y))
            lines(c(x[k], x[k]), c(y[k] - 2 * dy[k], y[k] + 2 * dy[k]), col = col)
    }

    errOne(0.0,  1)
    errOne(0.01, 2)
    errOne(0.02, 3)
    errOne(0.05, 4)
    errOne(0.10, 5)
    errOne(0.20, 6)
}

NeoCount.plotNeoNormLast <- function(countMaster) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    dframe <- subset(countMaster, tumorSize > 9E6 & neoRate > 2E-4)

    dframe$neoNormMean <- dframe$neoMean / dframe$neoRate
    dframe$neoNormErr  <- dframe$neoErr  / dframe$neoRate

    JamPlot.by(dframe,
               xkey  = "selectCoeff",
               ykey  = "neoNormMean",
               bykey = "neoRate",
               xlab  = "Selection coefficient",
               ylab  = "Normalized neoantigen load",
               ylim  = c(300, 800),
               col   = c(1, 2, 4),
               pch   = 15:18,
               legend.loc = "topright",
               legend.text = expression(10^-3, 10^-2, 10^-1))

    errOne <- function(neoRate, col) {
        slice <- dframe[which(dframe$neoRate == neoRate),]

        x  <- slice$selectCoeff
        y  <- slice$neoNormMean
        dy <- slice$neoNormErr

        for (k in seq_along(y))
            lines(c(x[k], x[k]), c(y[k] - 2 * dy[k], y[k] + 2 * dy[k]), col = col)
    }

    errOne(1E-3, 1)
    errOne(1E-2, 2)
    errOne(1E-1, 4)
}

NeoCount.plotMeanLast <- function(countMaster) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    dframe <- subset(countMaster, tumorSize > 9E6)

    JamPlot.logY(xlim = c(0.0, 0.20),
                 ylim = c(0.001, 100.0),
                 xlab = "Selection coefficient",
                 ylab = "Neoantigen load")

    plotOne <- function(neoRate, col, pch, lty) {
        slice <- dframe[which(dframe$neoRate == neoRate),]

        x  <- slice$selectCoeff
        y  <- slice$neoMean
        dy <- slice$neoErr

        lines( x, y, col = col, lty = lty)
        points(x, y, col = col, pch = pch)

        for (k in seq_along(y))
            lines(c(x[k], x[k]), c(y[k] - 2 * dy[k], y[k] + 2 * dy[k]), col = col)
    }

    plotOne(1E-4, 1, 15, 1)
    plotOne(1E-3, 2, 16, 2)
    plotOne(1E-2, 3, 17, 3)
    plotOne(1E-1, 4, 18, 4)
    return(0)
}

