
package tumor.divide;

import jam.lattice.Coord;

import tumor.carrier.Deme;

/**
 * Encapsulates the result of a deme division: the new clone and the
 * location where it should be placed.
 */
public final class DivisionResult {
    private final Deme clone;
    private final Coord coord;

    /**
     * Creates a new division result.
     *
     * @param clone the new clone.
     *
     * @param coord the coordinate where the clone should be placed.
     */
    public DivisionResult(Deme clone, Coord coord) {
        this.clone = clone;
        this.coord = coord;
    }

    /**
     * Returns the new clone.
     *
     * @return the new clone.
     */
    public Deme getClone() {
        return clone;
    }

    /**
     * Returns the coordinate where the clone should be placed.
     *
     * @return the coordinate where the clone should be placed.
     */
    public Coord getCoord() {
        return coord;
    }
}
