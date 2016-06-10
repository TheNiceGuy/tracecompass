/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.type;

/**
 * Duration data element
 *
 * @author Philippe Proulx
 */
public class DataDuration extends DataInteger {

    /**
     * Constructor
     *
     * @param value
     *            The duration value (as a long)
     */
    public DataDuration(long value) {
        super(value);
    }
}
