
package tumor.carrier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jam.lang.OrdinalIndex;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents a single tumor cell.
 */
public abstract class TumorCell extends TumorComponent {
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
     */
    protected TumorCell(GrowthRate growthRate) {
        this(null, growthRate, MutationList.EMPTY);
    }

    /**
     * Creates a daughter cell.
     *
     * @param parent the parent cell.
     *
     * @param daughterMut the mutations originating in the daughter.
     */
    protected TumorCell(TumorCell parent, MutationList daughterMut) {
        this(parent, parent.computeDaughterGrowthRate(daughterMut), daughterMut);
    }

    /**
     * Creates a daughter cell with new original mutations.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @return the daughter cell.
     */
    public abstract TumorCell newDaughter(MutationList daughterMut);

    /**
     * Advances this tumor cell through one discrete time step.
     *
     * @param tumor the tumor in which this cell resides.
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
        GrowthRate  growthRate  = tumor.getLocalGrowthRate(this);
        GrowthCount growthCount = growthRate.sample(1);

        assert growthCount.getEventCount() <= 1;

        if (growthCount.getBirthCount() == 1)
            return birthEvent(tumor);
        
        if (growthCount.getDeathCount() == 1)
            return deathEvent();

        // Nothing happenend...
        return Collections.emptyList();
    }

    private List<TumorCell> birthEvent(Tumor tumor) {
        //
        // This cell dies and is replaced by two daughters...
        //
        state = State.DEAD;
        return Arrays.asList(newDaughter(tumor), newDaughter(tumor));
    }

    private TumorCell newDaughter(Tumor tumor) {
        MutationGenerator mutGenerator = tumor.getLocalMutationGenerator(this);
        MutationList      daughterMut  = mutGenerator.generate();
        
        return newDaughter(daughterMut);
    }

    private List<TumorCell> deathEvent() {
        //
        // This cell dies without reproducing...
        //
        state = State.DEAD;
        return Collections.emptyList();
    }
    
    @Override public long countCells() {
        return 1L;
    }

    @Override public State getState() {
        return state;
    }
}
