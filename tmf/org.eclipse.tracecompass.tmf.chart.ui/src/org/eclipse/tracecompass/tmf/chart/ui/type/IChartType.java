package org.eclipse.tracecompass.tmf.chart.ui.type;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartNumericalAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartType;

/**
 * Interface for implementing chart type specific carasteristics.
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
     * Method used for checking if a X aspect is valid for the chart.
     *
     * @param aspect
     *            The aspect to check
     * @param filter
     *            The aspect used for filtering
     * @return {@code true} if the aspect passes the filter or {@code false} if
     *         the aspect didn't pass the filter
     */
    boolean checkIfXAspectValid(IDataChartAspect aspect, @Nullable IDataChartAspect filter);

    /**
     * Method used for checking if a Y aspect is valid for the chart.
     *
     * @param aspect
     *            The aspect to check
     * @param filter
     *            The aspect used for filtering
     * @return {@code true} if the aspect passes the filter or {@code false} if
     *         the aspect didn't pass the filter
     */
    boolean checkIfYAspectValid(IDataChartAspect aspect, @Nullable IDataChartAspect filter);

    /**
     * Method that checks if the X axis logarithmic scale can be enabled.
     *
     * @param filter
     *            An aspect that represent the X series
     * @return {@code true} if it can be logarithmic, {@code false} if can't be
     */
    boolean checkIfXLogscalePossible(@Nullable IDataChartAspect filter);

    /**
     * Method that checks if the Y axis logarithmic scale can be enabled.
     *
     * @param filter
     *            An aspect that represent the Y series
     * @return {@code true} if it can be logarithmic, {@code false} if can't be
     */
    boolean checkIfYLogscalePossible(@Nullable IDataChartAspect filter);

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

    /**
     * Util method for checking if an aspect is an instance of
     * {@link IDataChartNumericalAspect}.
     *
     * @param aspect
     *            The aspect to check
     * @return {@code true} if the aspect is an instance of
     *         {@link IDataChartNumericalAspect}, {@code false} if the aspect is
     *         something else or {@code null}
     */
    public static boolean checkIfNumerical(IDataChartAspect aspect) {
        if (aspect instanceof IDataChartNumericalAspect) {
            return true;
        }

        return false;
    }

}