
package tumor.carrier;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents multi-cell lineages whose component cells acquire
 * mutations from the global mutation generator returned by the 
 * method {@link tumor.mutation.MutationGenerator#global()}.
 */
public final class SystemLineage extends Lineage {
    /**
     * Creates a founding system lineage.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding lineage.
     *
     * @param cellCount the number of (identical) cells in the founding 
     * lineage.
     *
     * @return the founding system lineage.
     */
    public static SystemLineage founder(GrowthRate growthRate, long cellCount) {
        return new SystemLineage(growthRate, cellCount);
    }

    private SystemLineage(GrowthRate growthRate, long cellCount) {
        super(growthRate, cellCount);
    }

    @Override public MutationGenerator getMutationGenerator() {
        return MutationGenerator.global();
    }

    @Override public SystemLineage newClone(long cellCount) {
        return new SystemLineage(this, cellCount);
    }

    private SystemLineage(SystemLineage parent, long cellCount) {
        super(parent, cellCount);
    }
    
    @Override public SystemLineage newDaughter(MutationList daughterMut) {
        return new SystemLineage(this, daughterMut);
    }
    
    private SystemLineage(SystemLineage parent, MutationList daughterMut) {
        super(parent, daughterMut);
    }
}
