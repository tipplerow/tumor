
package tumor.perfect;

import tumor.carrier.Deme;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;

/**
 * Represents demes of perfectly replicating cells.
 */
public final class PerfectDeme extends Deme {
    private PerfectDeme(GrowthRate growthRate, long cellCount) {
        super(growthRate, cellCount);
    }
    
    private PerfectDeme(PerfectDeme parent, long cellCount) {
        super(parent, cellCount);
    }
    
    /**
     * Creates a perfect founding deme.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding deme.
     *
     * @param cellCount the number of (identical) cells in the founding 
     * deme.
     *
     * @return the perfect founding deme.
     */
    public static PerfectDeme founder(GrowthRate growthRate, long cellCount) {
        return new PerfectDeme(growthRate, cellCount);
    }

    @Override public MutationGenerator getMutationGenerator() {
        return MutationGenerator.empty();
    }

    @Override public PerfectDeme newClone(long cellCount) {
        return new PerfectDeme(this, cellCount);
    }
}
