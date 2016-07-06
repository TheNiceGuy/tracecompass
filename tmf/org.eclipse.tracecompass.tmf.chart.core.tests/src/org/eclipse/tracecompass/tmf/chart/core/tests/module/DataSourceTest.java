/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.tests.module;

import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.tmf.chart.core.tests.stubs.datagenerator.DataSourceLong;
import org.junit.Test;

/**
 * Tests related to the data source class.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataSourceTest {
    /**
     * Size of the data to generate
     */
    final static long DATA_SIZE = 10;

    @Test
    public void test() {
        INumericalSource source = new DataSourceLong(DATA_SIZE);

        source.getStreamNumber().forEach(System.out::println);
        source.getStreamNumber().forEach(System.out::println);
        source.getStreamNumber().forEach(System.out::println);
    }

}
