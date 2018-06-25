
package tumor.report;

import java.io.File;
import java.io.PrintWriter;

import jam.io.IOUtil;
import jam.lattice.Coord;
import jam.math.LongRange;
import jam.sim.StepRecord;
import jam.util.RegexUtil;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;

/**
 * Records the number of components and cells in a tumor for a given
 * trial and time step.
 */
public final class ComponentCoordRecord extends StepRecord {
    private final long  compIndex;
    private final long  cellCount;
    private final Coord compCoord;

    private ComponentCoordRecord(int   trialIndex,
                                 int   timeStep,
                                 long  compIndex,
                                 long  cellCount,
                                 Coord compCoord) {
        super(trialIndex, timeStep);

        LongRange.NON_NEGATIVE.validate("Cell count", cellCount);
        LongRange.NON_NEGATIVE.validate("Component index", compIndex);

        this.compIndex = compIndex;
        this.cellCount = cellCount;
        this.compCoord = compCoord;
    }

    /**
     * Creates a new component coordinate record for a given tumor and
     * component.
     *
     * @param <E> the tumor component type.
     *
     * @param tumor the tumor where the component resides.
     *
     * @param component the component of interest.
     *
     * @return the component coordinate record for the given tumor and
     * component.
     */
    public static <E extends TumorComponent> ComponentCoordRecord create(Tumor<E> tumor, E component) {
        int trialIndex = TumorDriver.global().getTrialIndex();
        int timeStep   = TumorDriver.global().getTimeStep();

        long compIndex = component.getIndex();
        long cellCount = component.countCells();

        Coord compCoord = tumor.locateComponent(component);

        return new ComponentCoordRecord(trialIndex, timeStep, compIndex, cellCount, compCoord);
    }

    /**
     * Returns the header line for component coordinate files.
     *
     * @return the header line for component coordinate files.
     */
    public static String header() {
        return "trialIndex,timeStep,compIndex,cellCount,x,y,z";
    }

    /**
     * Creates a new record by parsing a line from a component
     * coordinate file.
     *
     * @param s the line to parse.
     *
     * @return the record defined by the input string.
     *
     * @throws IllegalArgumentException unless the input string
     * is a valid representation of a record.
     */
    public static ComponentCoordRecord parse(String s) {
        String[] fields = RegexUtil.COMMA.split(s);

        if (fields.length != 7)
            throw new IllegalArgumentException("Invalid record: [" + s + "].");

        int trialIndex = Integer.parseInt(fields[0]);
        int timeStep   = Integer.parseInt(fields[1]);

        long compIndex = Long.parseLong(fields[2]);
        long cellCount = Long.parseLong(fields[3]);

        int x = Integer.parseInt(fields[4]);
        int y = Integer.parseInt(fields[5]);
        int z = Integer.parseInt(fields[6]);

        return new ComponentCoordRecord(trialIndex, timeStep, compIndex, cellCount, Coord.at(x, y, z));
    }

    /**
     * Generates a component coordinate report.
     *
     * @param <E> the tumor component type.
     *
     * @param reportDir the directory where the report file will be written.
     *
     * @param baseName the base name of the report file that will be written.
     *
     * @param tumor the active tumor in the simulation.
     */
    public static <E extends TumorComponent> void write(File reportDir, String baseName, Tumor<E> tumor) {
        PrintWriter writer = IOUtil.openWriter(reportDir, baseName);
        writer.println(header());

        for (E component : tumor.sortComponents())
            writer.println(create(tumor, component).format());

        writer.close();
    }

    /**
     * Formats this record for writing to a component coordinate file.
     *
     * @return the canonical string representation for this record.
     */
    public String format() {
        return String.format("%d,%d,%d,%d,%d,%d,%d",
                             getTrialIndex(),
                             getTimeStep(),
                             getCompIndex(),
                             getCellCount(),
                             getCompCoord().x,
                             getCompCoord().y,
                             getCompCoord().z);
    }

    /**
     * Returns the index of the component described by this record.
     *
     * @return the index of the component described by this record.
     */
    public long getCompIndex() {
        return compIndex;
    }

    /**
     * Returns the number of cells in the tumor component.
     *
     * @return the number of cells in the tumor component.
     */
    public long getCellCount() {
        return cellCount;
    }

    /**
     * Returns the coordinate of the tumor component.
     *
     * @return the coordinate of the tumor component.
     */
    public Coord getCompCoord() {
        return compCoord;
    }
}
