/*
 * Copyright 2003 - 2017 The eFaps Team
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
 *
 */

package org.efaps.db.stmt;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;

import org.eclipse.xtext.parser.IParseResult;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.EQLStandaloneSetup;
import org.efaps.eql2.IPrintObjectStatement;
import org.efaps.eql2.parser.antlr.EQLParser;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.efaps.util.RandomUtil;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

/**
 * The Class PrintStmtTest.
 */
public class PrintStmtTest
    extends AbstractTest
{

    /** The parser. */
    @Inject
    private EQLParser parser;

    /**
     * Sets the up.
     */
    @BeforeClass
    public void setUp()
    {
        EQLStandaloneSetup.doSetup(this);
    }

    @Test
    public void testSimplePrintObject()
        throws EFapsException
    {
        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.SimpleType.getId(), Mocks.TestAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder()
            .withSql("select T0.TestAttribute_COL from T_DEMO T0 where T0.ID = 4")
            .build();
        printStmt.execute();
        verify.verify();
    }

    @Test
    public void testSimplePrintObjectValue()
        throws EFapsException
    {
        final String sql = String.format("select T0.TestAttr_COL from T_DEMO T0 where T0.ID = 4",
                        Mocks.TypedType.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(String.class)
                        .append("A Value")
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.TypedType.getId(), Mocks.TypedTypeTestAttr.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test(description = "read a String Attribute")
    public void testStringAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(String.class)
                        .append("A Value")
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrStringAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), "A Value");
    }

    @Test
    public void testLongAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrLongAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(100)
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrLongAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), Long.valueOf(100));
    }

    @Test
    public void testIntegerAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrIntegerAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(101)
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrIntegerAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), Integer.valueOf(101));
    }

    @Test
    public void testBooleanAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrBooleanAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(true)
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrBooleanAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertTrue(evaluator.get(1));
    }

    @Test
    public void testDateAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrDateAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrDateAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), date);
    }

    @Test
    public void testTimeAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrTimeAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrTimeAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), date.toLocalTime());
    }

    @Test
    public void testDateTimeAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrDateTimeAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrDateTimeAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), date);
    }

    @Test
    public void testCreatedAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrCreatedAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrCreatedAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), date);
    }

    @Test
    public void testModifiedAttribute()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrModifiedAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        final DateTime date = new DateTime();
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                        .append(date.toDate())
                        .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s]",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrModifiedAttribute.getName()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), date);
    }

    @Test
    public void testLinkto()
        throws EFapsException
    {
        final String sql = String.format("select T1.%s,T1.ID from %s T0 "
                        + "left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.TestAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());
        final String strValue = RandomUtil.random(8);
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                    .append(strValue, 1L)
                    .asResult())
            .build();

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select linkto[%s].attribute[%s]",
                    Mocks.AllAttrType.getId(), Mocks.AllAttrLinkAttribute.getName(), Mocks.TestAttribute.getName()));

        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluator();
        assertEquals(evaluator.get(1), strValue);
    }

    @Test(description = "read a value using an Alias")
    public void testAttributeWithAliase()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s from %s T0 where T0.ID = 4",
                        Mocks.AllAttrStringAttribute.getSQLColumnName(),
                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(String.class)
                        .append("A Value")
                        .asResult())
            .build();
        final String alias = "AliasName";

        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select attribute[%s] as %s",
                        Mocks.AllAttrType.getId(), Mocks.AllAttrStringAttribute.getName(), alias));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(alias), "A Value");
    }

    @Test
    public void testInstanceSimpleType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID from %s T0 where T0.ID = 4",
                                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(4)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select instance",
                        Mocks.AllAttrType.getId()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), instance);
    }

    @Test
    public void testInstanceTypedType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                                        Mocks.TypedType.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                    .append(4, Mocks.TypedType.getId())
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.TypedType.getName(), "4");
        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select instance",
                        Mocks.TypedType.getId()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), instance);
    }

    @Test
    public void testLinktoInstanceSimpleType()
        throws EFapsException
    {
        final String sql = String.format("select T1.ID from %s T0 left join %s T1 on T0.%s=T1.ID where T0.ID = 4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttribute.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(4)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.SimpleType.getName(), "4");
        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select linkto[%s].instance",
                    Mocks.AllAttrType.getId(), Mocks.AllAttrLinkAttribute.getName()));

        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluator();
        assertEquals(evaluator.get(1), instance);
    }

    @Test
    public void testLinktoInstanceTypedType()
        throws EFapsException
    {
        final String sql = String.format("select T1.ID,T1.TYPE from %s T0 left join %s T1 on T0.%s=T1.ID "
                        + "where T0.ID = 4",
                        Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                        Mocks.AllAttrLinkAttributeTyped.getSQLColumnName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                    .append(4, Mocks.TypedType.getId())
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.TypedType.getName(), "4");
        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select linkto[%s].instance",
                    Mocks.AllAttrType.getId(), Mocks.AllAttrLinkAttributeTyped.getName()));

        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
               .execute()
               .evaluator();
        assertEquals(evaluator.get(1), instance);
    }

    @Test
    public void testOIDSimpleType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID from %s T0 where T0.ID = 4",
                                        Mocks.AllAttrTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Object.class)
                    .append(4)
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.AllAttrType.getName(), "4");
        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select oid",
                        Mocks.AllAttrType.getId()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), instance.getOid());
    }

    @Test
    public void testOIDTypedType()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID,T0.TYPE from %s T0 where T0.ID = 4",
                                        Mocks.TypedTypeSQLTable.getSqlTableName(),
                                        Mocks.TypedType.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(Object.class, Object.class)
                    .append(4, Mocks.TypedType.getId())
                    .asResult())
            .build();

        final Instance instance = Instance.get(Mocks.TypedType.getName(), "4");
        final IParseResult result = this.parser.doParse(String.format("print obj %s.4 select oid",
                        Mocks.TypedType.getId()));
        final IPrintObjectStatement stmt = (IPrintObjectStatement) result.getRootASTElement();
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluator();
        assertEquals(evaluator.get(1), instance.getOid());
    }
}
