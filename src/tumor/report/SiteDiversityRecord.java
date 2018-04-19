
package tumor.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jam.lattice.Coord;
import jam.math.HerfindahlIndex;
import jam.math.StatSummary;
import jam.math.VectorMoment;
import jam.report.ReportRecord;
import jam.sim.DiscreteTimeSimulation;

import tumor.carrier.Carrier;
import tumor.carrier.Lineage;
import tumor.lattice.LineageLatticeTumor;
import tumor.mutation.MutationFrequencyMap;

/**
 * Quantifies the genetic diversity at a single lattice site in a
 * tumor of lineages.
 */
public final class SiteDiversityRecord implements ReportRecord {
    private final int trialIndex;

    private final Coord  coord;
    private final double normR; // |coord - cm| / RG

    private final long cellCount;
    private final long lineageCount;
    private final long mutationCount;

    private final double lineageHFI;
    private final double meanVAF;
    private final double medianVAF;
    private final double vafQ1;
    private final double vafQ3;

    private SiteDiversityRecord(Coord  coord,
                                double normR,
                                long   cellCount,
                                long   lineageCount,
                                long   mutationCount,
                                double lineageHFI,
                                double meanVAF,
                                double medianVAF,
                                double vafQ1,
                                double vafQ3) {
        this.coord = coord;
        this.normR = normR;

        this.cellCount     = cellCount;
        this.lineageCount  = lineageCount;
        this.mutationCount = mutationCount;

        this.lineageHFI = lineageHFI;
        this.meanVAF    = meanVAF;
        this.medianVAF  = medianVAF;
        this.vafQ1      = vafQ1;
        this.vafQ3      = vafQ3;

        this.trialIndex = DiscreteTimeSimulation.getTrialIndex();
    }

    /**
     * Base name for the site genetic diversity report.
     */
    public static final String BASE_NAME = "site-diversity.csv";

    /**
     * Computes the diversity records for every occupied site in a
     * tumor of lineages.
     *
     * @param tumor the tumor to examine.
     *
     * @return the site diversity records for the given tumor.
     */
    public static Collection<SiteDiversityRecord> compute(LineageLatticeTumor tumor) {
        VectorMoment moment = tumor.computeVectorMoment();
        Map<Coord, Collection<Lineage>> lineageMap = tumor.mapComponents();

        Collection<SiteDiversityRecord> records =
            new ArrayList<SiteDiversityRecord>(lineageMap.size());

        for (Coord coord : lineageMap.keySet())
            records.add(compute(coord, moment, lineageMap.get(coord)));

        return records;
    }

    private static SiteDiversityRecord compute(Coord coord, VectorMoment moment, Collection<Lineage> lineages) {
        double normR = moment.normR(coord.toVector());

        MutationFrequencyMap frequencyMap =
            MutationFrequencyMap.compute(lineages);

        long cellCount     = Carrier.countCells(lineages);
        long lineageCount  = lineages.size();
        long mutationCount = frequencyMap.countMutations();

        double lineageHFI =
            HerfindahlIndex.compute(lineages, x -> (double) x.countCells()).getNormalized();

        StatSummary summaryVAF =
            frequencyMap.summarize();
        
        double meanVAF   = summaryVAF.getMean();
        double medianVAF = summaryVAF.getMedian();
        double vafQ1     = summaryVAF.getQuartile1();
        double vafQ3     = summaryVAF.getQuartile3();

        return new SiteDiversityRecord(coord,
                                       normR,
                                       cellCount,
                                       lineageCount,
                                       mutationCount,
                                       lineageHFI,
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
     * Returns the lattice coordinate of the site.
     *
     * @return the lattice coordinate of the site.
     */
    public Coord getCoord() {
        return coord;
    }

    /**
     * Returns the normalized radial coordinate of the site: the
     * distance of the site from the center of mass of the tumor
     * divided by the radius of gyration of the tumor.
     *
     * @return the normalized radial coordinate of the site.
     */
    public double getNormR() {
        return normR;
    }

    /**
     * Returns the total number of cells at the site.
     *
     * @return the total number of cells at the site.
     */
    public long getCellCount() {
        return cellCount;
    }

    /**
     * Returns the number of distinct lineages at the site.
     *
     * @return the number of distinct lineages at the site.
     */
    public long getLineageCount() {
        return lineageCount;
    }

    /**
     * Returns the number of unique mutations at the site.
     *
     * @return the number of unique mutations at the site.
     */
    public long getMutationCount() {
        return mutationCount;
    }

    /**
     * Returns the normalized Herfindahl index for the lineage sizes
     * at the site.
     *
     * @return the normalized Herfindahl index for the lineage sizes
     * at the site.
     */
    public double getLineageHFI() {
        return lineageHFI;
    }

    /**
     * Returns the mean variant allele frequency (VAF) at the site.
     *
     * @return the mean variant allele frequency (VAF) at the site.
     */
    public double getMeanVAF() {
        return meanVAF;
    }

    /**
     * Returns the median variant allele frequency (VAF) at the site.
     *
     * @return the median variant allele frequency (VAF) at the site.
     */ 
    public double getMedianVAF() {
        return medianVAF;
    }
    
    /**
     * Returns the first quartile of the variant allele frequency
     * (VAF) distribution at the site.
     *
     * @return the first quartile of the variant allele frequency
     * (VAF) distribution at the site.
     */ 
    public double getQuartile1VAF() {
        return vafQ1;
    }

    /**
     * Returns the third quartile of the variant allele frequency
     * (VAF) distribution at the site.
     *
     * @return the third quartile of the variant allele frequency
     * (VAF) distribution at the site.
     */ 
    public double getQuartile3VAF() {
        return vafQ3;
    }

    @Override public String formatLine() {
        return String.format("%d,%d,%d,%d,%.4f,%d,%d,%d,%.4f,%.4g,%.4g,%.4g,%.4g",
                             trialIndex,
                             coord.x,
                             coord.y,
                             coord.z,
                             normR,
                             cellCount,
                             lineageCount,
                             mutationCount,
                             lineageHFI,
                             meanVAF,
                             medianVAF,
                             vafQ1,
                             vafQ3);
    }

    @Override public String getBaseName() {
        return BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "trialIndex,x,y,z,normR,cellCount,lineageCount,mutationCount,lineageHFI,meanVAF,medianVAF,vafQ1,vafQ3";
    }
}
