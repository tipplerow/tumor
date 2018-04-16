
package tumor.lattice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jam.app.JamProperties;
import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.lattice.LatticeView;
import jam.lattice.Neighborhood;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.carrier.TumorEnv;
import tumor.growth.GrowthRate;
import tumor.growth.LocalGrowthModel;
import tumor.migrate.MigrationModel;
import tumor.migrate.MigrationType;
import tumor.mutation.Mutation;
import tumor.mutation.MutationList;
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

    private static Neighborhood resolveNeighborhood() {
        return JamProperties.getRequiredEnum(NEIGHBORHOOD_PROPERTY, Neighborhood.class);
    }

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
     * A map from mutations to their location of origin.
     */
    protected final Map<Mutation, Coord> mutationOrigin = new HashMap<Mutation, Coord>();

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
     * Computes the local growth capacity: the total number of new
     * tumor cells that may be accomodated at the site of a parent
     * component (about to grow stochastically) and a neighboring
     * expansion site where any offspring will be placed.
     *
     * @param parentCoord the coordinate of the parent component.
     *
     * @param neighborCoord the coordinate of the neighboring site
     * where any offspring will be placed.
     *
     * @return the local growth capacity for the specified lattice
     * sites.
     */
    public abstract long computeGrowthCapacity(Coord parentCoord, Coord neighborCoord);

    /**
     * Returns the total number of tumor cells present at a given
     * lattice site.
     *
     * @param coord the coordinate to examine.
     *
     * @return the total number of tumor cells present at the
     * specified lattice site.
     */
    public abstract long countCells(Coord coord);

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
     * Advances a parent component by one discrete time step in a
     * given local environment and returns any offspring produced.
     *
     * <p>Subclasses with multi-cellular components must also divide
     * the component between the original lattice site and expansion
     * site.
     *
     * @param parent the parent component to be advanced.
     *
     * @param parentCoord the initial location of the parent component.
     *
     * @param expansionCoord the location of the lattice site that will
     * accomondate the divided clone of a multi-cellular component that
     * grows to exceed the capacity of the parent site.
     *
     * @param localEnv the local environment at the parent site.
     *
     * @return any offspring created during advancement.
     */
    protected abstract List<E> advance(E parent,
                                       Coord parentCoord,
                                       Coord expansionCoord,
                                       TumorEnv localEnv);

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

        mapMutationOrigin(component.getOriginalMutations(), location);
    }

    /**
     * Records the location of origin for each mutation.
     *
     * @param mutations the mutations to map.
     *
     * @param location the location where mutations originated.
     */
    protected void mapMutationOrigin(MutationList mutations, Coord location) {
        for (Mutation mutation : mutations)
            if (!mutationOrigin.containsKey(mutation))
                throw new IllegalStateException("Mutation is already mapped.");
            else
                mutationOrigin.put(mutation, location);
    }

    /**
     * Moves a tumor component from one site to another.
     *
     * @param component the tumor component to move.
     *
     * @param fromCoord the old coordinate for the component.
     *
     * @param toCoord the new coordinate for the component.
     *
     * @throws IllegalStateException unless the lattice can accomodate
     * the component at the new location.
     */
    protected void moveComponent(E component, Coord fromCoord, Coord toCoord) {
        lattice.vacate(component);

        if (isAvailable(toCoord, component))
            lattice.occupy(component, toCoord);
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
     *
     * @param location the last site occupied by the component.
     */
    protected void removeComponent(E component, Coord location) {
        lattice.vacate(component);
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

    @Override public List<Tumor<E>> advance() {
        //
        // Advance the active (living) tumor components in a
        // randomized order...
        //
        advance(randomizeComponents());

        // Migrate after advancement...
        migrate();

        // This base class never divides...
        return Collections.emptyList();
    }

    private List<E> randomizeComponents() {
        List<E> randomized = new ArrayList<E>(viewComponents());
        ListUtil.shuffle(randomized, randomSource);
        
        return randomized;
    }

    private void advance(List<E> parents) {
        for (E parent : parents)
            advance(parent);
    }

    private void advance(E parent) {
        //
        // Ensure that dead parents have been removed and are not advanced...
        //
        if (parent.isDead())
            throw new IllegalStateException("Cannot advance a dead parent.");

        // Locate the parent...
        Coord parentCoord = locateComponent(parent);

        // Select an expansion site (which may be required to
        // accomodate offspring) at random...
        Coord expansionCoord = selectNeighbor(parentCoord);

        // Compute the local growth capacity: the total number of new
        // tumor cells that can be accomodated the parent site and the
        // neighbor site...
        long growthCapacity = computeGrowthCapacity(parentCoord, expansionCoord);

        // Construct the appropriate local environment...
        TumorEnv localEnv =
            new TumorEnv(growthCapacity,
                         getLocalGrowthRate(parent),
                         getLocalMutationGenerator(parent));

        // Advance the parent component within the local environment.
        // Subclasses with multi-cellular components will divide the
        // parent if necessary (it grows beyond the capacity of its
        // current site) and place the clone on the expansion site...
        List<E> daughters = advance(parent, parentCoord, expansionCoord, localEnv);

        // Remove dead parents...
        if (parent.isDead())
            removeComponent(parent, parentCoord);

        // Place the daughters at the parent site until it reaches
        // capacity, then place them at the expansion site...
        for (E daughter : daughters) {
            if (isAvailable(parentCoord, daughter))
                addComponent(daughter, parentCoord);
            else
                addComponent(daughter, expansionCoord);
        }
    }

    private void migrate() {
        if (migrationModel.getType() == MigrationType.PINNED)
            return;

        // Migrate the active (living) tumor components in a
        // randomized order...
        List<E> randomized = randomizeComponents();

        for (E component : randomized)
            migrate(component);
    }

    private void migrate(E component) {
        Coord fromCoord = locateComponent(component);
        Coord toCoord   = migrationModel.migrate(this, component);

        if (toCoord != null)
            moveComponent(component, fromCoord, toCoord);
    }

    @Override public Set<E> viewComponents() {
        return lattice.viewOccupants();
    }
}
