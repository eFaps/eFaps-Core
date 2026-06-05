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
package org.efaps.db.store;

import java.time.Instant;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.infinispan.protostream.annotations.ProtoField;

public class StoreResourceInfo
{

    private long generalId;

    private String fileName;

    private long fileLength;

    private Instant modifiedInstant;

    private String modifiedZoneId;

    private boolean[] exist;

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
    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    @ProtoField(number = 3)
    public long getFileLength()
    {
        return fileLength;
    }

    public void setFileLength(long fileLength)
    {
        this.fileLength = fileLength;
    }

    @ProtoField(number = 4)
    public boolean[] getExist()
    {
        return exist;
    }

    public void setExist(boolean[] exist)
    {
        this.exist = exist;
    }

    @ProtoField(number = 5)
    public Instant getModifiedInstant()
    {
        return modifiedInstant;
    }

    public void setModifiedInstant(Instant modifiedInstant)
    {
        this.modifiedInstant = modifiedInstant;
    }

    @ProtoField(number = 6)
    public String getModifiedZoneId()
    {
        return modifiedZoneId;
    }

    public void setModifiedZoneId(String modifiedZoneId)
    {
        this.modifiedZoneId = modifiedZoneId;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}
