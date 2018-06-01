
package tumor.report;

import jam.math.LongRange;
import jam.sim.StepRecord;
import jam.util.RegexUtil;

import tumor.driver.TumorDriver;

/**
 * Records the number of components and cells in a tumor for a given
 * trial and time step.
 */
public final class ComponentCountRecord extends StepRecord {
    private final long cellCount;
    private final long componentCount;

    private ComponentCountRecord(int trialIndex,
                                 int timeStep,
                                 long cellCount,
                                 long componentCount) {
        super(trialIndex, timeStep);

        LongRange.NON_NEGATIVE.validate("Cell count", cellCount);
        LongRange.NON_NEGATIVE.validate("Component count", componentCount);

        if (componentCount > cellCount)
            throw new IllegalArgumentException("Invalid cell/component counts.");

        this.cellCount = cellCount;
        this.componentCount = componentCount;
    }

    /**
     * Returns the header line for component count files.
     *
     * @return the header line for component count files.
     */
    public static String header() {
        return "trialIndex,timeStep,cellCount,componentCount";
    }

    /**
     * Creates a new record by parsing a line from a component count
     * file.
     *
     * @param s the line to parse.
     *
     * @return the record defined by the input string.
     *
     * @throws IllegalArgumentException unless the input string is a
     * valid representation of a record.
     */
    public static ComponentCountRecord parse(String s) {
        String[] fields = RegexUtil.COMMA.split(s);

        if (fields.length != 4)
            throw new IllegalArgumentException("Invalid record: [" + s + "].");

        int trialIndex = Integer.parseInt(fields[0]);
        int timeStep   = Integer.parseInt(fields[1]);

        long cellCount      = Long.parseLong(fields[2]);
        long componentCount = Long.parseLong(fields[3]);

        return new ComponentCountRecord(trialIndex, timeStep, cellCount, componentCount);
    }

    /**
     * Records the current state of the simulation.
     *
     * @return a new record reflecting the current state of the simulation.
     */
    public static ComponentCountRecord snap() {
        TumorDriver driver = TumorDriver.global();

        int trialIndex = driver.getTrialIndex();
        int timeStep   = driver.getTimeStep();

        long cellCount      = driver.getTumor().countCells();
        long componentCount = driver.getTumor().countComponents();

        return new ComponentCountRecord(trialIndex, timeStep, cellCount, componentCount);
    }

    /**
     * Formats this record for writing to a component count file.
     *
     * @return the canonical string representation for this record.
     */
    public String format() {
        return String.format("%d,%d,%d,%d",
                             getTrialIndex(),
                             getTimeStep(),
                             getCellCount(),
                             getComponentCount());
    }

    /**
     * Returns the number of cells for this record.
     *
     * @return the number of cells for this record.
     */
    public long getCellCount() {
        return cellCount;
    }

    /**
     * Returns the number of components for this record.
     *
     * @return the number of components for this record.
     */
    public long getComponentCount() {
        return componentCount;
    }
}
