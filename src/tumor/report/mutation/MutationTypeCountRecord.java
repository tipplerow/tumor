
package tumor.report.mutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multiset;

import jam.lattice.Coord;
import jam.math.DoubleUtil;
import jam.matrix.JamMatrix;
import jam.report.LineBuilder;
import jam.report.ReportRecord;
import jam.vector.JamVector;
import jam.vector.VectorView;

import tumor.carrier.TumorCell;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.report.TumorRecord;

/**
 * Records the number of mutations by type at a given lattice site.
 */
public final class MutationTypeCountRecord extends TumorRecord implements ReportRecord {
    private final String baseName;

    private final Coord     siteCoord;
    private final JamVector cmVector;
    private final double    normRadialDist;

    private final long siteCellCount;
    private final long siteCompCount;
    private final long tumorCellCount;

    private final String[] typeNames;
    private final int[]    typeCounts;

    private MutationTypeCountRecord(String    baseName,
                                    Coord     siteCoord,
                                    JamVector cmVector,
                                    double    normRadialDist,
                                    long      siteCellCount,
                                    long      siteCompCount,
                                    long      tumorCellCount,
                                    String[]  typeNames,
                                    int[]     typeCounts) {
        validateTypes(typeNames, typeCounts);

        this.baseName = baseName;

        this.siteCoord      = siteCoord;
        this.cmVector       = cmVector;
        this.normRadialDist = normRadialDist;

        this.siteCellCount  = siteCellCount;
        this.siteCompCount  = siteCompCount;
        this.tumorCellCount = tumorCellCount;

        this.typeNames  = typeNames;
        this.typeCounts = typeCounts;
    }

    private static void validateTypes(String[] typeNames, int[] typeCounts) {
        if (typeNames.length != typeCounts.length)
            throw new IllegalArgumentException("Type names and type counts do not agree.");
    }

    /**
     * Generates a type-count record for a single cell in the tumor
     * currently under simulation.
     *
     * @param baseName the base name of the report file where the
     * records will be written.
     *
     * @param typeNames names of the mutation types to record.
     *
     * @param siteCoord the coordinate where the cell will be sampled.
     *
     * @return the type-count record for a cell selected from the
     * given site.
     */
    public static MutationTypeCountRecord forCell(String baseName, String[] typeNames, Coord siteCoord) {
        return generate(baseName, typeNames, siteCoord, true);
    }

    /**
     * Generates type-count records for multiple cells in the tumor
     * currently under simulation.
     *
     * @param baseName the base name of the report file where the
     * records will be written.
     *
     * @param typeNames names of the mutation types to record.
     *
     * @param siteCoords the coordinates where the cells will be
     * sampled.
     *
     * @return a list containing one type-count record for each cell
     * sampled from the given sites.
     */
    public static List<MutationTypeCountRecord> forCells(String baseName,
                                                         String[] typeNames,
                                                         Collection<Coord> siteCoords) {
        List<MutationTypeCountRecord> records =
            new ArrayList<MutationTypeCountRecord>();

        for (Coord siteCoord : siteCoords)
            records.add(forCell(baseName, typeNames, siteCoord));

        return records;
    }

    /**
     * Generates a type-count record for a single site in the tumor
     * currently under simulation.
     *
     * @param baseName the base name of the report file where the
     * records will be written.
     *
     * @param typeNames names of the mutation types to record.
     *
     * @param siteCoord the coordinate of the site to examine.
     *
     * @return the type-count record for all cells at the given site.
     */
    public static MutationTypeCountRecord forSite(String baseName, String[] typeNames, Coord siteCoord) {
        return generate(baseName, typeNames, siteCoord, false);
    }

    /**
     * Generates type-count records for multiple sites in the tumor
     * currently under simulation.
     *
     * @param baseName the base name of the report file where the
     * records will be written.
     *
     * @param typeNames names of the mutation types to record.
     *
     * @param siteCoords the coordinates of the sites to examine.
     *
     * @return a list containing one type-count record for each site.
     */
    public static List<MutationTypeCountRecord> forSites(String baseName,
                                                         String[] typeNames,
                                                         Collection<Coord> siteCoords) {
        List<MutationTypeCountRecord> records =
            new ArrayList<MutationTypeCountRecord>();

        for (Coord siteCoord : siteCoords)
            records.add(forSite(baseName, typeNames, siteCoord));

        return records;
    }
    
    private static MutationTypeCountRecord generate(String   baseName,
                                                    String[] typeNames,
                                                    Coord    siteCoord,
                                                    boolean  forCell) {
        TumorDriver<? extends TumorComponent> driver = TumorDriver.global();
        LatticeTumor<? extends TumorComponent> tumor = driver.getLatticeTumor();

        long   siteCellCount  = tumor.countCells(siteCoord);
        long   siteCompCount  = tumor.countComponents(siteCoord);
        long   tumorCellCount = tumor.countCells();
        double normRadialDist = siteCoord.normRadialDist(tumor.getVectorMoment());

        JamVector cmVector =
            siteCoord.cmVector(tumor.getVectorMoment().getCM());

        Multiset<String> typeCountSet =
            forCell ? countCell(siteCoord) : countSite(siteCoord);

        int[] typeCounts = new int[typeNames.length];

        for (int typeIndex = 0; typeIndex < typeNames.length; ++typeIndex)
            typeCounts[typeIndex] = typeCountSet.count(typeNames[typeIndex]);

        return new MutationTypeCountRecord(baseName,
                                           siteCoord,
                                           cmVector,
                                           normRadialDist,
                                           siteCellCount,
                                           siteCompCount,
                                           tumorCellCount,
                                           typeNames,
                                           typeCounts);
    }

    private static Multiset<String> countCell(Coord siteCoord) {
        TumorDriver<? extends TumorComponent> driver = TumorDriver.global();
        LatticeTumor<? extends TumorComponent> tumor = driver.getLatticeTumor();

        TumorCell cell =
            tumor.collectSingleCellSample(siteCoord);

        return cell.getGenotype().countMutationTypes();
    }

    private static Multiset<String> countSite(Coord siteCoord) {
        TumorDriver<? extends TumorComponent> driver = TumorDriver.global();
        LatticeTumor<? extends TumorComponent> tumor = driver.getLatticeTumor();

        return tumor.countMutationTypes(siteCoord);
    }

    /**
     * Assembles the mutation counts into a matrix of floating-point
     * values.
     *
     * @param records the records to aggregate.
     *
     * @return an {@code M x N} matrix, where {@code M} is the number
     * of mutation types and {@code N} is the number of records, with
     * element {@code (i, j)} containing the number of mutations with
     * type {@code i} reported in record {@code j}.
     *
     * @throws IllegalArgumentException unless at least one record is
     * supplied and all records have identical type names.
     */
    public static JamMatrix toMatrix(List<MutationTypeCountRecord> records) {
        if (records.isEmpty())
            throw new IllegalArgumentException("Must supply at least one record.");

        String[]  fixedNames  = records.get(0).typeNames;
        JamMatrix countMatrix = new JamMatrix(fixedNames.length, records.size());

        for (int recordIndex = 0; recordIndex < records.size(); ++recordIndex) {
            MutationTypeCountRecord record = records.get(recordIndex);

            if (!Arrays.equals(record.typeNames, fixedNames))
                throw new IllegalArgumentException("Mismatched mutation type names.");

            for (int typeIndex = 0; typeIndex < fixedNames.length; ++typeIndex)
                countMatrix.set(typeIndex, recordIndex, record.typeCounts[typeIndex]);
        }

        return countMatrix;
    }

    public Coord getSiteCoord() {
        return siteCoord;
    }

    public VectorView getCMVector() {
        return cmVector;
    }

    public double getNormRadialDist() {
        return normRadialDist;
    }

    public long getSiteCellCount() {
        return siteCellCount;
    }

    public long getSiteComponentCount() {
        return siteCompCount;
    }

    public long getTumorCellCount() {
        return tumorCellCount;
    }

    public String[] getTypeNames() {
        return typeNames;
    }

    public int[] getTypeCounts() {
        return typeCounts;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(tumorCellCount);
        builder.append(siteCoord);
        appendRadialVector(builder, cmVector);
        builder.append(String.format("%.4f", normRadialDist));
        builder.append(siteCellCount);
        builder.append(siteCompCount);

        for (int typeCount : typeCounts)
            builder.append(typeCount);

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
        builder.append("siteCellCount");
        builder.append("siteComponentCount");

        for (String typeName : typeNames)
            builder.append(String.format("%s.count", typeName));

        return builder.toString();
    }
}
