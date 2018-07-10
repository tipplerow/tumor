
package tumor.report;

import java.util.List;

import jam.app.JamLogger;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;

import tumor.report.bulk.BulkSample;
import tumor.report.bulk.BulkSampleSpace;

/**
 * Provides a base class for simulation reports that may process data
 * after each completed time step and simulation trial.
 */
public abstract class TumorReport {
    /**
     * Initializes the report at the start of a new simulation.
     */
    public abstract void initializeSimulation();

    /**
     * Initializes the report at the start of a new simulation trial.
     */
    public abstract void initializeTrial();

    /**
     * Processes the results of the latest completed time step.
     */
    public abstract void processStep();

    /**
     * Reports the results of the latest completed simulation trial.
     */
    public abstract void finalizeTrial();

    /**
     * Reports the results of the completed simulation.
     */
    public abstract void finalizeSimulation();

    /**
     * Collects bulk samples from the primary tumor.
     *
     * @param sampleSpace the spatial distribution of the bulk samples.
     *
     * @param sampleSize the minumum number of tumor cells to include
     * in each bulk sample.
     *
     * @return a list containing the bulk samples.
     */
    public List<BulkSample> collectBulkSamples(BulkSampleSpace sampleSpace, long sampleSize) {
        JamLogger.info("Collecting [%d] bulk tumor samples...", sampleSpace.viewBasis().size());
        return sampleSpace.collect(getLatticeTumor(), sampleSize);
    }

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
        return (sampleInterval > 0) && (getTimeStep() > 0) && (getTimeStep() % sampleInterval == 0);
    }
}
