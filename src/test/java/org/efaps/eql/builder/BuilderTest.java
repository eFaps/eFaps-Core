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
package org.efaps.eql.builder;

import static org.testng.Assert.assertEquals;

import org.efaps.db.Instance;
import org.efaps.eql.EQL;
import org.efaps.mock.datamodel.CI;
import org.efaps.test.AbstractTest;
import org.testng.annotations.Test;

public class BuilderTest
    extends AbstractTest
{

    @Test
    public void testPrintQueryString()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.SimpleType)
            .where()
            .attribute(CI.SimpleType.TestAttr).eq("Hallo World")
            .select()
            .attribute(CI.SimpleType.TestAttr)
            .stmt()
            .asString();
        assertEquals(stmt, "print query type " + CI.SimpleType.uuid
                        + " where attribute[TestAttribute] == \"Hallo World\""
                        + " select attribute[TestAttribute] as \"CIALIAS_"+ CI.SimpleType.getType().getId() +"_TestAttribute\"");
    }

    @Test
    public void testPrintQueryInstanc()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.LongAttribute).eq(Instance.get(CI.SimpleType.uuid, 56))
            .select()
            .attribute(CI.AllAttrType.StringAttribute)
            .stmt()
            .asString();
        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrLongAttribute] == 56"
                        + " select attribute[AllAttrStringAttribute] as \"CIALIAS_"+ CI.AllAttrType.getType().getId() +"_AllAttrStringAttribute\"");
    }

    @Test
    public void testPrintQueryNumber()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute)
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " select attribute[AllAttrIntegerAttribute] as \"CIALIAS_"+ CI.AllAttrType.getType().getId() +"_AllAttrIntegerAttribute\"");
    }

    @Test
    public void testPrintQueryVarious()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .and()
            .attribute(CI.AllAttrType.StringAttribute).eq("Hello world")
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute)
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " and attribute[AllAttrStringAttribute] == \"Hello world\""
                        + " select attribute[AllAttrIntegerAttribute] as \"CIALIAS_"+ CI.AllAttrType.getType().getId() +"_AllAttrIntegerAttribute\"");
    }

    @Test
    public void testPrintQueryVariousWithSelect()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .and()
            .attribute(CI.AllAttrType.StringAttribute).eq("Hello world")
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute)
            .attribute(CI.AllAttrType.StringAttribute)
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " and attribute[AllAttrStringAttribute] == \"Hello world\""
                        + " select attribute[AllAttrIntegerAttribute] as \"CIALIAS_"+ CI.AllAttrType.getType().getId() +"_AllAttrIntegerAttribute\","
                        + " attribute[AllAttrStringAttribute] as \"CIALIAS_"+ CI.AllAttrType.getType().getId() +"_AllAttrStringAttribute\"");
    }

    @Test
    public void testPrintQueryWithAlias()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute).as("This is the Alias")
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " select attribute[AllAttrIntegerAttribute] as \"This is the Alias\"");
    }

    @Test
    public void testPrintQueryWithLinkto()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .select()
            .linkto(CI.AllAttrType.LinkAttribute).attribute(CI.SimpleType.TestAttr).as("This is the Alias")
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " select linkto[AllAttrLinkAttribute].attribute[TestAttribute] as \"This is the Alias\"");
    }

    @Test
    public void testPrintQueryOrder()
    {
        final String stmt = EQL
            .builder().print()
            .query(CI.AllAttrType)
            .where()
            .attribute(CI.AllAttrType.IntegerAttribute).eq(1)
            .select()
            .attribute(CI.AllAttrType.IntegerAttribute)
            .orderBy(CI.AllAttrType.IntegerAttribute)
            .stmt()
            .asString();

        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " where attribute[AllAttrIntegerAttribute] == 1"
                        + " select attribute[AllAttrIntegerAttribute] as \"CIALIAS_"+ CI.AllAttrType.getType().getId() +"_AllAttrIntegerAttribute\""
                        + " order by CIALIAS_"+ CI.AllAttrType.getType().getId() +"_AllAttrIntegerAttribute asc");
    }

    @Test
    public void testPrintClass()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.SimpleType)
            .select()
            .clazz(CI.CompanyType).attribute(CI.CompanyType.StringAttribute)
            .stmt()
            .asString();
        assertEquals(stmt, "print query type " + CI.SimpleType.uuid
            + " select class[" + CI.CompanyType.uuid + "].attribute[StringAttribute] as \"CIALIAS_"+ CI.CompanyType.getType().getId() + "_StringAttribute\"");
    }

    @Test
    public void testPrintClassAfterAttribute()
    {
        final String stmt = EQL.builder()
            .print()
            .query(CI.SimpleType)
            .select()
            .attribute(CI.SimpleType.TestAttr)
            .clazz(CI.CompanyType).attribute(CI.CompanyType.StringAttribute)
            .stmt()
            .asString();
        assertEquals(stmt, "print query type " + CI.SimpleType.uuid
            + " select attribute[TestAttribute] as \"CIALIAS_"+ CI.SimpleType.getType().getId() +"_TestAttribute\","
            + " class[" + CI.CompanyType.uuid + "].attribute[StringAttribute] as \"CIALIAS_"+ CI.CompanyType.getType().getId() +"_StringAttribute\"");
    }

    @Test
    public void testElementAfterAttributeSet() {
        final String stmt = EQL.builder()
                        .print()
                        .query(CI.AllAttrType)
                        .select()
                        .attribute(CI.AllAttrType.DateAttribute)
                        .attributeSet(CI.AllAttrType.LinkAttribute).attribute(CI.CompanyType.StringAttribute)
                        .attributeSet(CI.AllAttrType.LinkAttribute).attribute(CI.CompanyType.StringAttribute).as("Alias1")
                        .attributeSet(CI.AllAttrType.LinkAttribute)
                            .linkto("AnAttribute").attribute(CI.SimpleType.TestAttr)
                        .attributeSet(CI.AllAttrType.LinkAttribute)
                            .linkto("AnAttribute").attribute(CI.SimpleType.TestAttr).as("Alias2")
                        .stmt()
                        .asString();
        assertEquals(stmt, "print query type " + CI.AllAttrType.uuid
                        + " select attribute[AllAttrDateAttribute] as \"CIALIAS_" + CI.AllAttrType.getType().getId() + "_AllAttrDateAttribute\","
                        + " attributeset[AllAttrLinkAttribute].attribute[StringAttribute] as \"CIALIAS_" + CI.CompanyType.getType().getId() + "_StringAttribute\","
                        + " attributeset[AllAttrLinkAttribute].attribute[StringAttribute] as \"Alias1\","
                        + " attributeset[AllAttrLinkAttribute].linkto[AnAttribute].attribute[TestAttribute] as \"CIALIAS_" + CI.SimpleType.getType().getId() + "_TestAttribute\","
                        + " attributeset[AllAttrLinkAttribute].linkto[AnAttribute].attribute[TestAttribute] as \"Alias2\"");
    }
}
