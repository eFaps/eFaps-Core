/*
 * Copyright 2003 - 2023 The eFaps Team
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
package org.efaps.admin.common;

import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.mock.MockResult;
import org.efaps.mock.datamodel.Company;
import org.efaps.mock.datamodel.Company.CompanyBuilder;
import org.efaps.mock.datamodel.IDataModel;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
import org.testng.Assert;
import org.testng.annotations.Test;

import acolyte.jdbc.RowLists;

public class SystemConfigurationTest
    extends AbstractTest
{

    @Test
    public void testPriority()
        throws EFapsException
    {

        final Company companyMock = new CompanyBuilder()
                        .withName("Mock Company")
                        .build();

        final var company = org.efaps.admin.user.Company.get(companyMock.getId());

        MockResult.builder().withSql("select T0.ID,T0.NAME,T0.UUID from T_CMABSTRACT T0 where T0.NAME = ?")
                        .withResult(RowLists.rowList3(Long.class, String.class, String.class)
                                        .append(1L, "SystemConfiguration", UUID.randomUUID().toString())
                                        .asResult())
                        .build();

        final var attrTypeId = IDataModel.Admin_Common_SystemConfigurationAttribute.getId();
        final var linkTypeId = IDataModel.Admin_Common_SystemConfigurationLink.getId();
        final var result = RowLists.rowList5(Long.class, String.class, String.class, Long.class, String.class)
                        .append(attrTypeId, "org.efaps.Key1", "Value1", null, null)
                        .append(linkTypeId, "org.efaps.Key1", "Value2", null, null)
                        .append(attrTypeId, "org.efaps.KeyWithCompany", "Value3", null, null)
                        .append(attrTypeId, "org.efaps.KeyWithCompany", "Value4", company.getId(), null)
                        .append(attrTypeId, "org.efaps.KeyWithAppKey", "Value5", null, null)
                        .append(attrTypeId, "org.efaps.KeyWithAppKey", "Value6", null, "eFaps")
                        .append(attrTypeId, "org.efaps.KeyWithCompanyAndAppKey", "Value7", null, null)
                        .append(attrTypeId, "org.efaps.KeyWithCompanyAndAppKey", "Value8", null, "eFaps")
                        .append(attrTypeId, "org.efaps.KeyWithCompanyAndAppKey", "Value9", company.getId(), "eFaps")
                        .append(attrTypeId, "org.efaps.KeyWithCompanyAndAppKey", "Value0", company.getId(), null)
                        .asResult();

        MockResult.builder().withSql(
                        "select T0.TYPEID,T0.KEY,T0.VALUE,T0.COMPANYID,T0.APPKEY from T_CMSYSCONF T0 where T0.ABSTRACTID = ?")
                        .withResult(result)
                        .build();

        final var sysConf = SystemConfiguration.get("SystemConfiguration");

        Assert.assertEquals(sysConf.getAttributeValue("org.efaps.Key1"), "Value1");
        Assert.assertEquals(sysConf.getAttributeValue("org.efaps.KeyWithCompany"), "Value3");
        Assert.assertEquals(sysConf.getAttributeValue("org.efaps.KeyWithAppKey"), "Value6");
        Assert.assertEquals(sysConf.getAttributeValue("org.efaps.KeyWithCompanyAndAppKey"), "Value8");
        Context.getThreadContext().setCompany(company);
        Assert.assertEquals(sysConf.getAttributeValue("org.efaps.KeyWithCompany"), "Value4");
        Assert.assertEquals(sysConf.getAttributeValue("org.efaps.KeyWithCompanyAndAppKey"), "Value9");
    }

}
