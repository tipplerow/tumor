
package tumor.report.growth;

import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.report.TumorRecord;

/**
 * Records the total number of birth and death events that have
 * occurred in a simulation trial.
 */
public final class GrowthCountRecord extends TumorRecord implements ReportRecord {
    private final long cellCount;
    private final long birthCount;
    private final long deathCount;

    private GrowthCountRecord(long cellCount, long birthCount, long deathCount) {
        this.cellCount  = cellCount;
        this.birthCount = birthCount;
        this.deathCount = deathCount;
    }

    /**
     * Collects the growth count record for the active tumor at this
     * instant in the simulation.
     *
     * @return the growth count record describing the active tumor at
     * this instant in the simulation.
     */
    public static GrowthCountRecord snap() {
        return new GrowthCountRecord(TumorDriver.global().getTumor().countCells(),
                                     TumorComponent.getTotalBirthCount(),
                                     TumorComponent.getTotalDeathCount());
    }

    /**
     * Returns the total number of birth events.
     *
     * @return the total number of birth events.
     */
    public long getCellCount() {
        return cellCount;
    }

    /**
     * Returns the total number of birth events.
     *
     * @return the total number of birth events.
     */
    public long getBirthCount() {
        return birthCount;
    }

    /**
     * Returns the total number of death events.
     *
     * @return the total number of death events.
     */
    public long getDeathCount() {
        return deathCount;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(getCellCount());
        builder.append(getBirthCount());
        builder.append(getDeathCount());

        return builder.toString();
    }

    @Override public String getBaseName() {
        return GrowthCountReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("timeStep");
        builder.append("cellCount");
        builder.append("birthCount");
        builder.append("deathCount");

        return builder.toString();
    }
}
