
NeoDim.TIME_UNIT   <- 2.2   ## days / step
NeoDim.LENGTH_UNIT <- 0.022 ## mm / site
NeoDim.RG_RESCALE  <- 2.0 * sqrt(5.0 / 3.0)

NeoDim.aggregate <- function(selectCoeff, neoRate) {
    rawFrame <- NeoDim.load(selectCoeff, neoRate)

    aggFunc <- function(x) {
        timeStep    <- x$timeBin[1]
        timeDays    <- NeoDim.TIME_UNIT * timeStep
        selectCoeff <- x$selectCoeff[1]
        neoRate     <- x$neoRate[1]
        numObs      <- nrow(x)

        RG.mean <- NeoDim.LENGTH_UNIT * mean(x$RG)
        RG.err  <- NeoDim.LENGTH_UNIT * sd(x$RG) / sqrt(numObs)

        size.mean <- NeoDim.RG_RESCALE * RG.mean
        size.err  <- NeoDim.RG_RESCALE * RG.err

        asph.mean <- mean(x$asphericity / x$RG)
        asph.err  <- sd(x$asphericity / x$RG) / sqrt(numObs)

        data.frame(selectCoeff = selectCoeff,
                   neoRate     = neoRate,
                   timeStep    = timeStep,
                   timeDays    = timeDays,
                   numObs      = numObs,
                   RG.mean     = RG.mean,
                   RG.err      = RG.err,
                   size.mean   = size.mean,
                   size.err    = size.err,
                   asph.mean   = asph.mean,
                   asph.err    = asph.err)
    }

    do.call(rbind, by(rawFrame, rawFrame$timeBin, aggFunc))
}

NeoDim.dirName <- function(selectCoeff, neoRate) {
    file.path(sprintf("S%.2f", selectCoeff), sprintf("NR%s", neoRate))
}

NeoDim.list <- function(dirName) {
    list.files(path = dirName, pattern = "tumor-dimension.csv", full.names = TRUE, recursive = TRUE)
}

NeoDim.load <- function(selectCoeff, neoRate) {
    frameList <- list()
    fileNames <- NeoDim.list(NeoDim.dirName(selectCoeff, neoRate))

    for (fileName in fileNames)
        frameList[[fileName]] <- read.csv(fileName)

    result <- do.call(rbind, frameList)
    rownames(result) <- NULL

    result$selectCoeff <- selectCoeff
    result$neoRate     <- as.numeric(neoRate)
    result$timeBin     <- round(result$timeStep, -1)

    result
}

NeoDim.scanSelection <- function(selectCoeffs = c(0.0, 0.01, 0.02, 0.05, 0.10, 0.20),
                                 neoRates     = c("1E-4", "1E-3", "1E-2", "1E-1")) {
    frameList <- list()

    for (selectCoeff in selectCoeffs)
        for (neoRate in neoRates)
            frameList[[length(frameList) + 1]] <-
                NeoDim.aggregate(selectCoeff, neoRate)

    do.call(rbind, frameList)
}

NeoDim.plotRG <- function(selectCoeffs = c(0.0, 0.05, 0.10, 0.20), neoRate = "1E-3", xlim, ylim) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    plot(x = xlim,
         y = ylim,
         type = "n",
         xlim = xlim,
         ylim = ylim,
         xlab = "Time [days]",
         ylab = "Radius of gyration [mm]")

    colfunc <- function(col) {
        ifelse(col < 5, col, col + 1)
    }

    for (k in seq_along(selectCoeffs)) {
        dframe <- NeoDim.aggregate(selectCoeffs[k], neoRate)
        dframe <- subset(dframe, numObs > 5)

        x  <- dframe$timeDays
        y  <- dframe$RG.mean
        dy <- dframe$RG.err

        lines(x, y, col = k)
        points(x, y, pch = 16, col = colfunc(k))

        for (j in seq_along(x))
            lines(c(x[j], x[j]), c(y[j] - 2.0 * dy[j], y[j] + 2.0 * dy[j]), col = colfunc(k))
    }

    legend("topleft",
           bty    = "n",
           legend = as.character(selectCoeffs),
           col    = colfunc(1:length(selectCoeffs)),
           pch    = rep(16, length(selectCoeffs)))
}

NeoDim.plotSize <- function(selectCoeffs = c(0.0, 0.05, 0.10, 0.20), neoRate = "1E-3", xlim, ylim) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    plot(x = xlim,
         y = ylim,
         type = "n",
         xlim = xlim,
         ylim = ylim,
         xlab = "Time [days]",
         ylab = "Tumor dimension [mm]")

    colfunc <- function(col) {
        ifelse(col < 5, col, col + 1)
    }

    for (k in seq_along(selectCoeffs)) {
        dframe <- NeoDim.aggregate(selectCoeffs[k], neoRate)
        dframe <- subset(dframe, numObs > 5)

        x  <- c(0, dframe$timeDays)
        y  <- c(0, dframe$size.mean)
        dy <- c(0, dframe$size.err)

        if (k %in% c(2, 3)) {
            nx <- length(x) - 1
            x <- x[1:nx]
            y <- y[1:nx]
            dy <- dy[1:nx]
        }

        lines(x, y, col = colfunc(k))
        points(x, y, pch = 16, col = colfunc(k))

        for (j in seq_along(x))
            lines(c(x[j], x[j]), c(y[j] - 2.0 * dy[j], y[j] + 2.0 * dy[j]), col = colfunc(k))
    }

    legend("topleft",
           bty    = "n",
           legend = as.character(selectCoeffs),
           col    = colfunc(1:length(selectCoeffs)),
           pch    = rep(16, length(selectCoeffs)))
}

NeoDim.plotAsphericity <- function(selectCoeffs = c(0.0, 0.05, 0.10, 0.20), neoRate = "1E-3", xlim, ylim) {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    plot(x = xlim,
         y = ylim,
         type = "n",
         xlim = xlim,
         ylim = ylim,
         xlab = "Time [days]",
         ylab = "Asphericity")

    colfunc <- function(col) {
        ifelse(col < 5, col, col + 1)
    }

    for (k in seq_along(selectCoeffs)) {
        dframe <- NeoDim.aggregate(selectCoeffs[k], neoRate)
        dframe <- subset(dframe, numObs > 5)

        x  <- dframe$timeDays
        y  <- dframe$asph.mean
        dy <- dframe$asph.err

        if (k == 2) {
            nx <- length(x) - 1
            x <- x[1:nx]
            y <- y[1:nx]
            dy <- dy[1:nx]
        }

        lines(x, y, col = colfunc(k))
        points(x, y, pch = 16, col = colfunc(k))

        for (j in seq_along(x))
            lines(c(x[j], x[j]), c(y[j] - 2.0 * dy[j], y[j] + 2.0 * dy[j]), col = colfunc(k))
    }

    legend("topleft",
           bty    = "n",
           legend = as.character(selectCoeffs),
           col    = colfunc(1:length(selectCoeffs)),
           pch    = rep(16, length(selectCoeffs)))
}
