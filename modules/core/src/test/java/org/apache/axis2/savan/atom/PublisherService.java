/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.savan.atom;

import java.net.URI;
import java.util.Random;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.savan.publication.client.PublicationClient;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.util.CommonUtil;

public class PublisherService {
  
	ServiceContext serviceContext = null;
	private String eventName = "testTopic";
	
	public void init(ServiceContext serviceContext) throws AxisFault {
		try {
			System.out.println("Eventing Service INIT called");
			this.serviceContext = serviceContext;
			
			PublisherThread thread = new PublisherThread ();
			thread.start();
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			throw new AxisFault(e);
		}
	}
  
	public void dummyMethod(OMElement param) throws Exception  {
		System.out.println("Eventing Service dummy method called");
	}
	
	private class PublisherThread extends Thread {
		
		String Publication = "Publication";
		String publicationNamespaceValue = "http://tempuri/publication/";
		Random r = new Random ();
		
		public void run () {
			try {
				while (true) {
					
					
					
					//publishing
					System.out.println("Publishing next publication...");
					
					SubscriberStore store = CommonUtil.getSubscriberStore(serviceContext.getAxisService());
					if (store==null)
						throw new Exception ("Cant find the Savan subscriber store");
					
					OMElement envelope = getNextPublicationEvent ();
					PublicationClient client = new PublicationClient(serviceContext.getConfigurationContext());
					client.sendPublication(envelope,serviceContext.getAxisService(),null);
					Thread.sleep(10000);
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private int eventID = 0;
		
		public OMElement getNextPublicationEvent () {
			SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
			OMNamespace namespace = factory.createOMNamespace(publicationNamespaceValue,"ns1");
			OMElement publicationElement = factory.createOMElement(Publication,namespace);
			
			factory.createOMElement("foo",namespace,publicationElement).setText("Event "+eventID);
			
			OMElement publishMethod = factory.createOMElement("publish",namespace);
			publishMethod.addChild(publicationElement);
			
			return publishMethod;
		}
	}
}
