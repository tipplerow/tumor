
package tumor.report.metastasis;

import jam.report.ReportRecord;

import tumor.driver.TumorDriver;
import tumor.mutation.MutationalDistance;
import tumor.report.bulk.BulkSample;

/**
 * Characterizes the mutational distance beween a metastasis seed and
 * a bulk sample from the primary tumor.
 */
public final class MetMutDistRecord implements ReportRecord {
    // The simulation trial index...
    private final int trialIndex;

    // The time of dissemination...
    private final int disseminationTime;

    // The size of the primary tumor at the time of dissemination...
    private final long disseminationSize;

    // The time of bulk sampling in the primary tumor...
    private final int bulkSamplingTime;

    // The size of the primary tumor at the time of bulk sampling...
    private final long primaryTumorSize;

    // Number of clonal mutations in the metastatis...
    private final long metClonalMutCount;

    // Total number of unique mutations in the primary bulk sample...
    private final long bulkTotalMutCount;

    // Number of clonal mutations in the primary bulk sample...
    private final long bulkClonalMutCount;

    // Number of mutations shared between the metastasis and bulk
    // sample...
    private final long sharedMutCount;

    // Integral mutational distance between the metastasis and primary
    // tumor...
    private final int intMutDistance;

    // Fractional mutational distance between the metastasis and
    // primary tumor...
    private final double fracMutDistance;

    private MetMutDistRecord(int    disseminationTime,
                             long   disseminationSize,
                             int    bulkSamplingTime,
                             long   primaryTumorSize,
                             long   metClonalMutCount,
                             long   bulkTotalMutCount,
                             long   bulkClonalMutCount,
                             long   sharedMutCount,
                             int    intMutDistance,
                             double fracMutDistance) {
        this.trialIndex = TumorDriver.global().getTrialIndex();

        this.disseminationTime  = disseminationTime;
        this.disseminationSize  = disseminationSize;
        this.bulkSamplingTime   = bulkSamplingTime;
        this.primaryTumorSize   = primaryTumorSize;
        this.metClonalMutCount  = metClonalMutCount;
        this.bulkTotalMutCount  = bulkTotalMutCount;
        this.bulkClonalMutCount = bulkClonalMutCount;
        this.sharedMutCount     = sharedMutCount;
        this.intMutDistance     = intMutDistance;
        this.fracMutDistance    = fracMutDistance;
    }

    /**
     * Computes the mutational distance record for a metastasis and
     * primary tumor sample.
     *
     * @param metSample the metastasis sample.
     *
     * @param bulkSample a bulk sample from the primary tumor.
     *
     * @return the mutation distance record for the two samples.
     */
    public static MetMutDistRecord compute(MetSample metSample, BulkSample bulkSample) {
        int  disseminationTime = metSample.getDisseminationTime();
        long disseminationSize = metSample.getTumorSize();

        int  bulkSamplingTime = bulkSample.getCollectionTime();
        long primaryTumorSize = bulkSample.getTumorSize();

        long metClonalMutCount  = metSample.getMutationSet().size();
        long bulkTotalMutCount  = bulkSample.getUniqueMutations().size();
        long bulkClonalMutCount = bulkSample.getSharedMutations().size();

        MutationalDistance mutDist =
            MutationalDistance.compute(metSample.getMutationSet(),
                                       bulkSample.getSharedMutations());

        long   sharedMutCount  = mutDist.countShared();
        int    intMutDistance  = mutDist.intDistance();
        double fracMutDistance = mutDist.fracDistance();

        return new MetMutDistRecord(disseminationTime,
                                    disseminationSize,
                                    bulkSamplingTime,
                                    primaryTumorSize,
                                    metClonalMutCount,
                                    bulkTotalMutCount,
                                    bulkClonalMutCount,
                                    sharedMutCount,
                                    intMutDistance,
                                    fracMutDistance);
    }

    @Override public String formatLine() {
        return String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%.8f",
                             trialIndex,
                             disseminationTime,
                             disseminationSize,
                             bulkSamplingTime,
                             primaryTumorSize,
                             metClonalMutCount,
                             bulkTotalMutCount,
                             bulkClonalMutCount,
                             sharedMutCount,
                             intMutDistance,
                             fracMutDistance);
    }

    @Override public String getBaseName() {
        return MetMutDistReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "trialIndex"
            + ",disseminationTime"
            + ",disseminationSize"
            + ",bulkSamplingTime"
            + ",primaryTumorSize"
            + ",metClonalMutCount"
            + ",bulkTotalMutCount"
            + ",bulkClonalMutCount"
            + ",sharedMutCount"
            + ",intMutDistance"
            + ",fracMutDistance";
    }
}
