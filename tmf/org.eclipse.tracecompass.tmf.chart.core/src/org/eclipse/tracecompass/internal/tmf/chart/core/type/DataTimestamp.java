/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.type;

/**
 * Lami timestamp data type
 *
 * @author Alexandre Montplaisir
 */
public class DataTimestamp extends DataInteger {

    /**
     * Construct a time stamp from a value in ns.
     *
     * @param value
     *            The value
     */
    public DataTimestamp(long value) {
        super(value);
    }
}
