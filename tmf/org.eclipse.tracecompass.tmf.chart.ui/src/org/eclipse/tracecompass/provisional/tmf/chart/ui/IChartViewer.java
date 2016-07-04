/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.provisional.tmf.chart.ui;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartModel;
import org.eclipse.tracecompass.tmf.chart.ui.BarChart;
import org.eclipse.tracecompass.tmf.chart.ui.ScatterChart;

import com.google.common.collect.ImmutableList;

/**
 * Base class for any chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IChartViewer {
    /**
     * List of standard colors
     */
    public static final List<@NonNull Color> COLORS = ImmutableList.of(
                new Color(Display.getDefault(),  72, 120, 207),
                new Color(Display.getDefault(), 106, 204, 101),
                new Color(Display.getDefault(), 214,  95,  95),
                new Color(Display.getDefault(), 180, 124, 199),
                new Color(Display.getDefault(), 196, 173, 102),
                new Color(Display.getDefault(), 119, 190, 219)
                );

    /**
     * List of "light" colors (when unselected)
     */
    public static final List<@NonNull Color> LIGHT_COLORS = ImmutableList.of(
                new Color(Display.getDefault(), 173, 195, 233),
                new Color(Display.getDefault(), 199, 236, 197),
                new Color(Display.getDefault(), 240, 196, 196),
                new Color(Display.getDefault(), 231, 213, 237),
                new Color(Display.getDefault(), 231, 222, 194),
                new Color(Display.getDefault(), 220, 238, 246)
                );

    /**
     * Dispose the viewer widget.
     */
    void dispose();

    /**
     * Factory method creates a chart from a data series
     *
     * @param parent
     *              parent composite
     * @param data
     *              configured data series for the chart
     * @param model
     *              chart model to use
     * @param title
     *              title of the chart
     * @return chart object
     */
    static IChartViewer createChart(Composite parent, ChartData data, ChartModel model, String title) {
        switch (model.getChartType()) {
        case BAR_CHART:
            BarChart barChart = new BarChart(parent, data, model, title);
            barChart.populate();

            return barChart;
        case SCATTER_CHART:
            ScatterChart scatterChart = new ScatterChart(parent, data, model, title);
            scatterChart.populate();

            return scatterChart;
        case PIE_CHART:
            /**
             * TODO
             */
        default:
            return null;
        }
    }
}
