
package tumor.report;

import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;

import jam.sim.TrialRecord;
import jam.util.LongListUtil;
import jam.util.RegexUtil;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.mutation.Mutation;
import tumor.mutation.MutationList;

/**
 * Records the original or accumulated mutations for a tumor component.
 */
public final class ComponentMutationRecord extends TrialRecord {
    private final long component;
    private final LongList mutations;

    private ComponentMutationRecord(int trialIndex, long component, LongList mutations) {
        super(trialIndex);
        this.component = component;
        this.mutations = LongLists.unmodifiable(mutations);
    }

    /**
     * Records all accumulated mutations for a tumor component.
     *
     * @param component the tumor component to examine.
     *
     * @return the record of all accumulated mutations for the given
     * tumor component.
     */
    public static ComponentMutationRecord accumulated(TumorComponent component) {
        return ComponentMutationRecord.create(component, component.getAccumulatedMutations());
    }

    /**
     * Records only the original mutations for a tumor component.
     *
     * @param component the tumor component to examine.
     *
     * @return the record of original mutations for the given tumor
     * component.
     */
    public static ComponentMutationRecord original(TumorComponent component) {
        return ComponentMutationRecord.create(component, component.getOriginalMutations());
    }

    private static ComponentMutationRecord create(TumorComponent component, MutationList mutationList) {
        int trialIndex = TumorDriver.global().getTrialIndex();
        long componentIndex = component.getIndex();
        LongList mutationIndexes = mutationList.indexList();

        return new ComponentMutationRecord(trialIndex, componentIndex, mutationIndexes);
    }

    /**
     * Creates a new record by parsing a line from a component
     * mutation file.
     *
     * @param s the line to parse.
     *
     * @return the record defined by the input string.
     *
     * @throws IllegalArgumentException unless the input string is a
     * valid representation of a record.
     */
    public static ComponentMutationRecord parse(String s) {
        String[] fields = RegexUtil.SEMICOLON.split(s);

        if (fields.length != 3)
            throw new IllegalArgumentException("Invalid record: [" + s + "].");

        int  trialIndex = Integer.parseInt(fields[0].trim());
        long componentIndex = Long.parseLong(fields[1].trim());
        LongList mutationIndexes = LongListUtil.parse(fields[2], RegexUtil.COMMA);

        return new ComponentMutationRecord(trialIndex, componentIndex, mutationIndexes);
    }

    /**
     * Formats this record for writing to a component mutation file.
     *
     * @return the canonical string representation for this record.
     */
    public String format() {
        StringBuilder builder = new StringBuilder();

        builder.append(getTrialIndex());
        builder.append(";");
        builder.append(component);
        builder.append(";");
        builder.append(LongListUtil.format(mutations, ","));

        return builder.toString();
    }

    /**
     * Returns the index of the component described by this record.
     *
     * @return the index of the component described by this record.
     */
    public long getComponentIndex() {
        return component;
    }

    /**
     * Returns a read-only view of the mutation indexes in this record.
     *
     * @return a read-only view of the mutation indexes in this record.
     */
    public LongList getMutationIndexes() {
        return mutations;
    }
}
