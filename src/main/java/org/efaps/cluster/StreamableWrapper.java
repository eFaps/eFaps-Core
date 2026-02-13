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
package org.efaps.cluster;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.jgroups.Global;
import org.jgroups.util.ByteArray;
import org.jgroups.util.SizeStreamable;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFapsUUID("deceedf1-9dc9-4f8c-9178-0c276a926969")
@EFapsApplication("eFaps-Kernel")
public class StreamableWrapper
    implements SizeStreamable
{

    private static final Logger LOG = LoggerFactory.getLogger(StreamableWrapper.class);

    protected Object obj;
    protected ByteArray serialized;

    public StreamableWrapper()
    {
    }

    public StreamableWrapper(final Serializable obj)
    {
        this.obj = Objects.requireNonNull(obj);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject()
    {
        return (T) obj;
    }

    public synchronized StreamableWrapper setObject(final Serializable obj)
    {
        this.obj = obj;
        this.serialized = null;
        return this;
    }

    public synchronized ByteArray getSerialized()
    {
        if (serialized != null)
            return serialized;
        try {
            return serialized = Util.objectToBuffer(obj);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getLength()
    {
        return getSerialized().getLength();
    }

    @Override
    public String toString()
    {
        return String.format("wrapper for: %s %s", obj, serialized != null ? "(" + serialized.getLength() + " bytes)" : "");
    }

    @Override
    public int serializedSize()
    {
        final int retval = Global.INT_SIZE; // length (integer)
        if (obj == null) {
            return retval;
        }
        return retval + getLength();
    }

    @Override
    public void writeTo(DataOutput out)
        throws IOException
    {
        if (obj != null) {
            final ByteArray arr = getSerialized();
            out.writeInt(arr.getLength());
            out.write(arr.getArray(), 0, arr.getLength());
        } else {
            out.writeInt(-1);
        }
    }

    @Override
    public void readFrom(DataInput in)
        throws IOException, ClassNotFoundException
    {
        final int len = in.readInt();
        if (len == -1) {
            return;
        }
        final byte[] tmp = new byte[len];
        in.readFully(tmp, 0, len);
        serialized = new ByteArray(tmp);
        try {
            Context.begin();
            final var classloader = EFapsClassLoader.getOfflineInstance(getClass().getClassLoader());
            obj = Util.objectFromBuffer(serialized, classloader);
            Context.rollback();
        } catch (EFapsException | ClassNotFoundException | IOException e) {
            LOG.error("Catched", e);
        }
    }
}
