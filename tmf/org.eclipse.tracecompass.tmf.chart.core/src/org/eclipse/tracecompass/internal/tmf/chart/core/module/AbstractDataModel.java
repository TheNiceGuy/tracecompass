/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a basic interface in order to allow data from various analyses
 * to work with the chart plugin. Each aspect correspond to a source.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class AbstractDataModel {

    private List<DataDescriptor> fDataDescriptors;

    /**
     * Default constructor
     */
    public AbstractDataModel() {
        fDataDescriptors = new ArrayList<>();
    }

    /**
     * @return aspects list of the data model
     */
    public List<DataDescriptor> getDataDescriptors() {
        return fDataDescriptors;
    }
}
