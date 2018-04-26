
package tumor.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jam.lattice.Coord;
import jam.lattice.Neighborhood;
import jam.math.HerfindahlIndex;
import jam.math.JamRandom;
import jam.math.StatSummary;
import jam.math.VectorMoment;
import jam.report.ReportRecord;
import jam.sim.DiscreteTimeSimulation;
import jam.vector.VectorView;

import tumor.carrier.Carrier;
import tumor.carrier.Lineage;
import tumor.lattice.LineageLatticeTumor;
import tumor.mutation.MutationFrequencyMap;

/**
 * Quantifies the genetic diversity within one region of a tumor of
 * lineages.
 */
public final class RegionalDiversityRecord implements ReportRecord {
    private final int trialIndex;
    private final VectorView center;

    private final long cellCount;
    private final long lineageCount;
    private final long mutationCount;

    private final double normR; // |center(region) - cm(tumor)| / RG(tumor)
    private final double lineageHFI;
    private final double meanMutLoad;
    private final double medianMutLoad;
    private final double meanVAF;
    private final double medianVAF;
    private final double vafQ1;
    private final double vafQ3;

    private RegionalDiversityRecord(VectorView center,
                                    long cellCount,
                                    long lineageCount,
                                    long mutationCount,
                                    double normR,
                                    double lineageHFI,
                                    double meanMutLoad,
                                    double medianMutLoad,
                                    double meanVAF,
                                    double medianVAF,
                                    double vafQ1,
                                    double vafQ3) {
        this.center = center;

        this.cellCount     = cellCount;
        this.lineageCount  = lineageCount;
        this.mutationCount = mutationCount;

        this.normR      = normR;
        this.lineageHFI = lineageHFI;

        this.meanMutLoad  = meanMutLoad;
        this.medianMutLoad = medianMutLoad;

        this.meanVAF   = meanVAF;
        this.medianVAF = medianVAF;
        this.vafQ1     = vafQ1;
        this.vafQ3     = vafQ3;

        this.trialIndex = DiscreteTimeSimulation.getTrialIndex();
    }

    /**
     * Base name for the regional genetic diversity report.
     */
    public static final String BASE_NAME = "regional-diversity.csv";

    /**
     * Computes the diversity records for every region in a tumor of
     * lineages.
     *
     * @param tumor the tumor to examine.
     *
     * @param sitesPer the number of sites per region.
     *
     * @return the regional diversity records for the given tumor.
     *
     * @throws IllegalArgumentException if the number of sites per
     * region is larger than the size of the lattice neighborhood.
     */
    public static Collection<RegionalDiversityRecord> compute(LineageLatticeTumor tumor, int sitesPer) {
        VectorMoment moment = tumor.computeVectorMoment();
        Neighborhood neighborhood = tumor.getNeighborhood();
        Map<Coord, Collection<Lineage>> lineageMap = tumor.mapComponents();

        if (sitesPer > neighborhood.size())
            throw new IllegalArgumentException("Sites per region exceeds neighborhood size.");

        Collection<RegionalDiversityRecord> records =
            new ArrayList<RegionalDiversityRecord>(lineageMap.size());

        for (Coord centerCoord : lineageMap.keySet()) {
            List<Coord> coords = new ArrayList<Coord>();
            List<Lineage> lineages = new ArrayList<Lineage>();

            coords.add(centerCoord);
            coords.addAll(neighborhood.randomNeighbors(centerCoord, sitesPer - 1, JamRandom.global()));
            
            for (Coord coord : coords)
                if (lineageMap.containsKey(coord))
                    lineages.addAll(lineageMap.get(coord));

            records.add(compute(moment, coords, lineages));
        }

        return records;
    }

    private static RegionalDiversityRecord compute(VectorMoment  moment,
                                                   List<Coord>   coords,
                                                   List<Lineage> lineages) {
        VectorView center =
            VectorMoment.compute(coords).getCM();

        MutationFrequencyMap frequencyMap =
            MutationFrequencyMap.compute(lineages);

        long cellCount     = Carrier.countCells(lineages);
        long lineageCount  = lineages.size();
        long mutationCount = frequencyMap.countMutations();

        double normR = moment.normR(center);
        
        double lineageHFI =
            HerfindahlIndex.compute(lineages, x -> (double) x.countCells()).getNormalized();

        StatSummary mutLoadSummary =
            StatSummary.compute(lineages, x -> (double) x.countAccumulatedMutations());

        double meanMutLoad   = mutLoadSummary.getMean();
        double medianMutLoad = mutLoadSummary.getMedian();

        StatSummary summaryVAF =
            frequencyMap.summarize();
        
        double meanVAF   = summaryVAF.getMean();
        double medianVAF = summaryVAF.getMedian();
        double vafQ1     = summaryVAF.getQuartile1();
        double vafQ3     = summaryVAF.getQuartile3();

        return new RegionalDiversityRecord(center,
                                           cellCount,
                                           lineageCount,
                                           mutationCount,
                                           normR,
                                           lineageHFI,
                                           meanMutLoad,
                                           medianMutLoad,
                                           meanVAF,
                                           medianVAF,
                                           vafQ1,
                                           vafQ3);
    }

    /**
     * Returns the index of the simulation trial when this record was
     * generated.
     *
     * @return the index of the simulation trial when this record was
     * generated.
     */
    public int getTrialIndex() {
        return trialIndex;
    }

    /**
     * Returns the geometric center of the region.
     *
     * @return the geometric center of the region.
     */
    public VectorView getCenter() {
        return center;
    }

    /**
     * Returns the total number of cells in the region.
     *
     * @return the total number of cells in the region.
     */
    public long getCellCount() {
        return cellCount;
    }

    /**
     * Returns the number of distinct lineages in the region.
     *
     * @return the number of distinct lineages in the region.
     */
    public long getLineageCount() {
        return lineageCount;
    }

    /**
     * Returns the number of unique mutations in the region.
     *
     * @return the number of unique mutations in the region.
     */
    public long getMutationCount() {
        return mutationCount;
    }

    /**
     * Returns the normalized radial coordinate of the region: the
     * distance of the center of the region from the center of mass
     * of the tumor divided by the radius of gyration of the tumor.
     *
     * @return the normalized radial coordinate of the region.
     */
    public double getNormR() {
        return normR;
    }

    /**
     * Returns the normalized Herfindahl index for the lineage sizes
     * in the region.
     *
     * @return the normalized Herfindahl index for the lineage sizes
     * in the region.
     */
    public double getLineageHFI() {
        return lineageHFI;
    }

    /**
     * Returns the mean variant allele frequency (VAF) in the region.
     *
     * @return the mean variant allele frequency (VAF) in the region.
     */
    public double getMeanVAF() {
        return meanVAF;
    }

    /**
     * Returns the median variant allele frequency (VAF) in the region.
     *
     * @return the median variant allele frequency (VAF) in the region.
     */ 
    public double getMedianVAF() {
        return medianVAF;
    }
    
    /**
     * Returns the first quartile of the variant allele frequency
     * (VAF) distribution in the region.
     *
     * @return the first quartile of the variant allele frequency
     * (VAF) distribution in the region.
     */ 
    public double getQuartile1VAF() {
        return vafQ1;
    }

    /**
     * Returns the third quartile of the variant allele frequency
     * (VAF) distribution in the region.
     *
     * @return the third quartile of the variant allele frequency
     * (VAF) distribution in the region.
     */ 
    public double getQuartile3VAF() {
        return vafQ3;
    }

    @Override public String formatLine() {
        return String.format("%d,%.4f,%.4f,%.4f,%.4f,%d,%d,%d,%.4f,%.4g,%.4g,%.4g,%.4g,%.4g,%.4g",
                             trialIndex,
                             center.getDouble(0),
                             center.getDouble(1),
                             center.getDouble(2),
                             normR,
                             cellCount,
                             lineageCount,
                             mutationCount,
                             lineageHFI,
                             meanMutLoad,
                             medianMutLoad,
                             meanVAF,
                             medianVAF,
                             vafQ1,
                             vafQ3);
    }

    @Override public String getBaseName() {
        return BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "trialIndex,x,y,z,normR"
            + ",cellCount,lineageCount,mutationCount,lineageHFI"
            + ",meanMutLoad,medianMutLoad"
            + ",meanVAF,medianVAF,vafQ1,vafQ3";
    }
}
