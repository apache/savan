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

import org.apache.axiom.om.OMElement;
import org.apache.savan.SavanException;
import org.apache.savan.subscription.ExpirationBean;

import java.net.URI;

/**
 * Defines a subscriber which is the entity that define a specific subscription in savan.
 * Independent of the protocol type.
 */
public interface Subscriber {

    public URI getId();

    public void setId(URI id);

    public void sendEventData(OMElement eventData) throws SavanException;

    public void renewSubscription(ExpirationBean bean);

}
