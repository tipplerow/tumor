
package tumor.point;

import tumor.carrier.Lineage;
import tumor.carrier.UniformComponent;
import tumor.mutation.MutationList;
import tumor.mutation.MutationRate;
import tumor.mutation.NeutralGenerator;

/**
 * Represents a point tumor that generates only neutral mutations.
 */
public class NeutralPointTumor extends PointTumor {
    /**
     * Creates a new neutral point tumor with a founding lineage and
     * fixed mutation rate.
     *
     * @param founder the founding lineage.
     *
     * @param mutationRate the fixed rate at which mutations will be
     * generated.
     */
    public NeutralPointTumor(Lineage founder, MutationRate mutationRate) {
        super(founder, mutationRate);
    }

    /**
     * Creates a new neutral point tumor with a founding lineage and
     * fixed mutation rate.
     *
     * @param founder the founding lineage.
     *
     * @param mutationRate the fixed rate at which mutations will be
     * generated.
     *
     * @return the new neutral point tumor.
     */
    public static NeutralPointTumor create(Lineage founder, MutationRate mutationRate) {
        return new NeutralPointTumor(founder, mutationRate);
    }

    @Override public MutationList generateMutations(UniformComponent component) {
        //
        // Neutral mutations arise with the fixed mutation rate,
        // independently of the identity of the component...
        //
        return NeutralGenerator.INSTANCE.generate(mutationRate);
    }
}
