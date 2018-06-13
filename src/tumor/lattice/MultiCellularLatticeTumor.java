
package tumor.lattice;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.capacity.CapacityModel;
import tumor.carrier.Carrier;
import tumor.carrier.TumorComponent;
import tumor.carrier.TumorEnv;
import tumor.growth.GrowthRate;

/**
 * Represents a three-dimensional tumor containing multi-cellular
 * components (demes or lineages) on a lattice.
 *
 * @param <E> the concrete subtype for the tumor components.
 */
public abstract class MultiCellularLatticeTumor<E extends TumorComponent> extends LatticeTumor<E> {
    /**
     * A large tumor may contain millions of multi-cellular components.
     * Computing the total number of cells in the tumor by iterating
     * over the components is therefore very inefficient.  Instead we
     * maintain a private cache of the total cell count and update it
     * as necessary.
     */
    protected long totalCellCount = 0;

    /**
     * Creates a new (empty) tumor.
     *
     * <p>Concrete subclasses must add the founding tumor components
     * by calling {@code addComponent}.  This base class constructor
     * does not assign the founders because {@code addComponent} and
     * {@code seed} are virtual methods and therefore should not be
     * called from the base class constructor.
     *
     * @param parent the parent of the new tumor; {@code null} for a
     * primary tumor.
     *
     * @param lattice the underlying lattice.
     *
     * @throws IllegalArgumentException unless the lattice is empty.
     */
    protected MultiCellularLatticeTumor(MultiCellularLatticeTumor<E> parent, Lattice<E> lattice) {
        super(parent, lattice);
    }

    protected abstract void advanceWithExpansion(E parent, Coord parentCoord, long parentFreeCapacity);

    protected abstract void advanceInPlace(E parent, Coord parentCoord, long growthCapacity);

    protected void updateComponentCellCount(E component, Coord componentCoord, long priorCount) {
        totalCellCount += component.countCells() - priorCount;
    }

    @Override public long countCells() {
        //
        // Enable assertions to check the consistency of the cached
        // cell counts...
        //
        assert totalCellCount == Carrier.countCells(lattice.viewOccupants());
        return totalCellCount;
    }

    @Override public CapacityModel getCapacityModel() {
        return CapacityModel.global();
    }

    @Override protected void addComponent(E component, Coord location) {
        super.addComponent(component, location);
        totalCellCount += component.countCells();
    }

    @Override protected void removeComponent(E component, Coord location) {
        super.removeComponent(component, location);
        totalCellCount -= component.countCells();
    }

    @Override protected void advance(E parent) {
        // Locate the parent...
        Coord parentCoord = locateComponent(parent);

        // Compute the free capacity of the parent site...
        long parentFreeCapacity = getSiteCapacity(parentCoord) - parent.countCells();

        // Save the prior size in order to update the total cell-count
        // cache...
        long priorCount = parent.countCells();

        if (canExceedParentCapacity(parent, parentCoord, parentFreeCapacity))
            advanceWithExpansion(parent, parentCoord, parentFreeCapacity);
        else
            advanceInPlace(parent, parentCoord, parentFreeCapacity);

        // Update the cached cell count for the parent component...
        updateComponentCellCount(parent, parentCoord, priorCount);

        // Remove the parent if it has died...
        if (parent.isDead())
            removeComponent(parent, parentCoord);

        assert satisfiesCapacityConstraint(parentCoord);
    }

    private boolean canExceedParentCapacity(E parent, Coord parentCoord, long parentFreeCapacity) {
        //
        // This is a very loose criterion: examine the maximum
        // possible population growth in the local environment...
        //
        GrowthRate localRate = getLocalGrowthRate(parent);
        long       maxGrowth = localRate.resolveMaximumGrowth(parent.countCells());

        return maxGrowth > parentFreeCapacity;
    }
}
