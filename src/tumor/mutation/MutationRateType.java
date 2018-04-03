
package tumor.mutation;

/**
 * Enumerates the types of mutation rates.
 */
public enum MutationRateType {
    /**
     * Mutations arrive via a Poisson process with fixed mean; multiple
     * mutations are possible but very unlikely for small mean rates.
     */
    POISSON,

    /**
     * Exactly one mutation arrives with a fixed probability.
     */
    UNIFORM,

    /**
     * No mutations are generated.
     */
    ZERO;
}
