
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

    // Number of mutations in the MRCA...
    private final int mrcaMutCount;

    // Fractional threshold for VAF reporting...
    private final double vafThreshold;

    // Number of mutations present above the VAF threshold...
    private final int aboveThresholdCount;

    // Fraction of mutations present above the VAF threshold...
    private final double aboveThresholdFrac;

    private MutGenThresholdRecord(int    maxMutationTime,
                                  long   maxMutationCount,
                                  Coord  sampleSite,
                                  int    mrcaMutCount,
                                  double vafThreshold,
                                  int    aboveThresholdCount,
                                  double aboveThresholdFrac) {
        this.trialIndex = TumorDriver.global().getTrialIndex();

        this.maxMutationTime  = maxMutationTime;
        this.maxMutationCount = maxMutationCount;

        this.sampleSite   = sampleSite;
        this.mrcaMutCount = mrcaMutCount;
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

        int    mrcaMutCount = bulkSample.getAncestorGenotype().countAccumulatedMutations();
        double vafThreshold = MutGenThresholdReport.instance().getVAFThreshold();

        VAF vaf = bulkSample.getVAF();

        double aboveThresholdFrac  = vaf.computeFractionAbove(vafThreshold);
        int    aboveThresholdCount = (int) (aboveThresholdFrac * vaf.countMutations());

        return new MutGenThresholdRecord(maxMutationTime,
                                         maxMutationCount,
                                         sampleSite,
                                         mrcaMutCount,
                                         vafThreshold,
                                         aboveThresholdCount,
                                         aboveThresholdFrac);
        }

    @Override public String formatLine() {
        return String.format("%d,%d,%d,%d,%d,%d,%d,%.2f,%d,%.4f",
                             trialIndex,
                             maxMutationTime,
                             maxMutationCount,
                             sampleSite.x,
                             sampleSite.y,
                             sampleSite.z,
                             mrcaMutCount,
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
            + ",mrcaMutCount"
            + ",vafThreshold"
            + ",aboveThresholdCount"
            + ",aboveThresholdFrac";
    }
}
