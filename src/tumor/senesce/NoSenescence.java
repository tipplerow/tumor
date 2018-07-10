
package tumor.senesce;

import jam.lattice.Coord;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;

/**
 * Implements a senescence model where cells always remain active
 * (never senesce).
 */
public final class NoSenescence extends SenescenceModel {
    private NoSenescence() {
    }

    public static final NoSenescence INSTANCE = new NoSenescence();

    @Override public <E extends TumorComponent> boolean senesce(LatticeTumor<E> tumor, E component) {
        return false;
    }

    @Override public SenescenceType getType() {
        return SenescenceType.NONE;
    }
}
