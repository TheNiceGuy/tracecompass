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
import java.util.Collections;
import java.util.List;

/**
 * This is a basic interface in order to allow data from various analyses
 * to work with the chart plugin. Each aspect correspond to a source.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public abstract class AbstractDataModel {

    private static List<AbstractDataModel> fInstances = new ArrayList<>();

    private String fName;

    private List<DataDescriptor> fDataDescriptors;

    /**
     * TODO: destroy instances
     */

    /**
     * Default constructor
     *
     * @param name of the data model
     */
    public AbstractDataModel(String name) {
        fName = name;
        fDataDescriptors = new ArrayList<>();
        fInstances.add(this);
    }

    /**
     * @return name of the data model
     */
    public String getName() {
        return fName;
    }

    /**
     * @return aspects list of the data model
     */
    public List<DataDescriptor> getDataDescriptors() {
        return fDataDescriptors;
    }

    /**
     * @return collection of instances
     */
    static public List<AbstractDataModel> getInstances() {
        return Collections.unmodifiableList(fInstances);
    }
}
