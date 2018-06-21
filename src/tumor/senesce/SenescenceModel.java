
package tumor.senesce;

import jam.app.JamProperties;
import jam.lang.JamException;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;

/**
 * Determines when tumor cells enter a senescent state.
 */
public abstract class SenescenceModel {
    private static SenescenceModel global = null;

    /**
     * Name of the system property that defines the type of senescence
     * model.
     */
    public static final String MODEL_TYPE_PROPERTY = "tumor.senesce.modelType";

    /**
     * Returns the global senescence model defined by system properties.
     *
     * @return the global senescence model defined by system properties.
     */
    public static SenescenceModel global() {
        if (global == null)
            global = createGlobal();

        return global;
    }

    private static SenescenceModel createGlobal() {
        SenescenceType modelType = resolveModelType();

        switch (modelType) {
        case NEIGHBORHOOD_OCCUPANCY_FRACTION:
            return NOFSenescence.createGlobal();

        default:
            throw JamException.runtime("Unknown senescence model [%s].", modelType);
        }
    }

    private static SenescenceType resolveModelType() {
        return JamProperties.getRequiredEnum(MODEL_TYPE_PROPERTY, SenescenceType.class);
    }

    /**
     * Determines whether a tumor component should become senescent.
     *
     * @param <E> the runtime tumor component type.
     *
     * @param tumor the tumor containing the component in question.
     *
     * @param component the component in question.
     *
     * @return {@code true} iff the specified tumor component should
     * become senescent.
     */
    public abstract <E extends TumorComponent> boolean senesce(LatticeTumor<E> tumor, E component);

    /**
     * Returns the enumerated model type.
     *
     * @return the enumerated model type.
     */
    public abstract SenescenceType getType();
}
