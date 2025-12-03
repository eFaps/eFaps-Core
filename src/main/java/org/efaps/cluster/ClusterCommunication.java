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

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.util.EFapsException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterCommunication
{

    private static final Logger LOG = LoggerFactory.getLogger(ClusterCommunication.class);

    private static JChannel CHANNEL;

    public static void initialize()
    {
        final var config = ConfigProvider.getConfig();
        final var fileOpt = config.getOptionalValue("core.jGroups.file", String.class);
        if (fileOpt.isPresent()) {
            final var file = fileOpt.get();
            LOG.info("Got JGroupsFile: {}", file);

            try {
                final var channel = new JChannel(file);
                channel.setReceiver(new Receiver()
                {

                    @Override
                    public void receive(final Message msg)
                    {
                        LOG.info("received JGroups message: {}", msg);
                        try {
                            boolean permitContinue = true;
                            for (final var listener : Listener.get()
                                            .<IClusterMsgListener>invoke(IClusterMsgListener.class)) {
                                if (permitContinue) {
                                    LOG.debug("calling: {}", listener);
                                    permitContinue = listener.onMessage(msg);
                                } else {
                                    LOG.debug("skipped: {}", listener);
                                }
                            }
                        } catch (final EFapsException e) {
                            LOG.error("Catched", e);
                        }
                    }

                    @Override
                    public void viewAccepted(final View view)
                    {
                        LOG.info("received JGroups view: {}", view);
                    }
                });

                final var clusterName = config.getValue("core.jGroups.clusterName", String.class);
                channel.connect(clusterName);

                CHANNEL = channel;
            } catch (final Exception e) {
                LOG.error("Catched", e);
            }
        } else {
            LOG.debug("No JGroupsFile");
        }
    }

    public static JChannel getChannel()
    {
        return CHANNEL;
    }
}
