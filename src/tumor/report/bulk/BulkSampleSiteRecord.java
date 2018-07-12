
package tumor.report.bulk;

import java.util.ArrayList;
import java.util.Collection;

import jam.lattice.Coord;
import jam.report.ReportRecord;
import jam.sim.StepRecord;

import tumor.carrier.Carrier;
import tumor.carrier.TumorComponent;

/**
 * Represents the contents of one lattice site in a bulk sample.
 */
public final class BulkSampleSiteRecord extends StepRecord implements ReportRecord {
    private final long  sampleIndex;
    private final Coord centerSite;
    private final Coord sampleSite;
    private final long  compCount;
    private final long  cellCount;

    private BulkSampleSiteRecord(int   trialIndex,
                                 int   timeStep,
                                 long  sampleIndex,
                                 Coord centerSite,
                                 Coord sampleSite,
                                 long  compCount,
                                 long  cellCount) {
        super(trialIndex, timeStep);

        this.sampleIndex = sampleIndex;
        this.centerSite  = centerSite;
        this.sampleSite  = sampleSite;
        this.compCount   = compCount;
        this.cellCount   = cellCount;
    }

    /**
     * Splits a single bulk sample into a collection of individual
     * site records.
     *
     * @param bulkSample the bulk sample of interest.
     *
     * @return new site records for all lattice sites in the bulk
     * sample.
     */
    public static Collection<BulkSampleSiteRecord> split(BulkSample bulkSample) {
        int trialIndex = bulkSample.getTrialIndex();
        int timeStep   = bulkSample.getCollectionTime();

        long  sampleIndex = bulkSample.getIndex();
        Coord centerSite  = bulkSample.getCenterSite();

        Collection<Coord> sampleSites = bulkSample.viewComponentMap().keySet();
        Collection<BulkSampleSiteRecord> records = new ArrayList<BulkSampleSiteRecord>(sampleSites.size());

        for (Coord sampleSite : sampleSites) {
            Collection<? extends TumorComponent> siteComponents =
                bulkSample.viewComponentMap().get(sampleSite);

            long compCount = siteComponents.size();
            long cellCount = Carrier.countCells(siteComponents);

            records.add(new BulkSampleSiteRecord(trialIndex,
                                                 timeStep,
                                                 sampleIndex,
                                                 centerSite,
                                                 sampleSite,
                                                 compCount,
                                                 cellCount));
        }

        return records;
    }

    /**
     * Returns the index of the bulk sample.
     *
     * @return the index of the bulk sample.
     */
    public final long getSampleIndex() {
        return sampleIndex;
    }

    /**
     * Returns the lattice site at the center of the bulk sample.
     *
     * @return the lattice site at the center of the bulk sample.
     */
    public Coord getCenterSite() {
        return centerSite;
    }

    /**
     * Returns the location of this site record.
     *
     * @return the location of this site record.
     */
    public Coord getSampleSite() {
        return sampleSite;
    }

    /**
     * Returns the number of tumor components at the sample site.
     *
     * @return the number of tumor components at the sample site.
     */
    public long getComponentCount() {
        return compCount;
    }

    /**
     * Returns the total number of tumor cells at the sample site.
     *
     * @return the total number of tumor cells at the sample site.
     */
    public long getCellCount() {
        return cellCount;
    }

    @Override public String formatLine() {
        return String.format("%d,%d,%d, %d,%d,%d, %d,%d,%d, %d,%d",
                             getTrialIndex(),
                             getTimeStep(),
                             sampleIndex,
                             centerSite.x,
                             centerSite.y,
                             centerSite.z,
                             sampleSite.x,
                             sampleSite.y,
                             sampleSite.z,
                             compCount,
                             cellCount);
    }

    @Override public String getBaseName() {
        return BulkSampleSiteReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "trialIndex"
            + ",timeStep"
            + ",sampleIndex"
            + ",centerSiteX"
            + ",centerSiteY"
            + ",centerSiteZ"
            + ",sampleSiteX"
            + ",sampleSiteY"
            + ",sampleSiteZ"
            + ",compCount"
            + ",cellCount";
    }
}
