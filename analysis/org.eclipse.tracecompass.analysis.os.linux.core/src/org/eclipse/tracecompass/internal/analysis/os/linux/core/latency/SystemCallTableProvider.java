package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.AbstractAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.DurationAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.GenericAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.TimestampAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.IDataChartProvider;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.ResultTable;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.TableClass;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.TableEntry;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.AbstractData;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataDuration;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataTimestamp;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataString;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataTimeRange;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

public class SystemCallTableProvider implements IDataChartProvider {
    ISegmentStore<@NonNull ISegment> fSegmentStore;

    public SystemCallTableProvider(ISegmentStore<@NonNull ISegment> segmentStore) {
        fSegmentStore = segmentStore;
    }

    @Override
    public @Nullable ResultTable getResultTable() {
        List<TableEntry> entries = (List<@NonNull TableEntry>) getTableEntries();

        DataTimestamp min = entries.stream()
                .map(entry -> ((DataTimestamp)entry.getValue(0)))
                .min((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
                .get();
        DataTimestamp max = entries.stream()
                .map(entry -> ((DataTimestamp)entry.getValue(1)))
                .max((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
                .get();

        DataTimeRange timeRange = new DataTimeRange(min.getValue(), max.getValue());

        return new ResultTable(timeRange, getTableClass(), getTableEntries());
    }

    @Override
    public @Nullable TableClass getTableClass() {
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
