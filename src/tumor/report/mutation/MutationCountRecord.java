
package tumor.report.mutation;

import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.driver.TumorDriver;
import tumor.mutation.Mutation;
import tumor.report.TumorRecord;

/**
 * Records the total number of mutations generated in a simulation
 * trial.
 */
public final class MutationCountRecord extends TumorRecord implements ReportRecord {
    private final long cellCount;
    private final long componentCount;
    private final long mutationCount;

    private MutationCountRecord() {
        this.cellCount      = TumorDriver.global().getTumor().countCells();
        this.componentCount = TumorDriver.global().getTumor().countComponents();
        this.mutationCount  = Mutation.count();
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

    public long getCellCount() {
        return cellCount;
    }

    public long getComponentCount() {
        return componentCount;
    }

    public long getMutationCount() {
        return mutationCount;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(getCellCount());
        builder.append(getComponentCount());
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
        builder.append("cellCount");
        builder.append("componentCount");
        builder.append("mutationCount");

        return builder.toString();
    }
}
