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
package org.seasar.dolteng.core.teeda;

import junit.framework.TestCase;

/**
 * @author taichi
 * 
 */
public class TeedaEmulatorTest extends TestCase {

    public void testSkipIds() throws Exception {
        String[] ary = { "$aa", "a%p", "dd.", "allmessages", "messages",
                "Messages", "hogeMessage", "goHoge", "jumpHoge", "isHoge" };
        for (int i = 0; i < ary.length; i++) {
            assertTrue(ary[i], TeedaEmulator.MAPPING_SKIP_ID.matcher(ary[i])
                    .matches());
        }
        String[] ary2 = { "hoge-a", "message", "label", "gone", "jumper",
                "issue" };
        for (int i = 0; i < ary2.length; i++) {
            assertFalse(ary2[i], TeedaEmulator.MAPPING_SKIP_ID.matcher(ary2[i])
                    .matches());
        }
    }

    public void testCalcConditionMethodName() throws Exception {
        assertEquals("isTrue", TeedaEmulator
                .calcConditionMethodName("isNotTrue"));
        assertEquals("isTrue", TeedaEmulator.calcConditionMethodName("isTrue"));
        assertNull(TeedaEmulator.calcConditionMethodName("hoge"));
    }

    public void testConditionId() throws Exception {
        assertTrue(TeedaEmulator.MAPPING_CONDITION_ID.matcher("isComeFromList")
                .matches());
        assertTrue(TeedaEmulator.MAPPING_CONDITION_ID.matcher(
                "isNotComeFromList").matches());
    }

    public void testToMultiItemName() throws Exception {
        assertEquals("hogeItems", TeedaEmulator.toMultiItemName("hogeItems"));
        assertEquals("hogeItems", TeedaEmulator.toMultiItemName("hogeGrid"));
        assertEquals("hogeItems", TeedaEmulator.toMultiItemName("hogeGridX"));
        assertEquals("hogeItems", TeedaEmulator.toMultiItemName("hogeGridY"));
        assertEquals("hogeItems", TeedaEmulator.toMultiItemName("hogeGridXY"));
    }
}
