
package tumor.report.mutation;

import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.mutation.Mutation;
import tumor.report.TumorRecord;

/**
 * Records the total number of mutations generated in a simulation
 * trial.
 */
public final class MutationCountRecord extends TumorRecord implements ReportRecord {
    private final long mutationCount;

    private MutationCountRecord() {
        this.mutationCount = Mutation.count();
    }

    /**
     * Generates a record with the latest total mutation count for the
     * current simulation trial.
     *
     * @return a record containing the latest total mutation count for
     * the current simulation trial.
     */
    public static MutationCountRecord snap() {
        return new MutationCountRecord();
    }

    public long getMutationCount() {
        return mutationCount;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(getMutationCount());

        return builder.toString();
    }

    @Override public String getBaseName() {
        return MutationCountReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("timeStep");
        builder.append("mutationCount");

        return builder.toString();
    }
}
