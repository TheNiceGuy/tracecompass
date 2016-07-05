/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.aspect;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract class to represent the type of values a source may contains.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class AbstractAspect {
    /**
     * Name of the aspect
     */
    private final String fName;
    /**
     * Units of the aspect
     */
    private final @Nullable String fUnits;

    /**
     * Constructor
     *
     * @param name
     *              Aspect name
     * @param units
     *              The units of the value in this column
     */
    protected AbstractAspect(String name, @Nullable String units) {
        fUnits = units;
        fName = name;
    }

    /**
     * Get the label of this aspect.
     *
     * The label is composed of the name followed by the units in parentheses.
     *
     * @return The label
     */
    public String getLabel() {
        if (getUnits() == null) {
            return getName();
        }

        return (getName() + " (" + getUnits() + ')'); //$NON-NLS-1$
    }

    /**
     * Get the name of this aspect
     *
     * @return The name
     */
    public String getName() {
        return fName;
    }

    /**
     * Get the units of this aspect.
     *
     * @return The units
     */
    public @Nullable String getUnits() {
        return fUnits;
    }

    /**
     * Indicate if this aspect is numerical or not. This is used when we want to
     * know if the source is numerical or not.
     *
     * @return If this aspect is numerical or not
     */
    public abstract boolean isContinuous();

    /**
     * Indicate if this aspect represent timestamp or not. This can be used in chart
     * for axis labeling etc.
     *
     * @return  If this aspect represent a timestamp or not
     */
    public abstract boolean isTimeStamp();

    /**
     * Indicate if this aspect represent a time duration or not. This can be used in
     * chart for axis labeling etc.
     *
     * @return  If this aspect represent a time duration or not
     */
    public boolean isTimeDuration() {
        return false;
    }

}
