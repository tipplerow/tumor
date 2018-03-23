
package tumor.divide;

/**
 * Enumerates the types of deme division models.
 */
public enum DivisionType {
    /**
     * A deme divides iff the ratio of its size (number of cells) to
     * the site capacity of its current location exceeds a fractional
     * threshold value.
     */
    THRESHOLD;
}
