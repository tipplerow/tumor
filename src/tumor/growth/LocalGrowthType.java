
package tumor.growth;

/**
 * Enumerates the types of local growth models.
 */
public enum LocalGrowthType {
    /**
     * Each tumor component always grows with its intrinsic rate
     * (determined only by its accumulated mutations, independent 
     * of the local environment).
     */
    INTRINSIC;
}
