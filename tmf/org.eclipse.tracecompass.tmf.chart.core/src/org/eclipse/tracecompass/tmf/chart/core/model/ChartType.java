/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.model;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.model.Messages;

/**
 * Supported types of charts.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public enum ChartType {

    /**
     * Defines a bar chart
     */
    BAR_CHART(checkNotNull(Messages.ChartModel_EnumBarChart)),
    /**
     * Defines a scatter chart
     */
    SCATTER_CHART(checkNotNull(Messages.ChartModel_EnumScatterChart)),
    /**
     * Defines a pie chart
     */
    PIE_CHART(checkNotNull(Messages.ChartModel_EnumPieChart));

    /**
     * Name of the chart type, used in dialog
     */
    private final String fName;

    /**
     * Constructor.
     *
     * @param name
     *            Name of the chart type
     */
    private ChartType(String name) {
        fName = name;
    }

    /**
     * @param name
     *            Name of the chart type
     * @return The chart type
     */
    public static @Nullable ChartType resolveChartType(String name) {
        if (name.equals(BAR_CHART.fName)) {
            return BAR_CHART;
        } else if (name.equals(SCATTER_CHART.fName)) {
            return SCATTER_CHART;
        } else if (name.equals(PIE_CHART.fName)) {
            return PIE_CHART;
        }

        return null;
    }

    @Override
    public String toString() {
        return fName;
    }

}
