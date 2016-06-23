/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.source;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This interface allows reading of numerical data.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface INumericalSource extends IDataSource {
    /**
     * @return stream of numbers
     */
    default @NonNull Stream<@NonNull Number> getStreamNumber() {
        return checkNotNull(Stream.empty());
    }
}
