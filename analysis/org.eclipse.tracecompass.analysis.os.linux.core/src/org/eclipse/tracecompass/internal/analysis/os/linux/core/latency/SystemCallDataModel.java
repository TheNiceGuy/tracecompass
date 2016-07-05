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
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.StringAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.IStringSource;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreDataModel;

/**
 * This is a simple {@link AbstractDataModel} implementation extending
 * the {@link ISegmentStore} interface for the system call analysis.
 *
 * @author gabriel
 * @since 2.0
 */
public class SystemCallDataModel extends SegmentStoreDataModel {

    ISegmentStore<ISegment> fSegmentStore;

    private final class NameSource implements IStringSource {
        @Override
        public @NonNull Stream<@Nullable String> getStreamString() {
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

        getDataDescriptors().add(new DataDescriptor(new StringAspect(Messages.SegmentAspectName_SystemCall), new NameSource()));
    }
}
