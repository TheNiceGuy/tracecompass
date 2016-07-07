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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * This class contains the data used to populate a chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartData {

    /**
     * List of data descriptors for the X axis
     */
    private final List<DataDescriptor> fXData;
    /**
     * List of data descriptors for the Y axis
     */
    private final List<DataDescriptor> fYData;

    /**
     * Constructor.
     *
     * @param xData
     *            Data descriptors for the X axis
     * @param yData
     *            Data descriptors for the Y axis
     */
    public ChartData(List<DataDescriptor> xData, List<DataDescriptor> yData) {
        fXData = new ArrayList<>();
        fYData = new ArrayList<>();

        /* Create new instances in order to have unique hashes */

        for (DataDescriptor descriptor : xData) {
            fXData.add(new DataDescriptor(descriptor));
        }

        for (DataDescriptor descriptor : yData) {
            fYData.add(new DataDescriptor(descriptor));
        }
    }

    /**
     * Get the list of data descriptors that should be use on the X axis.
     *
     * @return List of data descriptor for the X axis
     */
    public List<DataDescriptor> getXData() {
        return checkNotNull(ImmutableList.copyOf(fXData));
    }

    /**
     * Get the list of data descriptors that should be use on the Y axis.
     *
     * @return List of data descriptor for the Y axis
     */
    public List<DataDescriptor> getYData() {
        return checkNotNull(ImmutableList.copyOf(fYData));
    }

    /**
     * Helper method to get an X axis descriptor
     *
     * @param index
     *            The index of the data descriptor in the list
     * @return The data descriptor
     */
    public DataDescriptor getX(int index) {
        return checkNotNull(fXData.get(index));
    }

    /**
     * Helper method to get an Y axis descriptor
     *
     * @param index
     *            The index of the data descriptor in the list
     * @return The data descriptor
     */
    public DataDescriptor getY(int index) {
        return checkNotNull(fYData.get(index));
    }

}
