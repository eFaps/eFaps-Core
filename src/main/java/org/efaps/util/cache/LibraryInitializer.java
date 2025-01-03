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

import org.efaps.admin.access.AccessTypeAdapter;
import org.efaps.admin.common.AssociationKey;
import org.efaps.admin.common.SystemConfigurationAdapter;
import org.efaps.admin.datamodel.AttributeAdapter;
import org.efaps.admin.datamodel.AttributeSetAdapter;
import org.efaps.admin.datamodel.AttributeTypeAdapter;
import org.efaps.admin.datamodel.ClassificationAdapter;
import org.efaps.admin.datamodel.SQLTableAdapter;
import org.efaps.admin.datamodel.Status.StatusGroup;
import org.efaps.admin.datamodel.StatusAdapter;
import org.efaps.admin.datamodel.TypeAdapter;
import org.efaps.admin.event.EventDefinitionAdapter;
import org.efaps.admin.ui.CommandAdapter;
import org.efaps.admin.ui.FormAdapter;
import org.efaps.admin.ui.ImageAdapter;
import org.efaps.admin.ui.MenuAdapter;
import org.efaps.admin.ui.ModuleAdapter;
import org.efaps.admin.ui.SearchAdapter;
import org.efaps.admin.ui.TableAdapter;
import org.efaps.admin.user.CompanyAdapter;
import org.efaps.admin.user.JAASSystemAdapter;
import org.efaps.admin.user.PersonAdapter;
import org.efaps.admin.user.RoleAdapter;
import org.efaps.db.store.StoreAdapter;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;
import org.infinispan.protostream.annotations.ProtoSyntax;
import org.infinispan.protostream.types.java.CommonContainerTypes;

@AutoProtoSchemaBuilder(dependsOn = {
                CommonContainerTypes.class
}, includeClasses = {
                AccessTypeAdapter.class,
                org.efaps.admin.user.AssociationAdapter.class,
                org.efaps.admin.common.AssociationAdapter.class,
                AssociationKey.class,
                AttributeAdapter.class,
                AttributeSetAdapter.class,
                AttributeTypeAdapter.class,
                ClassificationAdapter.class,
                CommandAdapter.class,
                CompanyAdapter.class,
                EventDefinitionAdapter.class,
                FormAdapter.class,
                ImageAdapter.class,
                JAASSystemAdapter.class,
                MenuAdapter.class,
                ModuleAdapter.class,
                PersonAdapter.class,
                RoleAdapter.class,
                SearchAdapter.class,
                StatusGroup.class,
                StatusAdapter.class,
                StoreAdapter.class,
                SQLTableAdapter.class,
                SystemConfigurationAdapter.class,
                TableAdapter.class,
                TypeAdapter.class
},
                schemaFileName = "efaps.v2.proto",
                schemaFilePath = "proto/",
                schemaPackageName = "efaps.v2",
                syntax = ProtoSyntax.PROTO3)
public interface LibraryInitializer
    extends GeneratedSchema
{

}
