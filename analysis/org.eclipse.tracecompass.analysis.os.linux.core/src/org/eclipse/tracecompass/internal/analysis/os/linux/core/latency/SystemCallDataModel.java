/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.StringAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.IDataSource;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreDataModel;

public class SystemCallDataModel extends SegmentStoreDataModel {

    ISegmentStore<ISegment> fSegmentStore;

    private final class NameSource implements IDataSource {
        @Override
        public @Nullable  Stream<?> getStream() {
            return fSegmentStore.stream()
                    .map(segment -> ((SystemCall)segment).getName());
        }
    }

    public SystemCallDataModel(String name, @NonNull ISegmentStore<ISegment> segmentStore) {
        super(name, segmentStore);

        fSegmentStore = segmentStore;

        getDataDescriptors().add(new DataDescriptor(new StringAspect("Name"), new NameSource()));
    }
}
