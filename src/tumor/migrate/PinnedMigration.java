
package tumor.migrate;

import jam.lattice.Coord;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;

/**
 * Implements a migration model where components are fixed (pinned in
 * place) and never migrate.
 */
public final class PinnedMigration extends MigrationModel {
    private PinnedMigration() {}

    /**
     * The global pinned migration model.
     */
    public static final MigrationModel INSTANCE = new PinnedMigration();

    @Override public Coord migrate(LatticeTumor tumor, TumorComponent component) {
        //
        // Components never migrate...
        //
        return null;
    }

    @Override public MigrationType getType() {
        return MigrationType.PINNED;
    }
}
