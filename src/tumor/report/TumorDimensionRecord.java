
package tumor.report;

import jam.math.PrincipalMoments;
import jam.math.VectorMoment;
import jam.report.ReportRecord;

import tumor.lattice.LatticeTumor;

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

    /**
     * Base name for the tumor dimension report.
     */
    public static final String BASE_NAME = "tumor-dimension.csv";

    /**
     * Creates a new tumor dimension record.
     *
     * @param trialIndex the index of the current simulation trial.
     *
     * @param timeStep the time step in the current simulation trial.
     *
     * @param tumor the active tumor in the simulation trial.
     */
    public TumorDimensionRecord(int trialIndex, int timeStep, LatticeTumor tumor) {
        this(trialIndex,
             timeStep,
             tumor.countCells(),
             tumor.countComponents(),
             tumor.computeVectorMoment());
    }

    /**
     * Creates a new tumor dimension record.
     *
     * @param trialIndex the index of the simulation trial.
     *
     * @param timeStep the time step when the dimensions were
     * measured.
     *
     * @param cellCount the total number of cells in the tumor.
     *
     * @param componentCount the total number of basic components
     * (cells, demes, or lineages) in the tumor.
     *
     * @param moment the vector moment describing the spatial
     * distribution of cells within the tumor.
     */
    public TumorDimensionRecord(int trialIndex,
                                int timeStep,
                                long cellCount,
                                long componentCount,
                                VectorMoment moment) {
        this.trialIndex = trialIndex;
        this.timeStep   = timeStep;

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
        return BASE_NAME;
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
