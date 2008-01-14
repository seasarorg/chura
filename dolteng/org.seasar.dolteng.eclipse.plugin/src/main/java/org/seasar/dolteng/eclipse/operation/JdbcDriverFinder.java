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
package org.seasar.dolteng.eclipse.operation;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.framework.util.ClassTraversal;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.ClassTraversal.ClassHandler;

/**
 * @author taichi
 * 
 */
public class JdbcDriverFinder implements IRunnableWithProgress {

    private URLClassLoader loader;

    private List<JarFile> jars = new ArrayList<JarFile>();

    private List<String> driverClasses = new ArrayList<String>();

    public JdbcDriverFinder(String[] paths) {
        List<URL> urls = new ArrayList<URL>(paths.length);
        for (String path : paths) {
            File f = new File(path);
            try {
                urls.add(f.toURI().toURL());
                jars.add(new JarFile(f));
            } catch (Exception e) {
                DoltengCore.log(e);
            }
        }
        loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    public String[] getDriverClasses() {
        return this.driverClasses
                .toArray(new String[this.driverClasses.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InterruptedException {
        int sizes = 0;
        for (JarFile jar : jars) {
            sizes += jar.size();
        }
        monitor.beginTask(Messages.JDBC_DRIVER_FINDING, sizes);
        try {
            for (JarFile jar : jars) {
                ClassTraversal.forEach(jar, new JdbcClassHandler(monitor));
            }
        } catch (OperationCanceledException e) {
            throw new InterruptedException(
                    Messages.JDBC_DRIVER_FINDING_CANCELLED);
        } finally {
            monitor.done();
        }
    }

    private class JdbcClassHandler implements ClassHandler {

        private IProgressMonitor monitor;

        JdbcClassHandler(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.seasar.framework.util.ClassTraversal.ClassHandler#processClass(java.lang.String,
         *      java.lang.String)
         */
        public void processClass(String packageName, String shortClassName) {
            try {
                String className = ClassUtil.concatName(packageName,
                        shortClassName);
                monitor.subTask(className);
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                Class clazz = JdbcDriverFinder.this.loader.loadClass(className);
                if (Driver.class.isAssignableFrom(clazz)
                        && Driver.class.equals(clazz) == false) {
                    JdbcDriverFinder.this.driverClasses.add(className);
                }
            } catch (ClassNotFoundException e) {
            } catch (NoClassDefFoundError e) {
            } finally {
                this.monitor.worked(1);
            }
        }
    }

}
