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
package org.efaps.db;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.infinispan.protostream.annotations.ProtoField;

public class GenInstInfo
{

    private long generalId;

    private long exchangeId;

    private long exchangeSystemId;

    @ProtoField(number = 1)
    public long getGeneralId()
    {
        return generalId;
    }

    public void setGeneralId(long generalId)
    {
        this.generalId = generalId;
    }

    @ProtoField(number = 2)
    public long getExchangeId()
    {
        return exchangeId;
    }

    public void setExchangeId(long exchangeId)
    {
        this.exchangeId = exchangeId;
    }

    @ProtoField(number = 3)
    public long getExchangeSystemId()
    {
        return exchangeSystemId;
    }

    public void setExchangeSystemId(long exchangeSystemId)
    {
        this.exchangeSystemId = exchangeSystemId;
    }

    @Override
    public int hashCode()
    {
        return Long.valueOf(generalId).hashCode() + Long.valueOf(exchangeId).hashCode()
                        + Long.valueOf(exchangeSystemId).hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        boolean ret = false;
        if (obj instanceof final GenInstInfo info) {
            ret = info.getGeneralId() == getGeneralId() && info.getExchangeId() == getExchangeId()
                            && info.getExchangeSystemId() == getExchangeSystemId();
        } else {
            super.equals(obj);
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
