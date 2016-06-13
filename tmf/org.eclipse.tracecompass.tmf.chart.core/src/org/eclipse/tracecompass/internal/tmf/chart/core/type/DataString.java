/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.type;

import org.eclipse.jdt.annotation.Nullable;

public class DataString extends AbstractData {

    private final String fValue;

    public DataString(String value) {
        fValue = value;
    }

    public String getValue() {
        return fValue;
    }

    @Override
    public @Nullable String toString() {
        return fValue;
    }
}
