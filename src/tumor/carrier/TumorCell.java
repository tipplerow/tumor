
package tumor.carrier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jam.lang.OrdinalIndex;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a single tumor cell.
 */
public final class TumorCell extends UniformComponent {
    //
    // Tumor cells are alive when created; the state becomes DEAD
    // during the time-step advancement if a death event occurs.
    //
    private State state = State.ALIVE;

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    private TumorCell(TumorCell parent, GrowthRate growthRate, MutationList originalMut) {
        super(ordinalIndex.next(), parent, growthRate, originalMut);
    }

    /**
     * Creates a founding tumor cell.
     *
     * <p>Note that any mutations that triggered the transformation to
     * malignancy will be carried by all daughter cells (and therefore
     * may be tracked in the tumor itself), so they do not need to be
     * explicitly specified in the founder cell.
     *
     * @param growthRate the intrinsic growth rate of the founder.
     *
     * @return the new founding tumor cell.
     */
    public static TumorCell founder(GrowthRate growthRate) {
        return new TumorCell(null, growthRate, MutationList.EMPTY);
    }

    /**
     * Advances this tumor cell through one discrete time step.
     *
     * @param tumor the tumor that contains this cell.
     *
     * @return a list containing any new tumor cells created by cell
     * division; the list will be empty if this parent cell does not
     * divide in the time step.
     */
    @Override public List<TumorCell> advance(Tumor tumor) {
        // Dead cells do not divide...
        if (isDead())
            return Collections.emptyList();

        // Stochastically sample the event to occur on this step...
        GrowthRate  growthRate  = tumor.adjustGrowthRate(this);
        GrowthCount growthCount = growthRate.sample(1);

        assert growthCount.getEventCount() <= 1;

        if (growthCount.getBirthCount() == 1)
            return advanceBirth(tumor);
        else if (growthCount.getDeathCount() == 1)
            return advanceDeath();
        else
            return Collections.emptyList(); // Nothing happened...
    }

    private List<TumorCell> advanceBirth(Tumor tumor) {
        //
        // This cell dies and is replaced by two daughters...
        //
        state = State.DEAD;
        return Arrays.asList(daughter(tumor), daughter(tumor));
    }

    private TumorCell daughter(Tumor tumor) {
        MutationList daughterMut  = tumor.generateMutations(this);
        GrowthRate   daughterRate = computeDaughterGrowthRate(daughterMut);
        
        return new TumorCell(this, daughterRate, daughterMut);
    }

    private List<TumorCell> advanceDeath() {
        //
        // This cell dies without reproducing...
        //
        state = State.DEAD;
        return Collections.emptyList();
    }
    
    @Override public int countCells() {
        return 1;
    }

    @Override public State getState() {
        return state;
    }
}
