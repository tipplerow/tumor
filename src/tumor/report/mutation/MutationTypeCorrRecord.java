
package tumor.report.mutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jam.lattice.Coord;
import jam.matrix.JamMatrix;
import jam.report.LineBuilder;
import jam.report.ReportRecord;
import jam.vector.JamVector;
import jam.vector.VectorAggregator;

import tumor.carrier.TumorCell;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.report.TumorRecord;

/**
 * Records the site-to-site correlation in the number of mutations.
 */
public final class MutationTypeCorrRecord extends TumorRecord implements ReportRecord {
    private final String baseName;
    private final double cmVectorCos;

    private final String[] typeNames;
    private final double[] countCorr;

    private MutationTypeCorrRecord(String baseName,
                                   double cmVectorCos,
                                   String[] typeNames,
                                   double[] countCorr) {
        validateTypes(typeNames, countCorr);

        this.baseName = baseName;
        this.cmVectorCos = cmVectorCos;

        this.typeNames = typeNames;
        this.countCorr = countCorr;
    }

    private static void validateTypes(String[] typeNames, double[] countCorr) {
        if (typeNames.length != countCorr.length)
            throw new IllegalArgumentException("Mutation types and correlation counts do not match.");
    }

    /**
     * Generates mutation-type correlation records for cells sampled
     * from a collection of sites.
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
    public static List<MutationTypeCorrRecord> forCells(String baseName,
                                                        String[] typeNames,
                                                        Collection<Coord> siteCoords) {
        List<MutationTypeCountRecord> countRecords =
            MutationTypeCountRecord.forCells(baseName, typeNames, siteCoords);

        JamMatrix countMatrix = MutationTypeCountRecord.toMatrix(countRecords);
        JamVector countMeans  = countMatrix.aggregateRows(VectorAggregator.MEAN);
        JamVector countVars   = countMatrix.aggregateRows(VectorAggregator.VARIANCE);

        List<MutationTypeCorrRecord> corrRecords =
            new ArrayList<MutationTypeCorrRecord>();

        for (int ii = 0; ii < countRecords.size() - 1; ++ii) {
            MutationTypeCountRecord r1 = countRecords.get(ii);

            for (int jj = ii + 1; jj < countRecords.size(); ++jj) {
                MutationTypeCountRecord r2 = countRecords.get(jj);

                double   cmVectorCos = JamVector.cosine(r1.getCMVector(), r2.getCMVector());
                double[] countCorr   = computeCountCorr(r1, r2, countMeans, countVars);
                
                corrRecords.add(new MutationTypeCorrRecord(baseName, cmVectorCos, typeNames, countCorr));
            }
        }                

        return corrRecords;
    }

    private static double[] computeCountCorr(MutationTypeCountRecord r1,
                                             MutationTypeCountRecord r2,
                                             JamVector countMeans,
                                             JamVector countVars) {
        int[] count1 = r1.getTypeCounts();
        int[] count2 = r2.getTypeCounts();

        double[] countCorr = new double[countMeans.length()];

        for (int k = 0; k < countCorr.length; ++k) {
            double meanK = countMeans.get(k);
            double varK  = countVars.get(k);

            countCorr[k] = (count1[k] - meanK) * (count2[k] - meanK) / varK;
            //countCorr[k] = (count1[k] == count2[k]) ? 1.0 : 0.0;
        }

        return countCorr;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(String.format("%.4f", cmVectorCos));

        for (double x : countCorr)
            builder.append(String.format("%.6f", x));

        return builder.toString();
    }

    @Override public String getBaseName() {
        return baseName;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("timeStep");
        builder.append("cmVectorCos");

        for (String typeName : typeNames)
            builder.append(String.format("%s.corr", typeName));

        return builder.toString();
    }
}
