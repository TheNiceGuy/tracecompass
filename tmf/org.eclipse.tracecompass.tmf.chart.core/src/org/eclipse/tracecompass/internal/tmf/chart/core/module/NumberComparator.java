/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import java.math.BigDecimal;
import java.util.Comparator;

public class NumberComparator<T extends Number> implements Comparator<T> {
    @Override
    public int compare(T a, T b) {
        if(a == null && b == null) {
            return 0;
        }

        if(a == null) {
            return 1;
        }

        if(b == null) {
            return -1;
        }

        return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
    }
}