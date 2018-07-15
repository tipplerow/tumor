
package tumor.report.bulk;

import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.mutation.MutationalDistance;
import tumor.report.TumorSample;

/**
 * Characterizes the mutational distance beween two bulk tumor
 * samples.
 */
public final class BulkMutDistRecord implements ReportRecord {
    private final TumorSample sample1;
    private final TumorSample sample2;
    private final MutationalDistance mrcaDist;

    private BulkMutDistRecord(TumorSample sample1, TumorSample sample2, MutationalDistance mrcaDist) {
        TumorSample.assertCommonTrial(sample1, sample2);

        this.sample1  = sample1;
        this.sample2  = sample2;
        this.mrcaDist = mrcaDist;
    }

    /**
     * Computes the mutational distance record for two bulk tumor
     * samples.
     *
     * @param sample1 the first bulk sample.
     *
     * @param sample2 the second bulk sample.
     *
     * @return the mutation distance record for the two samples.
     */
    public static BulkMutDistRecord compute(TumorSample sample1, TumorSample sample2) {
        MutationalDistance mrcaDist =
            MutationalDistance.compute(sample1.getVAF().viewClonalMutations(),
                                       sample2.getVAF().viewClonalMutations());

        return new BulkMutDistRecord(sample1, sample2, mrcaDist);
    }

    public int getTrialIndex() {
        return sample1.getTrialIndex();
    }

    public long getBulkIndex1() {
        return sample1.getIndex();
    }

    public long getBulkIndex2() {
        return sample2.getIndex();
    }

    public int getCollectTime1() {
        return sample1.getCollectionTime();
    }

    public long getCollectTime2() {
        return sample2.getCollectionTime();
    }

    public long getTumorSize1() {
        return sample1.getTumorSize();
    }

    public long getTumorSize2() {
        return sample2.getTumorSize();
    }

    public double getRadialAlignment() {
        return TumorSample.computeRadialAlignment(sample1, sample2);
    }

    public long getDistinctMutCount1() {
        return sample1.getVAF().countDistinctMutations();
    }
        
    public long getDistinctMutCount2() {
        return sample2.getVAF().countDistinctMutations();
    }
        
    public long getClonalMutCount1() {
        return sample1.getVAF().countClonalMutations();
    }

    public long getClonalMutCount2() {
        return sample2.getVAF().countClonalMutations();
    }

    public long getMRCASharedMutCount() {
        return mrcaDist.countShared();
    }

    public int getMRCAIntMutDistance() {
        return mrcaDist.intDistance();
    }

    public double getMRCAFracMutDistance() {
        return mrcaDist.fracDistance();
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getBulkIndex1());
        builder.append(getBulkIndex2());
        builder.append(getCollectTime1());
        builder.append(getCollectTime2());
        builder.append(getTumorSize1());
        builder.append(getTumorSize2());
        builder.append(getRadialAlignment());
        builder.append(getDistinctMutCount1());
        builder.append(getDistinctMutCount2());
        builder.append(getClonalMutCount1());
        builder.append(getClonalMutCount2());
        builder.append(getMRCASharedMutCount());
        builder.append(getMRCAIntMutDistance());
        builder.append(getMRCAFracMutDistance());

        return builder.toString();
    }

    @Override public String getBaseName() {
        return BulkMutDistReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("bulkIndex1");
        builder.append("bulkIndex2");
        builder.append("collectTime1");
        builder.append("collectTime2");
        builder.append("tumorSize1");
        builder.append("tumorSize2");
        builder.append("radialAlignment");
        builder.append("distinctMutCount1");
        builder.append("distinctMutCount2");
        builder.append("clonalMutCount1");
        builder.append("clonalMutCount2");
        builder.append("mrcaSharedMutCount");
        builder.append("mrcaIntMutDistance");
        builder.append("mrcaFracMutDistance");

        return builder.toString();
    }
}
