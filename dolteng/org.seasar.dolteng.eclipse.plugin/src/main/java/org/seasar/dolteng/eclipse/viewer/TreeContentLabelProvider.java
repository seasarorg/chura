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
package org.seasar.dolteng.eclipse.viewer;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.seasar.dolteng.eclipse.model.ContentDescriptor;

/**
 * @author taichi
 * 
 */
public class TreeContentLabelProvider extends LabelProvider {

    private LabelStrategy labelStrategy;

    public TreeContentLabelProvider() {
        this.labelStrategy = LabelStrategy.NULL;
    }

    public TreeContentLabelProvider(LabelStrategy strategy) {
        this.labelStrategy = strategy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        if (element instanceof ContentDescriptor) {
            ContentDescriptor cd = (ContentDescriptor) element;
            return cd.getImage();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        if (element instanceof ContentDescriptor) {
            ContentDescriptor cd = (ContentDescriptor) element;
            return this.labelStrategy.convertText(cd);
        }
        return "";
    }

    /**
     * @param labelStrategy
     *            The labelStrategy to set.
     */
    public void setLabelStrategy(LabelStrategy labelStrategy) {
        this.labelStrategy = labelStrategy;
    }

}
