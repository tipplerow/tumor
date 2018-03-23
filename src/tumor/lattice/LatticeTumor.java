
package tumor.lattice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jam.app.JamProperties;
import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.lattice.LatticeView;
import jam.lattice.Neighborhood;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.capacity.CapacityModel;
import tumor.carrier.Carrier;
import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;

/**
 * Represents a three-dimensional tumor whose components occupy sites
 * on a lattice.
 *
 * @param <E> the concrete subtype for the tumor components.
 *
 * <p><b>Lattice configuration parameters.</b> The lattice structure is
 * defined by the following system properties:
 */
public abstract class LatticeTumor<E extends TumorComponent> extends Tumor<E> {
    /**
     * The underlying lattice of components.
     */
    protected final Lattice<E> lattice;

    /**
     * The nearest neighbors on the lattice.
     */
    protected final Neighborhood neighborhood = resolveNeighborhood();

    /**
     * The site capacity model.
     */
    protected final CapacityModel capacityModel = CapacityModel.global();

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
    protected LatticeTumor(LatticeTumor<E> parent, Lattice<E> lattice) {
        super(parent);

        validateLattice(lattice);
        this.lattice = lattice;
    }

    private static void validateLattice(Lattice<?> lattice) {
        if (!lattice.isEmpty())
            throw new IllegalArgumentException("Initial lattice is not empty.");
    }

    /**
     * The coordinate of the founding component.
     */
    public static final Coord FOUNDER_COORD = Coord.ORIGIN;

    /**
     * Name of the system property that defines the nearest neighbors
     * on the lattice.
     */
    public static final String NEIGHBORHOOD_PROPERTY = "LatticeTumor.neighborhood";

    /**
     * Default value for the nearest-neighbor type.
     */
    public static final Neighborhood DEFAULT_NEIGHBORHOOD = Neighborhood.MOORE;

    /**
     * Name of the system property that defines the edge length of the
     * (periodic cubic) lattice.
     */
    public static final String PERIOD_LENGTH_PROPERTY = "LatticeTumor.periodLength";

    /**
     * Computes a lattice period that is safely large enough for
     * simulations of single tumors with a given maximum size.
     *
     * @param maxSites the number of lattice sites expected to be
     * occupied when the tumor has reached its maximum size (cell
     * count).
     *
     * @return a lattice period sufficient for the simulation of
     * tumors with the specified maximum occupancy.
     *
     * @throws IllegalArgumentException if the lattice period would
     * exceed the maximum integer value.
     */
    public static int defaultLatticePeriod(long maxSites) {
        //
        // Ten times the diameter of a perfectly spherical object
        // should be sufficient to contain the tumor without any
        // chance of conflict with periodic images.
        //
        long radius = estimateRadius(maxSites);
        long period = 2 * 10 * radius;

        // Lattice periods are specified with an "int", not a "long".
        // We will never simulate tumors that require a lattice with a
        // period greater than the maximum integer value, so it should
        // be safe to downcast to an "int".
        if (period > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Tumor size is too large.");
        
        return (int) period;
    }

    /**
     * Computes the approximate radius of a spherical tumor with a
     * given number of occupied lattice sites.
     *
     * @param siteCount the number of occupied lattice sites.
     *
     * @return the approximate radius of the tumor.
     */
    public static long estimateRadius(long siteCount) {
        //
        // Return the radius of a sphere with volume equal to the
        // number of components...
        //
        return Math.round(Math.cbrt(0.75 * ((double) siteCount) / Math.PI));
    }

    /**
     * Reads the enumerated nearest-neighbor type from the system
     * property named {@code NEIGHBORHOOD_PROPERTY}.
     *
     * @return the enumerated nearest-neighbor type.
     */
    private static Neighborhood resolveNeighborhood() {
        return JamProperties.getRequiredEnum(NEIGHBORHOOD_PROPERTY, Neighborhood.class);
    }

    /**
     * Reads the period length to be used for the component lattice
     * from the system property named {@code PERIOD_LENGTH_PROPERTY}.
     *
     * @return the period length to be used for the component lattice.
     *
     * @throws RuntimeException unless the required system property is
     * defined.
     */
    public static int resolvePeriodLength() {
        return JamProperties.getRequiredInt(PERIOD_LENGTH_PROPERTY);
    }

    /**
     * Advances a single component in this tumor by one discrete time
     * step and adds any offspring to this tumor.
     *
     * @param component the component to advance.
     */
    protected abstract void advance(E component);

    /**
     * Adds a component to this tumor.
     *
     * <p>This default method simply adds the component to the lattice
     * at the specified location.  Subclasses may also want to store
     * the the initial location to trace the migration of components
     * and mutations through the tumor.
     *
     * @param component the component to add.
     *
     * @param location the location where the component will be added.
     *
     * @throws IllegalStateException if the lattice site would exceed
     * its cell capacity after adding the component.
     */
    protected void addComponent(E component, Coord location) {
        if (isAvailable(location, component))
            lattice.occupy(component, location);
        else
            throw new IllegalStateException("Exceeded local site capacity.");
    }

    /**
     * Removes a component from this tumor.
     *
     * <p>This default method simply removes the component from the
     * lattice and takes no other action.  Subclasses may also want
     * to store the last occupied location of the component to trace
     * the migration of components and mutations through the tumor.
     *
     * @param component the component to remove.
     */
    protected void removeComponent(E component) {
        lattice.vacate(component);
    }

    /**
     * Identifies lattice sites that can accomodate a new component.
     *
     * @param coord the coordinate of the site to examine.
     *
     * @param component the component to be added at the given site.
     *
     * @return {@code true} iff the component can be placed at the
     * specified site without exceeding the capacity of that site.
     */
    public abstract boolean isAvailable(Coord coord, E component);

    /**
     * Returns the total number of tumor cells present at a given
     * lattice site <em>and all of its neighboring sites</em>.
     *
     * @param coord the coordinate to examine.
     *
     * @return the total number of tumor cells present at the
     * specified lattice site and all of its neighboring sites.
     */
    public long countNeighborhoodCells(Coord coord) {
        long result = countSiteCells(coord);

        for (Coord neighbor : getNeighbors(coord))
            result += countSiteCells(neighbor);
        
        return result;
    }

    /**
     * Returns the total number of tumor cells present at a given
     * lattice site.
     *
     * @param coord the coordinate to examine.
     *
     * @return the total number of tumor cells present at the
     * specified lattice site.
     */
    public long countSiteCells(Coord coord) {
        return Carrier.countCells(lattice.viewOccupants(coord));
    }

    /**
     * Returns the nearest-neighbor type.
     *
     * @return the nearest-neighbor type.
     */
    public Neighborhood getNeighborhood() {
        return neighborhood;
    }

    /**
     * Returns the coordinates of the nearest neighbors to a given
     * site.
     *
     * @param coord the site to examine.
     *
     * @return the coordinates of the nearest neighbors to the
     * specified site.
     */
    public List<Coord> getNeighbors(Coord coord) {
        return neighborhood.getNeighbors(coord);
    }

    /**
     * Returns the maximum number of tumor cells that may occupy a
     * given lattice site <em>and all of its neighboring sites</em>.
     *
     * @param coord the site to examine.
     *
     * @return the maximum number of tumor cells that may occupy the
     * specified lattice site and all of its neighboring sites.
     */
    public long getNeighborhoodCapacity(Coord coord) {
        long result = getSiteCapacity(coord);

        for (Coord neighbor : getNeighbors(coord))
            result += getSiteCapacity(neighbor);

        return result;
    }

    /**
     * Returns the maximum number of tumor cells that may occupy a
     * given lattice site.
     *
     * @param coord the site to examine.
     *
     * @return the maximum number of tumor cells that may occupy the
     * specified lattice site.
     */
    public long getSiteCapacity(Coord coord) {
        return capacityModel.getSiteCapacity(coord);
    }

    /**
     * Returns the maximum number of tumor cells that may occupy the
     * lattice site occupied by a specified component.
     *
     * @param component the component located at the site of interest.
     *
     * @return the maximum number of tumor cells that may occupy the
     * lattice site occupied by the specified component.
     *
     * @throws IllegalArgumentException unless the component is a
     * member of this tumor.
     */
    public long getSiteCapacity(E component) {
        return getSiteCapacity(locateComponent(component));
    }

    /**
     * Returns the location of a component in this tumor.
     *
     * @param component the component of interest.
     *
     * @return the location of the specified component.
     *
     * @throws IllegalArgumentException unless the component is a
     * member of this tumor.
     */
    public Coord locateComponent(E component) {
        Coord coord = lattice.locate(component);

        if (coord == null)
            throw new IllegalArgumentException("Component is not present in the tumor.");

        return coord;
    }

    /**
     * Returns a read-only view of the underlying lattice.
     *
     * @return a read-only view of the underlying lattice.
     */
    public LatticeView<E> viewLattice() {
        return lattice;
    }

    @Override public Collection<Tumor<E>> advance() {
        //
        // Advance the active (living) tumor components in a
        // randomized order...
        //
        List<E> shuffled = shuffleComponents();

        for (E component : shuffled)
            advance(component);

        // This base class never divides...
        return Collections.emptyList();
    }

    /**
     * Shuffles the active (living) components of this tumor into an
     * unbiased randomized order.
     *
     * @return a new list containing the active (living) components of
     * this tumor in an unbiased randomized order.
     */
    protected List<E> shuffleComponents() {
        List<E> shuffled = new ArrayList<E>(viewComponents());
        ListUtil.shuffle(shuffled, JamRandom.global());
        
        return shuffled;
    }

    /**
     * Returns the local growth <em>capacity</em> for a given tumor
     * component: the maximum number of new cells that the tumor can
     * support in the local environment surrounding the component.
     *
     * <p>This default implementation returns the difference between
     * the total capacity and the total occupancy in the neighborhood
     * around the component.
     *
     * @param component a component of this tumor.
     *
     * @return the local growth capacity.
     *
     * @throws IllegalArgumentException unless the component is a
     * member of this tumor.
     */
    @Override public long getLocalGrowthCapacity(TumorComponent component) {
        @SuppressWarnings("unchecked")
            Coord coord = locateComponent((E) component);
        
        long capacity = getNeighborhoodCapacity(coord);
        long occupancy = countNeighborhoodCells(coord);

        return capacity - occupancy;
    }
            
    /**
     * Returns the local growth rate for a tumor component.
     *
     * <p>This default implementation simply returns the intrinsic
     * growth rate for the component (applies no adjustment for the
     * local environment).
     *
     * @param component a component of this tumor.
     *
     * @return the intrinsic growth rate for the component.
     */
    @Override public GrowthRate getLocalGrowthRate(TumorComponent component) {
        return component.getGrowthRate();
    }
            
    /**
     * Returns the local mutation generator for a tumor component.
     *
     * <p>This default implementation simply returns the intrinsic
     * mutation generator for the component (applies no adjustment
     * for the local environment).
     *
     * @param component a component of this tumor.
     *
     * @return the intrinsic mutation generator for the component.
     */
    @Override public MutationGenerator getLocalMutationGenerator(TumorComponent component) {
        return component.getMutationGenerator();
    }
    
    @Override public Set<E> viewComponents() {
        return lattice.viewOccupants();
    }
}
