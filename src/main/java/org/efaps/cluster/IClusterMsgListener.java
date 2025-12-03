package org.efaps.cluster;

import org.efaps.admin.program.esjp.IEsjpListener;
import org.jgroups.Message;

public interface IClusterMsgListener extends IEsjpListener
{

    boolean onMessage(Message msg);

}
