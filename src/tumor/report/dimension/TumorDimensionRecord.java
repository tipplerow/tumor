
package tumor.report.dimension;

import jam.math.PrincipalMoments;
import jam.math.VectorMoment;
import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.report.TumorRecord;

/**
 * Encapsulates the tumor dimensions and characteristic values for the
 * gyration tensor.
 */
public final class TumorDimensionRecord extends TumorRecord implements ReportRecord {
    private final long cellCount;
    private final long componentCount;
    private final VectorMoment vectorMoment;

    private TumorDimensionRecord(long cellCount,
                                 long componentCount,
                                 VectorMoment vectorMoment) {
        this.cellCount      = cellCount;
        this.componentCount = componentCount;
        this.vectorMoment   = vectorMoment;
    }

    /**
     * Collects the tumor dimension record for the active tumor at
     * this instant in the simulation.
     *
     * @return the dimension record describing the active tumor at
     * this instant in the simulation.
     */
    public static TumorDimensionRecord snap() {
        return compute(TumorDriver.global().getLatticeTumor());
    }

    /**
     * Computes the dimension record for a given tumor.
     *
     * @param tumor the tumor under simulation.
     *
     * @return the dimension record describing the input tumor.
     */
    public static TumorDimensionRecord compute(LatticeTumor<?> tumor) {
        int trialIndex = TumorDriver.global().getTrialIndex();
        int timeStep   = TumorDriver.global().getTimeStep();

        return new TumorDimensionRecord(tumor.countCells(),
                                        tumor.countComponents(),
                                        tumor.getVectorMoment());
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

    /**
     * Returns the vector moment for the tumor at the time of collection.
     *
     * @return the vector moment for the tumor at the time of collection.
     */
    public VectorMoment getVectorMoment() {
        return vectorMoment;
    }

    public double getCMX() {
        return vectorMoment.getCM().getDouble(0);
    }

    public double getCMY() {
        return vectorMoment.getCM().getDouble(1);
    }

    public double getCMZ() {
        return vectorMoment.getCM().getDouble(2);
    }

    public double getRG() {
        return vectorMoment.scalar();
    }

    public double getPMX() {
        return vectorMoment.getPrincipalMoments().pmX;
    }

    public double getPMY() {
        return vectorMoment.getPrincipalMoments().pmY;
    }

    public double getPMZ() {
        return vectorMoment.getPrincipalMoments().pmZ;
    }

    public double getAsphericity() {
        return vectorMoment.asphericity();
    }

    public double getAcylindricity() {
        return vectorMoment.acylindricity();
    }

    public double getAnisotropy() {
        return vectorMoment.anisotropy();
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(getCellCount());
        builder.append(getComponentCount());
        builder.append(getCMX());
        builder.append(getCMY());
        builder.append(getCMZ());
        builder.append(getRG());
        builder.append(getPMX());
        builder.append(getPMY());
        builder.append(getPMZ());
        builder.append(getAsphericity());
        builder.append(getAcylindricity());
        builder.append(getAnisotropy());

        return builder.toString();
    }

    @Override public String getBaseName() {
        return TumorDimensionReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("timeStep");
        builder.append("cellCount");
        builder.append("componentCount");
        builder.append("cmX");
        builder.append("cmY");
        builder.append("cmZ");
        builder.append("RG");
        builder.append("pmX");
        builder.append("pmY");
        builder.append("pmZ");
        builder.append("asphericity");
        builder.append("acylindricity");
        builder.append("anisotropy");

        return builder.toString();
    }
}
