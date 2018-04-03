
package tumor.global;

import tumor.carrier.TumorCell;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents tumor cells that acquire mutations from the global
 * mutation generator [{@link tumor.mutation.MutationGenerator#global()}].
 */
public final class GlobalCell extends TumorCell {
    /**
     * Creates a founder cell.
     *
     * @param growthRate the intrinsic growth rate of the founder cell.
     *
     * @return the founder cell.
     */
    public static GlobalCell founder(GrowthRate growthRate) {
        return new GlobalCell(growthRate);
    }

    private GlobalCell(GrowthRate growthRate) {
        super(growthRate);
    }

    @Override public MutationGenerator getMutationGenerator() {
        return MutationGenerator.global();
    }

    @Override public GlobalCell newDaughter(MutationList daughterMut) {
        return new GlobalCell(this, daughterMut);
    }
    
    private GlobalCell(GlobalCell parent, MutationList daughterMut) {
        super(parent, daughterMut);
    }
}
