
package tumor.carrier;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import tumor.mutation.Genotype;

/**
 * Maps genotypes to lineages that contain them to faciliate the
 * identification of lineage clones.
 */
public final class GenotypeMap {
    private final Multimap<Genotype, Lineage> map = HashMultimap.create();

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
     * Adds a lineage to this map.
     *
     * @param lineage the lineage to add.
     */
    public void add(Lineage lineage) {
        map.put(lineage.getGenotype(), lineage);
    }

    /**
     * Adds lineages to this map.
     *
     * @param lineages the lineages to add.
     */
    public void addAll(Collection<Lineage> lineages) {
        for (Lineage lineage: lineages)
            add(lineage);
    }

    /**
     * Identifies genotypes contained in this map.
     *
     * @param genotype the genotype to search for.
     *
     * @return {@code true} iff this map contains the specified
     * genotype.
     */
    public boolean contains(Genotype genotype) {
        return map.containsKey(genotype);
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
        return get(lineage.getGenotype()).contains(lineage);
    }

    /**
     * Counts the number of lineage clones sharing the same genotype.
     *
     * @param genotype the genotype to search for.
     *
     * @return the number of lineage clones containing the specified
     * genotype.
     */
    public int count(Genotype genotype) {
        return get(genotype).size();
    }

    /**
     * Returns all lineages with a given genotype.
     *
     * @param genotype the genotype to search for.
     *
     * @return a collection containing all lineages in this map having
     * the specified genotype; an empty collection if there are none.
     */
    public Collection<Lineage> get(Genotype genotype) {
        return map.get(genotype);
    }

    /**
     * Attempts to fetch a unique lineage by its genotype.
     *
     * @param genotype the genotype to search for.
     *
     * @return the unique lineage with the specified genotype (if
     * exactly one exists in this map) or {@code null} if the map does
     * not contain the input genotype.
     *
     * @throws IllegalStateException if more than one lineage shares
     * the same genotype.
     */
    public Lineage getUnique(Genotype genotype) {
        Collection<Lineage> lineages = get(genotype);

        if (lineages.size() == 0)
            return null;

        if (lineages.size() == 1)
            return lineages.iterator().next();

        throw new IllegalStateException("Multiple lineages share the ostensibly unique genotype.");
    }

    /**
     * Removes a lineage from this map (if it is present).
     *
     * @param lineage the lineage to remove.
     *
     * @return {@code true} iff the map was altered (the lineage was
     * present and then removed).
     */
    public boolean remove(Lineage lineage) {
        return map.remove(lineage.getGenotype(), lineage);
    }

    /**
     * Removes a genotype and all lineages containing it from this map
     * (if it is present).
     *
     * @param genotype the genotype to remove.
     *
     * @return {@code true} iff the map was altered (the genotype was
     * present and then removed).
     */
    public boolean remove(Genotype genotype) {
        return map.removeAll(genotype).size() > 0;
    }
}
