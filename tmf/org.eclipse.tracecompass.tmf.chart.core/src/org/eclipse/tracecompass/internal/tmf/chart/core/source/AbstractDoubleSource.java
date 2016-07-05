/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.source;

/**
 * Abstract class implementing source of double values.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class AbstractDoubleSource implements INumericalSource {
    @Override
    public <T extends Number> int compare(T a, T b) {
        if(a == null && b == null) {
            return 0;
        }

        if(a == null) {
            return 1;
        }

        if(b == null) {
            return -1;
        }

        return (int)(a.doubleValue()-b.doubleValue());
    }

}
