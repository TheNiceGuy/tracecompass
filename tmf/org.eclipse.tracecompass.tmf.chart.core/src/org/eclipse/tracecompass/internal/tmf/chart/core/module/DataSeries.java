/*******************************************************************************

 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class contains everthing needed to make a chart.
 * TODO: rename it
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataSeries {
    private final ChartType fType;
    private DataDescriptor fXData;
    private Collection<DataDescriptor> fYData;
    private boolean fXLogscale;
    private boolean fYLogscale;

    /**
     * TODO: rename
     */
    public enum ChartType {
        /**
         * Defines a bar chart
         */
        BAR_CHART(Messages.DataSeries_EnumBarChart),
        /**
         * Defines a scatter chart
         */
        SCATTER_CHART(Messages.DataSeries_EnumScatterChart),
        /**
         * Defines a pie chart
         */
        PIE_CHART(Messages.DataSeries_EnumPieChart);

        String fName;

        private ChartType(String name) {
            fName = name;
        }

        /**
         * @param name name of the chart type
         * @return chart type
         */
        public static @Nullable ChartType resolveName(String name) {
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
     * Constructor.
     *
     * @param xData data descriptor for the X axis
     * @param yData data descriptors for the Y axis
     * @param xLogscale whether X axis is logarithmic
     * @param yLogscale whether Y axis is logarithmic
     * @param type chart type
     */
    public DataSeries(DataDescriptor xData, Collection<DataDescriptor> yData,
            boolean xLogscale, boolean yLogscale, ChartType type) {
        fXData = xData;
        fYData = yData;
        fXLogscale = xLogscale;
        fYLogscale = yLogscale;
        fType = type;
    }

    /**
     * @return chart type
     */
    public ChartType getChartType() {
        return fType;
    }

    /**
     * @return data descriptor for the X axis
     */
    public DataDescriptor getXData() {
        return fXData;
    }

    /**
     * @return data descriptors for the Y axis
     */
    public Collection<DataDescriptor> getYData() {
        return fYData;
    }

    /**
     * @return whether X axis is logarithmic
     */
    public boolean isXLogscale() {
        return fXLogscale;
    }

    /**
     * @return whether Y axis is logarithmic
     */
    public boolean isYLogscale() {
        return fYLogscale;
    }
}
