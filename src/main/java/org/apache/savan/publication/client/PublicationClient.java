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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.util.CommonUtil;

/**
 * This can be used to make the Publication Process easy.
 * Handle things like engaging the savan module correctly and setting the
 * correct subscriber store.
 */
public class PublicationClient {
	
	public static final String TEMP_PUBLICATION_ACTION = "UUID:TempPublicationAction";
	private ConfigurationContext configurationContext = null;
	
	public PublicationClient (ConfigurationContext configurationContext) {
		this.configurationContext = configurationContext;
	}
	
	/**
	 * This can be used by the Publishers in the same JVM (e.g. a service deployed in the same Axis2 instance).
	 * 
	 * @param publication - the XML message to be published
	 * @param service - The service to which this publication is bound to (i.e. this will be only sent to the subscribers of this service)
	 * @throws SavanException
	 */
	public void sendPublication (OMElement publication, AxisService service) throws SavanException {
		
		try {
			ServiceClient sc = new ServiceClient (configurationContext,null);
			Options options = new Options ();
			sc.setOptions(options);
			
			//Just a matter of getting it to the SavanOutHandler
			options.setTo(new EndpointReference ("http://temp.publication.URI"));
			
			if (options.getAction()==null)
				options.setAction(TEMP_PUBLICATION_ACTION);
			
			sc.setOptions(options);
			
			//this will not be required when the 
			Parameter parameter = new Parameter ();
			parameter.setName(SavanConstants.SUBSCRIBER_STORE);
			SubscriberStore store = CommonUtil.getSubscriberStore(service);
			parameter.setValue(store);
			sc.getAxisService().addParameter(parameter);
			
			//if already engaged, axis2 will neglect this engagement.
			sc.engageModule( new QName("savan"));
			
			MessageContext mc = new MessageContext ();
			mc.setEnvelope(getEnvelopeFromPublication (publication));
			OperationClient client = sc.createClient(ServiceClient.ANON_OUT_ONLY_OP);
			client.addMessageContext(mc);
			client.execute(true);
		} catch (AxisFault e) {
			String message = "Could not send the publication";
			throw new SavanException (message,e);
		}
	}
	
	
	private SOAPEnvelope getEnvelopeFromPublication (OMElement element) {
		
		//for now we are sending SOAP 1.1
		SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
		envelope.getBody().addChild(element);
		
		return envelope;
	}
	
}
