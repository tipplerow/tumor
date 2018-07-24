
package tumor.report.component;

import java.util.Map;

import jam.report.LineBuilder;
import jam.report.ReportRecord;
import jam.sim.StepRecordCache;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.report.TumorRecord;

/**
 * Records the number of components and the total number of cells in
 * the active tumor.
 */
public final class ComponentCountRecord extends TumorRecord implements ReportRecord {
    private final long cellCount;
    private final long compCount;

    private static final StepRecordCache<ComponentCountRecord> cache = StepRecordCache.create();

    private ComponentCountRecord(long cellCount, long compCount) {
        this.cellCount = cellCount;
        this.compCount = compCount;
    }

    /**
     * Collects the component count record for the active tumor at
     * this instant in the simulation.
     *
     * @return the component count record for the active tumor at this
     * instant in the simulation.
     */
    public static ComponentCountRecord snap() {
        Tumor<?> tumor = TumorDriver.global().getTumor();

        ComponentCountRecord record =
            new ComponentCountRecord(tumor.countCells(), tumor.countComponents());

        cache.add(record);
        return record;
    }

    /**
     * Returns the total number of cells in the active tumor.
     *
     * @return the total number of cells in the active tumor.
     */
    public long getCellCount() {
        return cellCount;
    }

    /**
     * Returns the number of unique components in the active tumor.
     *
     * @return the number of unique components in the active tumor.
     */
    public long getComponentCount() {
        return compCount;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(cellCount);
        builder.append(compCount);

        return builder.toString();
    }

    @Override public String getBaseName() {
        return ComponentCountReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("timeStep");
        builder.append("cellCount");
        builder.append("componentCount");

        return builder.toString();
    }
}
