/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui;

import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;

public class ChartMakerDialog extends Dialog {

    private Composite fComposite;
    private Combo fComboDataModel;
    private List fSelectionX;
    private CheckboxTableViewer fSelectionY;

    private class DataModelSelected extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent event) {
            int index = fComboDataModel.getSelectionIndex();

            fSelectionX.removeAll();
            AbstractDataModel.getInstances().get(index).getDataDescriptors().stream()
                    .map(descriptor -> descriptor.getAspect().getLabel())
                    .forEach(fSelectionX::add);

            java.util.List<String> input = AbstractDataModel.getInstances().get(index).getDataDescriptors().stream()
                    .map(descriptor -> descriptor.getAspect().getLabel())
                    .collect(Collectors.toList());
            fSelectionY.setInput(input);
        }
    }

    public ChartMakerDialog(Shell parent) {
        super(parent);

        fComboDataModel = new Combo (parent, SWT.READ_ONLY);
        fSelectionX = new List(parent, SWT.BORDER);
        fSelectionY = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);

        setShellStyle(getShellStyle() | SWT.RESIZE);
        AbstractDataModel.getInstances().stream()
                .map(model -> model.getName().hashCode())
                .forEach(System.out::println);
    }

    @Override
    public Composite createDialogArea(Composite parent) {
        fComposite = (Composite) super.createDialogArea(parent);

        /*
         * Layouts
         */
        GridLayout baseLayout = new GridLayout();
        baseLayout.numColumns = 1;
        baseLayout.marginHeight = 5;
        baseLayout.marginWidth = 5;

        GridData baseSelectorGridData = new GridData();
        baseSelectorGridData.horizontalAlignment = SWT.FILL;
        baseSelectorGridData.grabExcessHorizontalSpace = true;

        GridData baseCreatorGridData = new GridData();
        baseCreatorGridData.horizontalAlignment = SWT.FILL;
        baseCreatorGridData.verticalAlignment = SWT.FILL;
        baseCreatorGridData.grabExcessHorizontalSpace = true;
        baseCreatorGridData.grabExcessVerticalSpace = true;

        GridData baseOptionsGridData = new GridData();
        baseOptionsGridData.horizontalAlignment = SWT.FILL;
        baseOptionsGridData.grabExcessHorizontalSpace = true;

        GridLayout creatorLayout = new GridLayout();
        creatorLayout.numColumns = 2;

        GridLayout optionsLayout = new GridLayout();
        optionsLayout.numColumns = 2;

        fComposite.setLayout(baseLayout);

        /*
         * Groups
         */
        GridLayout selectorLayout = new GridLayout();
        selectorLayout.numColumns = 2;
        Composite selector = new Composite(fComposite, SWT.FILL);
        selector.setLayout(selectorLayout);
        selector.setLayoutData(baseSelectorGridData);

        Group creatorGroup = new Group(fComposite, SWT.BORDER);
        creatorGroup.setText("Series Creator");
        creatorGroup.setLayout(creatorLayout);
        creatorGroup.setLayoutData(baseCreatorGridData);

        Group optionsGroup = new Group(fComposite, SWT.BORDER);
        optionsGroup.setText("Options");
        optionsGroup.setLayout(optionsLayout);
        optionsGroup.setLayoutData(baseOptionsGridData);

        /*
         * Data model selector
         */
        GridData selectorComboGridData = new GridData(SWT.FILL);
        selectorComboGridData .horizontalAlignment = SWT.FILL;
        selectorComboGridData .grabExcessHorizontalSpace = true;

        Label selectorLabel = new Label(selector, SWT.NONE);
        selectorLabel.setText("Available Data");

        fComboDataModel.setParent(selector);
        fComboDataModel.setLayoutData(selectorComboGridData);
        fComboDataModel.addSelectionListener(new DataModelSelected());
        AbstractDataModel.getInstances().stream()
                .map(model -> model.getName())
                .forEach(fComboDataModel::add);

        /*
         * Series Creator
         */
        GridData creatorLabelGridData = new GridData();
        creatorLabelGridData.horizontalAlignment = SWT.CENTER;
        creatorLabelGridData.verticalAlignment = SWT.BOTTOM;

        GridData creatorBoxGridData = new GridData(SWT.FILL);
        creatorBoxGridData.horizontalAlignment = SWT.FILL;
        creatorBoxGridData.verticalAlignment = SWT.FILL;
        creatorBoxGridData.grabExcessHorizontalSpace = true;
        creatorBoxGridData.grabExcessVerticalSpace = true;

        Label creatorLabelX = new Label(creatorGroup, SWT.NONE);
        creatorLabelX.setText("X Axis");
        creatorLabelX.setLayoutData(creatorLabelGridData);

        Label creatorLabelY = new Label(creatorGroup, SWT.NONE);
        creatorLabelY.setText("Y Axis");
        creatorLabelY.setLayoutData(creatorLabelGridData);

        fSelectionX.setParent(creatorGroup);
        fSelectionX.setLayoutData(creatorBoxGridData);
        fSelectionY.getControl().setParent(creatorGroup);
        fSelectionY.getControl().setLayoutData(creatorBoxGridData);

        IStructuredContentProvider contentProvider = ArrayContentProvider.getInstance();
        fSelectionY.setContentProvider(contentProvider);

       // fSelectionY.setContentProvider(provider);

        /*
         * Options
         */
        //TODO

        this.getShell().addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Rectangle rect = getShell().getClientArea();
                Point size = fComposite.computeSize(rect.width, SWT.DEFAULT);
                fComposite.setSize(size);
            }
        });

        return fComposite;
    }
}
