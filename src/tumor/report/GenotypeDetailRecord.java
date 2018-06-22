
package tumor.report;

import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;

import jam.sim.StepRecord;
import jam.util.LongListUtil;
import jam.util.RegexUtil;

import tumor.driver.TumorDriver;
import tumor.mutation.Genotype;
import tumor.mutation.Mutation;

/**
 * Records the inherited and original mutations for each unique
 * genotype in a simulation.
 */
public final class GenotypeDetailRecord extends StepRecord {
    private final long     genoIndex;
    private final LongList inherited;
    private final LongList original;

    private GenotypeDetailRecord(int      trialIndex,
                                 int      timeStep,
                                 long     genoIndex,
                                 LongList inherited,
                                 LongList original) {
        super(trialIndex, timeStep);

        this.genoIndex = genoIndex;
        this.inherited = inherited;
        this.original  = original;
    }

    /**
     * Creates a new genotype detail record.
     *
     * @param genotype the genotype of interest.
     *
     * @return a new detail record for the given genotype.
     */
    public static GenotypeDetailRecord create(Genotype genotype) {
        int trialIndex = TumorDriver.global().getTrialIndex();
        int timeStep   = TumorDriver.global().getTimeStep();

        long genoIndex = genotype.getIndex();

        LongList inherited = Mutation.indexList(genotype.viewInheritedMutations());
        LongList original  = Mutation.indexList(genotype.viewOriginalMutations());

        return new GenotypeDetailRecord(trialIndex, timeStep, genoIndex, inherited, original);
    }

    /**
     * Creates a new record by parsing a line from a genotype detail
     * file.
     *
     * @param s the line to parse.
     *
     * @return the record defined by the input string.
     *
     * @throws IllegalArgumentException unless the input string is a
     * valid representation of a record.
     */
    public static GenotypeDetailRecord parse(String s) {
        String[] fields = RegexUtil.SEMICOLON.split(s);

        if (fields.length != 5)
            throw new IllegalArgumentException("Invalid record: [" + s + "].");

        int trialIndex = Integer.parseInt(fields[0]);
        int timeStep   = Integer.parseInt(fields[1]);

        long genoIndex = Long.parseLong(fields[2]);

        LongList inherited = LongListUtil.parse(fields[3], RegexUtil.COMMA);
        LongList original  = LongListUtil.parse(fields[4], RegexUtil.COMMA);

        return new GenotypeDetailRecord(trialIndex, timeStep, genoIndex, inherited, original);
    }

    /**
     * Formats this record for writing to a report file.
     *
     * @return the canonical string representation for this record.
     */
    public String format() {
        StringBuilder builder = new StringBuilder();

        builder.append(getTrialIndex());
        builder.append(";");
        builder.append(getTimeStep());
        builder.append(";");
        builder.append(genoIndex);
        builder.append(";");
        builder.append(LongListUtil.format(inherited, ","));
        builder.append(";");
        builder.append(LongListUtil.format(original, ","));

        return builder.toString();
    }

    /**
     * Returns the index of the genotype described by this record.
     *
     * @return the index of the genotype described by this record.
     */
    public long getGenotypeIndex() {
        return genoIndex;
    }

    /**
     * Returns a read-only view of the indexes of the inherited
     * mutations in this record.
     *
     * @return a read-only view of the indexes of the inherited
     * mutations in this record.
     */
    public LongList getInheritedIndexes() {
        return LongLists.unmodifiable(inherited);
    }

    /**
     * Returns a read-only view of the indexes of the original
     * mutations in this record.
     *
     * @return a read-only view of the indexes of the original
     * mutations in this record.
     */
    public LongList getOriginalIndexes() {
        return LongLists.unmodifiable(original);
    }
}
