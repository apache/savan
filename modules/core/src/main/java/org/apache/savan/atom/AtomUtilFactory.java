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

package org.apache.savan.atom;

import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.messagereceiver.MessageReceiverDeligater;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.subscription.SubscriptionProcessor;
import org.apache.savan.util.UtilFactory;

public class AtomUtilFactory implements UtilFactory {

	/** 
	 * this is a way to map different actions to different types of operations 
	 */
	public SavanMessageContext initializeMessage(SavanMessageContext smc) {
		MessageContext messageContext = smc.getMessageContext();
		//setting the message type.
		String action = messageContext.getOptions().getAction();
		if (AtomConstants.Actions.Subscribe.equals(action))
			smc.setMessageType(SavanConstants.MessageTypes.SUBSCRIPTION_MESSAGE);
		else if (AtomConstants.Actions.Renew.equals(action))
			smc.setMessageType(SavanConstants.MessageTypes.RENEW_MESSAGE);
		else if (AtomConstants.Actions.Unsubscribe.equals(action))
			smc.setMessageType(SavanConstants.MessageTypes.UNSUBSCRIPTION_MESSAGE);
		else if (AtomConstants.Actions.GetStatus.equals(action))
			smc.setMessageType(SavanConstants.MessageTypes.GET_STATUS_MESSAGE);
		else if (AtomConstants.Actions.SubscribeResponse.equals(action))
			smc.setMessageType(SavanConstants.MessageTypes.SUBSCRIPTION_RESPONSE_MESSAGE);
		else if (AtomConstants.Actions.RenewResponse.equals(action))
			smc.setMessageType(SavanConstants.MessageTypes.RENEW_RESPONSE_MESSAGE);
		else if (AtomConstants.Actions.UnsubscribeResponse.equals(action))
			smc.setMessageType(SavanConstants.MessageTypes.UNSUBSCRIPTION_RESPONSE_MESSAGE);
		else if (AtomConstants.Actions.GetStatusResponse.equals(action))
			smc.setMessageType(SavanConstants.MessageTypes.GET_STATUS_RESPONSE_MESSAGE);
		else 
			smc.setMessageType(SavanConstants.MessageTypes.UNKNOWN);
		return smc;
	}

	public SubscriptionProcessor createSubscriptionProcessor() {
		return new AtomSubscriptionProcessor ();
	}
	
	public MessageReceiverDeligater createMessageReceiverDeligater() {
		return new AtomMessageReceiverDeligater ();
	}

	public Subscriber createSubscriber() {
		return new AtomSubscriber ();
	}
	
}
