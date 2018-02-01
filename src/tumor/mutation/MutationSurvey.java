
package tumor.mutation;

import com.google.common.collect.Multiset;

import jam.lattice.Coord;
import jam.math.VectorMoment;

/**
 * Records every location of a mutation within a tumor and computes
 * some summary statistics.
 */
public final class MutationSurvey {
    private final int timeStep;
    private final int carrierCount;
    private final Coord origin;
    private final Mutation mutation;
    private final VectorMoment vecMoment;

    private MutationSurvey(Mutation mutation, int timeStep, Coord origin, Multiset<Coord> locations) {
        this.mutation = mutation;
        this.timeStep = timeStep;
        this.origin   = origin;
        this.vecMoment = VectorMoment.compute(locations);
        this.carrierCount = locations.size();
    }

    /**
     * Creates a new mutation survey.
     *
     * @param mutation the mutation being surveyed.
     *
     * @param timeStep the index of the latest simulation time step.
     *
     * @param origin the location where the mutation originated.
     *
     * @param locations the locations of cells or lineages carrying
     * the mutation, represented as a {@code Multiset} because more
     * than one carrier may be present at each location.
     *
     * @return the new mutation survey.
     */
    public static MutationSurvey create(Mutation mutation, int timeStep, Coord origin, Multiset<Coord> locations) {
        return new MutationSurvey(mutation, timeStep, origin, locations);
    }

    /**
     * Returns the surveyed mutation.
     *
     * @return the surveyed mutation.
     */
    public Mutation getMutation() {
        return mutation;
    }

    /**
     * Returns the index of the simulation time step when the survey
     * was taken.
     *
     * @return the index of the simulation time step when the survey
     * was taken.
     */
    public int getTimeStep() {
        return timeStep;
    }

    /**
     * Returns the location where the mutation originated.
     *
     * @return the location where the mutation originated.
     */
    public Coord getOrigin() {
        return origin;
    }

    /**
     * Returns the number of cells carrying this mutation.
     *
     * @return the number of cells carrying this mutation.
     */
    public int getCarrierCount() {
        return carrierCount;
    }

    /**
     * Returns the vector moment for the spatial distribution of
     * mutations.
     *
     * @return the vector moment for the spatial distribution of
     * mutations.
     */
    public VectorMoment getMoment() {
        return vecMoment;
    }
}
