
package tumor.report.metastasis;

import jam.lattice.Coord;
import jam.math.DoubleUtil;
import jam.report.ReportRecord;

import tumor.mutation.Genotype;
import tumor.report.bulk.BulkSample;

/**
 * Characterizes the mutational distance beween a metastasis seed and
 * a bulk sample from the primary tumor.
 */
public final class MetMutDistRecord implements ReportRecord {
    // The time of dissemination...
    private final int dissemTime;

    // The time when the last mutation arose in the metastasis seed...
    private final int metLastMutTime;

    // The size of the primary tumor at the time of dissemination...
    private final long dissemSize;

    // The time of bulk sampling in the primary tumor...
    private final int bulkSamplingTime;

    // The time when the last mutation arose in the common ancestor
    // from the bulk sample...
    private final int bulkLastMutTime;

    // The size of the primary tumor at the time of bulk sampling...
    private final long primaryTumorSize;

    // The physical distance between the site of dissemination and the
    // central surface site of the bulk sample (in lattice units)...
    private final double physicalDist;

    // Number of clonal mutations in the metastatis...
    private final int metClonalMutCount;

    // Total number of unique mutations in the primary bulk sample...
    private final int bulkTotalMutCount;

    // Number of clonal mutations in the primary bulk sample...
    private final int bulkClonalMutCount;

    // Number of mutations shared between the metastasis and bulk
    // sample...
    private final int sharedMutCount;

    // Fractional overlap between the metastasis and primary tumor:
    // sharedMutCount / min(metClonalMutCount, bulkClonalMutCount)
    private final double overlapFrac;

    private MetMutDistRecord(int    dissemTime,
                             int    metLastMutTime,
                             long   dissemSize,
                             int    bulkSamplingTime,
                             int    bulkLastMutTime,
                             long   primaryTumorSize,
                             double physicalDist,
                             int    metClonalMutCount,
                             int    bulkTotalMutCount,
                             int    bulkClonalMutCount,
                             int    sharedMutCount,
                             double overlapFrac) {
        this.dissemTime         = dissemTime;
        this.metLastMutTime     = metLastMutTime;
        this.dissemSize         = dissemSize;
        this.bulkSamplingTime   = bulkSamplingTime;
        this.bulkLastMutTime    = bulkLastMutTime;
        this.primaryTumorSize   = primaryTumorSize;
        this.physicalDist       = physicalDist;
        this.metClonalMutCount  = metClonalMutCount;
        this.bulkTotalMutCount  = bulkTotalMutCount;
        this.bulkClonalMutCount = bulkClonalMutCount;
        this.sharedMutCount     = sharedMutCount;
        this.overlapFrac        = overlapFrac;
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
        int  dissemTime     = metSample.getDisseminationTime();
        int  metLastMutTime = metSample.getGenotype().getLatestMutation().getOriginationTime();
        long dissemSize     = metSample.getTumorSize();

        int  bulkSamplingTime = bulkSample.getCollectionTime();
        int  bulkLastMutTime  = bulkSample.getAncestorGenotype().getLatestMutation().getOriginationTime();
        long primaryTumorSize = bulkSample.getTumorSize();

        double physicalDist =
            Math.sqrt(Coord.computeSquaredDistance(metSample.getSampleSite(),
                                                   bulkSample.getCenterSite()));

        int metClonalMutCount  = metSample.getGenotype().countAccumulatedMutations();
        int bulkTotalMutCount  = bulkSample.getAggregateGenotype().countAccumulatedMutations();
        int bulkClonalMutCount = bulkSample.getAncestorGenotype().countAccumulatedMutations();

        Genotype ancestor = Genotype.ancestor(metSample.getGenotype(), bulkSample.getAncestorGenotype());
        int sharedMutCount = ancestor.countAccumulatedMutations();

        double overlapFrac = DoubleUtil.ratio(sharedMutCount, Math.min(metClonalMutCount, bulkClonalMutCount));

        return new MetMutDistRecord(dissemTime,
                                    metLastMutTime,
                                    dissemSize,
                                    bulkSamplingTime,
                                    bulkLastMutTime,
                                    primaryTumorSize,
                                    physicalDist,
                                    metClonalMutCount,
                                    bulkTotalMutCount,
                                    bulkClonalMutCount,
                                    sharedMutCount,
                                    overlapFrac);
    }

    @Override public String formatLine() {
        return String.format("%d,%d,%d,%d,%d,%d,%.2f,%d,%d,%d,%d,%.4f",
                             dissemTime,
                             metLastMutTime,
                             dissemSize,
                             bulkSamplingTime,
                             bulkLastMutTime,
                             primaryTumorSize,
                             physicalDist,
                             metClonalMutCount,
                             bulkTotalMutCount,
                             bulkClonalMutCount,
                             sharedMutCount,
                             overlapFrac);
    }

    @Override public String getBaseName() {
        return MetMutDistReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "dissemTime"
            + ",metLastMutTime"
            + ",dissemSize"
            + ",bulkSamplingTime"
            + ",bulkLastMutTime"
            + ",primaryTumorSize"
            + ",physicalDist"
            + ",metClonalMutCount"
            + ",bulkTotalMutCount"
            + ",bulkClonalMutCount"
            + ",sharedMutCount"
            + ",overlapFrac";
    }
}
