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

package org.apache.savan.subscription;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.ServiceContext;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.publication.client.PublicationClient;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.util.CommonUtil;

/** Abstractly defines subscription methods. Each protocol may extend this to add its own work. */
public abstract class SubscriptionProcessor {

    public abstract void init(SavanMessageContext smc) throws SavanException;

    public void unsubscribe(SavanMessageContext endSubscriptionMessage) throws SavanException {
        String subscriberID = getSubscriberID(endSubscriptionMessage);
        if (subscriberID == null) {
            String message = "Cannot find the subscriber ID";
            throw new SavanException(message);
        }

        SubscriberStore store = endSubscriptionMessage.getSubscriberStore();
        if (store == null)
            throw new SavanException("AbstractSubscriber store not found");

        store.delete(subscriberID);
    }

    public void renewSubscription(SavanMessageContext renewMessage) throws SavanException {
        SubscriberStore store = renewMessage.getSubscriberStore();
        if (store == null)
            throw new SavanException("AbstractSubscriber store not found");

        ExpirationBean bean = getExpirationBean(renewMessage);
        Subscriber subscriber = store.retrieve(bean.getSubscriberID());
        if (subscriber == null) {
            throw new SavanException("Given subscriber is not present");
        }

        subscriber.renewSubscription(bean);
    }

    public void subscribe(SavanMessageContext subscriptionMessage) throws SavanException {
        SubscriberStore store = subscriptionMessage.getSubscriberStore();
        if (store == null)
            throw new SavanException("Sabscriber store not found");

        Subscriber subscriber = getSubscriberFromMessage(subscriptionMessage);
        store.store(subscriber);
    }

    public void endSubscription(String subscriberID, String reason, ServiceContext serviceContext)
            throws SavanException {

        SubscriberStore store = CommonUtil.getSubscriberStore(serviceContext.getAxisService());
        if (store == null) {
            throw new SavanException("SubscriberStore not found");
        }

        Subscriber subscriber = store.retrieve(subscriberID);
        if (subscriber == null) {
            throw new SavanException("No such subscriber '" + subscriberID + "'");
        }
//		doProtocolSpecificEndSubscription(subscriber,reason,serviceContext.getConfigurationContext());

        store.delete(subscriberID);
    }

    public void publish(SavanMessageContext publishMessage) throws SavanException {
        //TODO handle Topics
        SOAPEnvelope requestEnvelope = publishMessage.getEnvelope();
        ServiceContext serviceContext = publishMessage.getMessageContext().getServiceContext();
        PublicationClient client = new PublicationClient(serviceContext.getConfigurationContext());
        client.sendPublication(requestEnvelope.getBody().getFirstElement(),
                               serviceContext.getAxisService(), null);
    }

    public void getStatus(SavanMessageContext smc) {
    }

    public abstract void pauseSubscription(SavanMessageContext pauseSubscriptionMessage)
            throws SavanException;

    public abstract void resumeSubscription(SavanMessageContext resumeSubscriptionMessage)
            throws SavanException;

    public abstract Subscriber getSubscriberFromMessage(SavanMessageContext smc)
            throws SavanException;

    public abstract ExpirationBean getExpirationBean(SavanMessageContext renewMessage)
            throws SavanException;

    public abstract String getSubscriberID(SavanMessageContext smc) throws SavanException;
}
