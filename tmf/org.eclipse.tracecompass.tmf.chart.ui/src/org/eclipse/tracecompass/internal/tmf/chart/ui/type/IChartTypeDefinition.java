package org.eclipse.tracecompass.internal.tmf.chart.ui.type;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DescriptorTypeVisitor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DescriptorTypeVisitor.DescriptorType;

/**
 * Interface for implementing chart type specific carasteristics.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IChartTypeDefinition {

    /**
     * Accessor that returns the identifier of the chart.
     *
     * @return Identifier of the chart
     */
    ChartType getType();

    /**
     * Accessor that returns image data of the chart's icon.
     *
     * @return Image data of an icon
     */
    ImageData getImageData();

    /**
     * Method used for checking if a X descriptor is valid for the chart.
     *
     * @param desc
     *            The descriptor to check
     * @param filter
     *            The descriptor used for filtering
     * @return {@code true} if the descriptor passes the filter or {@code false}
     *         if the descriptor didn't pass the filter
     */
    boolean checkIfXDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter);

    /**
     * Method used for checking if a Y descriptor is valid for the chart.
     *
     * @param desc
     *            The descriptor to check
     * @param filter
     *            The descriptor used for filtering
     * @return {@code true} if the descriptor passes the filter or {@code false}
     *         if the descriptor didn't pass the filter
     */
    boolean checkIfYDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter);

    /**
     * Method that checks if the X axis logarithmic scale can be enabled.
     *
     * @param filter
     *            An descriptor that represent the X series
     * @return {@code true} if it can be logarithmic, {@code false} if can't be
     */
    boolean checkIfXLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter);

    /**
     * Method that checks if the Y axis logarithmic scale can be enabled.
     *
     * @param filter
     *            An descriptor that represent the Y series
     * @return {@code true} if it can be logarithmic, {@code false} if can't be
     */
    boolean checkIfYLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter);

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    /**
     * Util method for checking if an descriptor has the same class as the
     * filter.
     *
     * @param desc
     *            The descriptor to check
     * @param filter
     *            The descriptor used for filtering
     * @return {@code true} if both descriptors share the same class or the
     *         filter is {@code null}, or {@code false} if they are different
     */
    public static boolean filterSameDescriptor(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter) {
        if (filter != null) {
            if (desc.getClass() != filter.getClass()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Util method for checking if an descriptor is numerical
     *
     * @param desc
     *            The descriptor to check
     * @return {@code true} if the descriptor is numerical, {@code false} if the
     *         descriptor is something else or {@code null}
     */
    public static boolean checkIfNumerical(IDataChartDescriptor<?, ?> desc) {
        DescriptorTypeVisitor visitor = new DescriptorTypeVisitor();
        desc.accept(visitor);

        if (visitor.checkIfType(DescriptorType.NUMERICAL)) {
            return true;
        }

        return false;
    }

}