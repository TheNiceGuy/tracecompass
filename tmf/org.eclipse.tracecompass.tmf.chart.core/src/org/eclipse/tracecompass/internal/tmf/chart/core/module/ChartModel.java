/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This object should contain all the information needed to create a
 * chart in the GUI, independently of the actual chart implementation.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartModel {
    /**
     * Supported types of charts
     */
    public enum ChartType {
        /**
         * Defines a bar chart
         */
        BAR_CHART(Messages.ChartModel_EnumBarChart),
        /**
         * Defines a scatter chart
         */
        SCATTER_CHART(Messages.ChartModel_EnumScatterChart),
        /**
         * Defines a pie chart
         */
        PIE_CHART(Messages.ChartModel_EnumPieChart);


        /**
         * Name of the chart type, used in dialog
         */
        private String fName;

        /**
         * Constructor.
         *
         * @param name
         *              Name of the chart type
         */
        private ChartType(String name) {
            fName = name;
        }

        /**
         * @param name
         *                  Name of the chart type
         * @return The chart type
         */
        public static @Nullable ChartType resolveChartType(String name) {
            if(name.equals(BAR_CHART.fName)) {
                return BAR_CHART;
            } else if(name.equals(SCATTER_CHART.fName)) {
                return SCATTER_CHART;
            } else if(name.equals(PIE_CHART.fName)) {
                return PIE_CHART;
            }

            return null;
        }

        @Override
        public String toString() {
            return fName;
        }
    }

    /**
     * Type of chart to plot
     */
    private final ChartType fType;
    /**
     * Title of the chart
     */
    private final String fTitle;
    /**
     * Indicates wheter the X axis is logarithmic
     */
    private final boolean fXLogscale;
    /**
     * Indicates wheter the Y axis is logarithmic
     */
    private final boolean fYLogscale;

    /**
     * Constructor.
     *
     * @param type
     *              Chart type
     * @param title
     *              Title of the chart
     * @param xlog
     *              Whether X axis is logarithmic
     * @param ylog
     *              Whether Y axis is logarithmic
     */
    public ChartModel(ChartType type, String title, boolean xlog, boolean ylog) {
        fType = type;
        fTitle = title;
        fXLogscale = xlog;
        fYLogscale = ylog;
    }

    /**
     * @return The chart type
     */
    public ChartType getChartType() {
        return fType;
    }

    /**
     * @return The title of the chart
     */
    public String getTitle() {
        return fTitle;
    }

    /**
     * @return Whether X axis is logarithmic
     */
    public boolean isXLogscale() {
        return fXLogscale;
    }

    /**
     * @return Whether Y axis is logarithmic
     */
    public boolean isYLogscale() {
        return fYLogscale;
    }
}
