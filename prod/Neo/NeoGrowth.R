
NeoGrowth.aggregate <- function(masterFrame) {
    aggFunc <- function(x) {
        selectCoeff <- x$selectCoeff[1]
        numObs      <- nrow(x)

        birth.mean <- mean(x$birthCount)
        birth.err  <- sd(x$birthCount) / sqrt(numObs)

        death.mean <- mean(x$deathCount)
        death.err  <- sd(x$deathCount) / sqrt(numObs)

        data.frame(selectCoeff = selectCoeff,
                   numObs      = numObs,
                   birth.mean  = birth.mean,
                   birth.err   = birth.err,
                   death.mean  = death.mean,
                   death.err   = death.err)
    }

    do.call(rbind, by(masterFrame, masterFrame$selectCoeff, aggFunc))
}

NeoGrowth.dirName <- function(selectCoeff, neoRate) {
    file.path(sprintf("S%.2f", selectCoeff), sprintf("NR%s", neoRate))
}

NeoGrowth.list <- function(dirName) {
    list.files(path = dirName, pattern = "growth-count.csv", full.names = TRUE, recursive = TRUE)
}

NeoGrowth.loadLast <- function(selectCoeff, neoRate) {
    frameList <- list()
    fileNames <- NeoGrowth.list(NeoGrowth.dirName(selectCoeff, neoRate))

    for (fileName in fileNames) {
        dframe <- read.csv(fileName)
        dframe <- subset(dframe, timeStep == max(timeStep))

        frameList[[fileName]] <- dframe
    }

    result <- do.call(rbind, frameList)
    rownames(result) <- NULL

    result$selectCoeff <- selectCoeff
    result$neoRate     <- as.numeric(neoRate)

    result
}

NeoGrowth.loadMaster <- function(selectCoeffs = c(0.01, 0.02, 0.05, 0.10, 0.20, 0.40),
                                 neoRates     = c("1E-4", "1E-3", "1E-2", "1E-1")) {
    frameList <- list()

    for (selectCoeff in selectCoeffs)
        for (neoRate in neoRates)
            frameList[[length(frameList) + 1]] <-
                NeoGrowth.loadLast(selectCoeff, neoRate)

    do.call(rbind, frameList)
}

NeoGrowth.plot <- function(aggFrame) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    x   <- aggFrame$selectCoeff
    y1  <- aggFrame$birth.mean / 1E6
    dy1 <- aggFrame$birth.err  / 1E6
    y2  <- aggFrame$death.mean / 1E6
    dy2 <- aggFrame$death.err  / 1E6

    plot(x, y1,
         type = "n",
         xlab = "Selection coefficient",
         ylab = "Event count [millions]",
         ylim = c(0, 60))

    lines(x, y1, lwd = 2, col = 1)
    lines(x, y2, lwd = 2, col = 2)

    points(x, y1, pch = 16, col = 1)
    points(x, y2, pch = 16, col = 2)
}
