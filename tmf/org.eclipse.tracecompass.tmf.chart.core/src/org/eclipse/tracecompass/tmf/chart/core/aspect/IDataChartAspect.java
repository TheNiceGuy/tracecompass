/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.aspect;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for any aspect.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDataChartAspect {

    /**
     * Get the label of this aspect.
     *
     * The label is composed of the name followed by the units in parentheses.
     *
     * @return The label
     */
    default String getLabel() {
        String name = getName();
        String units = getUnits();

        if (units == null) {
            return name;
        }

        return (name + " (" + units + ')'); //$NON-NLS-1$
    }

    /**
     * Get the name of this aspect
     *
     * @return The name
     */
    String getName();

    /**
     * Get the units of this aspect.
     *
     * @return The units
     */
    default @Nullable String getUnits() {
        return null;
    }

}
