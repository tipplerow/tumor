
SelectionRate.plotNet <- function() {
    par(las = 1)
    par(fig = c(0.05, 0.95, 0.15, 0.85))

    k  <- 0:10
    d0 <- 0.45
    
    netRate <- function(s) {
        1.0 - 2.0 * d0 * ((1 - s) ^ k)
    }
        
    s01 <- netRate(0.01)
    s05 <- netRate(0.05)
    s10 <- netRate(0.10)
    s20 <- netRate(0.20)
    s40 <- netRate(0.40)

    plot(k, s40,
         type = "n",
         xlab = "Selective mutation count",
         ylab = "Net growth rate",
         ylim = c(0.0, 1.0))

    lines(k, s01, col = 1, lwd = 2)
    lines(k, s05, col = 2, lwd = 2)
    lines(k, s10, col = 3, lwd = 2)
    lines(k, s20, col = 4, lwd = 2)
    lines(k, s40, col = 6, lwd = 2)

    legend("topleft", bty = "n",
           legend = c("0.01", "0.05", "0.10", "0.20", "0.40"),
           lty    = c(1, 1, 1, 1, 1),
           lwd    = c(2, 2, 2, 2, 2),
           col    = c(1, 2, 3, 4, 6))
}
