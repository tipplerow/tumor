
package tumor.report.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tumor.driver.TumorDriver;

/**
 * Generates and stores tumor dimension records during a simulation.
 */
public final class TumorDimensionCache {
    private static final Map<RecordKey, TumorDimensionRecord> records =
        new HashMap<RecordKey, TumorDimensionRecord>();

    private TumorDimensionCache() {
    }

    private static final class RecordKey {
        private final int trialIndex;
        private final int timeStep;
        private final int hashCode;

        private RecordKey(int trialIndex, int timeStep) {
            this.trialIndex = trialIndex;
            this.timeStep   = timeStep;
            this.hashCode   = Objects.hash(trialIndex, timeStep);
        }

        @Override public boolean equals(Object obj) {
            return (obj instanceof RecordKey) && equalsKey((RecordKey) obj);
        }

        private boolean equalsKey(RecordKey that) {
            return this.trialIndex == that.trialIndex && this.timeStep == that.timeStep;
        }

        @Override public int hashCode() {
            return hashCode;
        }
    }

    /**
     * Generates and stores the tumor dimension record for the active
     * tumor at this instant in the simulation.
     *
     * @return the tumor dimension record describing the active tumor
     * at this instant in the simulation.
     */
    public static TumorDimensionRecord snap() {
        RecordKey key = snapKey();
        TumorDimensionRecord record = records.get(key);

        if (record == null) {
            record = TumorDimensionRecord.snap();
            records.put(key, record);
        }

        return record;
    }

    private static RecordKey snapKey() {
        return new RecordKey(TumorDriver.global().getTrialIndex(), TumorDriver.global().getTimeStep());
    }

    /**
     * Retrieves tumor dimension records from the cache.
     *
     * <p>If the trial index and time step match the state of the
     * active simulation, the record will be generated if it is not
     * already present in the cache.
     *
     * @param trialIndex the index of the simulation trial at the time
     * of collection.
     *
     * @param timeStep the time step at the time of collection.
     *
     * @return the dimension record for the given trial, time step,
     * and tumor.
     *
     * @throws IllegalStateException if the requested record was not
     * previously generated and cannot be generated in the current
     * simulation state (the trial index and time step do not match
     * the current state of the active simulation).
     */
    public static TumorDimensionRecord require(int trialIndex, int timeStep) {
        RecordKey key = new RecordKey(trialIndex, timeStep);
        TumorDimensionRecord record = records.get(key);

        if (record != null) {
            //
            // Return previously generated record...
            //
            return record;
        }
        else if (key.equals(snapKey())) {
            //
            // Generate, store, and return...
            //
            record = TumorDimensionRecord.snap();
            records.put(key, record);

            return record;
        }
        else {
            //
            // No matching record and it is too late to generate
            // one...
            //
            throw new IllegalStateException(String.format("Record [%d:%d] was not generated.", trialIndex, timeStep));
        }
    }
}
