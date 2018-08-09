
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
     * Extracts a single-cell sample from another tumor component.
     *
     * <p>This is a "virtual" sample: if the input component is a
     * multi-cellular component, its cell count does not actually
     * change.
     *
     * @param component the component to sample.
     *
     * @return a tumor cell with the same genotype and growth rate as
     * the input component.
     */
    public static TumorCell sample(TumorComponent component) {
        return new TumorCell(null, component.getGenotype().forClone(), component.getGrowthRate());
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
        //
        // Only active cells divide...
        //
        if (!isActive())
            return Collections.emptyList();

        // Stochastically sample the event to occur on this step...
        GrowthCount growthCount = resolveGrowthCount(tumorEnv);

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
        die();
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
        die();
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
}
