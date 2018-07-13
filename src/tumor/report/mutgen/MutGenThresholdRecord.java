
package tumor.report.mutgen;

import jam.lattice.Coord;
import jam.math.DoubleUtil;
import jam.report.ReportRecord;

import tumor.driver.TumorDriver;
import tumor.mutation.MutationGenerator;
import tumor.report.bulk.BulkSample;
import tumor.report.vaf.VAF;

/**
 * Characterizes the number and clonality of mutations in a bulk
 * sample as a function of the mutation generator cutoff threshold.
 */
public final class MutGenThresholdRecord implements ReportRecord {
    // Simulation trial index...
    private final int trialIndex;

    // Latest time allowed for mutation generation...
    private final int maxMutationTime;

    // Maximum number of mutations allowed...
    private final long maxMutationCount;

    // Bulk sample location...
    private final Coord sampleSite;

    // Total number of unique mutations...
    private final long uniqueCount;

    // Number of clonal mutations...
    private final long clonalCount;

    // Fraction of mutations that are clonal...
    private final double clonalFrac;

    // Fractional threshold for VAF reporting...
    private final double vafThreshold;

    // Number of mutations present above the VAF threshold...
    private final long aboveThresholdCount;

    // Fraction of mutations present above the VAF threshold...
    private final double aboveThresholdFrac;

    private MutGenThresholdRecord(int    maxMutationTime,
                                  long   maxMutationCount,
                                  Coord  sampleSite,
                                  long   uniqueCount,
                                  long   clonalCount,
                                  double clonalFrac,
                                  double vafThreshold,
                                  long   aboveThresholdCount,
                                  double aboveThresholdFrac) {
        this.trialIndex = TumorDriver.global().getTrialIndex();

        this.maxMutationTime  = maxMutationTime;
        this.maxMutationCount = maxMutationCount;

        this.sampleSite   = sampleSite;
        this.uniqueCount  = uniqueCount;
        this.clonalCount  = clonalCount;
        this.clonalFrac   = clonalFrac;
        this.vafThreshold = vafThreshold;

        this.aboveThresholdCount = aboveThresholdCount;
        this.aboveThresholdFrac  = aboveThresholdFrac;
    }

    /**
     * Computes the mutation generator threshold record for a primary
     * tumor sample.
     *
     * @param bulkSample a bulk sample from the primary tumor.
     *
     * @return the mutation generator threshold record for the sample.
     */
    public static MutGenThresholdRecord compute(BulkSample bulkSample) {
        int  maxMutationTime  = MutationGenerator.getMaxMutationTime();
        long maxMutationCount = MutationGenerator.getMaxMutationCount();

        Coord sampleSite = bulkSample.getCenterSite();

        VAF vaf = bulkSample.getVAF();

        long   uniqueCount  = vaf.countMutations();
        long   clonalCount  = vaf.countClonal();
        double clonalFrac   = DoubleUtil.ratio(clonalCount, uniqueCount);
        double vafThreshold = MutGenThresholdReport.instance().getVAFThreshold();

        long   aboveThresholdCount = vaf.countAbove(vafThreshold);
        double aboveThresholdFrac  = DoubleUtil.ratio(aboveThresholdCount, uniqueCount);

        return new MutGenThresholdRecord(maxMutationTime,
                                         maxMutationCount,
                                         sampleSite,
                                         uniqueCount,
                                         clonalCount,
                                         clonalFrac,
                                         vafThreshold,
                                         aboveThresholdCount,
                                         aboveThresholdFrac);
        }

    @Override public String formatLine() {
        return String.format("%d,%d,%d,%d,%d,%d,%d,%d,%.4g,%.2f,%d,%.4f",
                             trialIndex,
                             maxMutationTime,
                             maxMutationCount,
                             sampleSite.x,
                             sampleSite.y,
                             sampleSite.z,
                             uniqueCount,
                             clonalCount,
                             clonalFrac,
                             vafThreshold,
                             aboveThresholdCount,
                             aboveThresholdFrac);
    }

    @Override public String getBaseName() {
        return MutGenThresholdReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "trialIndex"
            + ",maxMutationTime"
            + ",maxMutationCount"
            + ",sampleSiteX"
            + ",sampleSiteY"
            + ",sampleSiteZ"
            + ",uniqueCount"
            + ",clonalCount"
            + ",clonalFrac"
            + ",vafThreshold"
            + ",aboveThresholdCount"
            + ",aboveThresholdFrac";
    }
}
