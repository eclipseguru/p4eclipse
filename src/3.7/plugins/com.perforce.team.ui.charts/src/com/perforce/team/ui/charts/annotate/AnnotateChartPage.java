/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.annotate;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.charts.ChartCanvas;
import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;

import java.text.MessageFormat;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AnnotateChartPage extends TimeLapsePage {

    private ManagedForm form;
    private Image headerImage;
    private ChartCanvas canvas;
    private String name = ""; //$NON-NLS-1$

    /**
     * Set the editor to use
     * 
     * @param editor
     */
    @Override
    public void setEditor(TextTimeLapseEditor editor) {
        super.setEditor(editor);
        IP4Resource resource = P4CoreUtils.convert(editor.getEditorInput(),
                IP4Resource.class);
        if (resource != null) {
            this.name = resource.getName();
        }
    }

    /**
     * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        form = new ManagedForm(parent);
        ScrolledForm formControl = form.getForm();
        FormToolkit toolkit = form.getToolkit();
        formControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite body = formControl.getBody();
        body.setBackgroundMode(SWT.INHERIT_DEFAULT);
        GridLayout bLayout = new GridLayout(1, true);
        bLayout.marginHeight = 0;
        bLayout.marginWidth = 0;
        body.setLayout(bLayout);
        toolkit.adapt(body);

        canvas = new ChartCanvas() {

            @Override
            protected boolean shouldDraw() {
                return model != null && model.getRevisionCount() != 0;
            }

            @Override
            protected Chart createChart() {
                AnnotateChart chart = new AnnotateChart();
                return chart.build(model);
            }
        };
        canvas.createControl(body);
        toolkit.decorateFormHeading(formControl.getForm());
        updateTitle();
    }

    /**
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return this.form.getForm();
    }

    private void updateTitle() {
        if (headerImage == null && name.length() > 0) {
            ImageDescriptor descriptor = P4UIUtils.getImageDescriptor(name);
            if (descriptor != null) {
                headerImage = descriptor.createImage();
                P4UIUtils.registerDisposal(getControl(), headerImage);
                form.getForm().setImage(headerImage);
            }
        }
        form.getForm().setText(
                MessageFormat.format(Messages.AnnotateChart_LineCountHistory,
                        name));
    }

    /**
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        this.form.getForm().setFocus();
    }

    /**
     * @see com.perforce.team.ui.charts.annotate.TimeLapsePage#modelRefreshed()
     */
    @Override
    protected void modelRefreshed() {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                if (P4UIUtils.okToUse(getControl())) {
                    canvas.redraw(true);
                }
            }
        });
    }

}
