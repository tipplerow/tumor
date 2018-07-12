
BulkSampleSite.plot3d <- function(compCoordFrame, sampleSiteFrame) {
    k <- sample(nrow(compCoordFrame), 50000)
    plot3d(compCoordFrame$x[k], compCoordFrame$y[k], compCoordFrame$z[k],
           axes = FALSE, xlab = "X", ylab = "Y", zlab = "Z",
           type = "p", size = 2)

    keepSamples <-
        which(sampleSiteFrame$centerSiteX >= 0
              & sampleSiteFrame$centerSiteY >= 0
              & sampleSiteFrame$centerSiteZ >= 0)

    sampleSiteFrame <- sampleSiteFrame[keepSamples,]
    sampleIndexes   <- sort(unique(sampleSiteFrame$sampleIndex))

    for (k in seq_along(sampleIndexes)) {
        slice <- subset(sampleSiteFrame, sampleIndex == sampleIndexes[k])
        slice <- slice[sample(nrow(slice), 500),]
        plot3d(slice$sampleSiteX, slice$sampleSiteY, slice$sampleSiteZ, col = 2, size = 3, add = TRUE)
    }

    box3d()
}

    
