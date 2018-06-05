package com.perforce.team.ui.mylyn.editor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttributePart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;

/**
 * Adds sorting to fields based on preferences
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobTaskEditorPart extends TaskEditorAttributePart {

    private static Field ATTRIBUTE_EDITORS_FIELD = null;

    static {
        try {
            ATTRIBUTE_EDITORS_FIELD = TaskEditorAttributePart.class
                    .getDeclaredField("attributeEditors"); //$NON-NLS-1$
            if (ATTRIBUTE_EDITORS_FIELD != null) {
                ATTRIBUTE_EDITORS_FIELD.setAccessible(true);
            }
        } catch (SecurityException e) {
            ATTRIBUTE_EDITORS_FIELD = null;
        } catch (NoSuchFieldException e) {
            ATTRIBUTE_EDITORS_FIELD = null;
        } catch (IllegalArgumentException e) {
            ATTRIBUTE_EDITORS_FIELD = null;
        }
    }

    private JobFieldGroup page;
    private Map<String, Integer> fields;

    /**
     * Create a job editor part
     * 
     * @param page
     */
    public JobTaskEditorPart(JobFieldGroup page) {
        this.page = page;
        this.fields = new HashMap<String, Integer>();
        if (this.page != null) {
            JobField[] fields = this.page.getFields();
            for (int i = 0; i < fields.length; i++) {
                this.fields.put(fields[i].getField().getName(), i);
            }
        }
    }

    /**
     * @see org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttributePart#createContent(org.eclipse.ui.forms.widgets.FormToolkit,
     *      org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContent(FormToolkit toolkit, Composite parent) {
        if (ATTRIBUTE_EDITORS_FIELD != null) {
            try {
                Object reflected = ATTRIBUTE_EDITORS_FIELD.get(this);
                if (reflected instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) reflected;
                    sortAttributeEditors(list);
                }
            } catch (IllegalArgumentException e) {
                // Ignore and accept default sorting
            } catch (IllegalAccessException e) {
                // Ignore and accept default sorting
            }
        }

        Control control = super.createContent(toolkit, parent);
        if (control instanceof Composite) { // Horizontal fill editable control
            for (Control child : ((Composite) control).getChildren()) {
                if (child instanceof Text || child instanceof Combo) {
                    Object data = control.getLayoutData();
                    if (data instanceof GridData) {
                        ((GridData) data).grabExcessHorizontalSpace = true;
                        ((GridData) data).horizontalAlignment = SWT.FILL;
                        child.setLayoutData(data);
                    }
                }
            }
        }

        if (fields.size() == 0) {
            if (control instanceof Composite) {
                Label label = new Label((Composite) control, SWT.NONE);
                label.setText(Messages.P4JobEditorPage_AdvancedAttributesEmpty);
            }
        }

        return control;
    }

    /**
     * @see org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorAttributePart#shouldExpandOnCreate()
     */
    @Override
    protected boolean shouldExpandOnCreate() {
        return true;
    }

    private boolean isGroupByType() {
        return PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.GROUP_FIELDS_BY_TYPE);
    }

    private void sortAttributeEditors(List<Object> editors) {
        if (isGroupByType()) {
            Collections.sort(editors, new Comparator<Object>() {

                public int compare(Object o1, Object o2) {
                    if (o1 instanceof AbstractAttributeEditor
                            && o2 instanceof AbstractAttributeEditor) {
                        AbstractAttributeEditor a1 = (AbstractAttributeEditor) o1;
                        AbstractAttributeEditor a2 = (AbstractAttributeEditor) o2;
                        LayoutHint hint1 = a1.getLayoutHint();
                        int p1 = hint1 != null
                                ? hint1.getPriority()
                                : LayoutHint.DEFAULT_PRIORITY;
                        LayoutHint hint2 = a2.getLayoutHint();
                        int p2 = hint2 != null
                                ? hint2.getPriority()
                                : LayoutHint.DEFAULT_PRIORITY;
                        if (p1 != p2) {
                            return p1 - p2;
                        } else {
                            p1 = fields.get(a1.getTaskAttribute().getMetaData()
                                    .getLabel());
                            p2 = fields.get(a2.getTaskAttribute().getMetaData()
                                    .getLabel());
                            return p1 - p2;
                        }
                    } else {
                        return 0;
                    }
                }
            });
        } else {
            Collections.sort(editors, new Comparator<Object>() {

                public int compare(Object o1, Object o2) {
                    if (o1 instanceof AbstractAttributeEditor
                            && o2 instanceof AbstractAttributeEditor) {
                        AbstractAttributeEditor a1 = (AbstractAttributeEditor) o1;
                        AbstractAttributeEditor a2 = (AbstractAttributeEditor) o2;
                        int p1 = fields.get(a1.getTaskAttribute().getMetaData()
                                .getLabel());
                        int p2 = fields.get(a2.getTaskAttribute().getMetaData()
                                .getLabel());
                        return p1 - p2;
                    } else {
                        return 0;
                    }
                }
            });
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart#createAttributeEditor(org.eclipse.mylyn.tasks.core.data.TaskAttribute)
     */
    @Override
    protected AbstractAttributeEditor createAttributeEditor(
            TaskAttribute attribute) {
        AbstractAttributeEditor editor = null;
        if (!TaskAttribute.DESCRIPTION.equals(attribute.getId())) {
            TaskAttributeMetaData properties = attribute.getMetaData();
            if (fields.containsKey(properties.getLabel())
                    && TaskAttribute.KIND_DEFAULT.equals(properties.getKind())) {
                editor = super.createAttributeEditor(attribute);
            }
        }
        return editor;
    }

}