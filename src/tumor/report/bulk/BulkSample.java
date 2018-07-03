
package tumor.report.bulk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import tumor.carrier.Carrier;
import tumor.carrier.TumorComponent;
import tumor.mutation.Genotype;

/**
 * Represents a collection of tumor components collected as a single
 * bulk sample.
 */
public final class BulkSample<E extends TumorComponent> {
    private final Set<E> components;

    // Total number of cells, computed on demand...
    private long cellCount = -1;

    // The common ancestral genotype, computed on demand...
    private Genotype ancestor = null;

    /**
     * Creates a new bulk sample.
     *
     * @param components the sample components.
     */
    public BulkSample(Collection<E> components) {
        // Defensive private copy...
        this.components = Collections.unmodifiableSet(new HashSet<E>(components));
    }

    /**
     * Returns the total number of cells in this sample.
     *
     * @return the total number of cells in this sample.
     */
    public long countCells() {
        if (cellCount < 0)
            cellCount = Carrier.countCells(components);

        return cellCount;
    }

    /**
     * Returns the number of unique components in this sample.
     *
     * @return the number of unique components in this sample.
     */
    public long countComponents() {
        return components.size();
    }

    /**
     * Returns the ancestral genotype with mutations shared by every
     * component in this sample.
     *
     * @return the ancestral genotype with mutations shared by every
     * component in this sample.
     */
    public Genotype getAncestor() {
        if (ancestor == null)
            ancestor = Genotype.ancestor(TumorComponent.getGenotypes(components));

        return ancestor;
    }

    /**
     * Returns a read-only view of the tumor components in this sample.
     *
     * @return a read-only view of the tumor components in this sample.
     */
    public Set<E> viewComponents() {
        return components;
    }
}
