
package tumor.system;

import tumor.carrier.TumorCell;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents tumor cells that acquire mutations from the global
 * mutation generator [{@link tumor.mutation.MutationGenerator#global()}].
 */
public final class SystemCell extends TumorCell {
    /**
     * Creates a founder cell.
     *
     * @param growthRate the intrinsic growth rate of the founder cell.
     *
     * @return the founder cell.
     */
    public static SystemCell founder(GrowthRate growthRate) {
        return new SystemCell(growthRate);
    }

    private SystemCell(GrowthRate growthRate) {
        super(growthRate);
    }

    @Override public MutationGenerator getMutationGenerator() {
        return MutationGenerator.global();
    }

    @Override public SystemCell newDaughter(MutationList daughterMut) {
        return new SystemCell(this, daughterMut);
    }
    
    private SystemCell(SystemCell parent, MutationList daughterMut) {
        super(parent, daughterMut);
    }
}
