
package tumor.lattice;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.capacity.CapacityModel;
import tumor.carrier.Carrier;
import tumor.carrier.MultiCellularComponent;
import tumor.carrier.TumorEnv;
import tumor.driver.TumorDriver;
import tumor.growth.GrowthRate;

/**
 * Represents a three-dimensional tumor containing multi-cellular
 * components (demes or lineages) on a lattice.
 *
 * @param <E> the concrete subtype for the tumor components.
 */
public abstract class MultiCellularLatticeTumor<E extends MultiCellularComponent> extends LatticeTumor<E> {
    //
    // A large tumor may contain millions of multi-cellular components.
    // Computing the total number of cells in the tumor by iterating
    // over the components is therefore very inefficient.  Instead we
    // maintain a private cache of the total cell count and update it
    // as necessary.
    //
    private long totalCellCount = 0;

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
        super(parent, lattice, getMaxSiteCount());
    }

    private static long getMaxSiteCount() {
        long maxTumorSize = TumorDriver.global().getMaxTumorSize();
        long siteCapcity  = CapacityModel.global().getMeanCapacity();

        return maxTumorSize / siteCapcity;
    }

    /**
     * Advances a parent component and moves cells to an expansion
     * site if necessary to honor the capacity of the parent site.
     *
     * @param parent the parent component to advance.
     *
     * @param parentCoord the location of the parent component.
     *
     * @param parentFreeCapacity the free capacity at the parent site.
     */
    protected abstract void advanceWithExpansion(E parent, Coord parentCoord, long parentFreeCapacity);

    /**
     * Advances a parent component when the parent site is guaranteed
     * to have sufficient capacity for any offspring.
     *
     * @param parent the parent component to advance.
     *
     * @param parentCoord the location of the parent component.
     *
     * @param parentFreeCapacity the free capacity at the parent site.
     */
    protected abstract void advanceInPlace(E parent, Coord parentCoord, long parentFreeCapacity);

    /**
     * Computes the number of cells that can be accomodated at an
     * expansion site (a site <em>not</em> already containing the
     * parent component).
     *
     * @param expansionCoord the location of the expansion site.
     *
     * @return the number of cells that can be accomodated at the
     * expansion site.
     */
    protected abstract long computeExpansionFreeCapacity(Coord expansionCoord);

    /**
     * Computes the number of cells that can be accomodated at the
     * site of a parent during its advancement.
     *
     * @param parent the parent component to advance.
     *
     * @param parentCoord the location of a parent component.
     *
     * @return the number of cells that can be accomodated at the
     * parent site.
     */
    protected abstract long computeParentFreeCapacity(E parent, Coord parentCoord);

    /**
     * Updates the private cell-count cache when the size of a
     * component (already on the lattice) changes.
     *
     * @param component the component that has changed in size.
     *
     * @param coord the location of the changed component.
     */
    protected void updateComponentCellCount(E component, Coord coord) {
        totalCellCount += component.netChange();
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
        long parentFreeCapacity = computeParentFreeCapacity(parent, parentCoord);

        if (canExceedParentCapacity(parent, parentCoord, parentFreeCapacity))
            advanceWithExpansion(parent, parentCoord, parentFreeCapacity);
        else
            advanceInPlace(parent, parentCoord, parentFreeCapacity);

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
