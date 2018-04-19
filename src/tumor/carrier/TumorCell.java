
package tumor.carrier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents a single tumor cell.
 */
public class TumorCell extends TumorComponent {
    //
    // Tumor cells are alive when created; the state becomes DEAD
    // during the time-step advancement if a death event occurs.
    //
    private State state = State.ALIVE;

    /**
     * Creates a founding tumor cell containing the unique global
     * mutation list responsible for transformation; the global
     * mutation generator is the source of somatic mutations.
     *
     * @param growthRate the intrinsic growth rate of the founder.
     */
    protected TumorCell(GrowthRate growthRate) {
        super(growthRate);
    }

    /**
     * Creates a daughter cell with new original mutations.
     *
     * @param parent the parent cell.
     *
     * @param daughterMut the mutations originating in the daughter.
     */
    protected TumorCell(TumorCell parent, MutationList daughterMut) {
        super(parent, daughterMut);
    }

    /**
     * Creates a founding tumor cell with the global mutation generator
     * as the source of somatic mutations.
     *
     * <p>Note that any mutations that triggered the transformation to
     * malignancy will be carried by all daughter cells (and therefore
     * may be tracked in the tumor itself), so they do not need to be
     * explicitly specified in the founder cell.
     *
     * @param growthRate the intrinsic growth rate of the founder.
     *
     * @return the founding tumor cell.
     */
    public static TumorCell founder(GrowthRate growthRate) {
        return new TumorCell(growthRate);
    }

    /**
     * Creates founding tumor cells having the global mutation generator
     * as the source of somatic mutations.
     *
     * @param cellCount the number of founders to create.
     *
     * @param growthRate the (identical) intrinsic growth rate of the
     * cells.
     *
     * @return the founding tumor cells.
     */
    public static List<TumorCell> founders(int cellCount, GrowthRate growthRate) {
        List<TumorCell> result = new ArrayList<TumorCell>(cellCount);

        while (result.size() < cellCount)
            result.add(founder(growthRate));

        return result;
    }

    /**
     * Creates a daughter cell with new original mutations.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @return the daughter cell.
     */
    public TumorCell newDaughter(MutationList daughterMut) {
        return new TumorCell(this, daughterMut);
    }

    /**
     * Advances this tumor cell through one discrete time step.
     *
     * @param tumorEnv the local tumor environment where this cell
     * resides.
     *
     * @return a list containing any new tumor cells created by cell
     * division; the list will be empty if this parent cell does not
     * divide in the time step.
     */
    @Override public List<TumorCell> advance(TumorEnv tumorEnv) {
        // Dead cells do not divide...
        if (isDead())
            return Collections.emptyList();

        // Stochastically sample the event to occur on this step...
        long        netCapacity = tumorEnv.getGrowthCapacity();
        GrowthRate  growthRate  = tumorEnv.getGrowthRate();
        GrowthCount growthCount = growthRate.sample(1L, netCapacity);

        assert growthCount.getEventCount() <= 1;

        if (growthCount.getBirthCount() == 1)
            return birthEvent(tumorEnv);
        
        if (growthCount.getDeathCount() == 1)
            return deathEvent();

        // Nothing happenend...
        return Collections.emptyList();
    }

    private List<TumorCell> birthEvent(TumorEnv tumorEnv) {
        //
        // This cell dies and is replaced by two daughters...
        //
        state = State.DEAD;
        return Arrays.asList(newDaughter(tumorEnv), newDaughter(tumorEnv));
    }

    private TumorCell newDaughter(TumorEnv tumorEnv) {
        MutationGenerator mutGenerator = tumorEnv.getMutationGenerator();
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
    
    @Override public final long countCells() {
        return 1L;
    }

    @Override public final State getState() {
        return state;
    }
}
