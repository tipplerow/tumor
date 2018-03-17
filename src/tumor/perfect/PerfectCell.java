
package tumor.perfect;

import java.util.ArrayList;
import java.util.List;

import tumor.carrier.TumorCell;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents a perfectly replicating cell.
 */
public final class PerfectCell extends TumorCell {
    private PerfectCell(GrowthRate growthRate) {
        super(growthRate);
    }
    
    private PerfectCell(PerfectCell parent, MutationList daughterMut) {
        super(parent, daughterMut);
    }
    
    /**
     * Creates a perfect founder cell.
     *
     * @param growthRate the intrinsic growth rate of the cell.
     *
     * @return the perfect founder cell.
     */
    public static PerfectCell founder(GrowthRate growthRate) {
        return new PerfectCell(growthRate);
    }

    /**
     * Creates perfect founder cells.
     *
     * @param cellCount the number of founders to create.
     *
     * @param growthRate the (identical) intrinsic growth rate of the
     * cells.
     *
     * @return the perfect founder cells.
     */
    public static List<PerfectCell> founders(int cellCount, GrowthRate growthRate) {
        List<PerfectCell> result = new ArrayList<PerfectCell>(cellCount);

        while (result.size() < cellCount)
            result.add(founder(growthRate));

        return result;
    }

    @Override public MutationGenerator getMutationGenerator() {
        return MutationGenerator.empty();
    }

    @Override public PerfectCell newDaughter(MutationList daughterMut) {
        return new PerfectCell(this, daughterMut);
    }
}
