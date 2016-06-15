/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.AbstractAspect;

public class DataDescriptor {

    AbstractAspect fAspect;
    IDataSource fSource;

    public DataDescriptor(AbstractAspect aspect, IDataSource source) {
        fAspect = aspect;
        fSource = source;
    }

    public AbstractAspect getAspect() {
        return fAspect;
    }

    public IDataSource getSource() {
        return fSource;
    }
}
