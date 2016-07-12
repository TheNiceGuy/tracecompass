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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartDurationAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartTimestampAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.DataDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartModel;
import org.eclipse.tracecompass.tmf.chart.core.source.AbstractLongSource;

/**
 * This is a simple {@link IDataChartModel} implementation for the
 * {@link ISegmentStore} interface.
 *
 * @author gabriel
 */
public class SegmentStoreDataModel implements IDataChartModel {
    /**
     * Name of the segment store
     */
    private String fName;
    /**
     * Segment store to read data from
     */
    ISegmentStore<@NonNull ISegment> fSegmentStore;
    /**
     * List of data descriptors
     */
    private List<DataDescriptor> fDescriptors;

    private final class StartSource extends AbstractLongSource {
        @Override
        public @NonNull Stream<@Nullable Long> getStream() {
            Stream<@Nullable Long> stream = fSegmentStore.stream()
                    .map(segment -> segment.getStart());
            return checkNotNull(stream);
        }
    }

    private final class EndSource extends AbstractLongSource {
        @Override
        public @NonNull Stream<@Nullable Long> getStream() {
            Stream<@Nullable Long> stream = fSegmentStore.stream()
                    .map(segment -> segment.getEnd());
            return checkNotNull(stream);
        }
    }

    private final class LengthSource extends AbstractLongSource {
        @Override
        public @NonNull Stream<@Nullable Long> getStream() {
            Stream<@Nullable Long> stream = fSegmentStore.stream()
                    .map(segment -> segment.getLength());
            return checkNotNull(stream);
        }
    }

    /**
     * @param name
     *            name of the segment store
     * @param segmentStore
     *            segment store concerning the analysis
     */
    public SegmentStoreDataModel(String name, ISegmentStore<ISegment> segmentStore) {
        fName = name;
        fSegmentStore = segmentStore;
        fDescriptors = new ArrayList<>();

        IDataChartAspect aspectStart = new GenericDataChartTimestampAspect(checkNotNull(Messages.SegmentStoreDataModel_Start));
        IDataChartAspect aspectEnd = new GenericDataChartTimestampAspect(checkNotNull(Messages.SegmentStoreDataModel_End));
        IDataChartAspect aspectLength = new GenericDataChartDurationAspect(checkNotNull(Messages.SegmentStoreDataModel_Length));

        fDescriptors.add(new DataDescriptor(aspectStart, new StartSource()));
        fDescriptors.add(new DataDescriptor(aspectEnd, new EndSource()));
        fDescriptors.add(new DataDescriptor(aspectLength, new LengthSource()));

        IDataChartModel.getInstances().add(this);
    }

    @Override
    public @NonNull String getTitle() {
        return fName;
    }

    @Override
    public @NonNull List<@NonNull DataDescriptor> getDataDescriptors() {
        // TODO Auto-generated method stub
        return fDescriptors;
    }
}
