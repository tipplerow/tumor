
package tumor.mutation;

/**
 * Enumerates classes of mutations.
 */
public enum MutationType {
    /**
     * Mutations that generate immunologically active neoantigens.
     */
    NEOANTIGEN,

    /**
     * Generic neutral (passenger) mutations.
     */
    NEUTRAL,

    /**
     * Mutations that confer resistance to treatment.
     */
    RESISTANCE,

    /**
     * Selective mutations described by a single selection coefficient.
     */
    SCALAR;
}
