
package tumor.carrier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jam.lang.OrdinalIndex;
import jam.math.Probability;

import tumor.mutation.MutationList;

/**
 * Represents a collection of cell lineages located very close
 * together, e.g., at the same lattice site.
 */
public abstract class Deme extends TumorComponent {
    private final Set<Lineage> live = new HashSet<Lineage>();
    private final Set<Lineage> dead = new HashSet<Lineage>();

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * Creates a new deme originating with a single founding lineage.
     *
     * @param lineage the founding lineage.
     */
    protected Deme(Lineage lineage) {
        this(null, Arrays.asList(lineage));
    }

    /**
     * Creates a cloned deme with a collection of divided lineages.
     *
     * @param parent the parent deme.
     *
     * @param lineages the divided lineages.
     */
    protected Deme(Deme parent, Collection<Lineage> lineages) {
        super(ordinalIndex.next(), parent);
        updateLinages(lineages);
    }

    private void updateLinages(Collection<Lineage> lineages) {
        for (Lineage lineage : lineages)
            updateLineage(lineage);
    }

    private void updateLineage(Lineage lineage) {
        switch (lineage.getState()) {
        case ALIVE:
            live.add(lineage);
            dead.remove(lineage);
            break;

        case DEAD:
            dead.add(lineage);
            live.remove(lineage);
            break;

        default:
            throw new IllegalStateException("Lineage must be alive or dead.");
        }
    }

    // Probability that a dividing lineage retains a component cell...
    private static final Probability RETENTION_PROB = Probability.ONE_HALF;

    /**
     * Returns the maximum number of tumor cells that can be contained
     * in this deme.
     *
     * <p>When the deme is larger than this size, it must either (1)
     * divide into two demes if the local environment permits (e.g.,
     * if adjacent space is available), or (2) shrink in size via cell
     * death.
     *
     * @return the maximum number of tumor cells that can be contained
     * in this deme.
     */
    public abstract long getMaximumSize();

    /**
     * Creates a cloned deme with lineages that have divided in the
     * parent.
     *
     * @param lineages the divided lineages transferred to the clone.
     *
     * @return the cloned deme.
     */
    public abstract Deme newClone(Collection<Lineage> lineages);

    /**
     * Returns the total number of lineages (live and dead) in this
     * deme.
     *
     * @return the total number of lineages (live and dead) in this
     * deme.
     */
    public int countLineages() {
        return live.size() + dead.size();
    }

    /**
     * Returns the number of dead lineages in this deme.
     *
     * @return the number of dead lineages in this deme.
     */
    public int countDeadLineages() {
        return dead.size();
    }

    /**
     * Returns the number of live lineages in this deme.
     *
     * @return the number of live lineages in this deme.
     */
    public final int countLiveLineages() {
        return live.size();
    }

    /**
     * Identifies empty (extinguished or dead) demes.
     *
     * @return {@code true} iff there are no cells remaining in this
     * deme.
     */
    public boolean isEmpty() {
        return countCells() == 0;
    }

    /**
     * Returns a read-only set view of the lineages contained within
     * this deme.
     *
     * @return a read-only set view of the lineages contained within
     * this deme.
     */
    public Set<Lineage> viewLineages() {
        Set<Lineage> all = new HashSet<Lineage>();

        all.addAll(live);
        all.addAll(dead);

        return Collections.unmodifiableSet(all);
    }

    /**
     * Returns a read-only set view of the dead lineages contained
     * within this deme.
     *
     * @return a read-only set view of the dead lineages contained
     * within this deme.
     */
    public Set<Lineage> viewDeadLineages() {
        return Collections.unmodifiableSet(dead);
    }

    /**
     * Returns a read-only set view of the live lineages contained
     * within this deme.
     *
     * @return a read-only set view of the live lineages contained
     * within this deme.
     */
    public Set<Lineage> viewLiveLineages() {
        return Collections.unmodifiableSet(live);
    }

    /**
     * Advances this deme through one discrete time step.
     *
     * @param tumorEnv the local environment where this deme resides.
     *
     * @return a list with a single daughter if this deme exceeded its
     * maximum allowed size and the local tumor environment allowed
     * deme division; otherwise, an empty list.
     */
    @Override public List<Deme> advance(TumorEnv tumorEnv) {
        TumorEnv lineageEnv = getLineageEnv(tumorEnv);

        List<Lineage> deadParents   = new LinkedList<Lineage>();
        List<Lineage> liveDaughters = new LinkedList<Lineage>();

        for (Lineage parent : live) {
            liveDaughters.addAll(parent.advance(lineageEnv));

            if (parent.isDead())
                deadParents.add(parent);
        }

        updateLinages(deadParents);
        updateLinages(liveDaughters);

        if (mustDivide(tumorEnv))
            return fissionList(lineageEnv);
        else
            return Collections.emptyList();
    }

    private TumorEnv getLineageEnv(TumorEnv demeEnv) {
        if (mustRestrictLineageGrowth(demeEnv))
            return demeEnv.noGrowth();
        else
            return demeEnv;
    }

    private boolean mustRestrictLineageGrowth(TumorEnv demeEnv) {
        //
        // Lineage growth must be restricted if (1) the local
        // environment does not allow the deme to divide, and 
        // (2) the deme exceeds its maximum allowed size...
        //
        return !demeEnv.allowDemeDivision() && exceedsMaximumSize(demeEnv);
    }

    private boolean exceedsMaximumSize(TumorEnv demeEnv) {
        return countCells() > demeEnv.getLocalMaximumDemeSize(this);
    }

    private boolean mustDivide(TumorEnv demeEnv) {
        //
        // This deme must divide if (1) the local environment allows
        // deme division, and (2) this deme exceeds its maximum
        // allowed size...
        //
        return demeEnv.allowDemeDivision() && exceedsMaximumSize(demeEnv);
    }

    private List<Deme> fissionList(TumorEnv lineageEnv) {
        Deme daughter = divide(lineageEnv);

        if (daughter != null)
            return Arrays.asList(daughter);
        else
            return Collections.emptyList();
    }

    private Deme divide(TumorEnv lineageEnv) {
        Set<Lineage> fissionList = new HashSet<Lineage>();
        List<Lineage> deadParents = new LinkedList<Lineage>();

        for (Lineage lineage : live) {
            Lineage fission = lineage.divide(RETENTION_PROB);

            if (lineage.isDead())
                deadParents.add(lineage);

            if (fission != null)
                fissionList.add(fission);
        }

        updateLinages(deadParents);

        if (fissionList.isEmpty())
            return null;
        else
            return newClone(fissionList);
    }

    @Override public State getState() {
        return isEmpty() ? State.DEAD : State.ALIVE;
    }

    @Override public long countCells() {
        int cellCount = 0;

        for (Lineage lineage : live)
            cellCount += lineage.countCells();

        return cellCount;
    }

    @Override public MutationList getOriginalMutations() {
        //return MutationList.cat(accumulateMutations(live), accumulateMutations(dead));
        return accumulateMutations(live);
    }
}
