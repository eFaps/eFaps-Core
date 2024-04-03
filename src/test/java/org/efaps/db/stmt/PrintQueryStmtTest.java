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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.EQL2;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.mock.MockResult;
import org.efaps.mock.Mocks;
import org.efaps.test.AbstractTest;
import org.efaps.test.SQLVerify;
import org.efaps.util.EFapsException;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

public class PrintQueryStmtTest
    extends AbstractTest
{
    @Test
    public void testSimpleType()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0",
                        Mocks.TestAttribute.getSQLColumnName(), Mocks.SimpleTypeSQLTable.getSqlTableName());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                            .append("Val1", 6L)
                            .append("Val2", 8L)
                            .append("Val3", 11L)
                            .asResult())
            .build();

        final String stmtStr = String.format("print query type %s select attribute[%s]",
                        Mocks.SimpleType.getName(), Mocks.TestAttribute.getName());
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(stmtStr);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final Evaluator evaluator = printStmt.evaluate();
        assertTrue(evaluator.next());
        assertEquals(evaluator.get(1), "Val1");
        assertTrue(evaluator.next());
        assertEquals(evaluator.get(1), "Val2");
        assertTrue(evaluator.next());
        assertEquals(evaluator.get(1), "Val3");
        assertFalse(evaluator.next());
    }

    @Test
    public void testAbstractType()
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 "
                        + "where T0.TYPE in (%s,%s)",
                        Mocks.AbstractTypeStringAttribute.getSQLColumnName(),
                        Mocks.AbstractTypeSQLTable.getSqlTableName(),
                        Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                            ? Mocks.ChildType1.getId() : Mocks.ChildType2.getId() ,
                        Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                            ? Mocks.ChildType2.getId() : Mocks.ChildType1.getId());

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList3(String.class, Long.class, Long.class)
                            .append("Val1", 6L, Mocks.ChildType1.getId())
                            .append("Val2", 8L, Mocks.ChildType2.getId())
                            .append("Val3", 11L, Mocks.ChildType1.getId())
                            .asResult())
            .build();

        final String stmtStr = String.format("print query type %s select attribute[%s]",
                        Mocks.AbstractType.getName(), Mocks.AbstractTypeStringAttribute.getName());
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(stmtStr);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final Evaluator evaluator = printStmt.evaluate();
        assertTrue(evaluator.next());
        assertEquals(evaluator.get(1), "Val1");
        assertTrue(evaluator.next());
        assertEquals(evaluator.get(1), "Val2");
        assertTrue(evaluator.next());
        assertEquals(evaluator.get(1), "Val3");
        assertFalse(evaluator.next());
    }

    @Test(dataProvider = "status")
    public void testTypeWithStatusFilterWithKey(final String _statusFilter)
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s = %s",
                        Mocks.StatusStringAttribute.getSQLColumnName(),
                        Mocks.StatusTypeSQLTable.getSqlTableName(),
                        Mocks.StatusAttribute.getSQLColumnName(),
                        Mocks.StatusGrp.getStatusId("Open"));

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                            .append("Val1", 6L)
                            .append("Val2", 8L)
                            .append("Val3", 11L)
                            .asResult())
            .build();

        final String stmtStr = String.format("print query type %s where %s = \"Open\" select attribute[%s]",
                        Mocks.StatusType.getName(), _statusFilter, Mocks.StatusStringAttribute.getName());

        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(stmtStr);

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertTrue(evaluator.next());
    }

    @Test(dataProvider = "status")
    public void testTypeWithStatusFilterWithKeys(final String _statusFilter)
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s in (%s,%s)",
                        Mocks.StatusStringAttribute.getSQLColumnName(),
                        Mocks.StatusTypeSQLTable.getSqlTableName(),
                        Mocks.StatusAttribute.getSQLColumnName(),
                        Mocks.StatusGrp.getStatusId("Open"),
                        Mocks.StatusGrp.getStatusId("Closed"));

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                            .append("Val1", 6L)
                            .append("Val2", 8L)
                            .append("Val3", 11L)
                            .asResult())
            .build();

        final String stmtStr = String.format("print query type %s where %s in (\"Open\",\"Closed\") "
                        + "select attribute[%s]",
                        Mocks.StatusType.getName(), _statusFilter, Mocks.StatusStringAttribute.getName());

        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(stmtStr);

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertTrue(evaluator.next());
    }

    @Test(dataProvider = "status")
    public void testTypeWithStatusFilterWithId(final String _statusFilter)
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s = %s",
                        Mocks.StatusStringAttribute.getSQLColumnName(),
                        Mocks.StatusTypeSQLTable.getSqlTableName(),
                        Mocks.StatusAttribute.getSQLColumnName(),
                        Mocks.StatusGrp.getStatusId("Open"));

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                            .append("Val1", 6L)
                            .append("Val2", 8L)
                            .append("Val3", 11L)
                            .asResult())
            .build();

        final String stmtStr = String.format("print query type %s where %s = %s select attribute[%s]",
                        Mocks.StatusType.getName(),
                        _statusFilter,
                        Mocks.StatusGrp.getStatusId("Open"),
                        Mocks.StatusStringAttribute.getName());

        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(stmtStr);

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertTrue(evaluator.next());
    }

    @Test(dataProvider = "status")
    public void testTypeWithStatusFilterWithIds(final String _statusFilter)
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s in (%s,%s)",
                        Mocks.StatusStringAttribute.getSQLColumnName(),
                        Mocks.StatusTypeSQLTable.getSqlTableName(),
                        Mocks.StatusAttribute.getSQLColumnName(),
                        Mocks.StatusGrp.getStatusId("Open"),
                        Mocks.StatusGrp.getStatusId("Closed"));

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                            .append("Val1", 6L)
                            .append("Val2", 8L)
                            .append("Val3", 11L)
                            .asResult())
            .build();

        final String stmtStr = String.format("print query type %s where %s in (%s,%s) select attribute[%s]",
                        Mocks.StatusType.getName(),
                        _statusFilter,
                        Mocks.StatusGrp.getStatusId("Open"),
                        Mocks.StatusGrp.getStatusId("Closed"),
                        Mocks.StatusStringAttribute.getName());

        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(stmtStr);

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertTrue(evaluator.next());
    }

    @Test(dataProvider = "status")
    public void testTypeWithStatusFilterMixed(final String _statusFilter)
        throws EFapsException
    {
        final String sql = String.format("select T0.%s,T0.ID from %s T0 where T0.%s in (%s,%s)",
                        Mocks.StatusStringAttribute.getSQLColumnName(),
                        Mocks.StatusTypeSQLTable.getSqlTableName(),
                        Mocks.StatusAttribute.getSQLColumnName(),
                        Mocks.StatusGrp.getStatusId("Open"),
                        Mocks.StatusGrp.getStatusId("Closed"));

        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList2(String.class, Long.class)
                            .append("Val1", 6L)
                            .append("Val2", 8L)
                            .append("Val3", 11L)
                            .asResult())
            .build();

        final String stmtStr = String.format("print query type %s where %s in (\"Open\",%s) select attribute[%s]",
                        Mocks.StatusType.getName(),
                        _statusFilter,
                        Mocks.StatusGrp.getStatusId("Closed"),
                        Mocks.StatusStringAttribute.getName());
System.out.println(stmtStr);
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(stmtStr);

        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertTrue(evaluator.next());
    }

    @Test(description = "Exec select without any other selects")
    public void testSelectExec()
        throws EFapsException
    {
        final String sql = String.format("select T0.ID from %s T0",
                        Mocks.SimpleTypeSQLTable.getSqlTableName());
        MockResult.builder()
            .withSql(sql)
            .withResult(RowLists.rowList1(Long.class)
                .append(4L)
                .append(8L)
                .asResult())
            .build();

        final String stmtStr = String.format("print query type %s select exec %s as barcode",
                        Mocks.SimpleType.getName(), org.efaps.mock.esjp.SimpleSelect.class.getName());
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(stmtStr);
        final Evaluator evaluator = PrintStmt.get(stmt)
                        .execute()
                        .evaluate();
        assertTrue(evaluator.next());
        assertEquals(evaluator.get("barcode"),
                        String.format(org.efaps.mock.esjp.SimpleSelect.FORMAT,
                                        Instance.get(Mocks.SimpleType.getName(), "4").getOid()));
        assertTrue(evaluator.next());
        assertEquals(evaluator.get("barcode"),
                        String.format(org.efaps.mock.esjp.SimpleSelect.FORMAT,
                                        Instance.get(Mocks.SimpleType.getName(), "8").getOid()));
        assertFalse(evaluator.next());
    }

    @Test(description = "Test for different single where", dataProvider = "SingleWhereDataProvider")
    public void testSingleWheres(final String _stmt, final String _sql)
        throws EFapsException
    {
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(_stmt);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder().withSql(_sql).build();
        printStmt.execute();
        verify.verify();
    }

    @Test(description = "Test for different two where", dataProvider = "TwoWhereDataProvider")
    public void testTwoWheres(final String _stmt, final String _sql)
        throws EFapsException
    {
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(_stmt);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder().withSql(_sql).build();
        printStmt.execute();
        verify.verify();
    }

    @Test(description = "Test specific stmts", dataProvider = "SpecificDataProvider")
    public void testSpecific(final String _stmt, final String _sql)
        throws EFapsException
    {
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(_stmt);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder().withSql(_sql).build();
        printStmt.execute();
        verify.verify();
    }

    @Test(description = "Test for order by", dataProvider = "orderByDataProvider")
    public void testOrderBy(final String _stmt, final String _sql)
        throws EFapsException
    {
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(_stmt);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder().withSql(_sql).build();
        printStmt.execute();
        verify.verify();
    }

    @Test(description = "Test for limit", dataProvider = "limitDataProvider")
    public void testLimit(final String _stmt, final String _sql)
        throws EFapsException
    {
        final IPrintQueryStatement stmt = (IPrintQueryStatement) EQL2.parse(_stmt);
        final PrintStmt printStmt = PrintStmt.get(stmt);
        final SQLVerify verify = SQLVerify.builder().withSql(_sql).build();
        printStmt.execute();
        verify.verify();
    }

    @DataProvider(name = "SpecificDataProvider")
    public static Iterator<Object[]> specificDataProvider(final ITestContext _context)
    {
        final List<Object[]> ret = new ArrayList<>();

        ret.add(new Object[] {
                        String.format("""
                            print query type %s\s\s\
                            where attribute[%s] like "F988" or attribute[%s] like "F999" \
                            select attribute[%s]""",
                                        Mocks.TypedType.getName(), Mocks.TypedTypeTestAttr.getName(),
                                        Mocks.TypedTypeTestAttr.getName(), Mocks.TypedTypeTestAttr.getName()),
                        String.format("""
                            select T0.%s,T0.ID,T0.TYPE\s\
                            from %s T0\s\
                            where (T0.%s like 'F988' or T0.%s like 'F999') and T0.TYPE = %s""",
                                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                                        Mocks.AbstractTypeSQLTable.getSqlTableName(),
                                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                                        Mocks.TypedTypeTestAttr.getSQLColumnName(),
                                        Mocks.TypedType.getId())
        });
        ret.add(new Object[] {
                        String.format("""
                            print query type %s\s\s\
                            where attribute[%s] like "F988" or attribute[%s] like "F999" \
                            select attribute[%s]""",
                                        Mocks.SimpleType.getName(), Mocks.TestAttribute.getName(),
                                        Mocks.TestAttribute.getName(), Mocks.TestAttribute.getName()),
                        String.format("""
                            select T0.%s,T0.ID\s\
                            from %s T0\s\
                            where T0.%s like 'F988' or T0.%s like 'F999'""",
                                        Mocks.TestAttribute.getSQLColumnName(),
                                        Mocks.SimpleTypeSQLTable.getSqlTableName(),
                                        Mocks.TestAttribute.getSQLColumnName(),
                                        Mocks.TestAttribute.getSQLColumnName())
        });
        return ret.iterator();
    }

    @DataProvider(name = "TwoWhereDataProvider")
    public static Iterator<Object[]> dataProvider2(final ITestContext _context)
    {
        final List<Object[]> ret = new ArrayList<>();
        final Iterator<String> sqlIter = getSQLParts2().iterator();
        for (final String element : getStmtParts2()) {
            ret.add(new Object[] { element, sqlIter.next() });
        }
        return ret.iterator();
    }

    public static List<String> getStmtParts2() {

        final List<String> ret = new ArrayList<>();
        for (final String val : getStmtWheres(Mocks.TestAttribute.getName())) {
            ret.add(String.format("print query type %s where %s and %s select attribute[%s]",
                            Mocks.SimpleType.getName(), val, val, Mocks.TestAttribute.getName()));
        }
        for (final String val : getStmtWheres(Mocks.TypedTypeTestAttr.getName())) {
            ret.add(String.format("print query type %s where %s and %s select attribute[%s]",
                            Mocks.TypedType.getName(), val, val, Mocks.TypedTypeTestAttr.getName()));
        }
        for (final String val : getStmtWheres(Mocks.AbstractTypeStringAttribute.getName())) {
            ret.add(String.format("print query type %s where %s and %s select attribute[%s]",
                            Mocks.AbstractType.getName(), val, val, Mocks.AbstractTypeStringAttribute.getName()));
        }
        return ret;
    }

    public static List<String> getSQLParts2() {
        final List<String> ret = new ArrayList<>();
        for (final String val : getSQLWheres(Mocks.TestAttribute.getSQLColumnName())) {
            ret.add(String.format("select T0.%s,T0.ID from %s T0 where T0.%s and T0.%s",
                        Mocks.TestAttribute.getSQLColumnName(), Mocks.SimpleTypeSQLTable.getSqlTableName(), val, val));
        }
        for (final String val : getSQLWheres(Mocks.TypedTypeTestAttr.getSQLColumnName())) {
            ret.add(String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.%s and T0.%s and T0.TYPE = %s",
                            Mocks.TypedTypeTestAttr.getSQLColumnName(), Mocks.TypedTypeSQLTable.getSqlTableName(),
                            val, val, Mocks.TypedType.getId()));
        }
        for (final String val : getSQLWheres(Mocks.AbstractTypeStringAttribute.getSQLColumnName())) {
            ret.add(String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where "
                            + "T0.%s and T0.%s and T0.TYPE in (%s,%s)",
                            Mocks.AbstractTypeStringAttribute.getSQLColumnName(),
                            Mocks.AbstractTypeSQLTable.getSqlTableName(),
                            val, val,
                            Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                                ? Mocks.ChildType1.getId() : Mocks.ChildType2.getId(),
                            Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                                ? Mocks.ChildType2.getId() : Mocks.ChildType1.getId()));
        }
        return ret;
    }

    @DataProvider(name = "SingleWhereDataProvider")
    public static Iterator<Object[]> dataProvider1(final ITestContext _context)
    {
        final List<Object[]> ret = new ArrayList<>();
        final Iterator<String> sqlIter = getSQLParts1().iterator();
        for (final String element : getStmtParts1()) {
            ret.add(new Object[] { element, sqlIter.next() });
        }
        return ret.iterator();
    }

    public static List<String> getSQLParts1() {
        final List<String> ret = new ArrayList<>();
        for (final String element : getSQLWheres(Mocks.TestAttribute.getSQLColumnName())) {
            ret.add(String.format("select T0.%s,T0.ID from %s T0 where T0.%s", Mocks.TestAttribute.getSQLColumnName(),
                            Mocks.SimpleTypeSQLTable.getSqlTableName(), element));
        }
        for (final String element : getSQLWheres(Mocks.TypedTypeTestAttr.getSQLColumnName())) {
            ret.add(String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.%s and T0.TYPE = %s",
                            Mocks.TypedTypeTestAttr.getSQLColumnName(), Mocks.TypedTypeSQLTable.getSqlTableName(),
                            element, Mocks.TypedType.getId()));
        }
        for (final String element : getSQLWheres(Mocks.AbstractTypeStringAttribute.getSQLColumnName())) {
            ret.add(String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.%s and T0.TYPE in (%s,%s)",
                            Mocks.AbstractTypeStringAttribute.getSQLColumnName(),
                            Mocks.AbstractTypeSQLTable.getSqlTableName(),
                            element,
                            Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                                ? Mocks.ChildType1.getId() : Mocks.ChildType2.getId(),
                            Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                                ? Mocks.ChildType2.getId() : Mocks.ChildType1.getId()));
        }
        return ret;
    }

    public static List<String> getStmtParts1() {

        final List<String> ret = new ArrayList<>();
        for (final String element : getStmtWheres(Mocks.TestAttribute.getName())) {
            ret.add(String.format("print query type %s where %s select attribute[%s]",
                            Mocks.SimpleType.getName(), element, Mocks.TestAttribute.getName()));
        }
        for (final String element : getStmtWheres(Mocks.TypedTypeTestAttr.getName())) {
            ret.add(String.format("print query type %s where %s select attribute[%s]",
                            Mocks.TypedType.getName(), element, Mocks.TypedTypeTestAttr.getName()));
        }
        for (final String element : getStmtWheres(Mocks.AbstractTypeStringAttribute.getName())) {
            ret.add(String.format("print query type %s where %s select attribute[%s]",
                            Mocks.AbstractType.getName(), element, Mocks.AbstractTypeStringAttribute.getName()));
        }
        return ret;
    }

    public static List<String> getSQLWheres(final String _colName) {
        final List<String> ret = new ArrayList<>();
        ret.add(String.format("%s = 'ABC'", _colName));
        ret.add(String.format("%s = 'ABC'", _colName));
        ret.add(String.format("%s < 'ABC'", _colName));
        ret.add(String.format("%s <= 'ABC'", _colName));
        ret.add(String.format("%s > 'ABC'", _colName));
        ret.add(String.format("%s >= 'ABC'", _colName));
        ret.add(String.format("%s != 'ABC'", _colName));
        ret.add(String.format("%s like 'ABC'", _colName));
        ret.add(String.format("%s in ('ABC','DEF')", _colName));
        ret.add(String.format("%s not in ('ABC','DEF')", _colName));
        return ret;
    }

    public static List<String> getStmtWheres(final String _attributeName) {
        final List<String> ret = new ArrayList<>();
        ret.add(String.format("%s == 'ABC'", _attributeName));
        ret.add(String.format("%s eq 'ABC'", _attributeName));
        ret.add(String.format("%s < 'ABC'", _attributeName));
        ret.add(String.format("%s <= 'ABC'", _attributeName));
        ret.add(String.format("%s > 'ABC'", _attributeName));
        ret.add(String.format("%s >= 'ABC'", _attributeName));
        ret.add(String.format("%s != 'ABC'", _attributeName));
        ret.add(String.format("%s like 'ABC'", _attributeName));
        ret.add(String.format("%s in ('ABC', 'DEF')", _attributeName));
        ret.add(String.format("%s not in ('ABC', 'DEF')", _attributeName));
        return ret;
    }

    @DataProvider(name = "status")
    static public Object[][] statusDataProvider() {
        return new Object[][] {
            new Object[] { "status" },
            new Object[] { Mocks.StatusAttribute.getName() },
            new Object[] { "attribute["+  Mocks.StatusAttribute.getName() + "]" },
        };
    }

    @DataProvider
    public static Iterator<Object[]> orderByDataProvider()
    {
        final List<Object[]> ret = new ArrayList<>();
        ret.addAll(orderByData1());
        ret.addAll(orderByData2());
        return ret.iterator();
    }

    public static List<Object[]> orderByData1()
    {
        final List<String> stmts = new ArrayList<>();
        final List<String> sqls = new ArrayList<>();
        final List<String[]> orderbys = new ArrayList<>();
        orderbys.add(new String[]{"1", ""});
        orderbys.add(new String[]{"SomeKey", ""});
        orderbys.add(new String[]{"1 asc", ""});
        orderbys.add(new String[]{"SomeKey asc", ""});
        orderbys.add(new String[]{"1 desc", "desc"});
        orderbys.add(new String[]{"SomeKey desc", "desc"});

        for (final String[] values : orderbys) {
            stmts.add(String.format("print query type %s select attribute[%s] as SomeKey order by %s",
                            Mocks.SimpleType.getName(), Mocks.TestAttribute.getName(), values[0]));
            sqls.add(String.format("select T0.%s,T0.ID from %s T0  order by T0.%s %s", Mocks.TestAttribute.getSQLColumnName(),
                            Mocks.SimpleTypeSQLTable.getSqlTableName(), Mocks.TestAttribute.getSQLColumnName(), values[1]).trim());
        }
        for (final String[] values : orderbys) {
            stmts.add(String.format("print query type %s select attribute[%s] as SomeKey order by %s",
                            Mocks.TypedType.getName(), Mocks.TypedTypeTestAttr.getName(), values[0]));
            sqls.add(String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.TYPE = %s order by T0.%s %s",
                            Mocks.TypedTypeTestAttr.getSQLColumnName(), Mocks.TypedTypeSQLTable.getSqlTableName(),
                            Mocks.TypedType.getId(), Mocks.TypedTypeTestAttr.getSQLColumnName(), values[1]).trim());
        }
        for (final String[] values : orderbys) {
            stmts.add(String.format("print query type %s select attribute[%s] as SomeKey order by %s",
                            Mocks.AbstractType.getName(), Mocks.AbstractTypeStringAttribute.getName(), values[0]));
            sqls.add(String.format("select T0.%s,T0.ID,T0.TYPE from %s T0 where T0.TYPE in (%s,%s) order by T0.%s %s",
                            Mocks.AbstractTypeStringAttribute.getSQLColumnName(),
                            Mocks.AbstractTypeSQLTable.getSqlTableName(),
                            Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                                ? Mocks.ChildType1.getId() : Mocks.ChildType2.getId(),
                            Mocks.ChildType1.getId() < Mocks.ChildType2.getId()
                                ? Mocks.ChildType2.getId() : Mocks.ChildType1.getId(),
                            Mocks.AbstractTypeStringAttribute.getSQLColumnName(), values[1]).trim());
        }

        final List<Object[]> ret = new ArrayList<>();
        final Iterator<String> sqlIter = sqls.iterator();
        for (final String stmt : stmts) {
            ret.add(new Object[] { stmt, sqlIter.next() });
        }
        return ret;
    }

    public static List<Object[]> orderByData2()
    {
        final List<String> stmts = new ArrayList<>();
        final List<String> sqls = new ArrayList<>();
        final List<String[]> orderbys = new ArrayList<>();
        orderbys.add(new String[]{"1", "T0." + Mocks.AllAttrDateAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"Key1", "T0." + Mocks.AllAttrDateAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"2", "T0." + Mocks.AllAttrDecimalAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"Key2", "T0." + Mocks.AllAttrDecimalAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"3", "T0." + Mocks.AllAttrIntegerAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"Key3", "T0." + Mocks.AllAttrIntegerAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"4", "T0." + Mocks.AllAttrLongAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"Key4", "T0." + Mocks.AllAttrLongAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"5", "T0." + Mocks.AllAttrStringAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"Key5", "T0." + Mocks.AllAttrStringAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"1, 2", "T0." + Mocks.AllAttrDateAttribute.getSQLColumnName()
                + ", T0." + Mocks.AllAttrDecimalAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"Key1, Key2", "T0." + Mocks.AllAttrDateAttribute.getSQLColumnName()
                + ", T0." + Mocks.AllAttrDecimalAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"2,3,5", "T0." + Mocks.AllAttrDecimalAttribute.getSQLColumnName()
                + ", T0." + Mocks.AllAttrIntegerAttribute.getSQLColumnName() + ", T0." + Mocks.AllAttrStringAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"Key2,Key3,Key5", "T0." + Mocks.AllAttrDecimalAttribute.getSQLColumnName()
                + ", T0." + Mocks.AllAttrIntegerAttribute.getSQLColumnName() + ", T0." + Mocks.AllAttrStringAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"4,1,3", "T0." + Mocks.AllAttrLongAttribute.getSQLColumnName()
                + ", T0." + Mocks.AllAttrDateAttribute.getSQLColumnName() + ", T0." + Mocks.AllAttrIntegerAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"Key4,Key1,Key3", "T0." + Mocks.AllAttrLongAttribute.getSQLColumnName()
                + ", T0." + Mocks.AllAttrDateAttribute.getSQLColumnName() + ", T0." + Mocks.AllAttrIntegerAttribute.getSQLColumnName()});
        orderbys.add(new String[]{"4 desc,1 asc,3 desc", "T0." + Mocks.AllAttrLongAttribute.getSQLColumnName()
                + " desc, T0." + Mocks.AllAttrDateAttribute.getSQLColumnName()
                + ", T0." + Mocks.AllAttrIntegerAttribute.getSQLColumnName() + " desc"});
        orderbys.add(new String[]{"Key4 desc,Key1 asc,Key3 desc", "T0." + Mocks.AllAttrLongAttribute.getSQLColumnName()
                + " desc, T0." + Mocks.AllAttrDateAttribute.getSQLColumnName()
                + ", T0." + Mocks.AllAttrIntegerAttribute.getSQLColumnName() + " desc"});

        for (final String[] values : orderbys) {
            stmts.add(String.format("""
                print query type %s select attribute[%s] as Key1, attribute[%s] as Key2, attribute[%s] as Key3,\s\
                attribute[%s] as Key4, attribute[%s] as Key5\s\
                order by %s""",
                            Mocks.AllAttrType.getName(), Mocks.AllAttrDateAttribute.getName(),
                            Mocks.AllAttrDecimalAttribute.getName(), Mocks.AllAttrIntegerAttribute.getName(),
                            Mocks.AllAttrLongAttribute.getName(), Mocks.AllAttrStringAttribute.getName(),
                            values[0]));

            sqls.add(String.format("select T0.%s,T0.%s,T0.%s,T0.%s,T0.%s,T0.ID from %s T0  order by %s",
                            Mocks.AllAttrDateAttribute.getSQLColumnName(),
                            Mocks.AllAttrDecimalAttribute.getSQLColumnName(),
                            Mocks.AllAttrIntegerAttribute.getSQLColumnName(),
                            Mocks.AllAttrLongAttribute.getSQLColumnName(),
                            Mocks.AllAttrStringAttribute.getSQLColumnName(),
                            Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                            values[1]).trim());
        }

        final List<Object[]> ret = new ArrayList<>();
        final Iterator<String> sqlIter = sqls.iterator();
        for (final String stmt : stmts) {
            ret.add(new Object[] { stmt, sqlIter.next() });
        }
        return ret;
    }

    @DataProvider
    public static Iterator<Object[]> limitDataProvider()
    {
        final List<Object[]> ret = new ArrayList<>();
        ret.addAll(limitData1());
        return ret.iterator();
    }

    public static List<Object[]> limitData1()
    {
        final List<String> stmts = new ArrayList<>();
        final List<String> sqls = new ArrayList<>();
/**
        stmts.add(String.format("print query type %s limit 100 offset 200 select attribute[%s] ",
                            Mocks.SimpleType.getName(), Mocks.TestAttribute.getName()));
        sqls.add(String.format("select T0.%s,T0.ID from %s T0  limit 100 offset 200",
                        Mocks.TestAttribute.getSQLColumnName(), Mocks.SimpleTypeSQLTable.getSqlTableName()));

        stmts.add(String.format("print query type %s limit 100 offset 200 select attribute[%s], linkfrom[%s#%s].oid",
                        Mocks.SimpleType.getName(), Mocks.TestAttribute.getName(), Mocks.RelationType.getName(),
                        Mocks.RealtionFromLinkAttribute.getName()));
        sqls.add(String.format("""
            select T0.%s,T1.ID,T0.ID from %s T0 left join %s T1 on T0.ID=T1.%s  \
            where T0.ID in ( \
            select L0.ID from  %s L0  limit 100 offset 200\
            )""",
                    Mocks.TestAttribute.getSQLColumnName(),
                    Mocks.SimpleTypeSQLTable.getSqlTableName(),
                    Mocks.RelationTypeSQLTable.getSqlTableName(),
                    Mocks.RealtionFromLinkAttribute.getSQLColumnName(),
                    Mocks.SimpleTypeSQLTable.getSqlTableName()));


        stmts.add(String.format("print query type %s limit 100 offset 200 select attribute[%s], linkfrom[%s#%s].oid order by 1",
                        Mocks.SimpleType.getName(), Mocks.TestAttribute.getName(), Mocks.RelationType.getName(),
                        Mocks.RealtionFromLinkAttribute.getName()));
        sqls.add(String.format("""
            select T0.%s,T1.ID,T0.ID \
            from %s T0 \
            left join %s T1 on T0.ID=T1.%s  \
            where T0.ID in ( \
            select L0.ID from  %s L0  order by L0.%s limit 100 offset 200\
            ) \
            order by T0.%s""",
                    Mocks.TestAttribute.getSQLColumnName(),
                    Mocks.SimpleTypeSQLTable.getSqlTableName(),
                    Mocks.RelationTypeSQLTable.getSqlTableName(),
                    Mocks.RealtionFromLinkAttribute.getSQLColumnName(),
                    Mocks.SimpleTypeSQLTable.getSqlTableName(),
                    Mocks.TestAttribute.getSQLColumnName(),
                    Mocks.TestAttribute.getSQLColumnName()));

        stmts.add(String.format("print query type %s where attribute[%s] like \"F988\"  "
                        + "limit 100 offset 200 select attribute[%s], linkfrom[%s#%s].oid order by 1",
                        Mocks.SimpleType.getName(),Mocks.TestAttribute.getName(), Mocks.TestAttribute.getName(),
                        Mocks.RelationType.getName(), Mocks.RealtionFromLinkAttribute.getName()));
        sqls.add(String.format("""
            select T0.%1$s,T1.ID,T0.ID \
            from %2$s T0 \
            left join %3$s T1 on T0.ID=T1.%4$s \
            where T0.%1$s like 'F988' and T0.ID in ( \
            select L0.ID from  %2$s L0 where L0.%1$s like 'F988' order by L0.%1$s limit 100 offset 200\
            ) \
            order by T0.%1$s""",
                    Mocks.TestAttribute.getSQLColumnName(),
                    Mocks.SimpleTypeSQLTable.getSqlTableName(),
                    Mocks.RelationTypeSQLTable.getSqlTableName(),
                    Mocks.RealtionFromLinkAttribute.getSQLColumnName()));
**/

        stmts.add(String.format("""
            print query type %1$s where attribute[%2$s] like "F988" \
            and ( attribute[%3$s] in ( query type %4$s ) or attribute[%3$s] in ( 44,55 )) \
            limit 100 offset 200 select attribute[%2$s], linkfrom[%5$s#%6$s].oid order by 1""",
                        Mocks.AllAttrType.getName(),
                        Mocks.AllAttrStringAttribute.getName(),
                        Mocks.AllAttrLongAttribute.getName(),
                        Mocks.SimpleType.getName(),
                        Mocks.RelationType.getName(),
                        Mocks.RealtionFromLinkAttribute.getName()));
        sqls.add(String.format("""
            select T0.%1$s,T1.ID,T0.ID \
            from %2$s T0 left join %3$s T1 on T0.ID=T1.%4$s \
            where T0.%1$s like 'F988' and (T0.%5$s in ( select N0.ID from T_DEMO N0 ) or T0.%5$s in (44,55)) \
            and T0.ID in ( select L0.ID from  %2$s L0 where L0.%1$s like 'F988' and (L0.%5$s in ( select N0.ID from T_DEMO N0 ) or L0.%5$s in (44,55)) order by L0.%1$s limit 100 offset 200) \
            order by T0.%1$s""",
                    Mocks.AllAttrStringAttribute.getSQLColumnName(),
                    Mocks.AllAttrTypeSQLTable.getSqlTableName(),
                    Mocks.RelationTypeSQLTable.getSqlTableName(),
                    Mocks.RealtionFromLinkAttribute.getSQLColumnName(),
                    Mocks.AllAttrLongAttribute.getSQLColumnName()));

        final List<Object[]> ret = new ArrayList<>();
        final Iterator<String> sqlIter = sqls.iterator();
        for (final String stmt : stmts) {
            ret.add(new Object[] { stmt, sqlIter.next() });
        }
        return ret;
    }
}
