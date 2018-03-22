
package tumor.capacity;

import jam.lattice.Coord;

/**
 * Implements a capacity model that enforces single occupancy: at most
 * one occupant is allowed on a site.
 */
public final class SingleCapacity extends CapacityModel {
    private SingleCapacity() {}

    /**
     * The global single-occupancy capacity model.
     */
    public static final CapacityModel INSTANCE = new SingleCapacity();

    @Override public long getSiteCapacity(Coord coord) {
        //
        // The coordinate is ignored...
        //
        return 1L;
    }

    @Override public CapacityType getType() {
        return CapacityType.SINGLE;
    }
}
