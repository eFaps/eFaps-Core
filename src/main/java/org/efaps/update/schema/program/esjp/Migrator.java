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

import org.apache.commons.lang3.Strings;


public class Migrator
{

    public static String migrate(final String orginalStr)
    {
        final var replacementStr1 = Strings.CS.replace(orginalStr, "javax.ws", "jakarta.ws");
        final var replacementStr2 = Strings.CS.replace(replacementStr1, "javax.xml.soap", "jakarta.xml.soap");
        final var replacementStr3 = Strings.CS.replace(replacementStr2,
                        "net.sf.jasperreports.engine.export.JRXlsExporter",
                        "net.sf.jasperreports.poi.export.JRXlsExporter");

        final var replacementStr4 =  replacementStr3.replaceAll("EFAPSMIGRATE_DELSTART[\\w\\W]*EFAPSMIGRATE_DELSTOP", "");
        return replacementStr4;
    }
}
