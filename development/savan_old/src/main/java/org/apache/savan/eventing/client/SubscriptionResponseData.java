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

package org.apache.savan.eventing.client;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.savan.subscription.ExpirationBean;

public class SubscriptionResponseData {

	EndpointReference subscriptionManager = null;
	ExpirationBean  expiration = null;
	
	public SubscriptionResponseData () {
		expiration = new ExpirationBean ();
	}

	public EndpointReference getSubscriptionManager() {
		return subscriptionManager;
	}

	public ExpirationBean getExpiration() {
		return expiration;
	}

	public void setExpiration(ExpirationBean expiration) {
		this.expiration = expiration;
	}

	public void setSubscriptionManager(EndpointReference subscriptionManager) {
		this.subscriptionManager = subscriptionManager;
	}
}
