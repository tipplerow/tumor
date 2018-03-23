
package tumor.divide;

import jam.app.JamProperties;
import jam.lang.JamException;
import jam.lattice.Coord;
import jam.math.Probability;

import tumor.carrier.Deme;
import tumor.lattice.DemeLatticeTumor;

/**
 * Governs the division of demes in lattice tumors.
 *
 * <p>The global model ({@link DivisionModel#global()}) is defined by
 * the system property <b>{@code DivisionModel.modelType}</b>.
 */
public abstract class DivisionModel {
    private static DivisionModel global = null;

    /**
     * Name of the system property that defines the type of division
     * model.
     */
    public static final String MODEL_TYPE_PROPERTY = "DivisionModel.modelType";

    /**
     * The fixed retention probability for deme division.
     */
    public static final Probability RETENTION_PROBABILITY = Probability.ONE_HALF;

    /**
     * Returns the global division model defined by system properties.
     *
     * @return the global division model defined by system properties.
     */
    public static DivisionModel global() {
        if (global == null)
            global = createGlobal();

        return global;
    }

    private static DivisionModel createGlobal() {
        DivisionType modelType = resolveModelType();

        switch (modelType) {
        case THRESHOLD:
            return ThresholdDivision.createGlobal();

        default:
            throw JamException.runtime("Unknown division model [%s].", modelType);
        }
    }

    private static DivisionType resolveModelType() {
        return JamProperties.getRequiredEnum(MODEL_TYPE_PROPERTY, DivisionType.class);
    }

    /**
     * Decides whether a deme will divide and, if it does, where the
     * clone will be placed.
     *
     * @param tumor the tumor in question.
     *
     * @param deme the deme in question.
     *
     * @return the result of the deme division, if one occurred;
     * {@code null} otherwise.
     */
    public abstract DivisionResult divide(DemeLatticeTumor tumor, Deme deme);

    /**
     * Returns the enumerated model type.
     *
     * @return the enumerated model type.
     */
    public abstract DivisionType getType();
}
