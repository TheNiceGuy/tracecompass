package org.eclipse.tracecompass.tmf.chart.ui.type;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartType;

/**
 * Interface for implementing chart-specific carasteristics.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IChartType {

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
     * Method used for filtering aspect in the X axis selection.
     *
     * @param aspect
     *            The aspect to check
     * @param filter
     *            The aspect used for filtering
     * @return {@code true} if the aspect passes the filter or {@code false} if
     *         the aspect didn't pass the filter
     */
    boolean filterX(IDataChartAspect aspect, @Nullable IDataChartAspect filter);

    /**
     * Method used for filtering aspect in the Y axis selection.
     *
     * @param aspect
     *            The aspect to check
     * @param filter
     *            The aspect used for filtering
     * @return {@code true} if the aspect passes the filter or {@code false} if
     *         the aspect didn't pass the filter
     */
    boolean filterY(IDataChartAspect aspect, @Nullable IDataChartAspect filter);

    /**
     * Util method for checking if an aspect has the same class as the filter.
     *
     * @param aspect
     *            The aspect to check
     * @param filter
     *            The aspect used for filtering
     * @return {@code true} if both aspects share the same class or the filter
     *         is {@code null}, or {@code false} if they are different
     */
    public static boolean filterSameAspect(IDataChartAspect aspect, @Nullable IDataChartAspect filter) {
        if (filter != null) {
            if (aspect.getClass() != filter.getClass()) {
                return false;
            }
        }

        return true;
    }

}