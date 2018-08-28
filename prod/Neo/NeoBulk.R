
NeoBulk.aggregateTrialByNRD <- function(trialFrame, binSize = 0.1) {
    aggFunc <- function(x) {
        data.frame(selectCoeff = x$selectCoeff[1],
                   neoRate     = x$neoRate[1],
                   trialIndex  = x$trialIndex[1],
                   roundNRD    = x$roundNRD[1],
                   anyNeoMean  = mean(x$NEOANTIGEN.count > 0.1),
                   neoMean     = mean(x$NEOANTIGEN.count),
                   selectMean  = mean(x$SCALAR.count))
    }

    trialFrame <- trialFrame[trialFrame$timeStep == max(trialFrame$timeStep),]
    trialFrame$roundNRD <- binSize * round(trialFrame$normRadialDist / binSize)

    result <- do.call(rbind, by(trialFrame, trialFrame$roundNRD, aggFunc))
    rownames(result) <- NULL

    result
}

NeoBulk.aggregateTrialByCount <- function(trialFrame) {
    aggFunc <- function(x) {
        data.frame(selectCoeff = x$selectCoeff[1],
                   neoRate     = x$neoRate[1],
                   trialIndex  = x$trialIndex[1],
                   timeStep    = x$timeStep[1],
                   cellCount   = x$cellCount[1],
                   anyNeoMean  = mean(x$NEOANTIGEN.count > 0.1),
                   neoMean     = mean(x$NEOANTIGEN.count),
                   selectMean  = mean(x$SCALAR.count))
    }

    result <- do.call(rbind, by(trialFrame, trialFrame$timeStep, aggFunc))
    rownames(result) <- NULL

    result
}

NeoBulk.aggregateTrialsByNRD <- function(trialFrames) {
    aggFunc <- function(x) {
        data.frame(selectCoeff = x$selectCoeff[1],
                   neoRate     = x$neoRate[1],
                   roundNRD    = x$roundNRD[1],
                   anyNeoMean  = mean(x$anyNeoMean),
                   anyNeoErr   = sd(x$anyNeoMean) / sqrt(nrow(x)),
                   neoMean     = mean(x$neoMean),
                   neoErr      = sd(x$neoMean) / sqrt(nrow(x)),
                   selectMean  = mean(x$selectMean),
                   selectErr   = sd(x$selectMean) / sqrt(nrow(x)))
    }

    result <- do.call(rbind, by(trialFrames, trialFrames$roundNRD, aggFunc))
    rownames(result) <- NULL

    result
}

NeoBulk.aggregateTrialsByCount <- function(trialFrames) {
    aggFunc <- function(x) {
        data.frame(selectCoeff = x$selectCoeff[1],
                   neoRate     = x$neoRate[1],
                   cellCount   = x$cellCount[1],
                   timeStep    = mean(x$timeStep),
                   timeErr     = sd(x$timeStep) / sqrt(nrow(x)),
                   anyNeoMean  = mean(x$anyNeoMean),
                   anyNeoErr   = sd(x$anyNeoMean) / sqrt(nrow(x)),
                   neoMean     = mean(x$neoMean),
                   neoErr      = sd(x$neoMean) / sqrt(nrow(x)),
                   selectMean  = mean(x$selectMean),
                   selectErr   = sd(x$selectMean) / sqrt(nrow(x)))
    }

    result <- do.call(rbind, by(trialFrames, trialFrames$cellCount, aggFunc))
    rownames(result) <- NULL

    result
}

NeoBulk.baseName <- function() {
    "bulk-cell-mutation-type-count.csv"
}

NeoBulk.dirName <- function(selectCoeff, neoRate, trialIndex) {
    file.path(sprintf("S%.2f", selectCoeff), sprintf("NR%s", neoRate), sprintf("Trial%02d", trialIndex))
}

NeoBulk.loadTrial <- function(selectCoeff, neoRate, trialIndex) {
    fileName <- file.path(NeoBulk.dirName(selectCoeff, neoRate, trialIndex), NeoBulk.baseName())

    JamLog.info("Loading [%s]...", fileName)
    trialFrame <- read.csv(fileName)

    trialFrame$selectCoeff <- selectCoeff
    trialFrame$neoRate     <- as.numeric(neoRate)
    trialFrame$cellCount   <- round(trialFrame$tumorCellCount, -floor(log10(trialFrame$tumorCellCount)))

    trialFrame
}

NeoBulk.loadTrials <- function(selectCoeff, neoRate, trialIndexes, binSize = 0.1) {
    nrdFrames  <- list()
    countFrames <- list()

    for (k in seq_along(trialIndexes)) {
        trialIndex <- trialIndexes[k]
        trialFrame <- NeoBulk.loadTrial(selectCoeff, neoRate, trialIndex)

        nrdFrames[[k]]   <- NeoBulk.aggregateTrialByNRD(trialFrame, binSize)
        countFrames[[k]] <- NeoBulk.aggregateTrialByCount(trialFrame)
    }

    nrdAgg   <- NeoBulk.aggregateTrialsByNRD(do.call(rbind, nrdFrames))
    countAgg <- NeoBulk.aggregateTrialsByCount(do.call(rbind, countFrames))

    list(NRDAgg = nrdAgg, CountAgg = countAgg)
}

NeoBulk.loadMaster <- function(selectCoeffs = c(0.01, 0.02, 0.05, 0.10, 0.20, 0.40),
                               neoRates     = c("1E-4", "1E-3", "1E-2", "1E-1"),
                               trialIndexes = 0:9,
                               binSize      = 0.1) {
    nrdList <- list()
    countList <- list()

    for (selectCoeff in selectCoeffs) {
        for (neoRate in neoRates) {
            trialAgg <- NeoBulk.loadTrials(selectCoeff, neoRate, trialIndexes, binSize)
            
            nrdList[[length(nrdList) + 1]] <- trialAgg$NRDAgg
            countList[[length(countList) + 1]] <- trialAgg$CountAgg
        }
    }

    nrdMaster   <- do.call(rbind, nrdList)
    countMaster <- do.call(rbind, countList)

    list(NRDMaster = nrdMaster, CountMaster = countMaster)
}

NeoBulk.plotNRDAnyByCoeff <- function(master, targetNeoRate, ...) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    slice <- subset(master, neoRate == targetNeoRate)

    slice$x <- slice$roundNRD / sqrt(5.0 / 3.0)
    slice <- subset(slice, x > 0.05)
    slice <- subset(slice, x < 1.05)

    JamPlot.by(slice,
               xkey  = "x",
               ykey  = "anyNeoMean",
               dykey = "anyNeoErr",
               bykey = "selectCoeff",
               xlab  = "Normalized radial distance",
               ylab  = "Immunogenic fraction",
               log   = "y",
               pch   = 16,
               ...)
}

NeoBulk.plotNRDNeoByCoeff <- function(master, targetNeoRate, ...) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    slice <- subset(master, neoRate == targetNeoRate)
    slice$x <- slice$roundNRD / sqrt(5.0 / 3.0)
    slice <- subset(slice, x < 1.05)

    JamPlot.by(slice,
               xkey  = "x",
               ykey  = "neoMean",
               dykey = "neoErr",
               bykey = "selectCoeff",
               xlab  = "Normalized radial distance",
               ylab  = "Neoantigen count",
               ...)
}

NeoBulk.plotCountNeoByCoeff <- function(master, targetNeoRate, ...) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    slice <- subset(master, neoRate == targetNeoRate)

    JamPlot.logX(xlim = c(1E3, 1E7),
                 ylim = c(0, 6),
                 xlab = "Tumor size [cells]",
                 ylab = "Neoantigen count",
                 tick.power = 3:7)

    JamPlot.by(slice,
               xkey  = "cellCount",
               ykey  = "neoMean",
               dykey = "neoErr",
               bykey = "selectCoeff",
               xlab  = "Tumor size [cells]",
               ylab  = "Neoantigen count",
               pch   = 16,
               overlay = TRUE,
               ...)
}

NeoBulk.plotTimeNeoByCoeff <- function(master, targetNeoRate, ...) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    slice <- subset(master, neoRate == targetNeoRate)
    slice$timeDays <- slice$timeStep * 2.2

    JamPlot.by(slice,
               xkey  = "timeDays",
               ykey  = "neoMean",
               dykey = "neoErr",
               bykey = "selectCoeff",
               xlab  = "Time [days]",
               ylab  = "Neoantigen count",
               pch   = 16,
               ...)
}

NeoBulk.plotAnyByNeoRate <- function(finalCount) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    JamPlot.logX(xlim = c(1E-4, 1E-1),
                 ylim = c(0.0, 1.0),
                 xlab = "Neoantigen mutation rate",
                 ylab = "Immunogenic fraction",
                 tick.power = -4:-1)

    JamPlot.by(finalCount,
               xkey  = "neoRate",
               ykey  = "anyNeoMean",
               dykey = "anyNeoErr",
               bykey = "selectCoeff",
               pch   = 16,
               col   = c(1, 2, 4),
               overlay = TRUE,
               legend.text = sprintf("%.2f", sort(unique(finalCount$selectCoeff))))
}
