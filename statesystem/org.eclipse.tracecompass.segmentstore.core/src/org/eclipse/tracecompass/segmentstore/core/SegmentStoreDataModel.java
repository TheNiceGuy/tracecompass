/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.segmentstore.core;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.DurationAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.TimestampAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.IDataSource;

/**
 * This is a simple {@link AbstractDataModel} implementation for
 * the {@link ISegmentStore} interface.
 *
 * @author gabriel
 */
public class SegmentStoreDataModel extends AbstractDataModel {

    ISegmentStore<@NonNull ISegment> fSegmentStore;

    private final class StartSource implements IDataSource {
        @Override
        public @NonNull Stream<Double> getStreamNumerical() {
            Stream<Double> stream = fSegmentStore.stream()
                    .map(segment -> segment.getStart())
                    .map(num -> num.doubleValue());
            return checkNotNull(stream);
        }
    }

    private final class EndSource implements IDataSource {
        @Override
        public @NonNull Stream<Double> getStreamNumerical() {
            Stream<Double> stream = fSegmentStore.stream()
                    .map(segment -> segment.getEnd())
                    .map(num -> num.doubleValue());
            return checkNotNull(stream);
        }
    }

    private final class LengthSource implements IDataSource {
        @Override
        public @NonNull Stream<Double> getStreamNumerical() {
            Stream<Double> stream = fSegmentStore.stream()
                    .map(segment -> segment.getLength())
                    .map(num -> num.doubleValue());
            return checkNotNull(stream);
        }
    }

    /**
     * @param name name of the segment store
     * @param segmentStore segment store concerning the analysis
     */
    public SegmentStoreDataModel(String name, ISegmentStore<ISegment> segmentStore) {
        super(name);

        fSegmentStore = segmentStore;

        getDataDescriptors().add(new DataDescriptor(new TimestampAspect("Start"), new StartSource()));
        getDataDescriptors().add(new DataDescriptor(new TimestampAspect("End"), new EndSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect("Length"), new LengthSource()));
    }
}
