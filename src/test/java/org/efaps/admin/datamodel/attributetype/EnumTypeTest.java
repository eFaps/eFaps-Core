/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.efaps.admin.datamodel.attributetype;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.mock.MockEnum;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.annotations.Test;


public class EnumTypeTest
    extends AbstractTest
{

    @Test
    public void testReadIntValue()
        throws EFapsException
    {
        final var attr = Attribute.get(Mocks.AllAttrEnumAttribute.getId());

        final List<Object> valueList = Collections.singletonList(1);
        final var result = new EnumType().readValue(attr, valueList);
        assertEquals(result, MockEnum.VAL2);
    }

    @Test
    public void testReadStringNumberValue()
        throws EFapsException
    {
        final var attr = Attribute.get(Mocks.AllAttrEnumAttribute.getId());

        final List<Object> valueList = Collections.singletonList("1");
        final var result = new EnumType().readValue(attr, valueList);
        assertEquals(result, MockEnum.VAL2);
    }

    @Test
    public void testReadStringKeyValue()
        throws EFapsException
    {
        final var attr = Attribute.get(Mocks.AllAttrEnumAttribute.getId());

        final List<Object> valueList = Collections.singletonList("VAL2");
        final var result = new EnumType().readValue(attr, valueList);
        assertEquals(result, MockEnum.VAL2);
    }

    @Test
    public void testReadStringKeyValueIgnorecase()
        throws EFapsException
    {
        final var attr = Attribute.get(Mocks.AllAttrEnumAttribute.getId());

        final List<Object> valueList = Collections.singletonList("val2");
        final var result = new EnumType().readValue(attr, valueList);
        assertEquals(result, MockEnum.VAL2);
    }
}
