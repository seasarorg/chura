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
package org.seasar.dolteng.core.kuina;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.seasar.framework.util.StringUtil;
import org.seasar.kuina.dao.internal.Command;
import org.seasar.kuina.dao.internal.builder.AbstractQueryCommandBuilder;
import org.seasar.kuina.dao.internal.condition.ConditionalExpressionBuilderFactory;

/**
 * @author taichi
 * 
 */
public class KuinaEmulator {

    public static class Operations extends ConditionalExpressionBuilderFactory {
        public static String[][][] getOperations() {
            return OPERATIONS;
        }

        public static String toPropertyName(String name, String suffix) {
            return ConditionalExpressionBuilderFactory.toPropertyName(name,
                    suffix);
        }
    }

    public static class QueryPatterns extends AbstractQueryCommandBuilder {
        public static Pattern[] getPatterns() {
            QueryPatterns a = new QueryPatterns();
            return new Pattern[] { a.orderbyPattern, a.firstResultPattern,
                    a.maxResultsPattern };
        }

        public Command build(Class<?> arg0, Method arg1) {
            return null;
        }
    }

    public static boolean isQueryPatterns(String paramName) {
        Pattern[] patterns = QueryPatterns.getPatterns();
        for (int i = 0; i < patterns.length; i++) {
            Pattern pattern = patterns[i];
            if (pattern.matcher(paramName).matches()) {
                return true;
            }
        }
        return false;
    }

    public static String[] splitPropertyName(String paramName) {
        String[] result = null;
        if (StringUtil.isEmpty(paramName) == false) {
            result = paramName.split("\\$");
            if (result != null && 0 < result.length) {
                result[result.length - 1] = toPropertyName(result[result.length - 1]);
            }
        }

        return result;
    }

    public static String toPropertyName(String paramName) {
        String result = null;
        if (StringUtil.isEmpty(paramName) == false) {
            String[][][] operations = Operations.getOperations();
            for (int j = 0; j < operations.length; j++) {
                for (int k = 0; k < operations[j].length; k++) {
                    for (int l = 0; l < operations[j][k].length; l++) {
                        String s = operations[j][k][l];
                        if (paramName.endsWith(s)) {
                            return Operations.toPropertyName(paramName, s);
                        }
                    }
                }
            }
        }

        return result;
    }
}
