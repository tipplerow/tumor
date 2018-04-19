
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

import tumor.carrier.Carrier;
import tumor.carrier.Lineage;
import tumor.carrier.TumorEnv;

/**
 * Represents a three-dimensional tumor composed of lineages arranged
 * on a cubic lattice.
 *
 * <p><b>Multiple lineage occupancy.</b> The site capacity model limts
 * the total number of <em>cells</em> at each lattice site, but apart
 * from that restriction, there is no explicit limit on the number of
 * <em>lineages</em> at one site.
 */
public final class LineageLatticeTumor extends CellGroupLatticeTumor<Lineage> {
    //
    // In a large tumor, there may be hundreds of lineages present at
    // each site.  Computing the total number of cells at a site by
    // explicitly iterating over each lineage becomes the most time
    // consuming operation in the simulation.  As a performance
    // optimization, we maintain a cache of the total cell count and
    // the count at each site and update the cache when lineages
    // advance (change their cell count), produce offspring, or die.
    //
    private long totalCellCount = 0;
    
    private final Object2LongOpenHashMap<Coord> siteCellCounts =
        new Object2LongOpenHashMap<Coord>();
    
    private LineageLatticeTumor(LineageLatticeTumor parent) {
        super(parent, createLattice());
    }

    private static Lattice<Lineage> createLattice() {
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

    @Override protected List<Lineage> advance(Lineage  parent,
                                              Coord    parentCoord,
                                              Coord    expansionCoord,
                                              TumorEnv localEnv) {
        
        // The initial parent size is required to update the cache...
        long preAdvancementSize = parent.countCells();

        // Advance the lineage...
        List<Lineage> daughters = parent.advance(localEnv);

        // Update the cell count cache...
        updateCellCount(parentCoord, parent, preAdvancementSize);

        // Divide the parent if necessary...
        if (mustDivide(parent, parentCoord))
            divideParent(parent, parentCoord, expansionCoord);

        return daughters;
    }

    @Override protected long computeCloneCapacity(Coord cloneCoord) {
        //
        // Multiple lineages may occupy a site...
        //
        return getSiteCapacity(cloneCoord) - countCells(cloneCoord);
    }

    @Override protected void divideParent(Lineage parent, Coord parentCoord, Coord expansionCoord) {
        // The pre-division size is required to update the cache...
        long preDivisionSize = parent.countCells();

        // The superclass places the clone at the expansion site...
        super.divideParent(parent, parentCoord, expansionCoord);

        // Update the cell count cache with the new lineage size...
        updateCellCount(parentCoord, parent, preDivisionSize);
    }

    @Override protected Lineage divideParent(Lineage parent, long minCloneCellCount, long maxCloneCellCount) {
        return parent.divide(TRANSFER_PROBABILITY, minCloneCellCount, maxCloneCellCount);
    }

    @Override protected void addComponent(Lineage component, Coord location) {
        super.addComponent(component, location);
        addCellCount(location, component);
    }

    @Override protected void moveComponent(Lineage component, Coord fromCoord, Coord toCoord) {
        super.moveComponent(component, fromCoord, toCoord);
        
        addCellCount(toCoord, component);
        removeCellCount(fromCoord, component);
    }

    @Override protected void removeComponent(Lineage component, Coord location) {
        super.removeComponent(component, location);
        removeCellCount(location, component);
    }

    private void addCellCount(Coord location, Lineage component) {
        addCellCount(location, component.countCells());
    }

    private void addCellCount(Coord location, long cellCount) {
        totalCellCount += cellCount;
        siteCellCounts.addTo(location, cellCount);
    }

    private void removeCellCount(Coord location, Lineage component) {
        addCellCount(location, -component.countCells());
    }

    private void updateCellCount(Coord location, Lineage component, long prevCount) {
        addCellCount(location, component.countCells() - prevCount);
    }
}
