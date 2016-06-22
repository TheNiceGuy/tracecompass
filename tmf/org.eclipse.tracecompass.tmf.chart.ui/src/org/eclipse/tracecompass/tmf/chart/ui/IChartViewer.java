/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;

import com.google.common.collect.ImmutableList;

/**
 * Base class for any chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IChartViewer {

    /**
     * List of colors used when coloring series.
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
     * This method creates a chart from a data series.
     *
     * @param parent parent composite
     * @param series data series to plot
     * @return chart object
     */
    static IChartViewer createChart(Composite parent, DataSeries series) {
        switch (series.getChartType()) {
        case BAR_CHART:
            return new BarChart(parent, series);
        case SCATTER_CHART:
            return new ScatterChart(parent, series);
        case PIE_CHART:
            /**
             * TODO
             */
            return null;
        default:
            return null;
        }
    }
}
