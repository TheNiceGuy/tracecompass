/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver;

import java.util.Comparator;

/**
 * Abstract class implementing a mapper that returns double values.
 *
 * @param <T>
 *            The type of object it receives
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class AbstractDoubleResolver<T> implements INumericalResolver<T, Double> {

    @Override
    public Comparator<Double> getComparator() {
        return (o1, o2) -> Double.compare(o1, o2);
    }

    @Override
    public Double getMinValue() {
        return Double.MIN_VALUE;
    }

    @Override
    public Double getMaxValue() {
        return Double.MAX_VALUE;
    }

    @Override
    public Double getZeroValue() {
        return (double) 0;
    }

}
