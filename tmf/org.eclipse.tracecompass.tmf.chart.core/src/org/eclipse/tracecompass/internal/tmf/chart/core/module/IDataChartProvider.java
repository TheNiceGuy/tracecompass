package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public interface IDataChartProvider {
    @Nullable ResultTable getResultTable();

    @Nullable TableClass getTableClass();

    @NonNull Collection<@NonNull TableEntry> getTableEntries();
}
