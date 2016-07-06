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
 * Base class and factory constructor for charts.
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
     * Dispose the viewer widget.
     */
    void dispose();

    /**
     * Factory method to create a chart.
     *
     * @param parent
     *              Parent composite
     * @param data
     *              Configured data series for the chart
     * @param model
     *              Chart model to use
     * @param title
     *              Title of the chart
     * @return The chart object
     */
    static IChartViewer createChart(Composite parent, ChartData data, ChartModel model) {
        switch (model.getChartType()) {
        case BAR_CHART:
            BarChart barChart = new BarChart(parent, data, model);
            barChart.populate();

            return barChart;
        case SCATTER_CHART:
            ScatterChart scatterChart = new ScatterChart(parent, data, model);
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
