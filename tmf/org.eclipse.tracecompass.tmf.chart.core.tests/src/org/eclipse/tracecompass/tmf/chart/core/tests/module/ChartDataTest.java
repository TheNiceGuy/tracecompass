/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.tests.module;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.tracecompass.internal.tmf.chart.core.module.ChartData;
import org.junit.Test;

public class ChartDataTest {
    @Test
    public void testTest() {
        ChartData data = new ChartData(null, null);

        assertNull(data.getXData());
        assertNotNull(data.getYData());
    }
}
