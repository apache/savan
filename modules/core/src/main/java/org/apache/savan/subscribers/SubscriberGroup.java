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

package org.apache.savan.subscribers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.savan.SavanException;

/**
 * Defines a set of subscribers that are acting as a group or a Topic.
 *
 */
public class SubscriberGroup  {

	protected ArrayList subscribers = null;
	
	private URI id;
	
	public URI getId() {
		return id;
	}

	public void setId(URI id) {
		this.id = id;
	}

	public SubscriberGroup (){
		subscribers = new ArrayList ();
	}
	
	public void addSubscriber (Subscriber subscriber) throws SavanException {
		subscribers.add(subscriber);
	}

	public void sendEventDataToGroup(OMElement eventData) throws SavanException {
		for (Iterator it = subscribers.iterator();it.hasNext();) {
			Subscriber subscriber = (Subscriber) it.next();
			subscriber.sendEventData(eventData);
		}
	}
	
}
