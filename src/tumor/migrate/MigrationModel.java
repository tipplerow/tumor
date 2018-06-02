
package tumor.migrate;

import jam.app.JamProperties;
import jam.lang.JamException;
import jam.lattice.Coord;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;

/**
 * Governs the movement (migration) of components within a tumor.
 *
 * <p>The global model ({@link MigrationModel#global()}) is defined by
 * the system property <b>{@code MigrationModel.modelType}</b>.
 */
public abstract class MigrationModel {
    private static MigrationModel global = null;

    /**
     * Name of the system property that defines the type of migration
     * model.
     */
    public static final String MODEL_TYPE_PROPERTY = "tumor.migrate.modelType";

    /**
     * Returns the global migration model defined by system properties.
     *
     * @return the global migration model defined by system properties.
     */
    public static MigrationModel global() {
        if (global == null)
            global = createGlobal();

        return global;
    }

    private static MigrationModel createGlobal() {
        MigrationType modelType = resolveModelType();

        switch (modelType) {
        case PINNED:
            return PinnedMigration.INSTANCE;

        default:
            throw JamException.runtime("Unknown migration model [%s].", modelType);
        }
    }

    private static MigrationType resolveModelType() {
        return JamProperties.getRequiredEnum(MODEL_TYPE_PROPERTY, MigrationType.class);
    }

    /**
     * Decides whether a tumor component will migrate and, if so,
     * its new location.
     *
     * @param tumor the tumor in question.
     *
     * @param component the component in question.
     *
     * @return the new location for the tumor component, if it
     * migrated to a new site; {@code null} otherwise.
     */
    public abstract Coord migrate(LatticeTumor tumor, TumorComponent component);

    /**
     * Returns the enumerated model type.
     *
     * @return the enumerated model type.
     */
    public abstract MigrationType getType();
}
