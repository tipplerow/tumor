
package tumor.mutation;

/**
 * Enumerates the types of mutation generators.
 */
public enum MutationGeneratorType {
    /**
     * No mutations are ever generated.
     */
    EMPTY,

    /**
     * Only neutral mutations are generated.
     */
    NEUTRAL,

    /**
     * Both neutral mutations and selective mutations (with a fixed
     * selection coefficient) are generated; the neutral and selective
     * mutations arrive with different rates.
     */
    NEUTRAL_SELECTIVE_FIXED,

    /**
     * Selective mutations are generated with a fixed selection
     * coefficient.
     */
    SELECTIVE_FIXED;
}
