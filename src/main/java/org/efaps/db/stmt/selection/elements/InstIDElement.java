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
package org.efaps.db.stmt.selection.elements;

import java.util.Collections;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.SQLOrder;
import org.efaps.util.EFapsException;

public class InstIDElement
    extends AbstractInstanceElement<InstIDElement>
    implements IOrderable
{

    public InstIDElement(final Type type)
    {
        super(type);
    }

    @Override
    public InstIDElement getThis()
    {
        return this;
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        final Object object = super.getObject(_row);
        return object == null ? null : ((Instance) object).getId();
    }

    @Override
    public void append2SQLOrder(int sequence,
                                SQLOrder order,
                                boolean desc)
        throws EFapsException
    {
        order.addElement(sequence, getTableIdx(order.getSqlSelect()).getIdx(), Collections.singletonList("ID"), desc);
    }
}
