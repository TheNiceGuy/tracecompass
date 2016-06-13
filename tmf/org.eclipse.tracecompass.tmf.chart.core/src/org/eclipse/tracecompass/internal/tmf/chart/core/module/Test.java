package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.AbstractAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.DurationAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.GenericAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.TimestampAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.AbstractData;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataDuration;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataTimestamp;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataString;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

public class Test implements IDataChartProvider {
    ISegmentStore<@NonNull ISegment> fSegmentStore;

    public Test(ISegmentStore<@NonNull ISegment> segmentStore) {
        fSegmentStore = segmentStore;
    }

    @Override
    public ResultTable getResultTable() {


        return null;
    }

    @Override
    public TableClass getTableClass() {
        if(fSegmentStore.size() != 0) {
            return null;
        }

        List<AbstractAspect> aspects = new ArrayList<>();
        aspects.add(new TimestampAspect("Start", 0));
        aspects.add(new TimestampAspect("End", 1));
        aspects.add(new DurationAspect("Duration", 2));
        aspects.add(new GenericAspect("System Call", null, 3, false, false));

        return new TableClass("ClassName", "Title", aspects, Collections.EMPTY_SET);
    }

    @Override
    public Collection<TableEntry> getTableEntries() {
        List<TableEntry> entries = fSegmentStore.stream()
                .filter(segment -> segment instanceof SystemCall)
                .map(segment -> {
                    SystemCall syscall = (SystemCall) segment;
                    List<AbstractData> entry = new ArrayList<>();

                    entry.add(new DataTimestamp(syscall.getStart()));
                    entry.add(new DataTimestamp(syscall.getEnd()));
                    entry.add(new DataDuration(syscall.getLength()));
                    entry.add(new DataString(syscall.getName()));

                    return new TableEntry(entry);
                }).collect(Collectors.toList());

        return entries;
    }

}
