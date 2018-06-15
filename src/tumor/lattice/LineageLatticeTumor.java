
package tumor.lattice;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.carrier.Carrier;
import tumor.carrier.GenotypeMap;
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
public final class LineageLatticeTumor extends MultiCellularLatticeTumor<Lineage> {
    //
    // To minimize the number of lineage instances in the tumor, we
    // allow only one lineage clone (unique genotype) per site.  To
    // enforce this restriction, we maintain a genotype map at each
    // lattice coordinate in order to identify genetically identical
    // lineages on neighboring lattice sites. We may then transfer
    // cells between identical lineages rather than dividing lineages
    // and placing a new instance on a neighbor site.
    //
    private final Map<Coord, GenotypeMap> genotypeMaps = new HashMap<Coord, GenotypeMap>();
    
    // A large tumor with high mutation rates may contain hundreds of
    // lineages at each site.  Computing the total number of cells at
    // a site by explicitly iterating over each lineage becomes a
    // severe bottleneck.  As a performance optimization, we maintain
    // a cache of the cell count at each site and update the cache
    // when lineages change their cell count, produce offspring, or
    // die.  The multi-cellular superclass maintains a cache of the
    // total cell count in the tumor.
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

    @Override protected void advanceInPlace(Lineage parent, Coord parentCoord, long growthCapacity) {
        // Construct the appropriate local environment...
        TumorEnv localEnv = createLocalEnv(parent, parentCoord, growthCapacity);

        // Advance the parent component within the local environment...
        List<Lineage> daughters = parent.advance(localEnv);

        // Sanity check...
        assert populationGrowth(parent, daughters) <= growthCapacity;

        // Update the cached cell count for the parent component...
        updateComponentCellCount(parent, parentCoord);

        // Add the daughters at the parent site, which is guaranteed
        // to have the necessary free capacity...
        for (Lineage daughter : daughters)
            addComponent(daughter, parentCoord);
    }

    private long populationGrowth(Lineage parent, List<Lineage> daughters) {
        return parent.netChange() + Lineage.DAUGHTER_CELL_COUNT * daughters.size();
    }

    @Override protected void advanceWithExpansion(Lineage parent, Coord parentCoord, long parentFreeCapacity) {
        // Select a neighboring expansion site at random...
        Coord expansionCoord = selectNeighbor(parentCoord);

        // Compute the total growth capacity...
        long growthCapacity = parentFreeCapacity + computeExpansionFreeCapacity(expansionCoord);

        // Advance the parent component using the total growth capacity...
        TumorEnv      localEnv  = createLocalEnv(parent, parentCoord, growthCapacity);
        List<Lineage> daughters = parent.advance(localEnv);

        assert populationGrowth(parent, daughters) <= growthCapacity;

        // Update the cached cell count for the parent component...
        updateComponentCellCount(parent, parentCoord);

        // Compute the net population growth...
        assert Lineage.DAUGHTER_CELL_COUNT == 1;
        
        int parentGrowth   = (int) parent.netChange();
        int daughterGrowth = daughters.size();
        int familyGrowth   = parentGrowth + daughterGrowth;

        if (familyGrowth <= parentFreeCapacity) {
            //
            // All new parent cells and daughter lineages can be
            // accomodated at the parent site...
            //
            for (Lineage daughter : daughters)
                addComponent(daughter, parentCoord);
        }
        else if (parentGrowth <= parentFreeCapacity) {
            //
            // All new parent cells can be accomodated at the parent
            // site.  Place the daughter lineages at the parent site
            // until it reaches capacity, then at the expansion site.
            //
            parentFreeCapacity -= parentGrowth;

            for (int daughterIndex = 0; daughterIndex < (int) parentFreeCapacity; ++daughterIndex)
                addComponent(daughters.get(daughterIndex), parentCoord);

            for (int daughterIndex = (int) parentFreeCapacity; daughterIndex < daughters.size(); ++daughterIndex)
                addComponent(daughters.get(daughterIndex), expansionCoord);
        }
        else {
            //
            // Increase in the parent cell count requires its excess
            // cells to be moved to the expansion site; all daughter
            // lineages must also then go to the expansion site.
            //
            long parentExcess = parentGrowth - parentFreeCapacity;
            
            // Look for a genetically identical lineage at the expansion
            // site.  If one is found, transfer the excess cells to it;
            // otherwise, divide the parent lineage and place the clone
            // at the expansion site.
            Lineage clone = findClone(parent, expansionCoord);

            if (clone != null) {
                //
                // Transfer the excess parent cell count to the clone,
                // and update the cell counts...
                //
                parent.transfer(clone, parentExcess);

                updateComponentCellCount(parent, parentCoord);
                updateComponentCellCount(clone, expansionCoord);
            }
            else {
                //
                // Create a new clone and add it to the expansion
                // site.  [The addComponent() method updates the
                // cell-count cache at the expansion coordinate
                // so we do not.]
                //
                addComponent(parent.divide(parentExcess), expansionCoord);
                updateComponentCellCount(parent, parentCoord);
            }

            // And finally add all daughters to the expansion site...
            for (Lineage daughter : daughters)
                addComponent(daughter, expansionCoord);
        }

        assert satisfiesCapacityConstraint(expansionCoord);
    }

    private Lineage findClone(Lineage lineage, Coord coord) {
        GenotypeMap genoMap = genotypeMaps.get(coord);

        if (genoMap != null)
            return genoMap.getUnique(lineage.getGenotype());
        else
            return null;
    }

    @Override protected long computeExpansionFreeCapacity(Coord expansionCoord) {
        return getSiteCapacity(expansionCoord) - countCells(expansionCoord);
    }

    @Override protected long computeParentFreeCapacity(Lineage parent, Coord parentCoord) {
        return getSiteCapacity(parentCoord) - countCells(parentCoord);
    }

    @Override protected void updateComponentCellCount(Lineage component, Coord location) {
        //
        // Avoid a needless hash table lookup...
        //
        long netChange = component.netChange();

        if (netChange != 0) {
            // The superclass updates the tumor total...
            super.updateComponentCellCount(component, location);

            // We update the site-specific count...
            siteCellCounts.addTo(location, netChange);
        }
    }

    @Override protected void addComponent(Lineage component, Coord location) {
        super.addComponent(component, location);

        addCellCount(location, component);
        addGenotype(location, component);
    }

    private void addCellCount(Coord location, Lineage component) {
        siteCellCounts.addTo(location, component.countCells());
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

    @Override protected void moveComponent(Lineage component, Coord fromCoord, Coord toCoord) {
        super.moveComponent(component, fromCoord, toCoord);

        removeCellCount(fromCoord, component);
        removeGenotype(fromCoord, component);
        
        addCellCount(toCoord, component);
        addGenotype(toCoord, component);
    }

    private void removeCellCount(Coord location, Lineage component) {
        siteCellCounts.addTo(location, -component.countCells());
    }

    private void removeGenotype(Coord location, Lineage component) {
        if (!getGenotypeMap(location).remove(component))
            throw new IllegalStateException("Lineage genotype was not mapped.");
    }

    @Override protected void removeComponent(Lineage component, Coord location) {
        super.removeComponent(component, location);

        removeCellCount(location, component);
        removeGenotype(location, component);
    }
}
