
package tumor.report;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;

/**
 * Provides a base class for simulation reports that may process data
 * after each completed time step and simulation trial.
 */
public abstract class TumorReport {
    /**
     * Initializes the report at the start of a new simulation trial.
     */
    public abstract void initializeTrial();

    /**
     * Reports the results of the latest completed time step.
     */
    public abstract void reportStep();

    /**
     * Reports the results of the latest completed simulation trial.
     */
    public abstract void reportTrial();

    /**
     * Returns the global driver application.
     *
     * @return the global driver application.
     */
    public TumorDriver<? extends TumorComponent> getDriver() {
        return TumorDriver.global();
    }

    /**
     * Returns the primary tumor under simulation.
     *
     * @return the primary tumor under simulation.
     */
    public Tumor<? extends TumorComponent> getTumor() {
        return getDriver().getTumor();
    }

    /**
     * Returns the primary lattice tumor under simulation.
     *
     * @return the primary lattice tumor under simulation.
     *
     * @throws ClassCastException unless the tumor is actually a
     * lattice tumor.
     */
    public LatticeTumor<? extends TumorComponent> getLatticeTumor() {
        return (LatticeTumor<? extends TumorComponent>) getTumor();
    }

    /**
     * Returns the index of the latest completed time step.
     *
     * @return the index of the latest completed time step.
     */
    public int getTimeStep() {
        return getDriver().getTimeStep();
    }

    /**
     * Returns the index of the latest completed simulation trial.
     *
     * @return the index of the latest completed simulation trial.
     */
    public int getTrialIndex() {
        return getDriver().getTrialIndex();
    }

    /**
     * Identifies time steps that must be reported.
     *
     * @param sampleInterval the report-specific sampling (update)
     * interval (expressed as a number of time steps).
     *
     * @return {@code true} iff the report should be updated for the
     * latest completed time step.
     */
    public boolean isSampleStep(int sampleInterval) {
        return (getTimeStep() > 0) && (getTimeStep() % sampleInterval == 0);
    }
}
