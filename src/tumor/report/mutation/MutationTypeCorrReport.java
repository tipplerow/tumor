
package tumor.report.mutation;

import jam.app.JamProperties;
import jam.math.IntRange;
import jam.util.RegexUtil;

import tumor.report.TumorRecordReport;

/**
 * Records the number of mutations of a particular type at a given
 * lattice site.
 */
public abstract class MutationTypeCorrReport extends TumorRecordReport<MutationTypeCorrRecord> {
    /**
     * The number of sites to sample at each recording interval.
     */
    protected final int siteCount;

    /**
     * The mutation types to record.
     */
    protected final String[] typeNames;

    /**
     * Creates a new mutation-type count report.
     *
     * @param sampleIntervalProperty the name of the system property that
     * specifies the number of time steps between sampling/updating/output.
     *
     * @param reportingSizesProperty the name of the system property that
     * specifies the threshold tumor sizes (total number of cells) that 
     * trigger sampling/updating/output.
     *
     * @param siteCountProperty the name of the system property that
     * specifies the number of surface sites to sample at each
     * recording interval.
     *
     * @param typeNamesProperty the name of the system property that
     * specifies the mutation types to record.
     */
    protected MutationTypeCorrReport(String sampleIntervalProperty,
                                     String reportingSizesProperty,
                                     String siteCountProperty,
                                     String typeNamesProperty) {
        super(sampleIntervalProperty, reportingSizesProperty);
        this.siteCount = resolveSiteCount(siteCountProperty);
        this.typeNames = resolveTypeNames(typeNamesProperty);
    }

    private static int resolveSiteCount(String siteCountProperty) {
        return JamProperties.getRequiredInt(siteCountProperty, IntRange.POSITIVE);
    }

    private static String[] resolveTypeNames(String typeNamesProperty) {
        String   nameString = JamProperties.getRequired(typeNamesProperty);
        String[] typeNames  = RegexUtil.COMMA.split(nameString);

        for (int k = 0; k < typeNames.length; ++k)
            typeNames[k] = typeNames[k].trim();

        return typeNames;
    }
}
