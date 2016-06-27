/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import java.util.List;

/**
 * This class contains everything needed to make a chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartData {
    private List<DataDescriptor> fXData;
    private List<DataDescriptor> fYData;

    /**
     * Constructor.
     *
     * @param xData data descriptor for the X axis
     * @param yData data descriptors for the Y axis
     */
    public ChartData(List<DataDescriptor> xData, List<DataDescriptor> yData) {
        fXData = xData;
        fYData = yData;
    }

    /**
     * @return data descriptor for the X axis
     */
    public List<DataDescriptor> getXData() {
        return fXData;
    }

    /**
     * @return data descriptors for the Y axis
     */
    public List<DataDescriptor> getYData() {
        return fYData;
    }
}
