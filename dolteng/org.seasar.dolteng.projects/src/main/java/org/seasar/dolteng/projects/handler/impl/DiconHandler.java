package org.seasar.dolteng.projects.handler.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.handler.ResourceHandler;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.dolteng.projects.model.dicon.DiconModel;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.InputStreamUtil;

/**
 * diconファイルを構築するハンドラ
 * 
 * @author daisuke
 */
public abstract class DiconHandler extends DefaultHandler {

    protected transient IFile diconFile;

    private String filename;

    public static HashMap<String, DiconModel> staticModels = new HashMap<String, DiconModel>();
    
    private final HashMap<String, DiconModel> models;

    protected DiconHandler(String filename, HashMap<String, DiconModel> models) {
        this.models = models;
        this.filename = filename;
        if (models.get(filename) == null) {
            models.put(filename, new DiconModel(filename));
        }
    }

    @Override
    public abstract String getType();

    @Override
    public void add(Entry entry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void merge(ResourceHandler handler) {
        // nothing to do
    }

    @Override
    public int getNumberOfFiles() {
        int result = models.size();
        for (DiconModel dicon : models.values()) {
            result += dicon.size();
        }
        return result;
    }

    @Override
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        monitor.setTaskName(Messages.bind(Messages.PROCESS, filename));

        IFile output = builder.getProjectHandle().getFile(
                builder.getConfigContext()
                        .get(Constants.CTX_MAIN_RESOURCE_PATH)
                        + "/" + filename);

        InputStream src = null;
        BufferedReader in = null;
        try {
            String definition = models.get(filename).buildElement(0, monitor);
            src = new ByteArrayInputStream(definition.getBytes("UTF-8"));
            output.create(src, IResource.FORCE, null);
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new IORuntimeException(e);
                }
            }
            InputStreamUtil.close(src);
        }
    }

    public DiconModel getModel() {
        return models.get(filename);
    }

    public static void init() {
        staticModels = new HashMap<String, DiconModel>();
    }
}
