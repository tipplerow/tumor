
package tumor.lattice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.Probability;

import tumor.capacity.CapacityModel;
import tumor.carrier.Carrier;
import tumor.carrier.GenotypeMap;
import tumor.carrier.Lineage;
import tumor.carrier.TumorEnv;
import tumor.mutation.MutationList;

/**
 * Represents a three-dimensional tumor composed of lineages arranged
 * on a cubic lattice.
 *
 * <p><b>Multiple lineage occupancy.</b> The site capacity model limts
 * the total number of <em>cells</em> at each lattice site, but apart
 * from that restriction, there is no explicit limit on the number of
 * <em>lineages</em> at one site.
 */
public final class LineageLatticeTumor extends LatticeTumor<Lineage> {
    //
    // To minimize the number of lineage instances in the tumor, we
    // allow only one genotype (lineage clone) per site.  We maintain
    // a genotype map at each lattice coordinate in order to identify
    // genetically identical lineages on neighboring lattice sites. We
    // may then transfer cells between identical lineages rather than
    // dividing lineages and placing a new instance on a neighbor site.
    //
    private final Map<Coord, GenotypeMap> genotypeMaps = new HashMap<Coord, GenotypeMap>();
    
    // In a large tumor, there may be hundreds of lineages present at
    // each site.  Computing the total number of cells at a site by
    // explicitly iterating over each lineage becomes the most time
    // consuming operation in the simulation.  As a performance
    // optimization, we maintain a cache of the total cell count and
    // the count at each site and update the cache when lineages
    // advance (change their cell count), produce offspring, or die.
    private long totalCellCount = 0;
    
    private final Object2LongOpenHashMap<Coord> siteCellCounts =
        new Object2LongOpenHashMap<Coord>();

    private LineageLatticeTumor(LineageLatticeTumor parent) {
        super(parent, createLineageLattice());
    }

    private static Lattice<Lineage> createLineageLattice() {
        //
        // Multiple lineages per site...
        //
        return Lattice.sparseMO(resolvePeriodLength());
    }

    /**
     * Creates a primary tumor with a single founding lineage located at
     * the origin.
     *
     * @param founder the founding lineage.
     *
     * @return the new primary tumor.
     */
    public static LineageLatticeTumor primary(Lineage founder) {
        LineageLatticeTumor tumor = new LineageLatticeTumor(null);
        tumor.seed(founder);
        return tumor;
    }

    private void seed(Lineage founder) {
        addComponent(founder, FOUNDER_COORD);
    }

    @Override public long computeExpansionFreeCapacity(Coord expansionCoord) {
        return getSiteCapacity(expansionCoord) - countCells(expansionCoord);
    }

    @Override public long countCells() {
        //
        // Enable assertions to check the consistency of the cached
        // cell counts...
        //
        assert totalCellCount == Carrier.countCells(lattice.viewOccupants());
        return totalCellCount;
    }

    @Override public long countCells(Coord coord) {
        //
        // Enable assertions to check the consistency of the cached
        // cell counts...
        //
        assert siteCellCounts.getLong(coord) == Carrier.countCells(lattice.viewOccupants(coord));
        return siteCellCounts.getLong(coord);
    }

    @Override public CapacityModel getCapacityModel() {
        return CapacityModel.global();
    }

    @Override public boolean isAvailable(Coord coord, Lineage lineage) {
        //
        // Any number of lineages may occupy a site as long as the
        // total cell count does not exceed the site capacity...
        //
        return countCells(coord) + lineage.countCells() <= getSiteCapacity(coord);
    }

    @Override public Map<Coord, Collection<Lineage>> mapComponents() {
        Map<Coord, Collection<Lineage>> map = new HashMap<Coord, Collection<Lineage>>();

        for (Lineage lineage : viewComponents()) {
            Coord coord = locateComponent(lineage);
            Collection<Lineage> occupants = map.get(coord);

            if (occupants == null) {
                occupants = new ArrayList<Lineage>();
                map.put(coord, occupants);
            }

            occupants.add(lineage);
        }

        return map;
    }

    @Override protected List<Lineage> advance(Lineage parent, Coord parentCoord, TumorEnv localEnv) {
        //
        // Save the prior size in order to update the cell-count cache
        // at the parent site...
        //
        long priorCount = parent.countCells();

        List<Lineage> daughters = parent.advance(localEnv);
        updateCellCount(parentCoord, priorCount, parent.countCells());

        return daughters;
    }

    @Override protected void distributeExcessOccupants(Lineage parent,
                                                       Coord   parentCoord,
                                                       Coord   expansionCoord,
                                                       long    excessOccupancy) {
        //
        // Look for a genetically identical lineage at the expansion
        // site.  If one is found, transfer the excess cells to it;
        // otherwise, divide the parent lineage and place the clone
        // at the expansion site.
        //
        Lineage clone = findClone(parent, expansionCoord);

        if (clone != null)
            transferExcess(parent, clone, parentCoord, expansionCoord, excessOccupancy);
        else
            divideParent(parent, parentCoord, expansionCoord, excessOccupancy);
    }

    private Lineage findClone(Lineage lineage, Coord coord) {
        MutationList genotype = lineage.getAccumulatedMutations();
        GenotypeMap  genoMap  = genotypeMaps.get(coord);

        if (genoMap != null)
            return genoMap.lookup(genotype);
        else
            return null;
    }

    private void transferExcess(Lineage parent, Lineage clone, Coord parentCoord, Coord cloneCoord, long cloneCount) {
        //
        // Save the prior sizes in order to update the cell-count
        // cache...
        //
        long parentPriorCount = parent.countCells();
        long clonePriorCount  = clone.countCells();

        parent.transfer(clone, cloneCount);

        updateCellCount(parentCoord, parentPriorCount, parent.countCells());
        updateCellCount(cloneCoord,  clonePriorCount,  clone.countCells());
    }

    private void divideParent(Lineage parent, Coord parentCoord, Coord expansionCoord, long excessOccupancy) {
        //
        // Save the prior size in order to update the cell-count cache
        // at the parent site.  (The addComponent() method updates the
        // cache at the expansion site)...
        //
        long priorCount = parent.countCells();

        addComponent(parent.divide(excessOccupancy), expansionCoord);
        updateCellCount(parentCoord, priorCount, parent.countCells());
    }

    @Override protected void addComponent(Lineage component, Coord location) {
        System.out.println("Adding: " + component + " => " + location);
        super.addComponent(component, location);

        addCellCount(location, component);
        addGenotype(location, component);
    }

    @Override protected void moveComponent(Lineage component, Coord fromCoord, Coord toCoord) {
        super.moveComponent(component, fromCoord, toCoord);

        removeCellCount(fromCoord, component);
        removeGenotype(fromCoord, component);
        
        addCellCount(toCoord, component);
        addGenotype(toCoord, component);
    }

    @Override protected void removeComponent(Lineage component, Coord location) {
        System.out.println("Removing: " + component);
        super.removeComponent(component, location);

        removeCellCount(location, component);
        removeGenotype(location, component);
    }

    private void addCellCount(Coord location, Lineage component) {
        addCellCount(location, component.countCells());
    }

    private void addCellCount(Coord location, long cellCount) {
        totalCellCount += cellCount;
        siteCellCounts.addTo(location, cellCount);
    }

    private void addGenotype(Coord location, Lineage component) {
        getGenotypeMap(location).add(component);
    }

    private GenotypeMap getGenotypeMap(Coord location) {
        GenotypeMap genotypeMap = genotypeMaps.get(location);

        if (genotypeMap == null) {
            genotypeMap = new GenotypeMap();
            genotypeMaps.put(location, genotypeMap);
        }

        return genotypeMap;
    }

    private void removeCellCount(Coord location, Lineage component) {
        addCellCount(location, -component.countCells());
    }

    private void removeGenotype(Coord location, Lineage component) {
        if (!getGenotypeMap(location).remove(component))
            throw new IllegalStateException("Lineage genotype was not mapped.");
    }

    private void updateCellCount(Coord location, long prevCount, long currCount) {
        addCellCount(location, currCount - prevCount);
    }
}
