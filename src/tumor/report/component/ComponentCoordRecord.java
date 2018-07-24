
package tumor.report.component;

import java.util.ArrayList;
import java.util.Collection;

import jam.lattice.Coord;
import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.report.TumorRecord;

/**
 * Records the spatial coordinate of every component in the active
 * tumor.
 */
public final class ComponentCoordRecord extends TumorRecord implements ReportRecord {
    private final long  cellCount;
    private final Coord compCoord;

    private ComponentCoordRecord(long cellCount, Coord compCoord) {
        this.cellCount = cellCount;
        this.compCoord = compCoord;
    }

    /**
     * Collects the component coordinate records for the active tumor
     * at this instant in the simulation.
     *
     * @return the component coordinate records for the active tumor
     * at this instant in the simulation.
     */
    public static Collection<ComponentCoordRecord> snap() {
        @SuppressWarnings("unchecked")
            Tumor<TumorComponent> tumor = (Tumor<TumorComponent>) TumorDriver.global().getTumor();

        Collection<TumorComponent> components = tumor.viewComponents();
        Collection<ComponentCoordRecord> records = new ArrayList<ComponentCoordRecord>();

        for (TumorComponent component : components)
            records.add(new ComponentCoordRecord(component.countCells(), tumor.locateComponent(component)));

        return records;
    }

    /**
     * Returns the number of cells in the component described by this
     * record.
     *
     * @return the number of cells in the component described by this
     * record.
     */
    public long getCellCount() {
        return cellCount;
    }

    /**
     * Returns the coordinate of the component described by this
     * record.
     *
     * @return the coordinate of the component described by this
     * record.
     */
    public Coord getComponentCord() {
        return compCoord;
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getTimeStep());
        builder.append(cellCount);
        builder.append(compCoord.x);
        builder.append(compCoord.y);
        builder.append(compCoord.z);

        return builder.toString();
    }

    @Override public String getBaseName() {
        return ComponentCoordReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("timeStep");
        builder.append("cellCount");
        builder.append("compX");
        builder.append("compY");
        builder.append("compZ");

        return builder.toString();
    }
}
