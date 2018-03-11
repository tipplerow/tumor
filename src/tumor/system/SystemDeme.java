
package tumor.system;

import java.util.Collection;

import jam.app.JamProperties;
import jam.math.DoubleRange;

import tumor.carrier.Deme;
import tumor.carrier.Lineage;

/**
 * Represents demes with a fixed maximum size specified by the global
 * system properties.
 */
public final class SystemDeme extends Deme {
    private static long maximumSize = 0L;
    
    /**
     * Name of the system property that defines the maximum deme size.
     */
    public static final String MAXIMUM_DEME_SIZE_PROPERTY = "tumor.carrier.maximumDemeSize";
    
    /**
     * Creates a new deme with a single founding lineage.
     *
     * @param lineage the founding lineage.
     *
     * @return the founding deme.
     */
    public static SystemDeme founder(Lineage lineage) {
        return new SystemDeme(lineage);
    }

    private SystemDeme(Lineage lineage) {
        super(lineage);
    }

    @Override public long getMaximumSize() {
        if (maximumSize < 1L)
            maximumSize = resolveMaximumSize();

        return maximumSize;
    }

    private static long resolveMaximumSize() {
        return (long) JamProperties.getRequiredDouble(MAXIMUM_DEME_SIZE_PROPERTY, DoubleRange.POSITIVE);
    }

    @Override public SystemDeme newClone(Collection<Lineage> lineages) {
        return new SystemDeme(this, lineages);
    }
    
    private SystemDeme(SystemDeme parent, Collection<Lineage> lineages) {
        super(parent, lineages);
    }
}
