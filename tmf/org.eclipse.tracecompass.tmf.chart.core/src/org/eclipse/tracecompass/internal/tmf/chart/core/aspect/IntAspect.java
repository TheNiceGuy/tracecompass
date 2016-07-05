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
 * Aspect for a string
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class IntAspect extends AbstractAspect {
    /**
     * Constructor
     *
     * @param name
     *              Aspect name
     */
    public IntAspect(String name) {
        super(name, null);
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
        return false;
    }
}
