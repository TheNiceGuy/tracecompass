/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.ui.aggregator.IConsumerAggregator;

import com.google.common.collect.ImmutableList;

/**
 * This class implements a {@link IChartConsumer} for a XY chart. It offers
 * optional aggregation of X and Y {@link IDataConsumer} after all the objects
 * are processed.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class XYChartConsumer implements IChartConsumer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final List<XYSeriesConsumer> fSeries;
    private final @Nullable IConsumerAggregator fXAggregator;
    private final @Nullable IConsumerAggregator fYAggregator;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param series
     *            The list of XY series consumer
     * @param xAggregator
     *            An optional aggregator for the X consumers
     * @param yAggregator
     *            An optional aggregator for the Y consumers
     */
    public XYChartConsumer(List<XYSeriesConsumer> series,
            @Nullable IConsumerAggregator xAggregator,
            @Nullable IConsumerAggregator yAggregator) {
        fSeries = ImmutableList.copyOf(series);
        fXAggregator = xAggregator;
        fYAggregator = yAggregator;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(Object obj) {
        fSeries.forEach(consumer -> consumer.accept(obj));
    }

    @Override
    public void finish() {
        /* Aggregate X consumer if needed */
        IConsumerAggregator aggregatorX = fXAggregator;
        if (aggregatorX != null) {
            fSeries.forEach(s -> aggregatorX.accept(s.getXConsumer()));
        }

        /* Aggregate Y consumer if needed */
        IConsumerAggregator aggregatorY = fYAggregator;
        if (aggregatorY != null) {
            fSeries.forEach(s -> aggregatorY.accept(s.getYConsumer()));
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns an immutable list of the series consumer
     *
     * @return The list of series consumer
     */
    public List<XYSeriesConsumer> getSeries() {
        return fSeries;
    }

    /**
     * Accessor that return the X consumers aggregator, if present.
     *
     * @return The X consumers aggregator
     */
    public @Nullable IConsumerAggregator getXAggregator() {
        return fXAggregator;
    }

    /**
     * Accessor that return the Y consumers aggregator, if present.
     *
     * @return The Y consumers aggregator
     */
    public @Nullable IConsumerAggregator getYAggregator() {
        return fYAggregator;
    }

}
