
package tumor.carrier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tumor.mutation.MutationList;

/**
 * Maps a genotype (all mutations accumulated in a lineage) to the
 * original lineage.
 */
public final class GenotypeMap {
    private final Map<MutationList, Lineage> map = new HashMap<MutationList, Lineage>();

    /**
     * Creates an empty genotype map.
     */
    public GenotypeMap() {
    }

    /**
     * Creates a genotype map for a series of lineages.
     *
     * @param lineages the lineages to map.
     *
     * @return the genotype map for the specified lineages.
     *
     * @throws IllegalArgumentException unless the lineages have
     * unique genotypes.
     */
    public static GenotypeMap create(Lineage... lineages) {
        return create(List.of(lineages));
    }

    /**
     * Creates a genotype map for a collection of lineages.
     *
     * @param lineages the lineages to map.
     *
     * @return the genotype map for the specified lineages.
     *
     * @throws IllegalArgumentException unless the lineages have
     * unique genotypes.
     */
    public static GenotypeMap create(Collection<Lineage> lineages) {
        GenotypeMap map = new GenotypeMap();
        map.addAll(lineages);
        return map;
    }

    /**
     * Extracts the genotype from a lineage.
     *
     * @param lineage the lineage to examine.
     *
     * @return all mutations that the given lineage has accumulated.
     */
    public static MutationList getGenotype(Lineage lineage) {
        return lineage.getAccumulatedMutations();
    }

    /**
     * Maps the genotype for a lineage.
     *
     * @param lineage the lineage to map.
     *
     * @throws IllegalArgumentException if this map already contains
     * the genotype carried by the specified lineage.
     */
    public void add(Lineage lineage) {
        MutationList genotype = getGenotype(lineage);
        Lineage      prevLine = map.put(genotype, lineage);

        if (prevLine != null) {
            //
            // Restore the previous state before throwing the
            // exception...
            //
            map.put(genotype, prevLine);
            throw new IllegalArgumentException("Duplicate genotype.");
        }
    }

    /**
     * Maps the genotypes for a collection of lineages.
     *
     * @param lineages the lineages to map.
     *
     * @throws IllegalArgumentException if this map already contains
     * a genotype carried by one of the lineage.
     */
    public void addAll(Collection<Lineage> lineages) {
        for (Lineage lineage: lineages)
            add(lineage);
    }

    /**
     * Identifies lineages contained in this map.
     *
     * @param lineage the lineage to search for.
     *
     * @return {@code true} iff this map contains the specified
     * lineage.
     */
    public boolean contains(Lineage lineage) {
        return map.containsKey(getGenotype(lineage));
    }

    /**
     * Identifies genotypes contained in this map.
     *
     * @param genotype the genotype to search for.
     *
     * @return {@code true} iff this map contains the specified
     * genotype.
     */
    public boolean contains(MutationList genotype) {
        return map.containsKey(genotype);
    }

    /**
     * Fetches a lineage by its genotype.
     *
     * @param genotype the genotype to search for.
     *
     * @return the lineage with the specified genotype; {@code null}
     * if the genotype is not mapped to a lineage.
     */
    public Lineage lookup(MutationList genotype) {
        return map.get(genotype);
    }

    /**
     * Removes a lineage from this map (if it is present).
     *
     * @param lineage the lineage to remove.
     *
     * @return {@code true} iff the map was altered (the genotype was
     * present and then removed).
     */
    public boolean remove(Lineage lineage) {
        MutationList genotype = getGenotype(lineage);
        Lineage      mapped   = map.get(genotype);

        // Only remove the mapping if the genotype maps to the input
        // lineage.  Use "==" to test for physical object identity,
        // not logical equivalence...
        if (lineage == mapped)
            return map.remove(genotype) != null;
        else
            return false;
    }

    /**
     * Removes a genotype from this map (if it is present).
     *
     * @param genotype the genotype to remove.
     *
     * @return {@code true} iff the map was altered (the genotype was
     * present and then removed).
     */
    public boolean remove(MutationList genotype) {
        return map.remove(genotype) != null;
    }
}
