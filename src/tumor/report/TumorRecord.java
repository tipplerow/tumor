
package tumor.report;

import jam.sim.StepRecord;

import tumor.driver.TumorDriver;

/**
 * Provides a base class for all report records with the trial index
 * and time step set automatically at the time of collection.
 */
public abstract class TumorRecord extends StepRecord {
    /**
     * Creates a new tumor record with the trial index and time step
     * assigned from the global driver application.
     */
    protected TumorRecord() {
        super(TumorDriver.global().getTrialIndex(), TumorDriver.global().getTimeStep());
    }
}
