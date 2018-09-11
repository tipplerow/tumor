
package tumor.report;

import java.text.DecimalFormat;

import jam.report.LineBuilder;
import jam.sim.StepRecord;
import jam.vector.JamVector;

import tumor.driver.TumorDriver;

/**
 * Provides a base class for all report records with the trial index
 * and time step set automatically at the time of collection.
 */
public abstract class TumorRecord extends StepRecord {
    private static final DecimalFormat RADIAL_VECTOR_FORMAT = new DecimalFormat("#0.0###");
    
    /**
     * Creates a new tumor record with the trial index and time step
     * assigned from the global driver application.
     */
    protected TumorRecord() {
        super(TumorDriver.global().getTrialIndex(), TumorDriver.global().getTimeStep());
    }

    /**
     * Appends a radial vector to an output line.
     *
     * @param builder the builder for the output line.
     *
     * @param vector the vector to append.
     */
    public void appendRadialVector(LineBuilder builder, JamVector vector) {
        builder.append(vector.getDouble(0), RADIAL_VECTOR_FORMAT);
        builder.append(vector.getDouble(1), RADIAL_VECTOR_FORMAT);
        builder.append(vector.getDouble(2), RADIAL_VECTOR_FORMAT);
    }
}
