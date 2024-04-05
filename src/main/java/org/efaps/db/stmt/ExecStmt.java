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
package org.efaps.db.stmt;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.eql.IEsjpExecute;
import org.efaps.eql.JSONData;
import org.efaps.eql2.IExecStatement;
import org.efaps.eql2.StmtFlag;
import org.efaps.json.data.DataList;
import org.efaps.json.data.ObjectData;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecStmt
    extends AbstractStmt
{

    private static final Logger LOG = LoggerFactory.getLogger(ExecStmt.class);
    private List<Map<String, Object>> data;

    public ExecStmt(final StmtFlag... _flags)
    {
        super(_flags);
    }

    public static ExecStmt get(final IExecStatement execStmt)
    {
        final var ret = new ExecStmt(execStmt.getFlags());
        ret.setEQLStmt(execStmt);
        return ret;
    }

    public ExecStmt execute()
        throws EFapsException
    {
        final IExecStatement execStmt = (IExecStatement) getEQLStmt();

        final Map<String, String> map = new LinkedHashMap<>();
        for (final var sel : execStmt.getSelections()) {
            map.put(sel.getIndex(), sel.getAlias());
        }
        try {
            final Class<?> clazz = Class.forName(execStmt.getClassName(), false, EFapsClassLoader.getInstance());

            final IEsjpExecute esjp = (IEsjpExecute) clazz.getConstructor().newInstance();
            ExecStmt.LOG.debug("Instantiated class: {}", esjp);
            if (execStmt.getParametersLength() == 0) {
                data = esjp.execute(map);
            } else {
                data = esjp.execute(map, execStmt.getParameters());
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            LOG.error("Catched", e);
        }
        return this;
    }

    public List<Map<String, Object>> getData()
        throws EFapsException
    {
        if (data == null) {
            execute();
        }
        return data;
    }

    public DataList getDataList()
        throws EFapsException
    {
        getData();
        final IExecStatement execStmt = (IExecStatement) getEQLStmt();
        final DataList ret = new DataList();
        for (final var entry : data) {
            final ObjectData objectData = new ObjectData();
            for (final var select : execStmt.getSelections()) {
                final String key = select.getAlias() == null ? String.valueOf(select.getIndex()) : select.getAlias();
                objectData.getValues().add(JSONData.getValue(key, entry.get(key)));
            }
            ret.add(objectData);
        }
        return ret;
    }
}
