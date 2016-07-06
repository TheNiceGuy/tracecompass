/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.core.model;

import java.util.List;

/**
 * This is a basic interface in order to allow data from various analyses to
 * work with the chart plugin. In order for an analysis to create a chart with
 * its data, it must first implement this interface.
 * <p>
 * A list of {@link DataDescriptor} is needed. Each descriptor is composed of a
 * source and an aspect. The source depends on the location of data. It is often
 * better to implement the source in an anonymous class.
 *
 * <pre>
 * <code>
 * public class ExampleSource implements AbstractLongSource {
 *     {@literal @Override}
 *     public Stream<Number> getStreamNumber() {
 *         return table.getColumns().stream().map(col -> col.get(1));
 *     }
 * }
 * </code>
 * </pre>
 *
 * Then, each source can be paired with an aspect in the constructor. Then, we
 * can add them to the list.
 *
 * <pre>
 * <code>
 * public DataModel(…) {
 *     fDataDescriptors List{@literal<DataDescriptor>} = new ArrayList<>();
 *
 *     IDataSource source = new ExampleSource();
 *     IDataChartAspect aspect = new GenericNumberAspect(…);
 *     DataDescriptor descriptor = new DataDescriptor(aspect, source)
 *
 *     fDataDescriptors.add(descriptor);
 * }
 *
 * {@literal @Override}
 * public List{@literal<DataDescriptor>} getDataDescriptors() {
 *     return fDataDescriptors;
 * }
 * </code>
 * </pre>
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDataChartModel {

    /**
     * @return The name of the data model
     */
    String getTitle();

    /**
     * @return Aspects list of the data model
     */
    List<DataDescriptor> getDataDescriptors();

}
