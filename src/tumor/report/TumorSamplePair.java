
package tumor.report;

import jam.lang.ObjectPair;

/**
 * Contains an immutable pair of tumor samples.
 */
public final class TumorSamplePair extends ObjectPair<TumorSample, TumorSample> {
    private TumorSamplePair(TumorSample first, TumorSample second) {
        super(first, second);
    }

    /**
     * Returns an immutable pair of tumor samples.
     *
     * @param first the first sample.
     *
     * @param second the second sample.
     *
     * @return the paired samples.
     */
    public static TumorSamplePair of(TumorSample first, TumorSample second) {
        return new TumorSamplePair(first, second);
    }
}
