
package tumor.report.bulk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jam.vector.VectorView;

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
