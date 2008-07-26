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

package org.apache.savan.publication.client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.savan.SavanException;
import org.apache.savan.publication.PublicationReport;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.subscribers.SubscriberGroup;
import org.apache.savan.util.CommonUtil;

import java.net.URI;
import java.util.Iterator;

/**
 * This can be used to make the Publication Process easy. Handle things like engaging the savan
 * module correctly and setting the correct subscriber store.
 */
public class PublicationClient {

    public static final String TEMP_PUBLICATION_ACTION = "UUID:TempPublicationAction";
    private ConfigurationContext configurationContext = null;

    public PublicationClient(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * This can be used by the Publishers in the same JVM (e.g. a service deployed in the same Axis2
     * instance).
     *
     * @param eventData - The XML message to be published
     * @param service   - The service to which this publication is bound to (i.e. this will be only
     *                  sent to the subscribers of this service)
     * @param eventName - The name of the event, this can be a action which represents an out only
     *                  operation or a Topic ID.
     * @throws SavanException
     */
    public void sendPublication(OMElement eventData, AxisService service, URI eventName)
            throws SavanException {

        try {

            SubscriberStore subscriberStore = CommonUtil.getSubscriberStore(service);
            if (subscriberStore == null)
                throw new SavanException("Cannot find the Subscriber Store");

            PublicationReport report = new PublicationReport();
            if (eventName != null) {
                //there should be a valid operation or a SubscriberGroup to match this event.
                AxisOperation operation = getAxisOperationFromEventName(eventName);
                if (operation != null) {
                    //send to all subscribers with this operation.
                    throw new UnsupportedOperationException("Not implemented");
                } else {
                    //there should be a valid SubscriberGroup to match this eventName

                    String groupId = eventName.toString();
                    SubscriberGroup group =
                            (SubscriberGroup)subscriberStore.getSubscriberGroup(groupId);
                    if (group != null)
                        group.sendEventDataToGroup(eventData);
                    else
                        throw new SavanException(
                                "Could not find a subscriberGroup or an operation to match the eventName");

                }
            } else {
                //no event name, so send it to everybody.

                //sending to all individual subscribers
                for (Iterator iter = subscriberStore.retrieveAllSubscribers(); iter.hasNext();) {
                    Subscriber subscriber = (Subscriber)iter.next();
                    subscriber.sendEventData(eventData);
                }

                //sending to all Subscriber Groups
                for (Iterator iter = subscriberStore.retrieveAllSubscriberGroups();
                     iter.hasNext();) {
                    SubscriberGroup subscriberGroup = (SubscriberGroup)iter.next();
                    subscriberGroup.sendEventDataToGroup(eventData);
                }
            }

        } catch (AxisFault e) {
            String message = "Could not send the publication";
            throw new SavanException(message, e);
        }
    }

    private AxisOperation getAxisOperationFromEventName(URI eventName) {
        //TODO do operation lookup
		return null;
	}
	
}
