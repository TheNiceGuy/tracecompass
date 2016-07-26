/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import java.util.function.Consumer;

/**
 * This interface consumes any data that comes from a {@link ISeriesConsumer}.
 * <p>
 * For example, it can be though of an axis consumer in XY charts since there
 * should be one for the X and Y axis.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDataConsumer extends Consumer<Object> {

    /**
     * This method maps and tests if a value is valid for the chart. It should
     * always be called before {@link #accept(Object)}.
     *
     * @param obj
     *            The object to map the value from
     * @return {@code true} if the mapped value is valid, else {@code false}
     */
    boolean test(Object obj);

}
