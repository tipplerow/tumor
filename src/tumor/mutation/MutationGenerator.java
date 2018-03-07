
package tumor.mutation;

/**
 * Generates mutations in a carrier.
 */
public interface MutationGenerator {
    /**
     * Stochastically generates the mutations that originate in a
     * single daughter carrier during cell division.
     *
     * <p>Note that the list will frequently be empty because
     * mutations are typically rare.
     *
     * @return the mutations that originated in the daughter carrier
     * (or an empty list if no mutations occurred).
     */
    public abstract MutationList generate();

    /**
     * Returns the rate at which mutations arrive. 
     *
     * @return the rate at which mutations arrive. 
     */
    public abstract MutationRate getMutationRate();
}
