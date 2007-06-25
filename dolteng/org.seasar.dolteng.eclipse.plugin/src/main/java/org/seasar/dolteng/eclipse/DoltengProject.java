package org.seasar.dolteng.eclipse;

import org.seasar.dolteng.core.types.AsTypeResolver;
import org.seasar.dolteng.core.types.MxComponentValueResolver;
import org.seasar.dolteng.core.types.TypeMappingRegistry;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;

public interface DoltengProject {

    DoltengPreferences getProjectPreferences();

    TypeMappingRegistry getTypeMappingRegistry();

    AsTypeResolver getAsTypeResolver();

    MxComponentValueResolver getMxComponentValueResolver();

}
