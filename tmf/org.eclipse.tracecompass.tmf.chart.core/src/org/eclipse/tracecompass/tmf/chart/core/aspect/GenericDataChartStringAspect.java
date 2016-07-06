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
 * Generic aspect for a string
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class GenericDataChartStringAspect implements IDataChartAspect {

    /**
     * Name of the aspect
     */
    private final String fName;

    /**
     * Constructor
     *
     * @param name
     *            Aspect name
     */
    public GenericDataChartStringAspect(String name) {
        fName = name;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public @Nullable String getUnits() {
        return null;
    }

}
