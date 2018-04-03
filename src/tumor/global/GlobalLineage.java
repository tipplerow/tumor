
package tumor.global;

import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents multi-cell lineages whose component cells acquire
 * mutations from the global mutation generator returned by the 
 * method {@link tumor.mutation.MutationGenerator#global()}.
 */
public final class GlobalLineage extends Lineage {
    /**
     * Creates a founding global lineage.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding lineage.
     *
     * @param cellCount the number of (identical) cells in the founding 
     * lineage.
     *
     * @return the founding global lineage.
     */
    public static GlobalLineage founder(GrowthRate growthRate, long cellCount) {
        return new GlobalLineage(growthRate, cellCount);
    }

    private GlobalLineage(GrowthRate growthRate, long cellCount) {
        super(growthRate, cellCount);
    }

    @Override public MutationGenerator getMutationGenerator() {
        return MutationGenerator.global();
    }

    @Override public GlobalLineage newClone(long cellCount) {
        return new GlobalLineage(this, cellCount);
    }

    private GlobalLineage(GlobalLineage parent, long cellCount) {
        super(parent, cellCount);
    }
    
    @Override public GlobalLineage newDaughter(MutationList daughterMut) {
        return new GlobalLineage(this, daughterMut);
    }
    
    private GlobalLineage(GlobalLineage parent, MutationList daughterMut) {
        super(parent, daughterMut);
    }
}
