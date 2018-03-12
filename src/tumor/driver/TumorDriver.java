
package tumor.driver;

import java.io.File;
import java.io.PrintWriter;

import jam.app.JamProperties;
import jam.io.FileUtil;
import jam.io.IOUtil;
import jam.math.DoubleRange;

import tumor.growth.GrowthRate;

/**
 * Provides common features for tumor simulation applications.
 */
public abstract class TumorDriver {
    private final String[] propertyFiles;
    
    private final File reportDir;
    
    /**
     * Name of the system property which specifies the directory in
     * which to write the report files.  If this is not specified, the
     * report files will be written into the directory containing the
     * first property file.
     */
    public static final String REPORT_DIR_PROPERTY = "TumorDriver.reportDir";

    /**
     * Creates a new driver and reads system properties from a set of
     * property files.
     *
     * @param propertyFiles one or more files containing the system
     * properties that define the simulation parameters.
     *
     * @throws IllegalArgumentException unless at least one property
     * file is specified.
     */
    protected TumorDriver(String[] propertyFiles) {
        if (propertyFiles.length < 1)
            throw new IllegalArgumentException("At least one property file is required.");
        
        this.propertyFiles = propertyFiles;
        JamProperties.loadFiles(propertyFiles, false);
        
        this.reportDir = resolveReportDir();
    }

    private File resolveReportDir() {
        String defaultDir = getDefaultReportDir();
        String reportDir  = JamProperties.getOptional(REPORT_DIR_PROPERTY, defaultDir);

        return new File(reportDir);
    }

    private String getDefaultReportDir() {
        //
        // Parent directory of the first property file...
        //
        return FileUtil.getParentName(new File(propertyFiles[0]));
    }

    /**
     * Reads the global growth rate from system properties specifying
     * the birth and death rates.
     *
     * @param birthProperty the name of the system property defining
     * the birth rate.
     *
     * @param deathProperty the name of the system property defining
     * the death rate.
     *
     * @return the growth rate defined by the given system properties.
     */
    public static GrowthRate resolveGrowthRate(String birthProperty,
                                               String deathProperty) {
        double birthRate = JamProperties.getRequiredDouble(birthProperty, DoubleRange.NON_NEGATIVE);
        double deathRate = JamProperties.getRequiredDouble(deathProperty, DoubleRange.NON_NEGATIVE);

        return new GrowthRate(birthRate, deathRate);
    }

    /**
     * Returns the directory where report files will be written.
     *
     * @return the directory where report files will be written.
     */
    public File getReportDir() {
        return reportDir;
    }

    /**
     * Returns a file located in the report directory.
     *
     * @param baseName the base name for the report file.
     *
     * @return a file with the given base name located in the report
     * directory.
     */
    public File getReportFile(String baseName) {
        return new File(reportDir, baseName);
    }

    /**
     * Opens a writer for a file located in the report directory.
     *
     * @param baseName the base name for the report file.
     *
     * @return a writer for the specified file.
     *
     * @throws RuntimeException unless the file is open for writing.
     */
    public PrintWriter openWriter(String baseName) {
        return IOUtil.openWriter(getReportFile(baseName), false);
    }
}
