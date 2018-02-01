
package tumor.carrier;

import java.util.Collection;

/**
 * Represents a component within a tumor: a single tumor cell, cell
 * lineage, or deme.
 */
public abstract class TumorComponent extends Carrier {
    /**
     * Creates all tumor components.
     *
     * @param index the ordinal index of the component.
     *
     * @param parent the parent of the new component; {@code null}
     * for a founding component.
     */
    protected TumorComponent(long index, TumorComponent parent) {
        super(index, parent);
    }
    
    /**
     * Advances this tumor component through one discrete time step.
     *
     * <p>After calling this method, the replication state (identified
     * by the {@code getState()} method) may be changed: cells may die.
     *
     * <p>Subclasses are encouraged to change the return type to the
     * most concrete type possible.
     *
     * @param tumor the tumor that contains this component.
     *
     * @return any new components created during the time step.
     */
    public abstract Collection<? extends TumorComponent> advance(Tumor tumor);
}
