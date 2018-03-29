
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
import tumor.carrier.TumorEnv;
import tumor.growth.GrowthRate;
import tumor.growth.LocalGrowthModel;
import tumor.migrate.MigrationModel;
import tumor.migrate.MigrationType;
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
     * The local growth rate model.
     */
    protected final LocalGrowthModel localGrowthModel = LocalGrowthModel.global();

    /**
     * The component migration model.
     */
    protected final MigrationModel migrationModel = MigrationModel.global();

    /**
     * The random number generator.
     */
    protected final JamRandom randomSource = JamRandom.global();

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
     * Divides a multi-cellular parent component by creating a clone
     * of the parent.
     *
     * @param parent the multi-cellular parent component.
     *
     * @param minCloneCellCount the minimum number of cells to move to
     * the new clone.
     *
     * @param maxCloneCellCount the maximum number of cells to move to
     * the new clone.
     *
     * @return the new clone.
     *
     * @throws UnsupportedOperationException if the tumor components
     * are indivisible (individual tumor cells).
     */
    protected abstract E divideParent(E parent, long minCloneCellCount, long maxCloneCellCount);

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
     * Moves a tumor component from one site to another.
     *
     * @param component the tumor component to move.
     *
     * @param newCoord the new coordinate for the component.
     *
     * @throws IllegalStateException unless the lattice can accomodate
     * the component at the new location.
     */
    protected void moveComponent(E component, Coord newCoord) {
        lattice.vacate(component);

        if (isAvailable(newCoord, component))
            lattice.occupy(component, newCoord);
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
    public boolean isAvailable(Coord coord, E component) {
        return component.countCells() <= computeExcessCapacity(coord);
    }

    /**
     * Computes the number of <em>additional</em> tumor cells that a
     * lattice site can accomodate: the difference between its total
     * capacity and the number of cells currently occupying that site.
     *
     * @param coord the site to examine.
     *
     * @return the excess capacity of the specified lattice site.
     */
    public long computeExcessCapacity(Coord coord) {
        return getSiteCapacity(coord) - countCells(coord);
    }

    /**
     * Computes the local growth capacity: the excess capacity at the
     * site of a componenet and an expansion site that has been chosen
     * to accomodate any offspring.
     *
     * @param componentCoord the coordinate of a tumor component.
     *
     * @param expansionCoord the coordinate of the expansion site.
     *
     * @return the local growth capacity for the specified lattice
     * sites.
     */
    public long computeGrowthCapacity(Coord componentCoord, Coord expansionCoord) {
        return computeExcessCapacity(componentCoord) + computeExcessCapacity(expansionCoord);
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
    public long countCells(Coord coord) {
        return Carrier.countCells(lattice.viewOccupants(coord));
    }

    /**
     * Returns the local growth rate for a tumor component.
     *
     * @param component a component of this tumor.
     *
     * @return the intrinsic growth rate for the component.
     */
    public GrowthRate getLocalGrowthRate(TumorComponent component) {
        return localGrowthModel.getLocalGrowthRate(this, component);
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
    public MutationGenerator getLocalMutationGenerator(TumorComponent component) {
        return component.getMutationGenerator();
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
     * central site.
     *
     * @param center the site in the center of the neighborhood.
     *
     * @return the coordinates of the nearest neighbors to the
     * central site.
     */
    public List<Coord> getNeighbors(Coord center) {
        return neighborhood.getNeighbors(center);
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
     * Selects one neighboring lattice site at random (with equal
     * probability for all neighbors).
     *
     * @param center the coordinate at the center of the neighborhood.
     *
     * @return one site neighboring the central coordinate chosen at
     * random (with equal probability for all neighbors).
     */
    public Coord selectNeighbor(Coord center) {
        return neighborhood.randomNeighbor(center, randomSource);
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
        List<E> randomized = randomizeComponents();

        for (E component : randomized)
            advance(component);

        // Migrate after advancement...
        migrate();

        // This base class never divides...
        return Collections.emptyList();
    }

    /**
     * Assembles the active (living) components of this tumor in an
     * unbiased randomized order.
     *
     * @return a new list containing the active (living) components of
     * this tumor in an unbiased randomized order.
     */
    protected List<E> randomizeComponents() {
        List<E> randomized = new ArrayList<E>(viewComponents());
        ListUtil.shuffle(randomized, randomSource);
        
        return randomized;
    }

    /**
     * Advances a single parent component in this tumor by one
     * discrete time step and adds any offspring to this tumor.
     *
     * @param parent the parent component to advance.
     */
    protected void advance(E parent) {
        //
        // Locate the parent...
        //
        Coord parentCoord = locateComponent(parent);

        // Select an expansion site (which may be required to
        // accomodate offspring) at random...
        Coord expansionCoord = selectNeighbor(parentCoord);

        // Compute the local growth capacity: the excess capacity at
        // the parent site and the expansion site...
        long growthCapacity = computeGrowthCapacity(parentCoord, expansionCoord);

        // Construct the appropriate local environment...
        TumorEnv localEnv =
            new TumorEnv(growthCapacity,
                         getLocalGrowthRate(parent),
                         getLocalMutationGenerator(parent));

        // Advance the parent component within the local environment...
        @SuppressWarnings("unchecked")
            Collection<E> daughters = (Collection<E>) parent.advance(localEnv);

        if (parent.isDead()) {
            //
            // Remove the dead parent...
            //
            removeComponent(parent);
        }
        else if (parent.countCells() > 1) {
            //
            // Divide the multi-cellular parent (deme or lineage) if it
            // has grown beyond the capacity of its current location...
            //
            long excessOccupancy =
                Math.max(0, countCells(parentCoord) - getSiteCapacity(parentCoord));

            if (excessOccupancy > 0) {
                //
                // The new clone must take a number of cells at least as
                // large as the excess occupancy of the parent site, but
                // no larger than the excess capacity of the expansion
                // site...
                //
                long minCloneCellCount = excessOccupancy;
                long maxCloneCellCount =
                    Math.min(computeExcessCapacity(expansionCoord), parent.countCells() - 1);
                
                E parentClone = divideParent(parent, minCloneCellCount, maxCloneCellCount);
                addComponent(parentClone, expansionCoord);
            }
        }

        // Place the offspring at the parent site until it is filled,
        // then the expansion site...
        for (E daughter : daughters) {
            if (isAvailable(parentCoord, daughter))
                addComponent(daughter, parentCoord);
            else
                addComponent(daughter, expansionCoord);
        }
    }

    /**
     * Allows tumor components to migrate from one lattice site to
     * another according to the global migration model.
     */
    protected void migrate() {
        if (migrationModel.getType() == MigrationType.PINNED)
            return;

        // Migrate the active (living) tumor components in a
        // randomized order...
        List<E> randomized = randomizeComponents();

        for (E component : randomized)
            migrate(component);
    }

    /**
     * Allows a tumor component to migrate from one lattice site to
     * another according to the global migration model.
     *
     * @param component the tumor component eligible for migration.
     */
    protected void migrate(E component) {
        Coord newCoord = migrationModel.migrate(this, component);

        if (newCoord != null)
            moveComponent(component, newCoord);
    }

    @Override public Set<E> viewComponents() {
        return lattice.viewOccupants();
    }
}
