
package tumor.report;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;

import jam.bio.AncestryRecord;
import jam.io.IOUtil;
import jam.sim.StepRecord;
import jam.util.RegexUtil;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;

/**
 * Encapsulates tumor component ancestry for a fixed simulation trial.
 */
public final class ComponentAncestryRecord extends StepRecord {
    private final AncestryRecord bioRecord;

    private ComponentAncestryRecord(int trialIndex, int timeStep, AncestryRecord bioRecord) {
        super(trialIndex, timeStep);
        this.bioRecord = bioRecord;
    }

    /**
     * Creates a new ancestry record for a given tumor component.
     *
     * @param component the tumor component to examine.
     *
     * @return the ancestry record for the given tumor component.
     */
    public static ComponentAncestryRecord create(TumorComponent component) {
        return new ComponentAncestryRecord(TumorDriver.global().getTrialIndex(),
                                           TumorDriver.global().getTimeStep(),
                                           AncestryRecord.create(component));
    }

    /**
     * Creates a new record by parsing a line from a component
     * ancestry file.
     *
     * @param s the line to parse.
     *
     * @return the record defined by the input string.
     *
     * @throws IllegalArgumentException unless the input string is a
     * valid representation of a record.
     */
    public static ComponentAncestryRecord parse(String s) {
        String[] fields = RegexUtil.SEMICOLON.split(s);

        if (fields.length != 3)
            throw new IllegalArgumentException("Invalid record: [" + s + "].");

        return new ComponentAncestryRecord(Integer.parseInt(fields[0]),
                                           Integer.parseInt(fields[1]),
                                           AncestryRecord.parse(fields[2]));
    }

    /**
     * Generates a component ancestry report.
     *
     * @param reportDir the directory where the report file will be written.
     *
     * @param baseName the base name of the report file that will be written.
     *
     * @param components the tumor components to report.
     */
    public static void write(File reportDir, String baseName, Collection<? extends TumorComponent> components) {
        PrintWriter writer = IOUtil.openWriter(reportDir, baseName);

        for (TumorComponent component : components)
            writer.println(create(component).format());

        writer.close();
    }

    /**
     * Formats this record for writing to a component ancestry file.
     *
     * @return the canonical string representation for this record.
     */
    public String format() {
        return getTrialIndex() + ";" + getTimeStep() + ";" + getBioRecord().format();
    }

    /**
     * Returns the {@code jam.bio} package ancestry record.
     *
     * @return the {@code jam.bio} package ancestry record.
     */
    public AncestryRecord getBioRecord() {
        return bioRecord;
    }
}
