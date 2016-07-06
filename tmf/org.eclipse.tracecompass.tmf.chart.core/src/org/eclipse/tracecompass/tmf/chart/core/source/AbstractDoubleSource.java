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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract class implementing source of double values.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class AbstractDoubleSource implements INumericalSource<@Nullable Double> {

    @Override
    public Comparator<@Nullable Double> getComparator() {
        return (o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }

            if (o1 == null) {
                return 1;
            }

            if (o2 == null) {
                return -1;
            }

            return Double.compare(o1, o2);
        };
    }

}
