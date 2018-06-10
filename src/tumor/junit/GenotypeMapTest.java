
package tumor.junit;

import java.util.List;

import jam.junit.NumericTestBase;

import tumor.carrier.GenotypeMap;
import tumor.carrier.Lineage;
import tumor.carrier.TumorEnv;
import tumor.driver.TumorDriver;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

import org.junit.*;
import static org.junit.Assert.*;

public class GenotypeMapTest extends NumericTestBase {
    static {
        System.setProperty(MutationGenerator.GENERATOR_TYPE_PROPERTY,    "NEUTRAL");
        System.setProperty(MutationGenerator.NEUTRAL_RATE_TYPE_PROPERTY, "POISSON");
        System.setProperty(MutationGenerator.NEUTRAL_MEAN_RATE_PROPERTY, "1.0");
        TumorDriver.junit();
    }

    private final long       initCount  = 100L;
    private final GrowthRate growthRate = GrowthRate.net(0.0);

    private final Lineage  founder  = Lineage.founder(growthRate, initCount);
    private final TumorEnv tumorEnv = TumorEnv.unconstrained(founder);

    private final List<Lineage> children = founder.advance(tumorEnv);
    private final Lineage       child11  = children.get(11);

    private final MutationList founderGenotype = founder.getAccumulatedMutations();
    private final MutationList child11Genotype = child11.getAccumulatedMutations();

    @Test(expected = IllegalArgumentException.class)
    public void testAddDuplicate() {
        GenotypeMap map = GenotypeMap.create(founder);
        map.add(founder);
    }

    @Test public void testBasic() {
        GenotypeMap map = GenotypeMap.create();

        assertFalse(map.contains(founder));
        assertFalse(map.contains(founderGenotype));
        
        assertFalse(map.contains(child11));
        assertFalse(map.contains(child11Genotype));

        map.add(founder);
        
        assertTrue(map.contains(founder));
        assertTrue(map.contains(founderGenotype));
        
        assertFalse(map.contains(child11));
        assertFalse(map.contains(child11Genotype));

        map.addAll(children);

        assertTrue(map.contains(founder));
        assertTrue(map.contains(founderGenotype));
        
        assertTrue(map.contains(child11));
        assertTrue(map.contains(child11Genotype));

        assertTrue(map.remove(founder));
        assertTrue(map.remove(child11Genotype));

        assertFalse(map.remove(founder));
        assertFalse(map.remove(child11Genotype));
        
        assertFalse(map.contains(founder));
        assertFalse(map.contains(founderGenotype));
        
        assertFalse(map.contains(child11));
        assertFalse(map.contains(child11Genotype));
    }

    @Test public void testRemoveAbsent() {
        GenotypeMap map = GenotypeMap.create(children);
        assertFalse(map.remove(founder));
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.GenotypeMapTest");
    }
}
