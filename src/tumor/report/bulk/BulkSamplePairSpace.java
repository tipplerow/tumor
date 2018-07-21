
package tumor.report.bulk;

import java.util.ArrayList;
import java.util.List;

import jam.app.JamLogger;
import jam.vector.VectorPair;
import jam.vector.VectorView;

import tumor.report.TumorSample;
import tumor.report.TumorSamplePair;

/**
 * Enumerates pre-defined spatial distributions used for paired bulk
 * region sampling.
 */
public enum BulkSamplePairSpace {
    /**
     * One maximally-distant sample pair along each coordinate axis
     * (three in total).
     */
    AXES(List.of(VectorPair.of(VectorView.wrap(1, 0, 0), VectorView.wrap(-1,  0,  0)),
                 VectorPair.of(VectorView.wrap(0, 1, 0), VectorView.wrap( 0, -1,  0)),
                 VectorPair.of(VectorView.wrap(0, 0, 1), VectorView.wrap( 0,  0, -1)))),

    /**
     * One maximally-distant sample pair from each direction in the
     * Moore neighborhood (thirteen in total).
     */
    MOORE(List.of(VectorPair.of(VectorView.wrap( 1,  0,  0), VectorView.wrap(-1,  0,  0)),
                  VectorPair.of(VectorView.wrap( 0,  1,  0), VectorView.wrap( 0, -1,  0)),
                  VectorPair.of(VectorView.wrap( 0,  0,  1), VectorView.wrap( 0,  0, -1)),

                  VectorPair.of(VectorView.wrap( 0, -1, -1), VectorView.wrap( 0,  1,  1)),
                  VectorPair.of(VectorView.wrap(-1,  0, -1), VectorView.wrap( 1,  0,  1)),
                  VectorPair.of(VectorView.wrap( 0,  1, -1), VectorView.wrap( 0, -1,  1)),
                  VectorPair.of(VectorView.wrap( 1,  0, -1), VectorView.wrap(-1,  0,  1)),
                  VectorPair.of(VectorView.wrap(-1, -1,  0), VectorView.wrap( 1,  1,  0)),
                  VectorPair.of(VectorView.wrap(-1,  1,  0), VectorView.wrap( 1, -1,  0)),

                  VectorPair.of(VectorView.wrap( 1,  1,  1), VectorView.wrap(-1, -1, -1)),
                  VectorPair.of(VectorView.wrap(-1,  1,  1), VectorView.wrap( 1, -1, -1)),
                  VectorPair.of(VectorView.wrap(-1, -1,  1), VectorView.wrap( 1,  1, -1)),
                  VectorPair.of(VectorView.wrap( 1, -1,  1), VectorView.wrap(-1,  1, -1)))),

    /**
     * One maximally-distant sample pair from each three-dimensional
     * octant (four in total).
     */
    OCTANTS(List.of(VectorPair.of(VectorView.wrap( 1,  1,  1), VectorView.wrap(-1, -1, -1)),
                    VectorPair.of(VectorView.wrap(-1,  1,  1), VectorView.wrap( 1, -1, -1)),
                    VectorPair.of(VectorView.wrap(-1, -1,  1), VectorView.wrap( 1,  1, -1)),
                    VectorPair.of(VectorView.wrap( 1, -1,  1), VectorView.wrap(-1,  1, -1))));

    private final List<VectorPair> basisPairs;

    private BulkSamplePairSpace(List<VectorPair> basisPairs) {
        this.basisPairs = basisPairs;
    }

    /**
     * Collects paired bulk samples with the spatial distribution
     * defined by this enum value.
     *
     * @param targetSize the minimum number of cells to include in
     * each sample.
     *
     * @return a list of paired bulk samples corresponding to the
     * basis vectors in this enum.
     */
    public List<TumorSamplePair> collect(long targetSize) {
        List<TumorSamplePair> samplePairs = new ArrayList<TumorSamplePair>(basisPairs.size());

        for (VectorPair basisPair : basisPairs) {
            VectorView basis1 = basisPair.first;
            VectorView basis2 = basisPair.second;

            TumorSample sample1 = TumorSample.bulk(basis1, targetSize);
            TumorSample sample2 = TumorSample.bulk(basis2, targetSize);

            samplePairs.add(TumorSamplePair.of(sample1, sample2));
        }

        return samplePairs;
    }

    /**
     * Returns the paired basis vectors that define the spatial
     * distribution of bulk samples.
     *
     * @return the paired basis vectors that define the spatial
     * distribution of bulk samples.
     */
    public List<VectorPair> viewBasisPairs() {
        return basisPairs;
    }
}
