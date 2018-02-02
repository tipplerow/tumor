
package tumor.mutation;

/**
 * Generates a series of neutral mutations.
 */
public final class NeutralGenerator extends IndependentGenerator {
    private NeutralGenerator() {}

    public static final NeutralGenerator INSTANCE = new NeutralGenerator();

    @Override public Mutation generateOne() {
        return new NeutralMutation();
    }
}
