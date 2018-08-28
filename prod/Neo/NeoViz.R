
NeoViz.addColor <- function(coordFrame, numColor) {
    require(viridis)

    coordFrame <-
        subset(coordFrame, timeStep == max(timeStep))

    neoFrame <- data.frame(NEOANTIGEN.count   = 0:(numColor - 1),
                           viridis.NEOANTIGEN = viridis(numColor),
                           plasma.NEOANTIGEN  = plasma(numColor),
                           inferno.NEOANTIGEN = inferno(numColor))
    
    scalarFrame <- data.frame(SCALAR.count   = 0:(numColor - 1),
                              viridis.SCALAR = viridis(numColor),
                              plasma.SCALAR  = plasma(numColor),
                              inferno.SCALAR = inferno(numColor))

    coordFrame <- merge(coordFrame, neoFrame)
    coordFrame <- merge(coordFrame, scalarFrame)

    coordFrame$default.NEOANTIGEN <- 1 + coordFrame$NEOANTIGEN.count
    coordFrame$default.SCALAR     <- 1 + coordFrame$SCALAR.count

    coordFrame
}

NeoViz.plot3d <- function(coordFrame, mutCode, colorCode, type = "p",
                          legend = TRUE, legend.x = 0.8, legend.y = 0.8, ...) {
    coordFrame  <- subset(coordFrame, normRadialDist > 1.0)
    colorColumn <- sprintf("%s.%s", colorCode, mutCode)
    
    plot3d(coordFrame$siteCoordX,
           coordFrame$siteCoordY,
           coordFrame$siteCoordZ,
           xlab = "",
           ylab = "",
           zlab = "",
           axes = FALSE,
           type = type,
           col  = coordFrame[,colorColumn],
           ...)

    aspect3d(1, 1, 1)

    countColumn <- sprintf("%s.count", mutCode)
    legendFrame <- coordFrame[!duplicated(coordFrame[,countColumn]),]
    legendFrame <- legendFrame[order(legendFrame[,countColumn]),]

    if (legend)
        legend3d(legend.x, legend.y, bty = "n",
                 legend = as.character(legendFrame[,countColumn]),
                 pch    = 16,
                 col    = legendFrame[,colorColumn])
}

