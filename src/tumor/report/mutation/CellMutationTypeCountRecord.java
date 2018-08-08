
package tumor.report.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multiset;

import jam.lattice.Coord;
import jam.report.LineBuilder;
import jam.report.ReportRecord;
import jam.vector.JamVector;

import tumor.carrier.TumorCell;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.mutation.MutationType;
import tumor.report.TumorRecord;

/**
 * Records the number of mutations of each type present in a tumor
 * cell.
 */
public final class CellMutationTypeCountRecord extends TumorRecord implements ReportRecord {
    private final String baseName;

    private final Coord     siteCoord;
    private final JamVector cmVector;
    private final double    normRadialDist;

    private final long tumorCellCount;

    private final Multiset<MutationType> mutationCounts;

    private CellMutationTypeCountRecord(String    baseName,
                                        Coord     siteCoord,
                                        JamVector cmVector,
                                        double    normRadialDist,
                                        long      tumorCellCount,
                                        Multiset<MutationType> mutationCounts) {
        this.baseName = baseName;

        this.siteCoord = siteCoord;
        this.cmVector  = cmVector;

        this.normRadialDist = normRadialDist;
        this.tumorCellCount = tumorCellCount;
        this.mutationCounts = mutationCounts;
    }

    /**
     * Generates a type-count record for a single site in the tumor
     * currently under simulation.
     *
     * @param baseName the base name of the report file where the
     * records will be written.
     *
     * @param siteCoord the coordinate of the site to examine.
     *
     * @return the type-count record for the given site.
     */
    public static CellMutationTypeCountRecord generate(String baseName, Coord siteCoord) {
        TumorDriver<? extends TumorComponent> driver = TumorDriver.global();
        LatticeTumor<? extends TumorComponent> tumor = driver.getLatticeTumor();

        long   tumorCellCount = tumor.countCells();
        double normRadialDist = siteCoord.normRadialDist(tumor.getVectorMoment());

        JamVector cmVector =
            siteCoord.cmVector(tumor.getVectorMoment().getCM());

        TumorCell cell = tumor.collectSingleCellSample(siteCoord);

        Multiset<MutationType> typeCounts =
            cell.getGenotype().countMutationTypes();

        return new CellMutationTypeCountRecord(baseName,
                                               siteCoord,
                                               cmVector,
                                               normRadialDist,
                                               tumorCellCount,
                                               typeCounts);
    }

    /**
     * Generates type-count records for multiple sites in the tumor
     * currently under simulation.
     *
     * @param baseName the base name of the report file where the
     * records will be written.
     *
     * @param siteCoords the coordinates of the sites to examine.
     *
     * @return a list containing one type-count record for each
     * mutation type found at the given sites.
     */
    public static List<CellMutationTypeCountRecord> generate(String baseName, Collection<Coord> siteCoords) {
        List<CellMutationTypeCountRecord> records =
            new ArrayList<CellMutationTypeCountRecord>();

        for (Coord siteCoord : siteCoords)
            records.add(generate(baseName, siteCoord));

        return records;
    }
    
    public Coord getSiteCoord() {
        return siteCoord;
    }

    public double getNormRadialDist() {
        return normRadialDist;
    }

    public long getTumorCellCount() {
        return tumorCellCount;
    }

    public Multiset<MutationType> getMutationCounts() {
        return mutationCounts;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(tumorCellCount);
        builder.append(siteCoord.x);
        builder.append(siteCoord.y);
        builder.append(siteCoord.z);
        builder.append(cmVector.getDouble(0));
        builder.append(cmVector.getDouble(1));
        builder.append(cmVector.getDouble(2));
        builder.append(String.format("%.4f", normRadialDist));

        for (MutationType mutationType : MutationType.values())
            builder.append(mutationCounts.count(mutationType));

        return builder.toString();
    }

    @Override public String getBaseName() {
        return baseName;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("timeStep");
        builder.append("tumorCellCount");
        builder.append("siteCoordX");
        builder.append("siteCoordY");
        builder.append("siteCoordZ");
        builder.append("cmVectorX");
        builder.append("cmVectorY");
        builder.append("cmVectorZ");
        builder.append("normRadialDist");

        for (MutationType mutationType : MutationType.values())
            builder.append(String.format("%s.count", mutationType));

        return builder.toString();
    }
}
