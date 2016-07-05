/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.source;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This interface allows reading of numerical data.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface INumericalSource extends IDataSource {
    /**
     * @return A stream of numbers
     */
    public abstract @NonNull Stream<@Nullable Number> getStreamNumber();

    /**
     * Method to compare two values inside the data stream.
     *
     * @param a
     *              First value to compare
     * @param b
     *              Second value to compare
     * @return A negative value is {@code a} is greater,
     *         a positive value if {@code b} is greater or
     *         a zero if they're equal
     */
    public abstract <T extends Number> int compare(T a, T b);
}
