/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.model;

/**
 * Class for representing a plottable series. It contains a X data descriptor
 * and a Y data descriptor;
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartSeries {

    /**
     * The X data descriptor
     */
    private DataDescriptor fXDescriptor;
    /**
     * The Y data descriptor
     */
    private DataDescriptor fYDescriptor;

    /**
     * Constructor.
     *
     * @param x
     *            The X data descriptor
     * @param y
     *            The Y data descriptor
     */
    public ChartSeries(DataDescriptor x, DataDescriptor y) {
        fXDescriptor = x;
        fYDescriptor = y;
    }

    /**
     * Accessor that returns the X data descriptor.
     *
     * @return The X data descriptor
     */
    public DataDescriptor getX() {
        return fXDescriptor;
    }

    /**
     * Accessor that returns the Y data descriptor.
     *
     * @return The Y data descriptor
     */
    public DataDescriptor getY() {
        return fYDescriptor;
    }

}
