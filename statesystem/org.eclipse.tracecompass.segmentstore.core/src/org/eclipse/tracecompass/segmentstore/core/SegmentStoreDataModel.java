/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.segmentstore.core;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.DurationAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.TimestampAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.IDataSource;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;

public class SegmentStoreDataModel extends AbstractDataModel {

    ISegmentStore<@NonNull ISegment> fSegmentStore;

    private final class StartSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStore.stream()
                    .map(segment -> segment.getStart());
        }
    }

    private final class EndSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStore.stream()
                    .map(segment -> segment.getEnd());
        }
    }

    private final class LengthSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStore.stream()
                    .map(segment -> segment.getLength());
        }
    }

    public SegmentStoreDataModel(@NonNull ISegmentStore<ISegment> segmentStore) {
        fSegmentStore = segmentStore;

        getDataDescriptors().add(new DataDescriptor(new TimestampAspect("Start"), new StartSource()));
        getDataDescriptors().add(new DataDescriptor(new TimestampAspect("End"), new EndSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect("Length"), new LengthSource()));
    }
}
