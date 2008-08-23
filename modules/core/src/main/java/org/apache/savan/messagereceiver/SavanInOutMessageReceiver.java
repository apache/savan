/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.savan.messagereceiver;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.util.ProtocolManager;
import org.apache.savan.util.UtilFactory;

/** InOut message deceiver for Savan. May get called for control messages depending on the protocol. */
public class SavanInOutMessageReceiver extends AbstractInOutSyncMessageReceiver {

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage)
            throws AxisFault {

        SavanMessageContext savanInMessage = new SavanMessageContext(inMessage);

        //setting the Protocol
        Protocol protocol = ProtocolManager.getMessageProtocol(savanInMessage);
        if (protocol == null) {
            //this message does not have a matching protocol
            //so let it go
            throw new SavanException("Cannot find a matching protocol");
        }

        savanInMessage.setProtocol(protocol);

        UtilFactory utilFactory = protocol.getUtilFactory();
        MessageReceiverDelegator delegator = utilFactory.createMessageReceiverDelegator();

        delegator.processMessage(savanInMessage);
        delegator.doProtocolSpecificProcessing(savanInMessage, outMessage);
    }
}
