
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.FixedGenotype;
import tumor.mutation.Genotype;
import tumor.mutation.Mutation;
import tumor.mutation.MutationGenerator;

/**
 * Represents a single tumor cell.
 */
public final class TumorCell extends TumorComponent {
    private final GrowthRate growthRate;

    // Tumor cells are alive when created; the state becomes DEAD
    // during the time-step advancement if a death event occurs.
    private State state = State.ALIVE;

    private TumorCell(TumorCell parent, Genotype genotype, GrowthRate growthRate) {
        super(parent, genotype);
        this.growthRate = growthRate;
    }

    /**
     * Creates a founding tumor cell containing the mutations
     * responsible for transformation to malignancy.
     *
     * @param growthRate the intrinsic growth rate of the founder.
     *
     * @return the founding tumor cell.
     */
    public static TumorCell founder(GrowthRate growthRate) {
        return new TumorCell(null, FixedGenotype.TRANSFORMER, growthRate);
    }

    /**
     * Creates founding tumor cells containing the mutations
     * responsible for transformation to malignancy.
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
        GrowthCount growthCount = growthRate.sampleCount(1L, netCapacity);

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
        return List.of(newDaughter(tumorEnv), newDaughter(tumorEnv));
    }

    private TumorCell newDaughter(TumorEnv tumorEnv) {
        MutationGenerator mutGenerator = tumorEnv.getMutationGenerator();
        List<Mutation>    daughterMut  = mutGenerator.generateCellMutations();
        Genotype          daughterType = genotype.forDaughter(daughterMut);
        GrowthRate        daughterRate = Mutation.apply(growthRate, daughterMut);
        
        return new TumorCell(this, daughterType, daughterRate);
    }

    private List<TumorCell> deathEvent() {
        //
        // This cell dies without reproducing...
        //
        state = State.DEAD;
        return Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    @Override public List<TumorCell> advance(TumorEnv tumorEnv, int timeSteps) {
        return (List<TumorCell>) super.advance(tumorEnv, timeSteps);
    }

    @Override public final long countCells() {
        return 1L;
    }

    @Override public GrowthRate getGrowthRate() {
        return growthRate;
    }

    @Override public final State getState() {
        return state;
    }
}
