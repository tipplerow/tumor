
package tumor.report;

import jam.bio.AncestryRecord;
import jam.sim.TrialRecord;
import jam.util.RegexUtil;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;

/**
 * Encapsulates tumor component ancestry for a fixed simulation trial.
 */
public final class ComponentAncestryRecord extends TrialRecord {
    private final AncestryRecord bioRecord;

    private ComponentAncestryRecord(int trialIndex, AncestryRecord bioRecord) {
        super(trialIndex);
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
        return new ComponentAncestryRecord(TumorDriver.global().getTrialIndex(), AncestryRecord.create(component));
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

        if (fields.length != 2)
            throw new IllegalArgumentException("Invalid record: [" + s + "].");

        return new ComponentAncestryRecord(Integer.parseInt(fields[0]), AncestryRecord.parse(fields[1]));
    }

    /**
     * Formats this record for writing to a component ancestry file.
     *
     * @return the canonical string representation for this record.
     */
    public String format() {
        return getTrialIndex() + ";" + getBioRecord().format();
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
