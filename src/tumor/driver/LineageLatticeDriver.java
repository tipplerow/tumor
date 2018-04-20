
package tumor.driver;

import java.util.Collection;

import jam.app.JamProperties;
import jam.math.IntRange;
import jam.report.ReportWriter;

import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.lattice.LineageLatticeTumor;
import tumor.report.RegionalDiversityRecord;

/**
 * Simulates tumors composed of lineages on a lattice.
 */
public class LineageLatticeDriver extends MultiCellularLatticeDriver<Lineage> {
    private final int sitesPerRegion;
    private final boolean writeRegionalDiversity;

    private ReportWriter<RegionalDiversityRecord> regionalDiversityWriter;

    /**
     * Name of the system property that specifies whether or not to
     * write the regional diversity report (defaults to false).
     */
    public static final String WRITE_REGIONAL_DIVERSITY_PROPERTY = "TumorDriver.writeRegionalDiversity";

    /**
     * Name of the system property that defines the number of lattice
     * sites to include in each region in the context of the regional
     * diversity report (required for the regional diversity report).
     */
    public static final String SITES_PER_REGION_PROPERTY = "TumorDriver.sitesPerRegion";

    /**
     * Creates a new driver and reads system properties from a set of
     * property files.
     *
     * @param propertyFiles one or more files containing the system
     * properties that define the simulation parameters.
     *
     * @throws IllegalArgumentException unless at least one property
     * file is specified.
     */
    protected LineageLatticeDriver(String[] propertyFiles) {
        super(propertyFiles);

        this.sitesPerRegion = resolveSitesPerRegion();
        this.writeRegionalDiversity = resolveWriteRegionalDiversity();
    }

    private static int resolveSitesPerRegion() {
        if (resolveWriteRegionalDiversity())
            return JamProperties.getRequiredInt(SITES_PER_REGION_PROPERTY, IntRange.POSITIVE);
        else
            return -1;
    }

    private static boolean resolveWriteRegionalDiversity() {
        return JamProperties.getOptionalBoolean(WRITE_REGIONAL_DIVERSITY_PROPERTY, false);
    }

    /**
     * Runs one simulation.
     *
     * @param propertyFiles one or more files containing the system
     * properties that define the simulation parameters.
     *
     * @throws IllegalArgumentException unless at least one property
     * file is specified.
     */
    public static void run(String[] propertyFiles) {
        LineageLatticeDriver driver = new LineageLatticeDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override public String getComponentDescription() {
        return "lineage";
    }

    @Override protected LineageLatticeTumor createTumor() {
        return LineageLatticeTumor.primary(createFounder());
    }

    private Lineage createFounder() {
        return Lineage.founder(GrowthRate.global(), getInitialSize());
    }

    @Override protected LineageLatticeTumor getTumor() {
        return (LineageLatticeTumor) super.getTumor();
    }

    @Override protected void initializeSimulation() {
        super.initializeSimulation();

        if (writeRegionalDiversity) {
            regionalDiversityWriter = ReportWriter.create(getReportDir());
            autoClose(regionalDiversityWriter);
        }
    }

    @Override protected void finalizeTrial() {
        super.finalizeTrial();

        if (writeRegionalDiversity)
            writeRegionalDiversity();
    }

    private void writeRegionalDiversity() {
        regionalDiversityWriter.write(RegionalDiversityRecord.compute(getTumor(), sitesPerRegion));
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}
