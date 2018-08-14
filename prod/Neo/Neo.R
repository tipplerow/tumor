
Neo.meanTypeCount <- function(countFrame) {
    data.frame(neoCount    = mean(countFrame$NEOANTIGEN.count),
               scalarCount = mean(countFrame$SCALAR.count))
}

Neo.aggregateTypeCounts <- function(dirName) {
    countFiles  <- Neo.listTypeCount(dirName)
    countFrames <- lapply(countFiles, Neo.loadFinal)
    meanFrames  <- lapply(countFrames, Neo.meanTypeCount)
    meanFrame   <- do.call(rbind, meanFrames)
    
    data.frame(neoMean    = mean(meanFrame$neoCount),
               neoErr     = sd(meanFrame$neoCount) / sqrt(nrow(meanFrame)),
               scalarMean = mean(meanFrame$scalarCount),
               scalarErr  = sd(meanFrame$scalarCount) / sqrt(nrow(meanFrame)))
}

Neo.aggregateTypeCountsByCoeff <- function(scalarCoeffs      = c(0.0, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.4),
                                           capacityDir       = "C5000",
                                           scalarRateDir     = "SR1E-5",
                                           neoantigenRateDir = "NR1E-3") {
    aggList <- list()

    for (scalarCoeff in scalarCoeffs) {
        scalarCoeffDir <- sprintf("S%.2f", scalarCoeff)
        dirName <- file.path(capacityDir, scalarCoeffDir, scalarRateDir, neoantigenRateDir)

        meanFrame <- Neo.aggregateTypeCounts(dirName)
        meanFrame$scalarCoeff <- scalarCoeff

        aggList[[as.character(scalarCoeff)]] <- meanFrame
    }

    do.call(rbind, aggList)
}

Neo.aggregateTypeCountsByNeoRate <- function(neoRates = c("1E-4", "2E-4", "5E-4", "1E-3", "2E-3", "5E-3", "1E-2"),
                                             capacityDir       = "C5000",
                                             scalarCoeffDir    = "S0.10",
                                             scalarRateDir     = "SR1E-5",
                                             neoantigenRateDir = "NR1E-3") {
    aggList <- list()

    for (neoRate in neoRates) {
        neoRateDir <- sprintf("NR%s", neoRate)
        dirName    <- file.path(capacityDir, scalarCoeffDir, scalarRateDir, neoRateDir)

        meanFrame <- Neo.aggregateTypeCounts(dirName)
        meanFrame$neoRate <- as.numeric(neoRate)

        aggList[[neoRate]] <- meanFrame
    }

    do.call(rbind, aggList)
}

Neo.aggregateTypePair <- function(pairFrame) {
    pairFrame$siteDist <- as.integer(round(pairFrame$arcDist))
    
    meanNeo    <- mean(pairFrame$NEOANTIGEN.count1 == pairFrame$NEOANTIGEN.count2)
    meanScalar <- mean(pairFrame$SCALAR.count1     == pairFrame$SCALAR.count2)

    aggFunc <- function(x) {
        neoMatch <- as.numeric(x$NEOANTIGEN.count1 == x$NEOANTIGEN.count2)
        neoMean  <- mean(neoMatch)
        neoSD    <- sd(neoMatch)

        scalarMatch <- as.numeric(x$SCALAR.count1     == x$SCALAR.count2)
        scalarMean  <- mean(scalarMatch)
        scalarSD    <- sd(scalarMatch)

        data.frame(siteDist   = x$siteDist[1],
                   obsCount   = nrow(x),
                   neoMean    = neoMean,
                   neoSD      = neoSD,
                   scalarMean = scalarMean,
                   scalarSD   = scalarSD,
                   neoCorr    = (neoMean - meanNeo) / (1.0 - meanNeo),
                   scalarCorr = (scalarMean - meanScalar) / (1.0 - meanScalar),
                   neoErr     = neoMean / sqrt(nrow(x)) / (1.0 - meanNeo),
                   scalarErr  = scalarMean / sqrt(nrow(x)) / (1.0 - meanScalar))
    }

    do.call(rbind, by(pairFrame, pairFrame$siteDist, aggFunc))
}

Neo.aggregateTypePairs <- function(dirName) {
    pairFiles  <- Neo.listTypePair(dirName)
    pairFrames <- lapply(pairFiles, Neo.loadFinal)
    corrFrames <- lapply(pairFrames, Neo.aggregateTypePair)
    corrFrame  <- do.call(rbind, corrFrames)

    aggFunc <- function(x) {
        data.frame(siteDist   = x$siteDist[1],
                   obsCount   = nrow(x),
                   neoCorr    = mean(x$neoCorr),
                   neoErr     = sd(x$neoCorr) / sqrt(nrow(x)),
                   scalarCorr = mean(x$scalarCorr),
                   scalarErr  = sd(x$scalarCorr) / sqrt(nrow(x)))
    }
    
    do.call(rbind, by(corrFrame, corrFrame$siteDist, aggFunc))
}

Neo.plotCorrLog <- function(corrFrame, capacity, siteMax, xlim, ylim, ...) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    corrFrame$cellDist <- corrFrame$siteDist * (capacity ^ 0.3333)
    
    corrFrame <- subset(corrFrame, 1 <= siteDist & siteDist <= siteMax)
    corrFrame <- subset(corrFrame, cellDist <= xlim[2])
    corrFrame <- subset(corrFrame, scalarCorr > 0)

    JamPlot.logY(xlim, ylim, 
                 xlab = "Distance [cell diameters]",
                 ylab = "Mutation count correlation",
                 tick.power = -2:0,
                 ...)

    points(corrFrame$cellDist, corrFrame$scalarCorr)

    for (k in 1:nrow(corrFrame))
        
    points(corrFrame$cellDist, corrFrame$neoCorr, pch = 2)
}

Neo.listTypeCount <- function(dirName) {
    list.files(path = dirName, pattern = "surface-cell-mutation-type-count.csv", full.names = TRUE, recursive = TRUE)
}

Neo.listTypePair <- function(dirName) {
    list.files(path = dirName, pattern = "surface-cell-mutation-type-pair.csv", full.names = TRUE, recursive = TRUE)
}

Neo.loadFinal <- function(fileName) {
    JamLog.info("Loading [%s]...", fileName)

    dframe <- read.csv(fileName)
    dframe <- subset(dframe, timeStep == max(timeStep))
    dframe
}

Neo.plotTypeCount <- function(countFrame, colName, type = "s", size = 1, ...) {
    plot3d(countFrame$siteCoordX,
           countFrame$siteCoordY,
           countFrame$siteCoordZ,
           col  = 1 + countFrame[,colName],
           type = type,
           size = size,
           xlab = "",
           ylab = "",
           zlab = "",
           axes = FALSE,
           ...)

    counts <- sort(unique(countFrame[,colName]))
    legend3d(0.8, 0.8,
             bty    = "n",
             legend = as.character(counts),
             col    = 1 + counts,
             pch    = rep(16, length(counts)))
}

Neo.rbindFinalTypeCount <- function(dirName) {
    fileNames <- Neo.listTypeCount(dirName)
    rawFrames <- lapply(fileNames, Neo.loadFinal)
    rawFrame  <- do.call(rbind, rawFrames)
    rawFrame
}

Neo.countTypes <- function(rawFrame) {
    byNeo    <- by(rawFrame, rawFrame$NEOANTIGEN.count, nrow)
    byScalar <- by(rawFrame, rawFrame$SCALAR.count,     nrow)

    neoCount <-
        data.frame(mutCount = as.integer(names(byNeo)),
                   NEOANTIGEN.count = as.integer(byNeo))

    scalarCount <-
        data.frame(mutCount = as.integer(names(byScalar)),
                   SCALAR.count = as.integer(byScalar))

    result <- merge(neoCount, scalarCount, by = "mutCount", all = TRUE)

    result$NEOANTIGEN.count <- Filter.replaceNA(result$NEOANTIGEN.count, 0)
    result$SCALAR.count     <- Filter.replaceNA(result$SCALAR.count, 0)

    result$NEOANTIGEN.frac <- result$NEOANTIGEN.count / sum(result$NEOANTIGEN.count)
    result$SCALAR.frac     <- result$SCALAR.count     / sum(result$SCALAR.count)
    result
}

Neo.barplotTypeCount <- function(frameList, colName, countMax = 6, ...) {
    countMatrix <- matrix(data = 0.0, nrow = length(frameList), ncol = 1 + countMax)

    for (k in seq_along(frameList))
        countMatrix[k,] <-
            frameList[[k]][1:(countMax + 1),colName]

    par(las = 1)
    barplot(countMatrix, beside = TRUE, names.arg = as.character(0:countMax),
            xlab = "Mutations per cell", ylab = "Cell fraction",
            legend.text = names(frameList))
}

