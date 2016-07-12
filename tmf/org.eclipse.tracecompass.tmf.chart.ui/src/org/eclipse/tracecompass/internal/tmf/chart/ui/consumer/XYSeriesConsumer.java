/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;

import com.google.common.collect.ImmutableList;

/**
 * This class implements a {@link ISeriesConsumer} for XY series. Such series
 * have two data consumers: one for the X axis and the other for the Y axis.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class XYSeriesConsumer implements ISeriesConsumer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final IDataConsumer fXConsumer;
    private final IDataConsumer fYConsumer;
    private final ChartSeries fChartSeries;
    private final List<Object> fConsumedElements = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructors.
     *
     * @param series
     *            The chart series related to this consumer
     * @param xConsumer
     *            The consumer for the X axis
     * @param yConsumer
     *            The consumer for the Y axis
     */
    public XYSeriesConsumer(ChartSeries series, IDataConsumer xConsumer, IDataConsumer yConsumer) {
        fChartSeries = series;
        fXConsumer = xConsumer;
        fYConsumer = yConsumer;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(Object obj) {
        /* Make sure every consumer can consume their value */
        if (!fXConsumer.test(obj) || !fYConsumer.test(obj)) {
            return;
        }

        /* Consume the value for each consumer */
        fXConsumer.accept(obj);
        fYConsumer.accept(obj);

        /* Add the object to the list of consumed objects */
        fConsumedElements.add(obj);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the chart series related to this consumer.
     *
     * @return The chart series
     */
    public ChartSeries getSeries() {
        return fChartSeries;
    }

    /**
     * Accessor that returns the list of valid consumed objects.
     *
     * @return The list of consumed objects
     */
    public List<Object> getConsumedElements() {
        return ImmutableList.copyOf(fConsumedElements);
    }

    /**
     * Accessor that returns the X consumer.
     *
     * @return The X consumer
     */
    public IDataConsumer getXConsumer() {
        return fXConsumer;
    }

    /**
     * Accessor that returns the Y consumer.
     *
     * @return The Y consumer
     */
    public IDataConsumer getYConsumer() {
        return fYConsumer;
    }

}
