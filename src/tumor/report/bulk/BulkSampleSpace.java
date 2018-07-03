
package tumor.report.bulk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jam.lattice.Coord;
import jam.vector.VectorView;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;

/**
 * Enumerates pre-defined spatial distributions used for bulk region
 * sampling.
 *
 * <p>The center of bulk sample is defined by a single vector placed
 * at the tumor center of mass and extended until reaching the tumor
 * surface. The spatial distribution of a set of bulk samples is the
 * collection of basis vectors defined by this enumeration.
 */
public enum BulkSampleSpace {
    /**
     * Six samples centered on the coordinate axes.
     */
    AXES(List.of(VectorView.wrap( 1,  0,  0),
                 VectorView.wrap(-1,  0,  0),
                 VectorView.wrap( 0,  1,  0),
                 VectorView.wrap( 0, -1,  0),
                 VectorView.wrap( 0,  0,  1),
                 VectorView.wrap( 0,  0, -1))),

    /**
     * One sample from each three-dimensional octant (eight in total).
     */
    OCTANTS(List.of(VectorView.wrap( 1,  1,  1),
                    VectorView.wrap(-1,  1,  1),
                    VectorView.wrap(-1, -1,  1),
                    VectorView.wrap( 1, -1,  1),
                    VectorView.wrap( 1,  1, -1),
                    VectorView.wrap(-1,  1, -1),
                    VectorView.wrap(-1, -1, -1),
                    VectorView.wrap( 1, -1, -1)));

    private List<VectorView> basis;

    private BulkSampleSpace(List<VectorView> basis) {
        this.basis = basis;
    }

    /**
     * Collects bulk samples with the spatial distribution defined by
     * this enum value.
     *
     * @param <E> the concrete tumor component type.
     *
     * @param tumor the tumor from which to sample.
     *
     * @param targetSize the minimum number of cells to include in
     * each sample.
     *
     * @return a list of bulk samples corresponding to the basis
     * vectors in this enum.
     */
    public <E extends TumorComponent> List<BulkSample<E>> collect(LatticeTumor<E> tumor, long targetSize) {
        List<BulkSample<E>> samples = new ArrayList<BulkSample<E>>();

        for (VectorView vector : basis) {
            Coord  surfaceSite = tumor.findSurfaceSite(vector);
            Set<E> sampleComp  = tumor.collectBulkSample(surfaceSite, targetSize);

            samples.add(new BulkSample<E>(sampleComp));
        }

        return samples;
    }

    /**
     * Returns the basis vectors that define the spatial distribution
     * of bulk samples.
     *
     * @return the basis vectors that define the spatial distribution
     * of bulk samples.
     */
    public List<VectorView> viewBasis() {
        return basis;
    }
}
