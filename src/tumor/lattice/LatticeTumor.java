
package tumor.lattice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import jam.app.JamProperties;
import jam.dist.HypersphericalDistribution;
import jam.lang.JamException;
import jam.lattice.Coord;
import jam.lattice.DistanceComparator;
import jam.lattice.Lattice;
import jam.lattice.LatticeView;
import jam.math.JamRandom;
import jam.math.VectorMoment;
import jam.util.CollectionUtil;
import jam.util.ListUtil;
import jam.vector.JamVector;
import jam.vector.VectorView;

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
import tumor.senesce.SenescenceModel;

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
     * The local growth rate model.
     */
    protected final LocalGrowthModel localGrowthModel = LocalGrowthModel.global();

    /**
     * The component migration model.
     */
    protected final MigrationModel migrationModel = MigrationModel.global();

    /**
     * The cell senescence model.
     */
    protected final SenescenceModel senescenceModel = SenescenceModel.global();

    /**
     * The random number generator.
     */
    protected final JamRandom randomSource = JamRandom.global();

    // Distribution of vectors randomly located on the surface of a
    // sphere with radius equal to one-half the square root of three,
    // used to select expansion sites while favoring spherical tumor
    // shapes...
    private static final HypersphericalDistribution EXPANSION_DISTRIB =
        new HypersphericalDistribution(3, Math.sqrt(3.0) / 2.0);

    // Distribution of vectors randomly located on the surface of a
    // unit sphere, used to generate random search directions for
    // surface sites...
    private static final HypersphericalDistribution SURFACE_DISTRIB =
        new HypersphericalDistribution(3, 1.0);

    // In order to classify a lattice site as a surface site for this
    // tumor, the surface-search algorithm must find no occupied sites
    // over a distance equal to the product of the radius of gyration
    // of the tumor and this fraction...
    private static final double UNOCC_DIST_RG_FRAC = 0.25;

    // Or at least this distance (in units of lattice sites)...
    private static final double MIN_UNOCC_DIST = 5.0;

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
            throw JamException.runtime("%s cannot safely accomodate [%d] occupied sites.", lattice.getPeriod(), maxSiteCount);
    }

    /**
     * The coordinate of the founding component.
     */
    public static final Coord FOUNDER_COORD = Coord.ORIGIN;

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
     * Collects the tumor components that constitute a bulk sample.
     *
     * <p>The sampled components are obtained by sorting the occupied
     * lattice sites by their distance from the specified center site
     * and adding the site contents to the sample until the specified
     * size is accumulated.
     *
     * @param sampleSite the site at the center of the bulk sample
     * (may be a surface site).
     *
     * @param targetSize the minimum number of <em>cells</em> to
     * include in the sample.
     *
     * @return the components in the bulk sample.
     *
     * @throws IllegalArgumentException if the sample size exceeds the
     * number of cells in this tumor.
     */
    public Multimap<Coord, E> collectBulkSample(Coord sampleSite, long targetSize) {
        if (targetSize > countCells())
            throw new IllegalArgumentException("Target size exceeds tumor size.");

        List<Coord> occupiedCoord = new ArrayList<Coord>(getOccupiedCoord());
        Collections.sort(occupiedCoord, new DistanceComparator(sampleSite));

        long sampleSize = 0;
        Multimap<Coord, E> sampleMap = LinkedHashMultimap.create();

        for (Coord coord : occupiedCoord) {
            Collection<E> occupants = lattice.viewOccupants(coord);

            for (E occupant : occupants) {
                sampleMap.put(coord, occupant);
                sampleSize += occupant.countCells();

                if (sampleSize >= targetSize)
                    return sampleMap;
            }
        }

        // Should never get here...
        throw new IllegalStateException("Failed to meet target sample size.");
    }

    /**
     * Collects a single component from a specified sample site.
     *
     * <p>If the site contains more than one component, one component
     * is chosen at random with a probability equal to its fractional
     * share of the total cell count at the site.
     *
     * @param sampleSite the tumor site to sample.
     *
     * @return a component selected from the specified sample site.
     *
     * @throws IllegalStateException if the sample site is empty.
     */
    public E collectSingleSample(Coord sampleSite) {
        Collection<E> components = viewComponents(sampleSite);

        if (components.isEmpty())
            throw new IllegalStateException("Empty sample site.");

        if (components.size() == 1)
            return CollectionUtil.peek(components);

        return Carrier.random(new ArrayList<E>(components));
    }

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
     * Finds a lattice site at the surface of the tumor.
     *
     * <p>The search algorithm starts at the center of mass of this
     * tumor and steps along the specified search direction until it
     * has traversed a threshold distance D without encountering any
     * occupied lattice sites.  The last occupied site on the search
     * path is then identified as a surface site.  The distance D is
     * taken as {@code max(5, 0.25 * RG)}, where {@code RG} is the
     * scalar radius of gyration of the tumor, in lattice units.
     *
     * @param step the direction to move away from the center of mass
     * and toward the surface.
     *
     * @return the surface site along the specified direction from the
     * center of mass.
     *
     * @throws IllegalArgumentException unless the step direction is a
     * three-dimensional vector.
     *
     * @throws IllegalStateException if a surface site cannot be found.
     */
    public Coord findSurfaceSite(VectorView step) {
        VectorMoment moment = getVectorMoment();

        VectorView start = moment.getCM();
        double     udist = Math.max(MIN_UNOCC_DIST, UNOCC_DIST_RG_FRAC * moment.scalar());

        return findSurfaceSite(start, step, udist);
    }

    private Coord findSurfaceSite(VectorView start, VectorView step, double unoccDist) {
        //
        // Starting at the "start" coordinate, step along the "step"
        // direction with unit length until traversing a distance of
        // at least "unoccDist" without encountering a component; the
        // last occupied site is then the surface site.
        //
        JamVector unitStep  = JamVector.unit(step);
        JamVector cursorLoc = JamVector.copyOf(start);

        Coord cursorSite  = Coord.nearest(cursorLoc);
        Coord lastOccSite = null;

        // Guard against an endless loop (which could happen if the
        // lattice near full capacity along the step direction) by
        // limiting the number of steps to the side length for the
        // periodic box.
        int iterCount = 0;
        int maxIter   = 2 * lattice.getPeriod().getMaxLength();

        while (true) {
            ++iterCount;

            if (lattice.isOccupied(cursorSite)) {
                //
                // Store the location of the last occupied site...
                //
                lastOccSite = cursorSite;
            }
            else if (foundSurfaceSite(cursorSite, lastOccSite, unoccDist)) {
                //
                // The last occupied site is the surface site...
                //
                return lastOccSite;
            }
            else if (iterCount > maxIter) {
                //
                // Should never really happen if the lattice is large
                // enough...
                //
                throw new IllegalStateException("No surface site found.");
            }

            // Move the cursor along the search direction...
            cursorLoc  = cursorLoc.plus(unitStep);
            cursorSite = Coord.nearest(cursorLoc);
        }
    }

    private static boolean foundSurfaceSite(Coord cursorSite, Coord lastOccSite, double unoccDist) {
        return lastOccSite != null
            && Coord.computeSquaredDistance(cursorSite, lastOccSite) > unoccDist * unoccDist;
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
     * Finds a site with capacity to accomodate new tumor cells
     * generated by the growth of a parent component.
     *
     * <p>The expansion algorithm computes a step vector from the
     * tumor center of mass through the parent coordinate.  If the
     * length of that step vector is less than one (the tumor has only
     * a few occupied lattice sites), the algorithm chooses a neighbor
     * site at random.  Otherwise, the algorithm rescales the step
     * vector to unit length then starts at the parent coordinate and
     * steps outward (away from the center of mass) along the unitized
     * step vector until it finds an occupied site.
     *
     * @param parentCoord the coordinate of the parent component.
     *
     * @return a suitable expansion site.
     *
     * @throws IllegalStateException if an expansion site cannot be
     * found.
     */
    public Coord selectExpansionSite(Coord parentCoord) {
        return parentCoord.plus(Coord.nearest(EXPANSION_DISTRIB.sample(randomSource)));
    }

    /**
     * Selects a lattice site on the surface of this tumor at random
     * (with a uniform distribution along the surface of the tumor).
     *
     * @return a randomly selected site on the surface of this tumor.
     *
     * @throws IllegalStateException if a surface site cannot be found.
     */
    public Coord selectSurfaceSite() {
        //
        // Conduct a search with a randomly generated step direction...
        //
        return findSurfaceSite(SURFACE_DISTRIB.sample(randomSource));
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
        switch (component.getState()) {
        case ACTIVE:
            active.add(component);
            break;

        case SENESCENT:
            senescent.add(component);
            break;

        case DEAD:
            throw new IllegalStateException("Should not add a dead tumor component.");

        default:
            throw new IllegalStateException("Unknown component state.");
        }

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
        //
        // We do not know the previous state of the component, so just
        // remove it from both sets...
        //
        active.remove(component);
        senescent.remove(component);
        lattice.vacate(component);
    }

    @Override protected List<Tumor<E>> runAdvance() {
        //
        // Advance the active tumor components in a randomized
        // order...
        //
        advance(randomizeActiveComponents());

        // Check for newly senescent components...
        senesce();

        // Migrate after advancement...
        migrate();

        // This base class never divides...
        return Collections.emptyList();
    }

    private List<E> randomizeActiveComponents() {
        List<E> randomized = new ArrayList<E>(active);
        ListUtil.shuffle(randomized, randomSource);
        
        return randomized;
    }

    private void advance(List<E> parents) {
        for (E parent : parents) {
            checkParentState(parent);
            advance(parent);
            updateParentState(parent);
        }
    }

    private void checkParentState(E parent) {
        if (!parent.isActive())
            throw new IllegalStateException("Only active parents should advance.");
    }

    private void updateParentState(E parent) {
        switch (parent.getState()) {
        case ACTIVE:
            // Active components should never be empty...
            assert parent.countCells() > 0;

            // Still active, no changes necessary...
            break;

        case SENESCENT:
            active.remove(parent);
            senescent.add(parent);
            break;

        case DEAD:
            // Subclasses should remove dead parents...
            assert !lattice.contains(parent);
            break;

        default:
            throw new IllegalStateException("Unknown component state.");
        }
    }

    private void senesce() {
        //
        // Only active cells may become senescent...
        //
        Iterator<E> iterator = active.iterator();

        while (iterator.hasNext()) {
            E component = iterator.next();

            if (senescenceModel.senesce(this, component)) {
                iterator.remove();
                component.senesce();
                senescent.add(component);
            }
        }
    }

    private void migrate() {
        if (migrationModel.getType() == MigrationType.PINNED)
            return;

        // Migrate the active tumor components in a randomized
        // order...
        List<E> randomized = randomizeActiveComponents();

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

    @Override public Set<E> viewComponents(Coord location) {
        return lattice.viewOccupants(location);
    }
}
