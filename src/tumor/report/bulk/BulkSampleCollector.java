
package tumor.report.bulk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.report.TumorSample;

/**
 * Collects bulk samples from the active primary tumor and maintains
 * them in a cache for later analysis.
 */
public final class BulkSampleCollector {
    private static final Map<SampleListKey, List<TumorSample>> sampleMap =
        new HashMap<SampleListKey, List<TumorSample>>();

    private BulkSampleCollector() {
    }

    private static final class SampleListKey {
        private final int trialIndex;
        private final int timeStep;
        private final int hashCode;
        private final long targetSize;
        private final BulkSampleSpace sampleSpace;

        private SampleListKey(int trialIndex, int timeStep, BulkSampleSpace sampleSpace, long targetSize) {
            this.trialIndex  = trialIndex;
            this.timeStep    = timeStep;
            this.sampleSpace = sampleSpace;
            this.targetSize  = targetSize;
            this.hashCode    = Objects.hash(trialIndex, timeStep, sampleSpace, targetSize);
        }

        @Override public boolean equals(Object obj) {
            return (obj instanceof SampleListKey) && equalsKey((SampleListKey) obj);
        }

        private boolean equalsKey(SampleListKey that) {
            return this.trialIndex  == that.trialIndex
                && this.timeStep    == that.timeStep
                && this.targetSize  == that.targetSize
                && this.sampleSpace.equals(that.sampleSpace);
        }

        @Override public int hashCode() {
            return hashCode;
        }
    }

    /**
     * Collects bulk samples from the active primary tumor.
     *
     * @param sampleSpace the spatial distribution of the samples.
     *
     * @param targetSize the minumum number of tumor cells to include
     * in each bulk sample.
     *
     * @return an unmodifiable list containing the bulk samples.
     */
    public static List<TumorSample> collect(BulkSampleSpace sampleSpace, long targetSize) {
        int trialIndex = TumorDriver.global().getTrialIndex();
        int timeStep   = TumorDriver.global().getTimeStep();

        List<TumorSample> sampleList = getSampleList(trialIndex, timeStep, sampleSpace, targetSize);
        sampleList.addAll(sampleSpace.collect(targetSize));

        return Collections.unmodifiableList(sampleList);
    }

    private static List<TumorSample> getSampleList(int trialIndex, int timeStep, BulkSampleSpace sampleSpace, long targetSize) {
        SampleListKey    key  = new SampleListKey(trialIndex, timeStep, sampleSpace, targetSize);
        List<TumorSample> list = sampleMap.get(key);

        if (list == null) {
            list = new ArrayList<TumorSample>(sampleSpace.viewBasis().size());
            sampleMap.put(key, list);
        }

        return list;
    }

    /**
     * Retrieves previously collected bulk samples.
     *
     * @param trialIndex the index of the simulation trial at the time
     * of collection.
     *
     * @param timeStep the time step at the time of collection.
     *
     * @param sampleSpace the spatial distribution of the collected
     * samples.
     *
     * @param targetSize the minumum number of tumor cells included in
     * each bulk sample.
     *
     * @return an unmodifiable list containing the bulk samples.
     *
     * @throws IllegalStateException if the samples were not previously collected.
     */
    public static List<TumorSample> require(int trialIndex, int timeStep, BulkSampleSpace sampleSpace, long targetSize) {
        List<TumorSample> sampleList = getSampleList(trialIndex, timeStep, sampleSpace, targetSize);

        if (sampleList.isEmpty())
            throw new IllegalStateException(String.format("Sample [%d:%d:%s:%d] was not collected.",
                                                          trialIndex, timeStep, sampleSpace, targetSize));

        return Collections.unmodifiableList(sampleList);
    }
}
