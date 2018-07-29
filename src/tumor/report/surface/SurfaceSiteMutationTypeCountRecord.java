
package tumor.report.surface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import jam.lattice.Coord;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;
import tumor.mutation.Genotype;
import tumor.mutation.Mutation;
import tumor.mutation.MutationType;
import tumor.report.mutation.MutationTypeSiteCountRecord;

/**
 * Records the number of mutations of a particular type present at a
 * site on the surface of the active tumor.
 */
public final class SurfaceSiteMutationTypeCountRecord extends MutationTypeSiteCountRecord {
    private SurfaceSiteMutationTypeCountRecord(Coord        siteCoord,
                                               long         siteCellCount,
                                               long         tumorCellCount,
                                               MutationType mutationType,
                                               long         mutationCount) {
        super(siteCoord, siteCellCount, tumorCellCount, mutationType, mutationCount);
    }

    /**
     * Generates type-count records for a given tumor and surface site.
     *
     * @param tumor the active tumor to examine.
     *
     * @param siteCoord the coordinate of the site to examine.
     *
     * @return a list containing one type-count record for each
     * mutation type found at the given site.
     */
    public static List<SurfaceSiteMutationTypeCountRecord>
        generate(LatticeTumor<? extends TumorComponent> tumor, Coord siteCoord) {

        long siteCellCount = 0;
        Multiset<MutationType> typeCounts = HashMultiset.create();
        Set<? extends TumorComponent> components = tumor.viewComponents(siteCoord);

        for (TumorComponent component : components) {
            Genotype genotype = component.getGenotype();
            Iterator<Mutation> iterator = genotype.scanAccumulatedMutations();

            while (iterator.hasNext()) {
                Mutation mutation = iterator.next();
                typeCounts.add(mutation.getType(), (int) component.countCells());
            }

            // Must accumulated the cell count separately just in case
            // a component contains no mutations...
            siteCellCount += component.countCells();
        }

        List<SurfaceSiteMutationTypeCountRecord> records =
            new ArrayList<SurfaceSiteMutationTypeCountRecord>();

        for (MutationType mutationType : typeCounts.elementSet())
            records.add(new SurfaceSiteMutationTypeCountRecord(siteCoord,
                                                               siteCellCount,
                                                               tumor.countCells(),
                                                               mutationType,
                                                               typeCounts.count(mutationType)));

        return records;
    }

    @Override public String getBaseName() {
        return SurfaceSiteMutationTypeCountReport.BASE_NAME;
    }
}
