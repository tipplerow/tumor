
package tumor.report;

import java.util.ArrayList;
import java.util.List;

import jam.math.DoubleRange;
import jam.report.ReportRecord;

import tumor.mutation.MutationFrequency;

public final class MutFreqDetailRecord implements ReportRecord {
    private final long   mutationIndex;
    private final double rawFrequency;
    private final double normFrequency;

    /**
     * Base name for the mutation frequency detail report.
     */
    public static final String BASE_NAME = "mut-freq-detail.csv";

    /**
     * Creates a new mutation frequency detail record.
     *
     * @param mutationIndex the index of the mutation of interest.
     *
     * @param rawFrequency the fraction of tumor cells carrying the
     * mutation.
     *
     * @param normFrequency the ratio of the raw frequency to the
     * maximum raw frequency in the population.
     */
    public MutFreqDetailRecord(long mutationIndex, double rawFrequency, double normFrequency) {
        DoubleRange.FRACTIONAL.validate("Raw frequency", rawFrequency);
        DoubleRange.FRACTIONAL.validate("Normalized frequency", normFrequency);

        this.mutationIndex = mutationIndex;
        this.rawFrequency  = rawFrequency;
        this.normFrequency = normFrequency;
    }

    /**
     * Translates a list of mutation frequencies into a list of
     * mutation frequency detail records.
     *
     * @param freqList the mutation frequency list to translate.
     *
     * @return a list of correponding mutation freqeuency detail
     * records.
     */
    public static List<MutFreqDetailRecord> create(List<MutationFrequency> freqList) {
        double maxFrequency = freqList.get(0).getFrequency();
        List<MutFreqDetailRecord> recordList = new ArrayList<MutFreqDetailRecord>(freqList.size());

        for (MutationFrequency freq : freqList) {
            long   mutationIndex = freq.getMutation().getIndex();
            double rawFrequency  = freq.getFrequency();
            double normFrequency = rawFrequency / maxFrequency;

            recordList.add(new MutFreqDetailRecord(mutationIndex, rawFrequency, normFrequency));
        }

        return recordList;
    }

    /**
     * Returns the index of the mutation described by this record.
     *
     * @return the index of the mutation described by this record.
     */
    public long getMutationIndex() {
        return mutationIndex;
    }

    /**
     * Returns the fraction of tumor cells carrying the mutation.
     *
     * @return the fraction of tumor cells carrying the mutation.
     */
    public double getRawFrequency() {
        return rawFrequency;
    }

    /**
     * Returns the ratio of the raw frequency to the maximum raw
     * frequency in the population.
     *
     * @return the ratio of the raw frequency to the maximum raw
     * frequency in the population.
     */
    public double getNormFrequency() {
        return normFrequency;
    }

    @Override public String formatLine() {
        return String.format("%d,%.4g,%.4g",
                             mutationIndex,
                             rawFrequency,
                             normFrequency);
    }

    @Override public String getBaseName() {
        return BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "mutationIndex,rawFrequency,normFrequency";
    }
}
