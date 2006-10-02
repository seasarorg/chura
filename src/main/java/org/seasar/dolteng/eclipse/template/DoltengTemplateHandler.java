/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.dolteng.eclipse.template;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.core.template.RootModel;
import org.seasar.dolteng.core.template.TemplateHandler;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.PropertiesUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.OutputStreamUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DoltengTemplateHandler implements TemplateHandler {

    private Properties templateProps;

    private Properties structProps;

    private IProject project;

    private DoltengProjectPreferences pref;

    private TableMetaData table;

    private DoltengModel baseModel;

    /**
     * 
     */
    public DoltengTemplateHandler(String typeName, IProject project,
            TableNode node) {
        super();
        templateProps = PropertiesUtil.load("template/fm/" + typeName
                + ".properties");
        structProps = PropertiesUtil.load("template/fm/" + typeName
                + ".struct.properties");
        this.project = project;
        this.pref = DoltengCore.getPreferences(project);
        this.table = node.getMetaData();
        baseModel = new DoltengModel();
        baseModel.initialize(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#getResourceTypes()
     */
    public String[] getResourceTypes() {
        Set keys = templateProps.keySet();
        return (String[]) keys.toArray(new String[keys.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#getProcessModel(java.lang.String)
     */
    public RootModel getProcessModel(String typeName) {
        DoltengModel rootModel = new DoltengModel();
        rootModel.setTypeName(typeName);
        rootModel.setClazz(baseModel.getClazz());
        rootModel.setFields(baseModel.getFields());
        return rootModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#begin(org.seasar.dolteng.core.template.RootModel)
     */
    public void begin(RootModel model) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#open(org.seasar.dolteng.core.template.RootModel)
     */
    public OutputStream open(RootModel model) {
        try {
            NamingConvention nc = pref.getNamingConvention();
            String name = model.getTypeName();
            String path = structProps.getProperty(name.substring(0, name
                    .lastIndexOf('.')));
            path = path.replaceAll("__package_path__",
                    nc.getRootPackageNames()[0].replace('.', '/'));
            path = path.replaceAll("__sub_application__", table.getName()
                    .toLowerCase());
            path = path.replaceAll("__web_contentsRoot__", pref
                    .getWebContentsRoot());
            ResourcesUtil.createDir(project, path);
            // NamingConventionMirror的な感じで、model.getTypeNameの.で区切った２番目の区域から、File名を決める。
            String resourceName = ""; // TODO 未実装
            if (name.startsWith("java")) {
                resourceName = StringUtil.capitalize(resourceName);
                // テンプレートが適切にクラス名を、javaコードに記述出来る様にする。
                model.getClazz().setName(resourceName);
                model.getClazz().setPackageName(
                        nc.getRootPackageNames()[0] + "."
                                + table.getName().toLowerCase());
                resourceName += ".java";
            } else {
                resourceName = StringUtil.decapitalize(resourceName) + ".html";
            }
            IFile f = project.getFile(new Path(path));
            if (f.exists()) {
                f.delete(true, null);
            }
            ((DoltengModel) model).setResouce(f);
            return new FileOutputStream(f.getLocation().toFile());
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#done(org.seasar.dolteng.core.template.RootModel)
     */
    public void done(RootModel model) {
        DoltengModel dm = (DoltengModel) model;
        try {
            if (dm.getResouce().exists()) {
                dm.getResouce().refreshLocal(IResource.DEPTH_ZERO, null);
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#fail(org.seasar.dolteng.core.template.RootModel,
     *      java.lang.Exception)
     */
    public void fail(RootModel model, Exception e) {
        DoltengCore.log(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#close(java.io.OutputStream)
     */
    public void close(OutputStream stream) {
        OutputStreamUtil.close(stream);
    }

}
