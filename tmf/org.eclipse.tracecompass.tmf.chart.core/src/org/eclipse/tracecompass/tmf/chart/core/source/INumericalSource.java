/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.source;

import java.util.Comparator;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This interface allows reading and comparing of numerical data.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 * @param <T>
 *            Type of number it carries
 */
public interface INumericalSource<@Nullable T extends @Nullable Number> extends IDataSource {

    @Override
    Stream<@Nullable T> getStream();

    /**
     * This method returns a comparator used for comparing numbers inside the
     * stream.
     *
     * @return A comparator
     */
    Comparator<@Nullable T> getComparator();

}
