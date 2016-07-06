/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.aspect;

/**
 * Interface for any numerical aspect. Any aspect that describes numbers
 * is a subtype of this interface. This way, {@code instanceof} can be
 * used to check if an aspect is number of not.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDataChartNumericalAspect extends IDataChartAspect {

}
