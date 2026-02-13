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
package org.efaps.update.schema.program.esjp;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class MigratorTest
{

    @Test(description = "Migrator Test", dataProvider = "DataProvider")
    public void test1(final String orginalStr,
                      final String migratedStr) {
        assertEquals(Migrator.migrate(orginalStr), migratedStr);
    }


    @DataProvider(name = "DataProvider")
    public static Iterator<Object[]> dataProvider(final ITestContext context)
    {
        final List<Object[]> ret = new ArrayList<>();
        ret.add(new Object[] { "just a value", "just a value" });

        final var javaxws1 = """
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
                        """;

        final var javaxws2 = """
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
                        """;
        ret.add(new Object[] { javaxws1, javaxws2 });

        final var soap1 = """
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
                        """;

        final var soap2 = """
import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPMessage;
                        """;

        ret.add(new Object[] { soap1, soap2 });

        final var jrxls1 = """
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
                        """;

        final var jrxls2 = """
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.poi.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
                        """;

        ret.add(new Object[] { jrxls1, jrxls2 });

        final var remove1 = """

    //EFAPSMIGRATE_DELSTART
    @Override
    public JRQueryExecuter createQueryExecuter(final JRDataset _dataset,
                                               final Map<String, ? extends JRValueParameter> _parameters)
        throws JRException
    {
        // deprecated therefore empty
        return null;
    }
    //EFAPSMIGRATE_DELSTOP

""";

        final var remove2 = """

    //

""";


        ret.add(new Object[] { remove1, remove2 });
        return ret.iterator();
    }

}
