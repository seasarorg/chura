/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dolteng.eclipse.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.seasar.dolteng.core.types.MxComponentValueResolver;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.MxBindingMappingRow;
import org.seasar.dolteng.eclipse.model.impl.BasicMxBindingMappingRow;
import org.seasar.dolteng.eclipse.model.impl.ComponentIdColumn;
import org.seasar.dolteng.eclipse.model.impl.DestIdColumn;
import org.seasar.dolteng.eclipse.model.impl.IsGenerateColumn;
import org.seasar.dolteng.eclipse.model.impl.SrcAttrColumn;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ActionScriptUtil;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TableProvider;
import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;
import org.seasar.framework.util.StringUtil;

import uk.co.badgersinfoil.metaas.dom.ASClassType;
import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;
import uk.co.badgersinfoil.metaas.dom.ASField;
import uk.co.badgersinfoil.metaas.dom.ASType;

/**
 * @author taichi
 * 
 */
public class AddBindingWizardPage extends WizardPage {

    private Text typeText;

    private TableViewer viewer;

    private List<MxBindingMappingRow> mappingRows = new ArrayList<MxBindingMappingRow>();

    private IFile mxml = null;

    private IFile asdto = null;

    /**
     * @param pageName
     * @param title
     * @param titleImage
     */
    public AddBindingWizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    /**
     * @param pageName
     */
    public AddBindingWizardPage(String pageName) {
        super(pageName);
    }

    public AddBindingWizardPage() {
        super("AddBindingWizardPage");
    }

    public void setMxml(IFile mxml) {
        this.mxml = mxml;
    }

    public void setAsDto(IFile asdto) {
        this.asdto = asdto;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);

        GridData data;
        Label l = new Label(composite, SWT.NONE);
        l.setText(Labels.WIZARD_BINDING_DTO_FILE);
        typeText = new Text(composite, SWT.BORDER | SWT.SINGLE);
        typeText.setEnabled(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        typeText.setLayoutData(data);
        Button browse = new Button(composite, SWT.PUSH);
        browse.setText(Labels.BROWSE);
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                        WorkbenchUtil.getShell(), ProjectUtil
                                .getWorkspaceRoot(), IResource.PROJECT
                                | IResource.FOLDER | IResource.FILE);
                dialog.setTitle(Messages.SELECT_ACTION_SCRIPT_DTO);
                dialog.setAllowMultiple(false);
                dialog.setInitialSelection(mxml.getParent());
                dialog.addFilter(new ViewerFilter() {
                    @Override
                    public boolean select(Viewer viewer, Object parentElement,
                            Object element) {
                        if (element instanceof IFile) {
                            IFile file = (IFile) element;
                            return file.getFileExtension().endsWith("as");
                        }
                        return true;
                    }
                });
                if (dialog.open() != Window.OK) {
                    return;
                }
                Object[] selected = dialog.getResult();
                if (selected == null || selected.length < 1) {
                    return;
                }
                if ((selected[0] instanceof IFile) == false) {
                    return;
                }
                final IFile file = (IFile) selected[0];
                typeText.setText(file.getFullPath().toString());
                setAsDto(file);
                try {
                    getWizard().getContainer().run(false, false,
                            new IRunnableWithProgress() {
                                public void run(IProgressMonitor monitor) {
                                    monitor = ProgressMonitorUtil.care(monitor);
                                    monitor.beginTask(
                                            Messages.RELOAD_RESOURCES, 2);
                                    try {
                                        createRows();
                                        ProgressMonitorUtil.isCanceled(monitor,
                                                1);
                                        viewer.refresh();
                                        ProgressMonitorUtil.isCanceled(monitor,
                                                1);
                                    } finally {
                                        monitor.done();
                                    }
                                }
                            });
                } catch (Exception e) {
                    DoltengCore.log(e);
                }
            }
        });

        this.viewer = new TableViewer(composite, SWT.BORDER
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        table.setLayoutData(data);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TableProvider(viewer,
                createColumnDescs(table)));
        viewer.setSorter(new ComparableViewerSorter());
        viewer.setInput(this.mappingRows);

        setControl(composite);
    }

    private ColumnDescriptor[] createColumnDescs(Table table) {
        List<ColumnDescriptor> descs = new ArrayList<ColumnDescriptor>();
        descs.add(new IsGenerateColumn(table));
        descs.add(new ComponentIdColumn(table));
        descs.add(new SrcAttrColumn(table));
        descs.add(new DestIdColumn(table));
        return descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    @SuppressWarnings("unchecked")
    public Object createRows() {
        if (asdto == null || asdto.exists() == false || mxml == null
                || mxml.exists() == false) {
            return null;
        }
        Set<String> dtoIds = parseAsDTO(asdto);
        Map<String, String> mxmlIds = parseMxml(mxml);

        for (String id : dtoIds) {
            String srcAttr = mxmlIds.get(id);
            if (StringUtil.isEmpty(srcAttr) == false) {
                BasicMxBindingMappingRow row = new BasicMxBindingMappingRow();
                row.setComponentId(id);
                row.setSrcAttr(srcAttr);
                row.setDestId("page.model." + id);
                row.setGenerate(true);
                this.mappingRows.add(row);
            }
        }

        Collections.sort(this.mappingRows);
        return this.mappingRows;
    }

    private Map<String, String> parseMxml(IFile mxml) {
        MxComponentValueResolver resolver = DoltengCore.getMxResolver(mxml
                .getProject());
        Map<String, String> result = new HashMap<String, String>();

        try {
            FuzzyXMLNode[] nodes = FuzzyXMLUtil.selectNodes(mxml, "//@id");
            for (FuzzyXMLNode node : nodes) {
                if (node instanceof FuzzyXMLAttribute) {
                    FuzzyXMLAttribute a = (FuzzyXMLAttribute) node;
                    FuzzyXMLNode n = a.getParentNode();
                    String value = resolver
                            .resolveValueAttribute((FuzzyXMLElement) n);
                    result.put(a.getValue(), value);
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    private Set<String> parseAsDTO(IFile asdto) {
        Set<String> result = new HashSet<String>();
        ASCompilationUnit unit = ActionScriptUtil.parse(asdto);
        if (unit != null) {
            ASType type = unit.getType();
            if (type instanceof ASClassType) {
                ASClassType clazz = (ASClassType) type;
                List fields = clazz.getFields();
                for (Iterator i = fields.iterator(); i.hasNext();) {
                    ASField f = (ASField) i.next();
                    result.add(f.getName());
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            processDefaultLocationDto();
        }
        super.setVisible(visible);
    }

    private void processDefaultLocationDto() {
        try {
            getWizard().getContainer().run(false, false,
                    new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) {
                            monitor = ProgressMonitorUtil.care(monitor);
                            try {
                                monitor.beginTask(Messages.RELOAD_RESOURCES, 3);
                                String path = mxml
                                        .getPersistentProperty(Constants.PROP_FLEX_PAGE_DTO_PATH);
                                if (path != null
                                        && StringUtil.isEmpty(path) == false) {
                                    IWorkspaceRoot root = ProjectUtil
                                            .getWorkspaceRoot();
                                    IResource r = root
                                            .findMember(new Path(path));
                                    if (r != null
                                            && r.getType() == IResource.FILE) {
                                        IFile f = (IFile) r;
                                        asdto = f;
                                        typeText.setText(f.getFullPath()
                                                .toString());
                                        ProgressMonitorUtil.isCanceled(monitor,
                                                1);
                                        createRows();
                                        ProgressMonitorUtil.isCanceled(monitor,
                                                1);
                                        viewer.refresh();
                                        ProgressMonitorUtil.isCanceled(monitor,
                                                1);
                                    }
                                }
                            } catch (Exception e) {
                                DoltengCore.log(e);
                            } finally {
                                monitor.done();
                            }
                        }
                    });
        } catch (Exception e) {
            DoltengCore.log(e);
        }

    }

    public List<MxBindingMappingRow> getMappingRows() {
        return mappingRows;
    }
}
