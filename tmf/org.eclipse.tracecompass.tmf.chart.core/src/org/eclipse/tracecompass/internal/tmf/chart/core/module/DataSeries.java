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
        BAR_CHART,
        SCATTER_CHART,
        PIE_CHART;
    }

    public DataSeries(DataDescriptor xData, Collection<DataDescriptor> yData,
            boolean xLogscale, boolean yLogscale) {
        fXData = xData;
        fYData = yData;
        fXLogscale = xLogscale;
        fYLogscale = yLogscale;
        /**
         * TODO: modify constructor
         */
        fType = ChartType.SCATTER_CHART;
    }

    public ChartType getChartType() {
        return fType;
    }

    public DataDescriptor getXData() {
        return fXData;
    }

    public Collection<DataDescriptor> getYData() {
        return fYData;
    }

    public boolean isXLogscale() {
        return fXLogscale;
    }

    public boolean isYLogscale() {
        return fYLogscale;
    }
}
