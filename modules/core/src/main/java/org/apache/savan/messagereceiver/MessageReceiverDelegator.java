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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.configuration.ConfigurationManager;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscription.SubscriptionProcessor;
import org.apache.savan.util.UtilFactory;

/** Provide abstract functions that may be done by protocols at the MessageReceiver level. */
public abstract class MessageReceiverDelegator {


    public void processMessage(SavanMessageContext smc) throws SavanException {
        MessageContext msgContext = smc.getMessageContext();

        //setting the Protocol
        Protocol protocol = smc.getProtocol();

        if (protocol == null) {
            //this message does not have a matching protocol
            //so let it go
            throw new SavanException("Cannot find a matching protocol");
        }

        smc.setProtocol(protocol);

        AxisService axisService = msgContext.getAxisService();
        if (axisService == null)
            throw new SavanException("Service context is null");

        //setting the AbstractSubscriber Store
        Parameter parameter = axisService.getParameter(SavanConstants.SUBSCRIBER_STORE);
        if (parameter == null) {
            setSubscriberStore(smc);
        }

        UtilFactory utilFactory = smc.getProtocol().getUtilFactory();
        utilFactory.initializeMessage(smc);

        int messageType = smc.getMessageType();

        SubscriptionProcessor processor = utilFactory.createSubscriptionProcessor();
        processor.init(smc);
        switch (messageType) {
            case SavanConstants.MessageTypes.SUBSCRIPTION_MESSAGE:
                processor.subscribe(smc);
                break;
            case SavanConstants.MessageTypes.UNSUBSCRIPTION_MESSAGE:
                processor.unsubscribe(smc);
                break;
            case SavanConstants.MessageTypes.RENEW_MESSAGE:
                processor.renewSubscription(smc);
                break;
            case SavanConstants.MessageTypes.PUBLISH:
                processor.publish(smc);
                break;
            case SavanConstants.MessageTypes.GET_STATUS_MESSAGE:
                processor.getStatus(smc);
                break;
        }
    }

    private void setSubscriberStore(SavanMessageContext smc) throws SavanException {
        MessageContext msgContext = smc.getMessageContext();
        AxisService axisService = msgContext.getAxisService();

        Parameter parameter = axisService.getParameter(SavanConstants.SUBSCRIBER_STORE_KEY);
        String subscriberStoreKey = SavanConstants.DEFAULT_SUBSCRIBER_STORE_KEY;
        if (parameter != null)
            subscriberStoreKey = (String)parameter.getValue();

        ConfigurationManager configurationManager = (ConfigurationManager)smc
                .getConfigurationContext().getProperty(SavanConstants.CONFIGURATION_MANAGER);
        SubscriberStore store = configurationManager.getSubscriberStoreInstance(subscriberStoreKey);

        parameter = new Parameter();
        parameter.setName(SavanConstants.SUBSCRIBER_STORE);
        parameter.setValue(store);

        try {
            axisService.addParameter(parameter);
        } catch (AxisFault e) {
            throw new SavanException(e);
        }

    }

    public abstract void doProtocolSpecificProcessing(SavanMessageContext inSavanMessage,
                                                      MessageContext outMessage)
            throws SavanException;

    public abstract void doProtocolSpecificProcessing(SavanMessageContext inSavanMessage)
            throws SavanException;
}
