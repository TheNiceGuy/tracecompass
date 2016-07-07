/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartStringAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.DataDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartModel;
import org.eclipse.tracecompass.tmf.chart.core.source.IStringSource;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreDataModel;

/**
 * This is a simple {@link IDataChartModel} implementation extending
 * the {@link ISegmentStore} interface for the system call analysis.
 *
 * @author gabriel
 * @since 2.0
 */
public class SystemCallDataModel extends SegmentStoreDataModel {
    /**
     * Segment store to read data from
     */
    private ISegmentStore<ISegment> fSegmentStore;

    private final class NameSource implements IStringSource {
        @Override
        public @NonNull Stream<@Nullable String> getStream() {
            Stream<@Nullable String> stream = fSegmentStore.stream()
                    .map(segment -> ((SystemCall)segment).getName());
            return checkNotNull(stream);
        }
    }

    /**
     * @param name
     *              Name of the analysis
     * @param segmentStore
     *              Segment store concerning the system call analysis
     */
    public SystemCallDataModel(String name, ISegmentStore<ISegment> segmentStore) {
        super(name, segmentStore);

        fSegmentStore = segmentStore;

        IDataChartAspect aspectSystemCall = new GenericDataChartStringAspect(checkNotNull(Messages.SegmentAspectName_SystemCall));

        getDataDescriptors().add(new DataDescriptor(aspectSystemCall, new NameSource()));
    }
}
