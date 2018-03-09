
package tumor.carrier;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;

/**
 * Represents the local environment within a tumor (surrounding a
 * cell, lineage, or deme).
 *
 * <p>The presence or absence of anatomical constraints and supporting
 * tissues (vasculature), or the availability of nutrients, affect the
 * growth rate of tumor cells.  This class quantifies that effect by
 * adjusting the intrinsic growth rate in a manner appropriate for the
 * (time-varying) local environment.
 */
public abstract class TumorEnv {
    private final TumorEnv parent;

    /**
     * Creates a new local environment, possibly inheriting and/or
     * overriding some factors in another environment.
     *
     * @param parent the parent (enclosing) environment; {@code null}
     * for a top-level environment.
     */
    protected TumorEnv(TumorEnv parent) {
        this.parent = parent;
    }

    /**
     * The single unrestricted environment: cell and deme division is
     * always allowed and growth rates, mutation generators, and deme
     * size limits are never altered.
     */
    public static final TumorEnv UNRESTRICTED = new UnrestrictedEnv();

    private static final class UnrestrictedEnv extends TumorEnv {
        private UnrestrictedEnv() {
            super(null);
        }

        @Override public boolean allowCellDivision() {
            return true;
        }

        @Override public boolean allowDemeDivision() {
            return true;
        }

        @Override public GrowthRate getLocalGrowthRate(TumorKernel kernelObj) {
            return kernelObj.getGrowthRate();
        }

        @Override public MutationGenerator getLocalMutationGenerator(TumorKernel kernelObj) {
            return kernelObj.getMutationGenerator();
        }

        @Override public long getLocalMaximumDemeSize(Deme deme) {
            return deme.getMaximumSize();
        }
    }

    /**
     * Returns the parent of this environment.
     *
     * @return the parent of this environment, or {@code null} if this
     * environment is a top-level environment.
     */
    public final TumorEnv getParent() {
        return parent;
    }

    /**
     * Determines whether this environment allows for tumor cells (or
     * cells within lineages) to divide.
     *
     * @return {@code true} iff this environment allows cell division.
     */
    public abstract boolean allowCellDivision();

    /**
     * Determines whether this environment allows for demes to divide.
     *
     * @return {@code true} iff this environment allows deme division.
     */
    public abstract boolean allowDemeDivision();

    /**
     * Returns the local growth rate for a cell or lineage: a function
     * of the intrinsic growth rate and the factors operating in this
     * local environment.
     *
     * @param kernelObj a tumor kernel object present in this local
     * environment.
     *
     * @return the local growth rate.
     */
    public abstract GrowthRate getLocalGrowthRate(TumorKernel kernelObj);

    /**
     * Returns the local mutation generator for a cell or lineage: a
     * function of the intrinsic mutation generator and the factors
     * operating in this local environment.
     *
     * @param kernelObj a tumor kernel object present in this local
     * environment.
     *
     * @return the local mutation generator.
     */
    public abstract MutationGenerator getLocalMutationGenerator(TumorKernel kernelObj);

    /**
     * Returns the maximum number of tumor cells that can be contained
     * in a single deme.
     *
     * <p>Demes larger than this size must either (1) divide into two
     * demes if the local environment permits (e.g., if adjacent space
     * is available), or (2) shrink in size via cell death.
     *
     * @param deme a deme present in this local environment.
     *
     * @return the maximum number of tumor cells that can be contained
     * in the specified deme.
     */
    public abstract long getLocalMaximumDemeSize(Deme deme);

    private static abstract class OverrideEnv extends TumorEnv {
        //
        // Base class for overriding local environments.  Any number
        // of overrides can be nested...
        //
        protected OverrideEnv(TumorEnv parent) {
            super(parent);
        }

        @Override public boolean allowCellDivision() {
            return getParent().allowCellDivision();
        }

        @Override public boolean allowDemeDivision() {
            return getParent().allowDemeDivision();
        }

        @Override public GrowthRate getLocalGrowthRate(TumorKernel kernelObj) {
            return getParent().getLocalGrowthRate(kernelObj);
        }

        @Override public MutationGenerator getLocalMutationGenerator(TumorKernel kernelObj) {
            return getParent().getLocalMutationGenerator(kernelObj);
        }

        @Override public long getLocalMaximumDemeSize(Deme deme) {
            return getParent().getLocalMaximumDemeSize(deme);
        }
    }

    /**
     * Returns an <em>overriding environment</em> identical to this
     * environment but with all cell and deme division forbidden:
     * birth rates are set to zero but death rates are unchanged.
     *
     * @return an overriding no-birth environment.
     */
    public final TumorEnv noBirth() {
        return new NoBirthEnv(this);
    }

    private static final class NoBirthEnv extends OverrideEnv {
        NoBirthEnv(TumorEnv parent) {
            super(parent);
        }

        @Override public boolean allowCellDivision() {
            return false;
        }

        @Override public boolean allowDemeDivision() {
            return false;
        }

        @Override public GrowthRate getLocalGrowthRate(TumorKernel kernelObj) {
            return kernelObj.getGrowthRate().noBirth();
        }
    }

    /**
     * Returns an <em>overriding environment</em> identical to this
     * environment but with deme division forbidden.
     *
     * @return an overriding no-birth environment.
     */
    public final TumorEnv noDemeDivision() {
        return new NoDemeDivisionEnv(this);
    }

    private static final class NoDemeDivisionEnv extends OverrideEnv {
        NoDemeDivisionEnv(TumorEnv parent) {
            super(parent);
        }

        @Override public boolean allowDemeDivision() {
            return false;
        }
    }

    /**
     * Returns an <em>overriding environment</em> identical to this
     * environment but with net population growth forbidden.
     *
     * <p>In a no-growth environment, growth rates with population
     * growth factors less than or equal to 1.0 are left unchanged.
     * In other growth rates, birth and death rates are adjusted so
     * that the population growth factor becomes 1.0 and the overall
     * event rate is unchanged.
     *
     * @return an overriding no-growth environment.
     */
    public final TumorEnv noGrowth() {
        return new NoGrowthEnv(this);
    }

    private static final class NoGrowthEnv extends OverrideEnv {
        NoGrowthEnv(TumorEnv parent) {
            super(parent);
        }

        @Override public GrowthRate getLocalGrowthRate(TumorKernel kernelObj) {
            return kernelObj.getGrowthRate().noGrowth();
        }
    }
}
