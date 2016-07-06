/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.tests.stubs.datagenerator;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.AbstractLongSource;

/**
 * Simple implementation for generating a random source of long numbers;
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataSourceDouble extends AbstractLongSource {
    List<Long> fData;

    /**
     * Constructor.
     *
     * @param size
     *              Size of the data to generate
     */
    public DataSourceDouble(long size) {
        Random seed = new Random();

        fData = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            fData.add(seed.nextLong());
        }
    }

    @Override
    public @NonNull Stream<@Nullable Number> getStreamNumber() {
        Stream<@Nullable Number> stream = fData.stream()
                .map(num -> num.longValue());
        return checkNotNull(stream);
    }
}
