
package tumor.driver;

import java.util.Collection;

import jam.app.JamProperties;
import jam.report.ReportWriter;

import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.lattice.LineageLatticeTumor;
import tumor.report.SiteDiversityRecord;

/**
 * Simulates tumors composed of lineages on a lattice.
 */
public class LineageLatticeDriver extends MultiCellularLatticeDriver<Lineage> {
    private final boolean writeSiteDiversity;

    private ReportWriter<SiteDiversityRecord> siteDiversityWriter;

    /**
     * Name of the system property that specifies whether or not to
     * write the site diversity report (defaults to false).
     */
    public static final String WRITE_SITE_DIVERSITY_PROPERTY = "TumorDriver.writeSiteDiversity";

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
        this.writeSiteDiversity = resolveWriteSiteDiversity();
    }

    private static boolean resolveWriteSiteDiversity() {
        return JamProperties.getOptionalBoolean(WRITE_SITE_DIVERSITY_PROPERTY, false);
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

        if (writeSiteDiversity) {
            siteDiversityWriter = ReportWriter.create(getReportDir());
            autoClose(siteDiversityWriter);
        }
    }

    @Override protected void finalizeTrial() {
        super.finalizeTrial();

        if (writeSiteDiversity)
            writeSiteDiversity();
    }

    private void writeSiteDiversity() {
        siteDiversityWriter.write(SiteDiversityRecord.compute(getTumor()));
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}
