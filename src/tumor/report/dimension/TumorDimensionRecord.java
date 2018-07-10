
package tumor.report.dimension;

import jam.math.PrincipalMoments;
import jam.math.VectorMoment;
import jam.report.ReportRecord;

import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;

/**
 * Encapsulates the tumor dimensions and characteristic values for the
 * gyration tensor.
 */
public final class TumorDimensionRecord implements ReportRecord {
    private final int trialIndex;
    private final int timeStep;

    private final long cellCount;
    private final long componentCount;

    private final double cmX;
    private final double cmY;
    private final double cmZ;
    private final double RG;
    private final double pmX;
    private final double pmY;
    private final double pmZ;
    private final double asphericity;
    private final double acylindricity;
    private final double anisotropy;

    private TumorDimensionRecord(long cellCount,
                                 long componentCount,
                                 VectorMoment moment) {
        this.trialIndex = TumorDriver.global().getTrialIndex();
        this.timeStep   = TumorDriver.global().getTimeStep();

        this.cellCount      = cellCount;
        this.componentCount = componentCount;

        this.cmX = moment.getCM().getDouble(0);
        this.cmY = moment.getCM().getDouble(1);
        this.cmZ = moment.getCM().getDouble(2);

        this.RG = moment.scalar();

        this.pmX = moment.getPrincipalMoments().pmX;
        this.pmY = moment.getPrincipalMoments().pmY;
        this.pmZ = moment.getPrincipalMoments().pmZ;

        this.asphericity   = moment.asphericity();
        this.acylindricity = moment.acylindricity();
        this.anisotropy    = moment.anisotropy();
    }

    /**
     * Computes the dimension record for a given tumor.
     *
     * @param tumor the tumor under simulation.
     *
     * @return the dimension record describing the input tumor.
     */
    public static TumorDimensionRecord compute(LatticeTumor<?> tumor) {
        return new TumorDimensionRecord(tumor.countCells(),
                                        tumor.countComponents(),
                                        tumor.getVectorMoment());
    }

    /**
     * Returns the index of the simulation trial.
     *
     * @return the index of the simulation trial.
     */
    public int getTrialIndex() {
        return trialIndex;
    }

    /**
     * Returns the time step when the dimensions were measured.
     *
     * @return the time step when the dimensions were measured.
     */
    public int getTimeStep() {
        return timeStep;
    }

    /**
     * Returns the total number of cells in the tumor.
     *
     * @return the total number of cells in the tumor.
     */
    public long getCellCount() {
        return cellCount;
    }

    /**
     * Returns the total number of basic components (cells, demes, or
     * lineages) in the tumor.
     *
     * @return the total number of basic components in the tumor.
     */
    public long getComponentCount() {
        return componentCount;
    }

    @Override public String formatLine() {
        return String.format("%d,%d,%d,%d,%.3g,%.3g,%.3g,%.3g,%.6g,%.6g,%.6g,%.6g,%.6g,%.6g",
                             trialIndex,
                             timeStep,
                             cellCount,
                             componentCount,
                             cmX,
                             cmY,
                             cmZ,
                             RG,
                             pmX,
                             pmY,
                             pmZ,
                             asphericity,
                             acylindricity,
                             anisotropy);
    }

    @Override public String getBaseName() {
        return TumorDimensionReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        return "trialIndex"
            + ",timeStep"
            + ",cellCount"
            + ",componentCount"
            + ",cmX"
            + ",cmY"
            + ",cmZ"
            + ",RG"
            + ",pmX"
            + ",pmY"
            + ",pmZ"
            + ",asphericity"
            + ",acylindricity"
            + ",anisotropy";
    }
}
