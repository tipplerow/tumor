
package tumor.report.mutation;

import jam.lattice.Coord;
import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.mutation.MutationType;
import tumor.report.TumorRecord;

/**
 * Records the number of mutations of a particular type at a given
 * lattice site.
 */
public abstract class MutationTypeSiteCountRecord extends TumorRecord implements ReportRecord {
    private final Coord siteCoord;
    private final long  cellCount;
    private final MutationType mutationType;
    private final long         mutationCount;

    /**
     * Creates a new mutation-type site-count record.
     *
     * @param siteCoord the coordinate of the recorded site.
     *
     * @param cellCount the total number of cells at the recorded site.
     *
     * @param mutationType the type of mutation counted.
     *
     * @param mutationCount the number of mutations with the specified
     * type present at the given location.
     */
    protected MutationTypeSiteCountRecord(Coord siteCoord,
                                          long  cellCount,
                                          MutationType mutationType,
                                          long         mutationCount) {
        this.siteCoord = siteCoord;
        this.cellCount = cellCount;
        this.mutationType  = mutationType;
        this.mutationCount = mutationCount;
    }

    public Coord getSiteCoord() {
        return siteCoord;
    }

    public long getCellCount() {
        return cellCount;
    }

    public MutationType getMutationType() {
        return mutationType;
    }

    public long getMutationCount() {
        return mutationCount;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(siteCoord.x);
        builder.append(siteCoord.y);
        builder.append(siteCoord.z);
        builder.append(cellCount);
        builder.append(mutationType);
        builder.append(mutationCount);

        return builder.toString();
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("timeStep");
        builder.append("siteCoordX");
        builder.append("siteCoordY");
        builder.append("siteCoordZ");
        builder.append("cellCount");
        builder.append("mutationType");
        builder.append("mutationCount");

        return builder.toString();
    }
}
