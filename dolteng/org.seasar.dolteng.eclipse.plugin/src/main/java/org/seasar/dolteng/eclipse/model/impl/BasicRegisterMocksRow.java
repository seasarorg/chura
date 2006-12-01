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
package org.seasar.dolteng.eclipse.model.impl;

import org.seasar.dolteng.eclipse.model.RegisterMocksRow;

/**
 * @author taichi
 * 
 */
public class BasicRegisterMocksRow implements RegisterMocksRow {

    private boolean register = true;

    private String packageName;

    private String interfaceName;

    private String implementationName;

    /**
     * 
     */
    public BasicRegisterMocksRow(String pkg, String inf, String impl) {
        super();
        packageName = pkg;
        interfaceName = inf;
        implementationName = impl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.RegisterMocksRow#isRegister()
     */
    public boolean isRegister() {
        return register;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.RegisterMocksRow#setRegister(boolean)
     */
    public void setRegister(boolean is) {
        register = is;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.RegisterMocksRow#getPackageName()
     */
    public String getPackageName() {
        return packageName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.RegisterMocksRow#getInterfaceName()
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.RegisterMocksRow#getImplementationName()
     */
    public String getImplementationName() {
        return implementationName;
    }

}
