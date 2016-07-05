/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.aspect;

/**
 * Aspect for a time range duration
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DurationAspect extends AbstractAspect {
    /**
     * Constructor
     *
     * @param name
     *              Aspect name
     */
    public DurationAspect(String name) {
        super(name, "ns"); //$NON-NLS-1$
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isTimeStamp() {
        return false;
    }

    @Override
    public boolean isTimeDuration() {
        return true;
    }
}
