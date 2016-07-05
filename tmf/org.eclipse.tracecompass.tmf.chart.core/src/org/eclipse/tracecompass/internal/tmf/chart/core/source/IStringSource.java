/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.source;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This interface allows reading of string data.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IStringSource extends IDataSource {
    /**
     * @return stream of strings
     */
    @NonNull Stream<@Nullable String> getStreamString();
}
