
package tumor.perfect;

import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents lineages of perfectly replicating cells.
 */
public final class PerfectLineage extends Lineage {
    private PerfectLineage(GrowthRate growthRate, long cellCount) {
        super(growthRate, cellCount);
    }
    
    private PerfectLineage(PerfectLineage parent, long cellCount) {
        super(parent, cellCount);
    }
    
    private PerfectLineage(PerfectLineage parent, MutationList daughterMut) {
        super(parent, daughterMut);
    }
    
    /**
     * Creates a perfect founding lineage.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding lineage.
     *
     * @param cellCount the number of (identical) cells in the founding 
     * lineage.
     *
     * @return the perfect founding lineage.
     */
    public static PerfectLineage founder(GrowthRate growthRate, long cellCount) {
        return new PerfectLineage(growthRate, cellCount);
    }

    @Override public MutationGenerator getMutationGenerator() {
        return MutationGenerator.empty();
    }

    @Override public PerfectLineage newClone(long cellCount) {
        return new PerfectLineage(this, cellCount);
    }

    @Override public PerfectLineage newDaughter(MutationList daughterMut) {
        //
        // Perfect lineages should never produce offspring...
        //
        throw new UnsupportedOperationException("Perfect lineages should never produce offspring...");
    }
}
