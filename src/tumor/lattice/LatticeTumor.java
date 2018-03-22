
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
    protected final Neighborhood neighborhood;

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
        this.neighborhood = resolveNeighborhood();
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

    private static Neighborhood resolveNeighborhood() {
        return JamProperties.getOptionalEnum(NEIGHBORHOOD_PROPERTY, DEFAULT_NEIGHBORHOOD);
    }

    /**
     * Name of the system property that defines the edge length of the
     * (periodic cubic) lattice.
     */
    public static final String PERIOD_LENGTH_PROPERTY = "LatticeTumor.periodLength";

    /**
     * Returns the period length to be used for the component lattice.
     *
     * <p>This method first looks for the system property named by the
     * {@code PERIOD_LENGTH_PROPERTY} variable and defaults to a value
     * likely to be suitable for the maximum number of components if
     * the property is not set.
     *
     * @param maxSites the number of lattice sites expected to be
     * occupied when the tumor has reached its maximum size (cell
     * count).
     *
     * @return the period length to be used for the component lattice.
     */
    protected static int resolvePeriodLength(long maxSites) {
        return JamProperties.getOptionalInt(PERIOD_LENGTH_PROPERTY, defaultLatticePeriod(maxSites));
    }

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
     * Returns the maximum number of tumor cells that may occupy a
     * given lattice site.
     *
     * @param coord the site to examine.
     *
     * @return the maximum number of tumor cells that may occupy the
     * specified lattice site.
     */
    public abstract long getSiteCapacity(Coord coord);

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
        //
        // This method call will throw an exception if the number of
        // components at the site exceeds the lattice site capacity.
        //
        lattice.occupy(component, location);
        
        // Here we must also verify that the CELL capacity (which will
        // differ for lineage tumors) is also below its limit...
        if (countSiteCells(location) > getSiteCapacity(location))
            throw new IllegalStateException("Exceeded local cell capacity.");
    }

    /**
     * Seeds this tumor with a founding component located at the
     * origin.
     *
     * <p>Concrete base class constructors should call this method to
     * initialize this tumor.
     *
     * @param founder the founding component.
     *
     * @throws IllegalStateException unless the lattice is empty.
     */
    protected void seed(E founder) {
        seed(List.of(founder));
    }

    /**
     * Seeds this tumor with founding components surrounding the
     * origin.
     *
     * <p>Concrete base class constructors should call this method to
     * initialize this tumor.
     *
     * @param founders the founding components.
     *
     * @throws IllegalStateException unless the lattice is empty.
     */
    protected void seed(List<E> founders) {
        if (!lattice.isEmpty())
            throw new IllegalStateException("Cannot seed an occupied lattice.");

        addComponent(founders.get(0), FOUNDER_COORD);

        for (int index = 1; index < founders.size(); ++index) {
            //
            // Use the previous founder as the reference location to
            // place the next founder...
            //
            Coord parentCoord  = locateComponent(founders.get(index - 1));
            E     newComponent = founders.get(index);
            
            addComponent(newComponent, placeComponent(parentCoord, newComponent));
        }
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
     * Finds all neighboring lattice sites that can accomodate a new
     * component.
     *
     * @param center the coordinate of the central site to examine.
     *
     * @param component the new component to be added in the
     * neighborhood surrounding the given site.
     *
     * @return a list containing the coordinates of all neighboring
     * sites that can accomodate the new component without exceeding
     * their capacity (an empty list if there are no available sites).
     */
    public List<Coord> findAvailable(Coord center, E component) {
        List<Coord> neighbors = neighborhood.getNeighbors(center);
        List<Coord> available = new ArrayList<Coord>(neighbors.size());

        for (Coord neighbor : neighbors)
            if (isAvailable(neighbor, component))
                available.add(neighbor);

        return available;
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
     * Identifies lattice sites that can accomodate a new component.
     *
     * @param coord the coordinate of the site to examine.
     *
     * @param component the new component to be added at the given
     * site.
     *
     * @return {@code true} iff the new component can be placed at
     * the specified site without exceeding its capacity.
     */
    public boolean isAvailable(Coord coord, E component) {
        return countSiteCells(coord) + component.countCells() <= getSiteCapacity(coord);
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
     * Determines the location (lattice coordinate) where a new tumor
     * component will be placed.
     *
     * <p>This default implementation places the new component at the
     * parent location <em>if it is available</em> (has sufficient
     * space to accomodate the number of cells in the new component).
     * Otherwise, this method identifies all available sites in the
     * neighborhood surrounding the parent and chooses one at random.
     *
     * @param parentCoord the coordinate of the parent component.
     *
     * @param newComponent the new component to be placed.
     *
     * @return the lattice coordinate to occupied by the new component.
     *
     * @throws IllegalStateException if the lattice does not contain
     * sufficient space around the parent coordinate to place the new
     * component.
     */
    public Coord placeComponent(Coord parentCoord, E newComponent) {
        if (isAvailable(parentCoord, newComponent))
            return parentCoord;

        List<Coord> availCoord = findAvailable(parentCoord, newComponent);

        if (availCoord.isEmpty())
            throw new IllegalStateException("Nowhere to place the new tumor component.");

        return ListUtil.select(availCoord, JamRandom.global());
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
        // Iterate over the active (living) tumor components in a
        // randomized order...
        //
        List<E> shuffled = shuffleComponents();

        migrate(shuffled);
        advance(shuffled);

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
     * Implement a model of migration within the tumor: allow tumor
     * components to move from one site to another.
     *
     * @param components the active (living) tumor components arranged
     * in a randomized order.
     */
    protected void migrate(List<E> components) {
    }

    /**
     * Advances each component in this tumor by one discrete time
     * step.
     *
     * @param components the active (living) tumor components arranged
     * in a randomized order.
     */
    protected void advance(List<E> components) {
        for (E component : components)
            advance(component);
    }

    /**
     * Advances a single component in this tumor by one discrete time
     * step.
     *
     * @param parent the parent component to advance.
     */
    protected void advance(E parent) {
        //
        // Save the location of the parent for placement of the
        // children...
        //
        Coord parentCoord = locateComponent(parent);

        @SuppressWarnings("unchecked")
            Collection<E> children =
            (Collection<E>) parent.advance(this);

        // Remove dead parents before placing the children, since one
        // or more of the children may need to occupy that location...
        if (parent.isDead())
            removeComponent(parent);

        for (E child : children)
            addComponent(child, placeComponent(parentCoord, child));
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
