/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.source;

import java.util.stream.Stream;

/**
 * This interface allows the reading of specific type of data from an analysis.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDataSource {

    /**
     * This method returns a stream of data.
     *
     * @return A stream of data
     */
    Stream<?> getStream();

}
