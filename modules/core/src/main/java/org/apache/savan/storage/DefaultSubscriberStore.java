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

package org.apache.savan.storage;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.savan.SavanException;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.subscribers.SubscriberGroup;

public class DefaultSubscriberStore implements SubscriberStore {

	private HashMap subscriberMap = null;
	private HashMap subscriberGroups = null;
	
	public DefaultSubscriberStore () {
		subscriberMap = new HashMap ();
		subscriberGroups = new HashMap ();
	}
	
	public void init(ConfigurationContext configurationContext) throws SavanException {
		// TODO Auto-generated method stub
	}

	public Subscriber retrieve(String id) {
		return (Subscriber) subscriberMap.get(id);
	}

	public void store(Subscriber s) {
		URI subscriberID = s.getId();
		String key = subscriberID.toString();
		subscriberMap.put(key,s);
	}

	public void delete(String subscriberID) {
		subscriberMap.remove(subscriberID);
	}

	public Iterator retrieveAllSubscribers () {
		ArrayList allSubscribers = new ArrayList ();
		for (Iterator iter=subscriberMap.keySet().iterator();iter.hasNext();) {
			Object key = iter.next();
			allSubscribers.add(subscriberMap.get(key));
		}
		return allSubscribers.iterator();
	}

	public Iterator retrieveAllSubscriberGroups () {
		ArrayList allSubscriberGroups = new ArrayList ();
		for (Iterator iter=subscriberGroups.keySet().iterator();iter.hasNext();) {
			Object key = iter.next();
			allSubscriberGroups.add(subscriberGroups.get(key));
		}
		return allSubscriberGroups.iterator();
	}
	
	public void addSubscriberGroup(String groupId) {
		subscriberGroups.put(groupId, new SubscriberGroup ());
	}

	public void addSubscriberToGroup(String listId, Subscriber subscriber) throws SavanException {
		SubscriberGroup subscriberGroup = (SubscriberGroup) subscriberGroups.get(listId);
		if (subscriberGroup!=null)
			subscriberGroup.addSubscriber (subscriber);
		else 
			throw new SavanException ("Cannot find the Subscriber store");
	}

	public SubscriberGroup getSubscriberGroup(String groupId) {
		return (SubscriberGroup) subscriberGroups.get(groupId);
	}

}
