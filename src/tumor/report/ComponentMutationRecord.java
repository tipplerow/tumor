
package tumor.report;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;

import jam.io.IOUtil;
import jam.sim.StepRecord;
import jam.util.LongListUtil;
import jam.util.RegexUtil;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.mutation.Mutation;

/**
 * Records the original or accumulated mutations for a tumor component.
 */
public final class ComponentMutationRecord extends StepRecord {
    private final long component;
    private final LongList mutations;

    private ComponentMutationRecord(int trialIndex, int timeStep, long component, LongList mutations) {
        super(trialIndex, timeStep);
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

    private static ComponentMutationRecord create(TumorComponent component, List<Mutation> mutationList) {
        int trialIndex = TumorDriver.global().getTrialIndex();
        int timeStep   = TumorDriver.global().getTimeStep();
        
        long componentIndex = component.getIndex();
        LongList mutationIndexes = new LongArrayList(mutationList.size());

        for (Mutation mutation : mutationList)
            mutationIndexes.add(mutation.getIndex());

        return new ComponentMutationRecord(trialIndex, timeStep, componentIndex, mutationIndexes);
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

        if (fields.length != 4)
            throw new IllegalArgumentException("Invalid record: [" + s + "].");

        int trialIndex = Integer.parseInt(fields[0].trim());
        int timeStep   = Integer.parseInt(fields[1].trim());
        
        long componentIndex = Long.parseLong(fields[2].trim());
        LongList mutationIndexes = LongListUtil.parse(fields[3], RegexUtil.COMMA);

        return new ComponentMutationRecord(trialIndex, timeStep, componentIndex, mutationIndexes);
    }

    /**
     * Generates an accumulated mutation report.
     *
     * @param <E> the tumor component type.
     *
     * @param reportDir the directory where the report file will be written.
     *
     * @param baseName the base name of the report file that will be written.
     *
     * @param tumor the active tumor in the simulation.
     */
    public static <E extends TumorComponent> void writeAccumulated(File reportDir, String baseName, Tumor<E> tumor) {
        PrintWriter writer = IOUtil.openWriter(reportDir, baseName);

        for (E component : tumor.sortComponents())
            writer.println(accumulated(component).format());

        writer.close();
    }

    /**
     * Generates an original mutation report.
     *
     * @param <E> the tumor component type.
     *
     * @param reportDir the directory where the report file will be written.
     *
     * @param baseName the base name of the report file that will be written.
     *
     * @param tumor the active tumor in the simulation.
     */
    public static <E extends TumorComponent> void writeOriginal(File reportDir, String baseName, Tumor<E> tumor) {
        PrintWriter writer = IOUtil.openWriter(reportDir, baseName);

        for (E component : tumor.sortComponents())
            writer.println(original(component).format());

        writer.close();
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
        builder.append(getTimeStep());
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
