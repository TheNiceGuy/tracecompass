package org.eclipse.tracecompass.tmf.chart.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;

import com.google.common.collect.ImmutableList;

public class ChartDrawer {

    Composite fParent;

    DataSeries fSeries;

    SeriesType fType;

    Chart fChart;

    protected static final List<@NonNull Color> COLORS = ImmutableList.of(
            new Color(Display.getDefault(),  72, 120, 207),
            new Color(Display.getDefault(), 106, 204, 101),
            new Color(Display.getDefault(), 214,  95,  95),
            new Color(Display.getDefault(), 180, 124, 199),
            new Color(Display.getDefault(), 196, 173, 102),
            new Color(Display.getDefault(), 119, 190, 219)
            );

    public ChartDrawer(Composite parent, DataSeries series, SeriesType type) {
        fParent = parent;
        fSeries = series;
        fType = type;
    }

    public void create() {
        fChart = new Chart(fParent, SWT.NONE);

        fChart.getTitle().setText(generateTitle());
        fChart.getAxisSet().getXAxis(0).getTitle().setText(generateXTitle());
        fChart.getAxisSet().getYAxis(0).getTitle().setText(generateYTitle());

        Iterator<@NonNull Color> color = COLORS.iterator();
        List<IBarSeries> seriesList = new ArrayList<>();
        for(DataDescriptor descriptor : fSeries.getYData()) {
            String title = descriptor.getAspect().getLabel();

            double[] yData = descriptor.getSource().getStreamNumerical()
                    .mapToDouble(num -> (double) num)
                    .toArray();

            IBarSeries series = (IBarSeries) fChart.getSeriesSet()
                    .createSeries(SeriesType.BAR, title);
            series.setYSeries(yData);


            if(color.hasNext()) {
                series.setBarColor(color.next());
            } else {
                color = COLORS.iterator();
            }

            seriesList.add(series);
        }

        if(fSeries.getXData().getAspect().isContinuous()) {
           double[] xData = fSeries.getXData().getSource().getStreamNumerical()
                   .mapToDouble(num -> (double) num)
                   .toArray();

           for(IBarSeries series : seriesList) {
               series.setXSeries(xData);
           }
        } else {
            List<Object> xList = fSeries.getXData().getSource().getStreamString()
                    .collect(Collectors.toList());

            String[] xData = new String[xList.size()];
            for(int i = 0; i < xList.size(); i++) {
                xData[i] = xList.get(i).toString();
            }

            fChart.getAxisSet().getXAxis(0).setCategorySeries(xData);
            fChart.getAxisSet().getXAxis(0).enableCategory(true);
        }

        fChart.getAxisSet().getXAxis(0).getTick().setTickLabelAngle(90);
        fChart.getAxisSet().adjustRange();

        fChart.getAxisSet().getXAxis(0).enableLogScale(fSeries.isXLogscale());
        fChart.getAxisSet().getYAxis(0).enableLogScale(fSeries.isYLogscale());
    }

    private String generateTitle() {
        return generateXTitle() + " vs. " + generateYTitle();
    }

    private String generateXTitle() {
        return fSeries.getXData().getAspect().getLabel();
    }

    private String generateYTitle() {
        DataDescriptor descriptor = fSeries.getYData().iterator().next();

        if(fSeries.getYData().size() == 1) {
            return descriptor.getAspect().getLabel();
        }

        return "TODO";
    }
}
