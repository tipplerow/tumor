
package tumor.report;

import jam.lattice.Coord;
import jam.report.ReportRecord;

import tumor.mutation.Mutation;

public final class MutationDetailRecord implements ReportRecord {
    private final long mutationIndex;
    private final int  trialIndex;
    private final int  originTime;
    private final int  originX;
    private final int  originY;
    private final int  originZ;

    /**
     * Base name for the mutation detail report.
     */
    public static final String BASE_NAME = "mutation-detail.csv";

    /**
     * Creates a new mutation detail record.
     *
     * @param mutation the mutation of interest.
     *
     * @param trialIndex the index of the trial when the mutation
     * originated.
     *
     * @param originCoord the location where the mutation originated.
     */
    public MutationDetailRecord(Mutation mutation, int trialIndex, Coord originCoord) {
        this.mutationIndex = mutation.getIndex();
        this.trialIndex    = trialIndex;
        this.originTime    = mutation.getOriginationTime();
        this.originX       = originCoord.x;
        this.originY       = originCoord.y;
        this.originZ       = originCoord.z;
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
     * Returns the index of the trial when the mutation originated.
     *
     * @return the index of the trial when the mutation originated.
     */
    public int getTrialIndex() {
        return trialIndex;
    }

    /**
     * Returns the time step when the mutation originated.
     *
     * @return the time step when the mutation originated.
     */
    public int getOriginTime() {
        return originTime;
    }

    /**
     * Returns the x-coordinate of the location where the mutation
     * originated.
     *
     * @return the x-coordinate of the location where the mutation
     * originated. 
     */
    public int getOriginX() {
        return originX;
    }

    /**
     * Returns the y-coordinate of the location where the mutation
     * originated.
     *
     * @return the y-coordinate of the location where the mutation
     * originated. 
     */
    public int getOriginY() {
        return originY;
    }

    /**
     * Returns the z-coordinate of the location where the mutation
     * originated.
     *
     * @return the z-coordinate of the location where the mutation
     * originated. 
     */
    public int getOriginZ() {
        return originZ;
    }

    @Override public String formatLine() {
        return String.format("%d,%d,%d,%d,%d,%d",
                             mutationIndex,
                             trialIndex,
                             originTime,
                             originX,
                             originY,
                             originZ);
    }

    @Override public String getBaseName() {
        return BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "mutationIndex,trialIndex,originTime,originX,originY,originZ";
    }
}
