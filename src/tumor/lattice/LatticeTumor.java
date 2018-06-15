
package tumor.lattice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jam.app.JamProperties;
import jam.lang.JamException;
import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.lattice.LatticeView;
import jam.lattice.Neighborhood;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.capacity.CapacityModel;
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
     * @param maxSiteCount an estimate of the maximum number of
     * lattice sites that will be occupied by tumor components.
     *
     * @throws IllegalArgumentException unless the lattice is empty
     * and large enough to safely accomodate the maximum number of
     * components.
     */
    protected LatticeTumor(LatticeTumor<E> parent, Lattice<E> lattice, long maxSiteCount) {
        super(parent);

        validateLattice(lattice, maxSiteCount);
        this.lattice = lattice;
    }

    private static void validateLattice(Lattice<?> lattice, long maxSiteCount) {
        if (!lattice.isEmpty())
            throw new IllegalArgumentException("Initial lattice is not empty.");

        // Require a "safety factor" of two in each spatial dimension,
        // for a total safety factor of eight...
        if (lattice.getPeriod().getSiteCount() < 8 * maxSiteCount)
            throw JamException.runtime("A lattice period of [%d] cannot safely accomodate [%d] occupied sites.",
                                       lattice.getPeriod(), maxSiteCount);
    }

    /**
     * The coordinate of the founding component.
     */
    public static final Coord FOUNDER_COORD = Coord.ORIGIN;

    /**
     * Name of the system property that defines the nearest neighbors
     * on the lattice.
     */
    public static final String NEIGHBORHOOD_PROPERTY = "tumor.lattice.neighborhood";

    /**
     * Name of the system property that defines the edge length of the
     * (periodic cubic) lattice.
     */
    public static final String PERIOD_LENGTH_PROPERTY = "tumor.lattice.periodLength";

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
    public static int minimumLatticePeriod(long maxSites) {
        //
        // Two times the diameter of a perfect sphere should be
        // sufficient to contain the tumor without any chance of
        // conflict with periodic images.
        //
        long diameter = 2 * estimateRadius(maxSites);
        long period   = 2 * diameter;

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
        return (long) Math.ceil(Math.cbrt(0.75 * siteCount / Math.PI));
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
     * Advances a parent component by one discrete time step.
     *
     * @param parent the parent component to be advanced.
     */
    protected abstract void advance(E parent);

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
     * Returns the governing site capacity model.
     *
     * @return the governing site capacity model.
     */
    public abstract CapacityModel getCapacityModel();

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
     * Determines whether the number of tumor cells at a given lattice
     * site exceeds the capacity for that site.
     *
     * @param coord the coordinate of the site to test.
     *
     * @return {@code true} iff the number of tumor cells at the given
     * lattice site is greater than the capacity for that site.
     */
    public boolean exceedsCapacity(Coord coord) {
        return countCells(coord) > getSiteCapacity(coord);
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
     * <p>This default implementation simply returns the global
     * mutation generator (applies no adjustment for the local
     * environment).
     *
     * @param component a component of this tumor.
     *
     * @return the intrinsic mutation generator for the component.
     */
    public MutationGenerator getLocalMutationGenerator(TumorComponent component) {
        return MutationGenerator.global();
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
        return getCapacityModel().getSiteCapacity(coord);
    }

    /**
     * Determines whether the number of tumor cells at a given lattice
     * site is within the capacity bound for that site.
     *
     * @param coord the coordinate of the site to test.
     *
     * @return {@code true} iff the number of tumor cells at the given
     * lattice site is less than or equal to the capacity for that site.
     */
    public boolean satisfiesCapacityConstraint(Coord coord) {
        if (exceedsCapacity(coord)) {
            System.out.println(String.format("%s: CAPACITY: %d, COUNT: %d", coord, getSiteCapacity(coord), countCells(coord)));
        }
        return !exceedsCapacity(coord);
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
     * Creates the appropriate local environment for advancing a
     * parent component.
     *
     * @param parent the parent component being advanced.
     *
     * @param parentCoord the location of the parent coordinate.
     *
     * @param growthCapacity the total local growth capacity in the
     * neighborhood surrounding the parent.
     *
     * @return the local environment containing the appropriate net
     * growth rate.
     */
    protected TumorEnv createLocalEnv(E parent, Coord parentCoord, long growthCapacity) {
        //
        // The parent coordinate is currently not needed, but it may
        // be required in the future to optimize the retrieval of the
        // local growth rates and mutation generators...
        //
        return new TumorEnv(growthCapacity,
                            getLocalGrowthRate(parent),
                            getLocalMutationGenerator(parent));
    }

    /**
     * Maps the locations of the components in a single-occupancy
     * lattice tumor.
     *
     * @return a mapping from occupied sites to the single occupant of
     * those sites.
     */
    protected Map<Coord, Collection<E>> mapComponentsSO() {
        if (lattice.siteCapacity() > 1)
            throw new IllegalStateException("This method is only valid for single-occupancy lattices.");

        Map<Coord, Collection<E>> map = new HashMap<Coord, Collection<E>>();

        for (E component : viewComponents())
            map.put(locateComponent(component), List.of(component));

        return map;
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
            if (parent.isDead())
                throw new IllegalStateException("Cannot advance a dead parent.");
            else
                advance(parent);
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

    @Override public Coord locateComponent(E component) {
        Coord coord = lattice.locate(component);

        if (coord == null)
            throw new IllegalArgumentException("Component is not present in the tumor.");

        return coord;
    }

    @Override public Set<E> viewComponents() {
        return lattice.viewOccupants();
    }
}
