/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui.dialog;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartData;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartModel;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartSeries;
import org.eclipse.tracecompass.tmf.chart.core.model.DataDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartModel;
import org.eclipse.tracecompass.tmf.chart.ui.type.BarChartType;
import org.eclipse.tracecompass.tmf.chart.ui.type.IChartType;
import org.eclipse.tracecompass.tmf.chart.ui.type.ScatterChartType;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * This dialog is used to configure series before building a chart.
 *
 * TODO: restrict X axis selection, we could create a interface for each chart
 * type to implement
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartMakerDialog extends SelectionDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Parent composite
     */
    private Composite fComposite;

    private IDataChartModel fDataModel;

    private TableViewer fTypeTable;

    private TableViewer fSeriesTable;
    /**
     * List for selecting the X axis
     */
    private TableViewer fSelectionX;
    /**
     * Checkbox list for selecting the Y axis
     */
    private CheckboxTableViewer fSelectionY;

    private Button fAddButton;
    /**
     * Checkbox for indicating whether X axis is logarithmic
     */
    private Button fXLogscale;
    /**
     * Checkbox for indicating whether Y axis is logarithmic
     */
    private Button fYLogscale;
    /**
     * Data series created after the dialog
     */
    private @Nullable ChartData fDataSeries;
    /**
     * Data model created after the dialog
     */
    private @Nullable ChartModel fChartModel;

    private @Nullable IChartType fType;

    private List<ChartSeriesDialog> fSeries = new ArrayList<>();

    private @Nullable IDataChartAspect fXAspectFilter;

    private @Nullable IDataChartAspect fYAspectFilter;

    // ------------------------------------------------------------------------
    // Important methods
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent
     *            Parent shell
     * @param model
     *            Model to choose source from
     */
    public ChartMakerDialog(Shell parent, IDataChartModel model) {
        super(parent);

        fComposite = parent;
        fDataModel = model;
        fTypeTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSeriesTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSelectionX = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSelectionY = checkNotNull(CheckboxTableViewer.newCheckList(parent, SWT.BORDER));
        fAddButton = new Button(parent, SWT.NONE);

        fXLogscale = new Button(parent, SWT.CHECK);
        fYLogscale = new Button(parent, SWT.CHECK);

        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    public Composite createDialogArea(@Nullable Composite parent) {
        fComposite = checkNotNull((Composite) super.createDialogArea(parent));

        /*
         * Layouts
         */
        GridLayout baseLayout = new GridLayout();
        baseLayout.numColumns = 2;

        GridLayout genericGridLayout = new GridLayout();

        GridData genericFillGridData = new GridData();
        genericFillGridData.horizontalAlignment = SWT.FILL;
        genericFillGridData.verticalAlignment = SWT.FILL;
        genericFillGridData.grabExcessHorizontalSpace = true;
        genericFillGridData.grabExcessVerticalSpace = true;

        fComposite.setLayout(baseLayout);

        /*
         * Chart type selector
         */
        GridData typeGridData = new GridData();
        typeGridData.verticalSpan = 3;
        typeGridData.horizontalAlignment = SWT.FILL;
        typeGridData.verticalAlignment = SWT.FILL;
        typeGridData.grabExcessHorizontalSpace = false;
        typeGridData.grabExcessVerticalSpace = true;

        TableViewerColumn typeColumn = new TableViewerColumn(fTypeTable, SWT.NONE);
        typeColumn.getColumn().setWidth(70);
        typeColumn.setLabelProvider(new TypeLabelProvider());

        List<IChartType> types = new ArrayList<>();
        types.add(new BarChartType());
        types.add(new ScatterChartType());

        fTypeTable.getTable().setParent(fComposite);
        fTypeTable.getTable().setLayoutData(typeGridData);
        fTypeTable.setContentProvider(ArrayContentProvider.getInstance());
        fTypeTable.addSelectionChangedListener(new TypeSelectionListener());
        fTypeTable.setInput(types);

        /*
         * Series viewer
         */

        /*
         * This fixes a bug where the labels in the first column cannot be
         * aligned to the center.
         */
        TableViewerColumn dummyColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        dummyColumn.setLabelProvider(new SeriesDummyLabelProvider());

        TableViewerColumn xSelectionColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        xSelectionColumn.getColumn().setText(Messages.ChartMakerDialog_XSeries);
        xSelectionColumn.getColumn().setAlignment(SWT.CENTER);
        xSelectionColumn.getColumn().setResizable(false);
        xSelectionColumn.setLabelProvider(new SeriesXLabelProvider());

        TableViewerColumn ySelectionColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        ySelectionColumn.getColumn().setText(Messages.ChartMakerDialog_YSeries);
        ySelectionColumn.getColumn().setAlignment(SWT.CENTER);
        ySelectionColumn.getColumn().setResizable(false);
        ySelectionColumn.setLabelProvider(new SeriesYLabelProvider());

        TableViewerColumn removeColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        removeColumn.getColumn().setResizable(false);
        removeColumn.setLabelProvider(new SeriesRemoveLabelProvider());

        TableColumnLayout seriesLayout = new TableColumnLayout();
        seriesLayout.setColumnData(dummyColumn.getColumn(), new ColumnPixelData(0));
        seriesLayout.setColumnData(xSelectionColumn.getColumn(), new ColumnWeightData(50));
        seriesLayout.setColumnData(ySelectionColumn.getColumn(), new ColumnWeightData(50));
        seriesLayout.setColumnData(removeColumn.getColumn(), new ColumnPixelData(35));

        Group seriesGroup = new Group(fComposite, SWT.BORDER | SWT.FILL);
        seriesGroup.setText(Messages.ChartMakerDialog_SelectedSeries);
        seriesGroup.setLayout(genericGridLayout);
        seriesGroup.setLayoutData(genericFillGridData);

        Composite seriesComposite = new Composite(seriesGroup, SWT.NONE);
        seriesComposite.setLayout(seriesLayout);
        seriesComposite.setLayoutData(genericFillGridData);

        fSeriesTable.getTable().setParent(seriesComposite);
        fSeriesTable.getTable().setHeaderVisible(true);
        fSeriesTable.getTable().addListener(SWT.MeasureItem, new SeriesRowResize());
        fSeriesTable.setContentProvider(ArrayContentProvider.getInstance());
        fSeriesTable.setInput(fSeries);

        /*
         * Series creator
         */
        GridLayout creatorLayout = new GridLayout();
        creatorLayout.numColumns = 2;

        Group creatorGroup = new Group(fComposite, SWT.BORDER);
        creatorGroup.setText(Messages.ChartMakerDialog_SeriesCreator);
        creatorGroup.setLayout(creatorLayout);
        creatorGroup.setLayoutData(genericFillGridData);

        GridData creatorLabelGridData = new GridData();
        creatorLabelGridData.horizontalAlignment = SWT.CENTER;
        creatorLabelGridData.verticalAlignment = SWT.BOTTOM;

        /* Top labels */
        Label creatorLabelX = new Label(creatorGroup, SWT.NONE);
        creatorLabelX.setText(Messages.ChartMakerDialog_XAxis);
        creatorLabelX.setLayoutData(creatorLabelGridData);

        Label creatorLabelY = new Label(creatorGroup, SWT.NONE);
        creatorLabelY.setText(Messages.ChartMakerDialog_YAxis);
        creatorLabelY.setLayoutData(creatorLabelGridData);

        /* X axis table */
        TableViewerColumn creatorXColumn = new TableViewerColumn(fSelectionX, SWT.NONE);
        creatorXColumn.getColumn().setResizable(false);
        creatorXColumn.setLabelProvider(new DataDescriptorLabelProvider());

        TableColumnLayout creatorXLayout = new TableColumnLayout();
        creatorXLayout.setColumnData(creatorXColumn.getColumn(), new ColumnWeightData(100));

        Composite creatorXComposite = new Composite(creatorGroup, SWT.NONE);
        creatorXComposite.setLayout(creatorXLayout);
        creatorXComposite.setLayoutData(genericFillGridData);

        fSelectionX.getTable().setParent(creatorXComposite);
        fSelectionX.setContentProvider(ArrayContentProvider.getInstance());
        fSelectionX.setInput(fDataModel.getDataDescriptors());
        fSelectionX.setFilters(new CreatorXFilter());
        fSelectionX.addSelectionChangedListener(new CreatorXSelectedEvent());

        /* Y axis table */
        TableViewerColumn creatorYColumn = new TableViewerColumn(fSelectionY, SWT.NONE);
        creatorYColumn.getColumn().setResizable(false);
        creatorYColumn.setLabelProvider(new DataDescriptorLabelProvider());

        TableColumnLayout creatorYLayout = new TableColumnLayout();
        creatorYLayout.setColumnData(creatorYColumn.getColumn(), new ColumnWeightData(100));

        Composite creatorYComposite = new Composite(creatorGroup, SWT.NONE);
        creatorYComposite.setLayout(creatorYLayout);
        creatorYComposite.setLayoutData(genericFillGridData);

        fSelectionY.getTable().setParent(creatorYComposite);
        fSelectionY.setContentProvider(ArrayContentProvider.getInstance());
        fSelectionY.setInput(fDataModel.getDataDescriptors());
        fSelectionY.setFilters(new CreatorYFilter());
        fSelectionY.addCheckStateListener(new CreatorYSelectedEvent());

        /* Add button */
        Label creatorLabelEmpty = new Label(creatorGroup, SWT.NONE);
        creatorLabelEmpty.setText(""); //$NON-NLS-1$

        GridData creatorButtonGridData = new GridData();
        creatorButtonGridData.horizontalAlignment = SWT.RIGHT;
        creatorButtonGridData.widthHint = 30;
        creatorButtonGridData.heightHint = 30;

        Image addImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD);
        fAddButton.setParent(creatorGroup);
        fAddButton.setLayoutData(creatorButtonGridData);
        fAddButton.setImage(addImage);
        fAddButton.addListener(SWT.Selection, new AddButtonClickedEvent());

        /*
         * Options
         */
        GridData configOptionsGridData = new GridData();
        configOptionsGridData.horizontalAlignment = SWT.FILL;
        configOptionsGridData.grabExcessHorizontalSpace = true;

        GridLayout optionsLayout = new GridLayout();
        optionsLayout.numColumns = 1;

        Group optionsGroup = new Group(fComposite, SWT.BORDER);
        optionsGroup.setText(Messages.ChartMakerDialog_Options);
        optionsGroup.setLayout(optionsLayout);
        optionsGroup.setLayoutData(configOptionsGridData);

        fXLogscale.setParent(optionsGroup);
        fXLogscale.setText(Messages.ChartMakerDialog_LogScaleX);

        fYLogscale.setParent(optionsGroup);
        fYLogscale.setText(Messages.ChartMakerDialog_LogScaleY);

        getShell().setSize(800, 600);

        return fComposite;
    }

    /**
     * @return The configured data series
     */
    public @Nullable ChartData getDataSeries() {
        return fDataSeries;
    }

    /**
     * @return The configured chart model
     */
    public @Nullable ChartModel getChartModel() {
        return fChartModel;
    }

    @Override
    public void create() {
        super.create();

        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    public void okPressed() {
        // java.util.List<DataDescriptor> descriptors =
        // getInstance(fDataModelIndex).getDataDescriptors();
        // String title = getInstance(fDataModelIndex).getTitle();
        //
        // ChartModel.ChartType type =
        // ChartModel.ChartType.resolveChartType(checkNotNull(fComboChartType.getText()));
        // if (type == null) {
        // return;
        // }
        //
        // java.util.List<DataDescriptor> xAxis = new ArrayList<>();
        // java.util.List<DataDescriptor> yAxis = new ArrayList<>();
        //
        // for (DataDescriptor descriptor : descriptors) {
        // for (Object entry : fSelectionY.getCheckedElements()) {
        // if (descriptor.getAspect().getLabel().equals(entry.toString())) {
        // yAxis.add(descriptor);
        // xAxis.add(descriptors.get(fSelectionX.getSelectionIndex()));
        // }
        // }
        // }
        //
        // fDataSeries = new ChartData(xAxis, yAxis);
        // fChartModel = new ChartModel(type, title, fXLogscale.getSelection(),
        // fYLogscale.getSelection());
        //
        // super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    private boolean checkIfSeriesCompatible(IChartType typeA, IChartType typeB) {
        for (ChartSeries series : fSeries) {
            if (typeA.filterX(series.getX().getAspect(), null) != typeB.filterX(series.getX().getAspect(), null)) {
                return false;
            }

            if (typeA.filterY(series.getY().getAspect(), null) != typeB.filterY(series.getY().getAspect(), null)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkIfButtonReady() {
        if (fSelectionX.getSelection().isEmpty()) {
            return false;
        }

        if (fSelectionY.getCheckedElements().length == 0) {
            return false;
        }

        return true;
    }

    private boolean checkIfSeriesPresent(ChartSeries test) {
        for (ChartSeries series : fSeries) {
            if (series.getX() == test.getX() && series.getY() == test.getY()) {
                return true;
            }
        }

        return false;
    }

    private @Nullable ChartSeries findButtonOwner(Button button) {
        for (ChartSeriesDialog series : fSeries) {
            if (series.getButton() == button) {
                return series;
            }
        }

        return null;
    }

    private void removeIncompatibleSeries(IChartType type) {
        Iterator<ChartSeriesDialog> iterator = fSeries.iterator();

        while (iterator.hasNext()) {
            ChartSeriesDialog series = checkNotNull(iterator.next());

            /* Check if the series if compatible */
            if (!type.filterX(series.getX().getAspect(), null) || !type.filterY(series.getY().getAspect(), null)) {
                /* Remove the button of the series */
                Button button = series.getButton();
                if (button != null) {
                    button.dispose();
                }

                /* Remove the series of the series list */
                iterator.remove();
            }
        }
    }

    private void unselectIncompatibleSeries(IChartType type) {

    }

    private boolean tryResetXFilter() {
        if (fSeries.size() != 0) {
            return false;
        }

        fXAspectFilter = null;
        return true;
    }

    private boolean tryResetYFilter() {
        if (fSeries.size() != 0) {
            return false;
        }

        if (fSelectionY.getCheckedElements().length != 0) {
            return false;
        }

        fYAspectFilter = null;
        return true;
    }

    private void enableXLog() {
        // int index = fSelectionX.getSelectionIndex();
        //
        // DataDescriptor descriptor =
        // checkNotNull(getInstance(fDataModelIndex).getDataDescriptors().get(index));
        // if (descriptor.getAspect() instanceof IDataChartNumericalAspect) {
        // fXLogscale.setEnabled(true);
        // } else {
        // fXLogscale.setEnabled(false);
        // fXLogscale.setSelection(false);
        // }
    }

    private void enableYLog() {
        // if (fAspectFilter != null && fAspectFilter instanceof
        // IDataChartNumericalAspect) {
        // fYLogscale.setEnabled(true);
        // } else {
        // fYLogscale.setEnabled(false);
        // fYLogscale.setSelection(false);
        // }
    }

    // ------------------------------------------------------------------------
    // Listeners, Providers, etc
    // ------------------------------------------------------------------------

    /**
     * This class extension avoid the use of an hashmap for linking a series
     * with a button. Each button in the series table are linked to a series.
     */
    private class ChartSeriesDialog extends ChartSeries {
        private @Nullable Button fButton;

        private ChartSeriesDialog(DataDescriptor descriptorX, DataDescriptor descriptorY) {
            super(descriptorX, descriptorY);
        }

        private @Nullable Button getButton() {
            return fButton;
        }

        private void setButton(Button button) {
            fButton = button;
        }
    }

    /**
     * This provider provides the image in the chart type selection table.
     */
    private class TypeLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }

        @Override
        public Image getImage(@Nullable Object element) {
            IChartType type = checkNotNull((IChartType) element);
            return new Image(fComposite.getDisplay(), type.getImageData());
        }
    }

    /**
     * This listener handle the selection in the chart type selection table.
     */
    private class TypeSelectionListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(@Nullable SelectionChangedEvent event) {
            IStructuredSelection selection = fTypeTable.getStructuredSelection();
            IChartType type = (IChartType) selection.getFirstElement();

            /* Check if the series are compatible with the chart type */
            if (fSeries.size() != 0 && !checkIfSeriesCompatible(checkNotNull(fType), type)) {
                String warning = Messages.ChartMakerDialog_WarningConfirm;
                String message = String.format(Messages.ChartMakerDialog_WarningIncompatibleSeries,
                        type.getType().toString().toLowerCase());

                boolean choice = MessageDialog.openConfirm(fComposite.getShell(), warning, message);
                if (!choice) {
                    fTypeTable.setSelection(new StructuredSelection(fType));
                    return;
                }

                removeIncompatibleSeries(type);
                fSeriesTable.refresh();

                fSelectionX.getTable().deselectAll();
                fSelectionY.setAllChecked(false);
            }

            fType = type;

            if (tryResetXFilter()) {
                fSelectionX.refresh();
            }

            if (tryResetYFilter()) {
                fSelectionY.refresh();
            }

            fAddButton.setEnabled(checkIfButtonReady());

        }
    }

    /**
     * This dummy provider is used as a workaround in a column's bug.
     */
    private class SeriesDummyLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }
    }

    /**
     * This provider provides the labels for the X column of the series table.
     */
    private class SeriesXLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            ChartSeries series = checkNotNull((ChartSeries) element);
            return series.getX().getAspect().getLabel();
        }
    }

    /**
     * This provider provides the labels for the Y column of the series table.
     */
    private class SeriesYLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            ChartSeries series = checkNotNull((ChartSeries) element);
            return series.getY().getAspect().getLabel();
        }
    }

    /**
     * This provider provides the buttons for removing a series in the series
     * table.
     */
    private class SeriesRemoveLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }

        @Override
        public void update(@Nullable ViewerCell cell) {
            if (cell == null) {
                return;
            }

            /* Create a button if it doesn't exist */
            ChartSeriesDialog series = (ChartSeriesDialog) cell.getViewerRow().getElement();
            Button button;
            if (series.getButton() == null) {
                Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
                button = new Button((Composite) cell.getViewerRow().getControl(), SWT.NONE);
                button.setImage(image);
                button.addListener(SWT.Selection, new SeriesRemoveButtonEvent());
                series.setButton(button);
            } else {
                button = series.getButton();
            }

            /* Set the position of the button into the cell */
            TableItem item = (TableItem) cell.getItem();
            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(button, item, cell.getColumnIndex());
            editor.layout();
        }
    }

    /**
     * This listener handles the event when the button for removing a series is
     * clicked.
     */
    private class SeriesRemoveButtonEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event == null) {
                return;
            }

            Button button = (Button) checkNotNull(event.widget);
            button.dispose();

            ChartSeries series = findButtonOwner(button);
            fSeries.remove(series);
            fSeriesTable.refresh();

            tryResetXFilter();
            fSelectionX.refresh();

            tryResetYFilter();
            fSelectionY.refresh();

            /* Disable OK button if no series are made */
            if (fSeries.size() == 0) {
                getButton(IDialogConstants.OK_ID).setEnabled(false);
            }
        }
    }

    /**
     * This listener resizes the height of each row of the series table.
     */
    private class SeriesRowResize implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event == null) {
                return;
            }

            event.height = 25;
        }
    }

    /**
     * This provider provides labels for {@link DataDescriptor}.
     */
    private class DataDescriptorLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            DataDescriptor descriptor = checkNotNull((DataDescriptor) element);
            return descriptor.getAspect().getLabel();
        }
    }

    /**
     * This filter is used for filtering labels in the X selection table.
     */
    private class CreatorXFilter extends ViewerFilter {
        @Override
        public boolean select(@Nullable Viewer viewer, @Nullable Object parentElement, @Nullable Object element) {
            IChartType type = fType;

            if (type == null) {
                return false;
            }

            DataDescriptor descriptor = checkNotNull((DataDescriptor) element);
            return type.filterX(descriptor.getAspect(), fXAspectFilter);
        }
    }

    /**
     * This filter is used for filtering labels in the Y selection table.
     */
    private class CreatorYFilter extends ViewerFilter {
        @Override
        public boolean select(@Nullable Viewer viewer, @Nullable Object parentElement, @Nullable Object element) {
            IChartType type = fType;

            if (type == null) {
                return false;
            }

            DataDescriptor descriptor = checkNotNull((DataDescriptor) element);
            return type.filterY(descriptor.getAspect(), fYAspectFilter);
        }
    }

    /**
     * This listener handles the event when a selection is made in the X
     * selection table.
     */
    private class CreatorXSelectedEvent implements ISelectionChangedListener {
        @Override
        public void selectionChanged(@Nullable SelectionChangedEvent event) {
            fAddButton.setEnabled(checkIfButtonReady());
        }
    }

    /**
     * This listener handles the event when a value is checked in the Y
     * selection table.
     */
    private class CreatorYSelectedEvent implements ICheckStateListener {
        @Override
        public void checkStateChanged(@Nullable CheckStateChangedEvent event) {
            if (event == null) {
                return;
            }

            if (event.getChecked()) {
                if (fYAspectFilter == null) {
                    DataDescriptor descriptor = (DataDescriptor) event.getElement();
                    fYAspectFilter = descriptor.getAspect();
                }
            } else {
                tryResetYFilter();
            }

            fSelectionY.refresh();
            fAddButton.setEnabled(checkIfButtonReady());
        }
    }

    /**
     * This listener handle the event when the add button of the series creator
     * is clicked.
     */
    private class AddButtonClickedEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            DataDescriptor descriptorX = (DataDescriptor) checkNotNull(fSelectionX.getStructuredSelection().getFirstElement());
            Object[] descriptorsY = fSelectionY.getCheckedElements();

            /* Create a series for each Y axis */
            for (int i = 0; i < descriptorsY.length; i++) {
                DataDescriptor descriptorY = (DataDescriptor) descriptorsY[i];
                ChartSeriesDialog series = new ChartSeriesDialog(descriptorX, checkNotNull(descriptorY));

                if (!checkIfSeriesPresent(series)) {
                    fSeries.add(series);
                }
            }

            /* Set the X filter */
            if (fXAspectFilter == null) {
                fXAspectFilter = descriptorX.getAspect();
            }

            /* Refresh tables */
            fSeriesTable.refresh();
            fSelectionX.refresh();

            /* Enable OK button */
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
    }
}
