
package tumor.growth;

import jam.app.JamProperties;
import jam.lang.JamException;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;

/**
 * Quantifies the effect of the local tumor environment on the growth
 * rate of tumor components in that environment.
 *
 * <p>The global model ({@link LocalGrowthModel#global()}) is defined
 * by the system property <b>{@code LocalGrowthModel.modelType}</b>.
 */
public abstract class LocalGrowthModel {
    private static LocalGrowthModel global = null;

    /**
     * Name of the system property that defines the type of local
     * growth model.
     */
    public static final String MODEL_TYPE_PROPERTY = "LocalGrowthModel.modelType";

    /**
     * Returns the global local growth model defined by system
     * properties.
     *
     * @return the global local growth model defined by system
     * properties.
     */
    public static LocalGrowthModel global() {
        if (global == null)
            global = createGlobal();

        return global;
    }

    private static LocalGrowthModel createGlobal() {
        LocalGrowthType modelType = resolveModelType();

        switch (modelType) {
        case INTRINSIC:
            return IntrinsicLocalGrowth.INSTANCE;

        default:
            throw JamException.runtime("Unknown local growth model [%s].", modelType);
        }
    }

    private static LocalGrowthType resolveModelType() {
        return JamProperties.getRequiredEnum(MODEL_TYPE_PROPERTY, LocalGrowthType.class);
    }

    /**
     * Returns the local growth rate for a tumor component.
     *
     * @param tumor the tumor where the component resides.
     *
     * @param component the component under examination.
     *
     * @return the local growth rate for the given component.
     */
    public abstract GrowthRate getLocalGrowthRate(LatticeTumor tumor, TumorComponent component);

    /**
     * Returns the enumerated model type.
     *
     * @return the enumerated model type.
     */
    public abstract LocalGrowthType getType();
}
