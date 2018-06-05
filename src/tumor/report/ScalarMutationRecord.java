
package tumor.report;

import jam.sim.TrialRecord;
import jam.util.RegexUtil;

import tumor.driver.TumorDriver;
import tumor.mutation.Mutation;
import tumor.mutation.ScalarMutation;

/**
 * Records the index and selection coefficient for a scalar mutation.
 */
public final class ScalarMutationRecord extends TrialRecord {
    private final long   mutationIndex;
    private final double selectionCoeff;

    private ScalarMutationRecord(int trialIndex, long mutationIndex, double selectionCoeff) {
        super(trialIndex);
        
        this.mutationIndex  = mutationIndex;
        this.selectionCoeff = selectionCoeff;
    }

    /**
     * Creates a new record for a given mutation.
     *
     * @param mutation the mutation to record.
     *
     * @return a new record for the given mutation.
     *
     * @throws RuntimeException unless the mutation is a scalar
     * mutation (an instance of the {@code ScalarMutation} class)
     */
    public static ScalarMutationRecord create(Mutation mutation) {
        ScalarMutation scalar = (ScalarMutation) mutation;

        return new ScalarMutationRecord(TumorDriver.global().getTrialIndex(),
                                        scalar.getIndex(), scalar.getSelectionCoeff());
    }

    /**
     * Returns the header line for scalar mutation files.
     *
     * @return the header line for scalar mutation files.
     */
    public static String header() {
        return "trialIndex,mutationIndex,selectionCoeff";
    }

    /**
     * Creates a new record by parsing a line from a scalar
     * mutation file.
     *
     * @param line the line to parse.
     *
     * @return the record defined by the input string.
     *
     * @throws IllegalArgumentException unless the input string is a
     * valid representation of a record.
     */
    public static ScalarMutationRecord parse(String line) {
        String[] fields = RegexUtil.COMMA.split(line);

        if (fields.length != 3)
            throw new IllegalArgumentException("Invalid record: [" + line + "].");

        int    trialIndex     = Integer.parseInt(fields[0].trim());
        long   mutationIndex  = Long.parseLong(fields[1].trim());
        double selectionCoeff = Double.parseDouble(fields[2].trim());

        return new ScalarMutationRecord(trialIndex, mutationIndex, selectionCoeff);
    }

    /**
     * Formats this record for writing to a scalar mutation file.
     *
     * @return the canonical string representation for this record.
     */
    public String format() {
        StringBuilder builder = new StringBuilder();

        builder.append(getTrialIndex());
        builder.append(",");
        builder.append(getMutationIndex());
        builder.append(",");
        builder.append(getSelectionCoeff());

        return builder.toString();
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
     * Returns the selection coefficient for the mutation described by
     * this record.
     *
     * @return the selection coefficient for the mutation described by
     * this record.
     */
    public double getSelectionCoeff() {
        return selectionCoeff;
    }
}
