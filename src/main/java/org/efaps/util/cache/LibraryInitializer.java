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
package org.efaps.util.cache;

import org.efaps.admin.common.SystemConfigurationAdapter;
import org.efaps.admin.datamodel.AttributeAdapter;
import org.efaps.admin.datamodel.AttributeTypeAdapter;
import org.efaps.admin.datamodel.SQLTableAdapter;
import org.efaps.admin.datamodel.TypeAdapter;
import org.efaps.admin.user.AssociationAdapter;
import org.efaps.admin.user.CompanyAdapter;
import org.efaps.admin.user.JAASSystemAdapter;
import org.efaps.admin.user.PersonAdapter;
import org.efaps.admin.user.RoleAdapter;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;
import org.infinispan.protostream.annotations.ProtoSyntax;

@AutoProtoSchemaBuilder(
                includeClasses = {
                                AssociationAdapter.class,
                                AttributeAdapter.class,
                                AttributeTypeAdapter.class,
                                CompanyAdapter.class,
                                JAASSystemAdapter.class,
                                PersonAdapter.class,
                                RoleAdapter.class,
                                SQLTableAdapter.class,
                                SystemConfigurationAdapter.class,
                                TypeAdapter.class
                },
                schemaFileName = "library.proto",
                schemaFilePath = "proto/",
                schemaPackageName = "efaps",
                syntax = ProtoSyntax.PROTO3)
public interface LibraryInitializer extends GeneratedSchema
{

}
