
package tumor.report.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jam.lattice.Coord;
import jam.report.LineBuilder;
import jam.report.ReportRecord;
import jam.vector.JamVector;

import tumor.carrier.TumorCell;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.report.TumorRecord;

/**
 * Records the mutation count similarity between pairs of tumor sites.
 */
public final class MutationTypePairRecord extends TumorRecord implements ReportRecord {
    private final String baseName;
    private final double cosineRV;
    private final double arcDist;

    private final String[] typeNames;
    private final int[]    typeCount1;
    private final int[]    typeCount2;

    private MutationTypePairRecord(String   baseName,
                                   double   cosineRV,
                                   double   arcDist,
                                   String[] typeNames,
                                   int[]    typeCount1,
                                   int[]    typeCount2) {
        validateTypes(typeNames, typeCount1, typeCount2);

        this.baseName = baseName;
        this.cosineRV = cosineRV;
        this.arcDist  = arcDist;

        this.typeNames  = typeNames;
        this.typeCount1 = typeCount1;
        this.typeCount2 = typeCount2;
    }

    private static void validateTypes(String[] typeNames, int[] typeCount1, int[] typeCount2) {
        if (typeNames.length != typeCount1.length)
            throw new IllegalArgumentException("Mutation types and counts do not match.");

        if (typeNames.length != typeCount2.length)
            throw new IllegalArgumentException("Mutation types and counts do not match.");
    }

    /**
     * Generates mutation-type pair records for cells sampled from a
     * collection of sites.
     *
     * @param baseName the base name of the report file where the
     * records will be written.
     *
     * @param typeNames names of the mutation types to record.
     *
     * @param siteCoords the coordinates where the cells will be
     * sampled.
     *
     * @return the type-count record for the given site.
     */
    public static List<MutationTypePairRecord> forCells(String baseName,
                                                        String[] typeNames,
                                                        Collection<Coord> siteCoords) {
        List<MutationTypeCountRecord> countRecords =
            MutationTypeCountRecord.forCells(baseName, typeNames, siteCoords);

        List<MutationTypePairRecord> pairRecords =
            new ArrayList<MutationTypePairRecord>();

        for (    int index1 = 0;          index1 < countRecords.size() - 1; ++index1) {
            for (int index2 = index1 + 1; index2 < countRecords.size();     ++index2) {
                MutationTypeCountRecord record1 = countRecords.get(index1);
                MutationTypeCountRecord record2 = countRecords.get(index2);

                JamVector vec1 = JamVector.copyOf(record1.getCMVector());
                JamVector vec2 = JamVector.copyOf(record2.getCMVector());

                double cosineRV = JamVector.cosine(vec1, vec2);
                double arcDist  = 0.5 * (vec1.norm() + vec2.norm()) * Math.acos(cosineRV);

                int[] typeCount1 = record1.getTypeCounts();
                int[] typeCount2 = record2.getTypeCounts();
                
                pairRecords.add(new MutationTypePairRecord(baseName, cosineRV, arcDist, typeNames, typeCount1, typeCount2));
            }
        }                

        return pairRecords;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(String.format("%.4f", cosineRV));
        builder.append(String.format("%.4f", arcDist));

        for (int typeCount : typeCount1)
            builder.append(typeCount);

        for (int typeCount : typeCount2)
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
        builder.append("cosineRV");
        builder.append("arcDist");

        for (String typeName : typeNames)
            builder.append(String.format("%s.count1", typeName));

        for (String typeName : typeNames)
            builder.append(String.format("%s.count2", typeName));

        return builder.toString();
    }
}
