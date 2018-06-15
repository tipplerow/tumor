
package tumor.junit;

import java.util.List;

import jam.junit.NumericTestBase;

import tumor.carrier.GenotypeMap;
import tumor.carrier.Lineage;
import tumor.carrier.TumorEnv;
import tumor.driver.TumorDriver;
import tumor.growth.GrowthRate;
import tumor.mutation.Genotype;
import tumor.mutation.MutationGenerator;

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
    private final TumorEnv tumorEnv = TumorEnv.unconstrained(growthRate);

    private final List<Lineage> children = founder.advance(tumorEnv);

    private final Lineage child = children.get(11);
    private final Lineage clone = founder.divide(33);

    private final Genotype founderGenotype = founder.getGenotype();
    private final Genotype childGenotype   = child.getGenotype();
    private final Genotype cloneGenotype   = clone.getGenotype();


    @Test public void testBasic() {
        GenotypeMap map = GenotypeMap.create();

        assertFalse(map.contains(founder));
        assertFalse(map.contains(founderGenotype));
        
        assertFalse(map.contains(child));
        assertFalse(map.contains(childGenotype));

        map.add(founder);
        
        assertTrue(map.contains(founder));
        assertTrue(map.contains(founderGenotype));
        
        assertFalse(map.contains(child));
        assertFalse(map.contains(childGenotype));

        map.addAll(children);

        assertTrue(map.contains(founder));
        assertTrue(map.contains(founderGenotype));
        
        assertTrue(map.contains(child));
        assertTrue(map.contains(childGenotype));

        assertTrue(map.remove(founder));
        assertTrue(map.remove(childGenotype));

        assertFalse(map.remove(founder));
        assertFalse(map.remove(childGenotype));
        
        assertFalse(map.contains(founder));
        assertFalse(map.contains(founderGenotype));
        
        assertFalse(map.contains(child));
        assertFalse(map.contains(childGenotype));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDuplicate() {
        GenotypeMap map = GenotypeMap.create(founder, clone);
        map.getUnique(founder.getGenotype());
    }

    @Test public void testRemoveAbsent() {
        GenotypeMap map = GenotypeMap.create(children);
        assertFalse(map.remove(founder));
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.GenotypeMapTest");
    }
}
