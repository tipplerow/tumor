
package tumor.report;

import java.io.File;
import java.io.PrintWriter;

import jam.io.IOUtil;
import jam.math.LongRange;
import jam.sim.StepRecord;
import jam.util.RegexUtil;

import tumor.carrier.Tumor;
import tumor.driver.TumorDriver;

/**
 * Records the number of components and cells in a tumor for a given
 * trial and time step.
 */
public final class ComponentCountRecord extends StepRecord {
    private final long cellCount;
    private final long activeCount;
    private final long senescentCount;

    private ComponentCountRecord(int trialIndex,
                                 int timeStep,
                                 long cellCount,
                                 long activeCount,
                                 long senescentCount) {
        super(trialIndex, timeStep);

        LongRange.NON_NEGATIVE.validate("Cell count", cellCount);
        LongRange.NON_NEGATIVE.validate("Active count", activeCount);
        LongRange.NON_NEGATIVE.validate("Senescent count", senescentCount);

        this.cellCount = cellCount;
        this.activeCount = activeCount;
        this.senescentCount = senescentCount;
    }

    /**
     * Returns the header line for component count files.
     *
     * @return the header line for component count files.
     */
    public static String header() {
        return "trialIndex,timeStep,cellCount,activeCount,senescentCount";
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

        if (fields.length != 5)
            throw new IllegalArgumentException("Invalid record: [" + s + "].");

        int trialIndex = Integer.parseInt(fields[0]);
        int timeStep   = Integer.parseInt(fields[1]);

        long cellCount      = Long.parseLong(fields[2]);
        long activeCount    = Long.parseLong(fields[3]);
        long senescentCount = Long.parseLong(fields[4]);

        return new ComponentCountRecord(trialIndex, timeStep, cellCount, activeCount, senescentCount);
    }

    /**
     * Records the current component count.
     *
     * @param tumor the tumor being simulated.
     *
     * @return a new record containing the current component count.
     */
    public static ComponentCountRecord snap(Tumor tumor) {
        TumorDriver driver = TumorDriver.global();

        int trialIndex = driver.getTrialIndex();
        int timeStep   = driver.getTimeStep();

        long cellCount      = tumor.countCells();
        long activeCount    = tumor.countActive();
        long senescentCount = tumor.countSenescent();

        return new ComponentCountRecord(trialIndex, timeStep, cellCount, activeCount, senescentCount);
    }

    /**
     * Writes the current component count to a file.
     *
     * @param reportDir the directory where the report file will be written.
     *
     * @param baseName the base name of the report file that will be written.
     *
     * @param tumor the tumor being simulated.
     */
    public static void write(File reportDir, String baseName, Tumor tumor) {
        PrintWriter writer = IOUtil.openWriter(reportDir, baseName);

        writer.println(header());
        writer.println(snap(tumor).format());
        writer.close();
    }

    /**
     * Formats this record for writing to a component count file.
     *
     * @return the canonical string representation for this record.
     */
    public String format() {
        return String.format("%d,%d,%d,%d,%d",
                             getTrialIndex(),
                             getTimeStep(),
                             getCellCount(),
                             getActiveCount(),
                             getSenescentCount());
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
     * Returns the number of active components for this record.
     *
     * @return the number of active components for this record.
     */
    public long getActiveCount() {
        return activeCount;
    }

    /**
     * Returns the number of senescent components for this record.
     *
     * @return the number of senescent components for this record.
     */
    public long getSenescentCount() {
        return senescentCount;
    }

    /**
     * Returns the total number of components for this record.
     *
     * @return the total number of components for this record.
     */
    public long getComponentCount() {
        return activeCount + senescentCount;
    }
}
