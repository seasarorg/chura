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
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.seasar.dolteng.eclipse.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.CorextMessages;

public class Resources {

    private Resources() {
    }

    /**
     * Checks if the given resource is in sync with the underlying file system.
     * 
     * @param resource
     *            the resource to be checked
     * @return IStatus status describing the check's result. If <code>status.
     * isOK()</code>
     *         returns <code>true</code> then the resource is in sync
     */
    public static IStatus checkInSync(IResource resource) {
        return checkInSync(new IResource[] { resource });
    }

    /**
     * Checks if the given resources are in sync with the underlying file
     * system.
     * 
     * @param resources
     *            the resources to be checked
     * @return IStatus status describing the check's result. If <code>status.
     *  isOK() </code>
     *         returns <code>true</code> then the resources are in sync
     */
    public static IStatus checkInSync(IResource[] resources) {
        IStatus result = null;
        for (IResource resource : resources) {
            if (!resource.isSynchronized(IResource.DEPTH_INFINITE)) {
                result = addOutOfSync(result, resource);
            }
        }
        if (result != null) {
            return result;
        }
        return new Status(IStatus.OK, Constants.ID_PLUGIN, IStatus.OK, "", null); //$NON-NLS-1$
    }

    /**
     * Makes the given resource committable. Committable means that it is
     * writeable and that its content hasn't changed by calling
     * <code>validateEdit</code> for the given resource on <tt>IWorkspace</tt>.
     * 
     * @param resource
     *            the resource to be checked
     * @param context
     *            the context passed to <code>validateEdit</code>
     * @return status describing the method's result. If
     *         <code>status.isOK()</code> returns <code>true</code> then the
     *         resources are committable.
     * 
     * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[],
     *      java.lang.Object)
     */
    public static IStatus makeCommittable(IResource resource, Object context) {
        return makeCommittable(new IResource[] { resource }, context);
    }

    /**
     * Makes the given resources committable. Committable means that all
     * resources are writeable and that the content of the resources hasn't
     * changed by calling <code>validateEdit</code> for a given file on
     * <tt>IWorkspace</tt>.
     * 
     * @param resources
     *            the resources to be checked
     * @param context
     *            the context passed to <code>validateEdit</code>
     * @return IStatus status describing the method's result. If <code>status.
     * isOK()</code>
     *         returns <code>true</code> then the add resources are
     *         committable
     * 
     * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[],
     *      java.lang.Object)
     */
    public static IStatus makeCommittable(IResource[] resources, Object context) {
        List<IResource> readOnlyFiles = new ArrayList<IResource>();
        for (IResource resource : resources) {
            if (resource.getType() == IResource.FILE && isReadOnly(resource)) {
                readOnlyFiles.add(resource);
            }
        }
        if (readOnlyFiles.size() == 0) {
            return new Status(IStatus.OK, Constants.ID_PLUGIN, IStatus.OK,
                    "", null); //$NON-NLS-1$
        }

        Map oldTimeStamps = createModificationStampMap(readOnlyFiles);
        IStatus status = ResourcesPlugin.getWorkspace().validateEdit(
                readOnlyFiles
                        .toArray(new IFile[readOnlyFiles.size()]), context);
        if (!status.isOK()) {
            return status;
        }

        IStatus modified = null;
        Map newTimeStamps = createModificationStampMap(readOnlyFiles);
        for (Iterator iter = oldTimeStamps.keySet().iterator(); iter.hasNext();) {
            IFile file = (IFile) iter.next();
            if (!oldTimeStamps.get(file).equals(newTimeStamps.get(file))) {
                modified = addModified(modified, file);
            }
        }
        if (modified != null) {
            return modified;
        }
        return new Status(IStatus.OK, Constants.ID_PLUGIN, IStatus.OK, "", null); //$NON-NLS-1$
    }

    private static Map<IFile, Long> createModificationStampMap(List<IResource> files) {
        Map<IFile, Long> map = new HashMap<IFile, Long>();
        for (IResource file : files) {
            map.put((IFile) file, new Long(file.getModificationStamp()));
        }
        return map;
    }

    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[] { object });
    }

    private static IStatus addModified(IStatus status, IFile file) {
        IStatus entry = StatusUtil.createError(DoltengCore.getDefault(), 10003,
                format(CorextMessages.Resources_fileModified, file
                        .getFullPath().toString()), null);
        if (status == null) {
            return entry;
        } else if (status.isMultiStatus()) {
            ((MultiStatus) status).add(entry);
            return status;
        } else {
            MultiStatus result = new MultiStatus(Constants.ID_PLUGIN, 10003,
                    CorextMessages.Resources_modifiedResources, null);
            result.add(status);
            result.add(entry);
            return result;
        }
    }

    private static IStatus addOutOfSync(IStatus status, IResource resource) {
        IStatus entry = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES,
                IResourceStatus.OUT_OF_SYNC_LOCAL, format(
                        CorextMessages.Resources_outOfSync, resource
                                .getFullPath().toString()), null);
        if (status == null) {
            return entry;
        } else if (status.isMultiStatus()) {
            ((MultiStatus) status).add(entry);
            return status;
        } else {
            MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES,
                    IResourceStatus.OUT_OF_SYNC_LOCAL,
                    CorextMessages.Resources_outOfSyncResources, null);
            result.add(status);
            result.add(entry);
            return result;
        }
    }

    public static String[] getLocationOSStrings(IResource[] resources) {
        List<String> result = new ArrayList<String>(resources.length);
        for (IResource element : resources) {
            IPath location = element.getLocation();
            if (location != null) {
                result.add(location.toOSString());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static boolean isReadOnly(IResource resource) {
        ResourceAttributes resourceAttributes = resource
                .getResourceAttributes();
        if (resourceAttributes == null) {
            // this resource
            return false;
        }
        return resourceAttributes.isReadOnly();
    }

    static void setReadOnly(IResource resource, boolean readOnly) {
        ResourceAttributes resourceAttributes = resource
                .getResourceAttributes();
        if (resourceAttributes == null) {
            // this resource
            return;
        }

        resourceAttributes.setReadOnly(readOnly);
        try {
            resource.setResourceAttributes(resourceAttributes);
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }
}
