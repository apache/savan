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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.configuration.ConfigurationManager;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.filters.Filter;
import org.apache.savan.filters.XPathBasedFilter;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.subscription.ExpirationBean;
import org.apache.savan.subscription.SubscriptionProcessor;
import org.apache.xmlbeans.XmlException;

import com.wso2.eventing.atom.CreateFeedDocument;
import com.wso2.eventing.atom.RenewFeedDocument;
import com.wso2.eventing.atom.CreateFeedDocument.CreateFeed;

public class AtomSubscriptionProcessor extends SubscriptionProcessor {

	public void init (SavanMessageContext smc) throws SavanException {
		//setting the subscriber_id as a property if possible.
		
		String id = getSubscriberID(smc);
		if (id!=null) {
			smc.setProperty(AtomConstants.TransferedProperties.SUBSCRIBER_UUID,id);
		}
		
		
		
//		AtomSubscriber atomSubscriber = new AtomSubscriber();
//		smc.setProperty(AtomConstants.TransferedProperties.SUBSCRIBER_UUID,id);
//		atomSubscriber.setId(new URI(id));
//		String atomFeedPath = id.replaceAll(":", "_")+ ".atom";
//		atomSubscriber.setAtomFile(new File(realAtomPath,atomFeedPath));
//		atomSubscriber.setAuthor("DefaultUser");
//		atomSubscriber.setTitle("default Feed");
		
	}
	
	/**
	 * Sample subscription
	 * <createFeed xmlns="http://wso2.com/eventing/atom">
     * <EndTo>endpoint-reference</EndTo> ?
     * <Delivery Mode="xs:anyURI"? >xs:any</Delivery>
     * <Expires>[xs:dateTime | xs:duration]</Expires> ?
     * <Filter Dialect="xs:anyURI"? > xs:any </Filter> ?
     * </createFeed>
     * 
	 */
	
	
	
	public Subscriber getSubscriberFromMessage(SavanMessageContext smc) throws SavanException {

		try {
			ConfigurationManager configurationManager = (ConfigurationManager) smc.getConfigurationContext().getProperty(SavanConstants.CONFIGURATION_MANAGER);
			if (configurationManager == null)
				throw new SavanException ("Configuration Manager not set");
			
			Protocol protocol = smc.getProtocol();
			if (protocol== null)
				throw new SavanException ("Protocol not found");
			
			SOAPEnvelope envelope = smc.getEnvelope();
			if (envelope==null)
				return null;
			
			String subscriberName = protocol.getDefaultSubscriber();
			Subscriber subscriber = configurationManager.getSubscriberInstance(subscriberName);
			
			if (!(subscriber instanceof AtomSubscriber)) {
				String message = "Savan only support implementations of Atom subscriber as Subscribers";
				throw new SavanException (message);
			}

			//find the real path for atom feeds
			File repositoryPath = smc.getConfigurationContext().getRealPath("/"); 
			File realAtomPath = new File(repositoryPath.getAbsoluteFile(),"atom");
			
			//Get the service URL from request
			String serviceAddress = smc.getMessageContext().getTo().getAddress();
			int cutIndex = serviceAddress.indexOf("services");
			if(cutIndex > 0){
				serviceAddress = serviceAddress.substring(0,cutIndex-1);
			}
			
			AtomSubscriber atomSubscriber = (AtomSubscriber) subscriber;
			String id = UUIDGenerator.getUUID();
			smc.setProperty(AtomConstants.TransferedProperties.SUBSCRIBER_UUID,id);
			atomSubscriber.setId(new URI(id));
			String atomFeedPath = id2Path(id);
			atomSubscriber.setAtomFile(new File(realAtomPath,atomFeedPath));
			atomSubscriber.setFeedUrl(serviceAddress+"/services/"+smc.getMessageContext().getServiceContext().getAxisService().getName() +"/atom?feed="+ atomFeedPath);
			
			SOAPBody body = envelope.getBody();
			CreateFeedDocument createFeedDocument = CreateFeedDocument.Factory.parse(body.getFirstElement().getXMLStreamReader());
			CreateFeed createFeed = createFeedDocument.getCreateFeed();

			if(createFeed.getEndTo() != null){
				atomSubscriber.setEndToEPr(createFeed.getEndTo());	
			}
			if(createFeed.getExpires() != null){
				atomSubscriber.setSubscriptionEndingTime(createFeed.getExpires().getTime());	
			}
			
			if (createFeed.getFilter() != null) {
				Filter filter = null;
				String 	filterKey = createFeed.getFilter().getDialect();
				
				filter = configurationManager.getFilterInstanceFromId(filterKey);
				if (filter==null)
					throw new SavanException ("The Filter defined by the dialect is not available");
				
				if(filter instanceof XPathBasedFilter){
					((XPathBasedFilter)filter).setXPathString(createFeed.getFilter().getStringValue());
				}else{
					throw new SavanException("Only Xpath fileters are supported");
				}
				atomSubscriber.setFilter(filter);
			}
			atomSubscriber.setAuthor(createFeed.getAuthor());
			atomSubscriber.setTitle(createFeed.getTitle());
			smc.setProperty(AtomConstants.Properties.feedUrl, atomSubscriber.getFeedUrl());
			return atomSubscriber;
		} catch (AxisFault e) {
			throw new SavanException(e);
		} catch (OMException e) {
			throw new SavanException(e);
		} catch (XmlException e) {
			throw new SavanException(e);
		} catch (URISyntaxException e) {
			throw new SavanException(e);
		}
	}
	
//	private String findValue(String localName,OMElement parent,boolean throwfault) throws SavanException{
//		return findValue(AtomConstants.ATOM_NAMESPACE, localName, parent, throwfault);
//	}
	
	private String findValue(String nsURI,String localName,OMElement parent,boolean throwfault) throws SavanException{
		QName name = new QName (nsURI,AtomConstants.IDEDNTIFIER_ELEMENT);
		OMElement ele = parent.getFirstChildWithName(name);
		if(ele != null){
			return ele.getText();
		}else{
			if(throwfault){
				throw new SavanException (localName + " element is not defined");	
			}else{
				return null;
			}
		}
	}
	
//	private OMElement findElement(String localName,OMElement parent,boolean throwfault) throws SavanException{
//		QName name = new QName (AtomConstants.ATOM_NAMESPACE,AtomConstants.ID_ELEMENT);
//		OMElement ele = parent.getFirstChildWithName(name);
//		if(ele != null){
//			return ele;
//		}else{
//			if(throwfault){
//				throw new SavanException (localName + " element is not defined");	
//			}else{
//				return null;
//			}
//		}
//	}

	public void pauseSubscription(SavanMessageContext pauseSubscriptionMessage) throws SavanException {
		throw new UnsupportedOperationException ("Eventing specification does not support this type of messages");
	}

	public void resumeSubscription(SavanMessageContext resumeSubscriptionMessage) throws SavanException {
		throw new UnsupportedOperationException ("Eventing specification does not support this type of messages");
	}
	
	/**
	 * <renewFeed><Expires></Expires></renewFeed>
	 */

	public ExpirationBean getExpirationBean(SavanMessageContext renewMessage) throws SavanException {
		try {
			SOAPEnvelope envelope = renewMessage.getEnvelope();
			
			RenewFeedDocument renewFeedDocument = RenewFeedDocument.Factory.parse( envelope.getBody().getXMLStreamReader());
//		SOAPBody body = envelope.getBody();
//		
			ExpirationBean expirationBean =  new ExpirationBean();
//		OMElement renewFeedEle = findElement(AtomConstants.RENEW_FEED, body, true);
//		Date expieringTime = getExpirationBeanFromString(findValue(AtomConstants.EXPIRES_ELEMENT, renewFeedEle, true));
			expirationBean.setDuration(false);
			expirationBean.setDateValue(renewFeedDocument.getRenewFeed().getExpires().getTime());
			
			String subscriberID = getSubscriberID(renewMessage);
			if (subscriberID==null) {
				String message = "Cannot find the subscriber ID";
				throw new SavanException (message);
			}
			
			renewMessage.setProperty(AtomConstants.TransferedProperties.SUBSCRIBER_UUID,subscriberID);
			
			expirationBean.setSubscriberID(subscriberID);
			return expirationBean;
		} catch (OMException e) {
			throw new SavanException(e);
		} catch (XmlException e) {
			throw new SavanException(e);
		}
	}

	public String getSubscriberID(SavanMessageContext smc) throws SavanException {
		SOAPEnvelope envelope = smc.getEnvelope();
		SOAPHeader header = envelope.getHeader();
		if (header==null) {
			return null;
		}
		
		return findValue(AtomConstants.ATOM_NAMESPACE,AtomConstants.IDEDNTIFIER_ELEMENT, envelope.getHeader(), false);
	}

	public void unsubscribe(SavanMessageContext endSubscriptionMessage) throws SavanException {
		String subscriberID = getSubscriberID (endSubscriptionMessage);
		File feedPath = endSubscriptionMessage.getConfigurationContext().getRealPath("atom/"+id2Path(subscriberID));
		if(feedPath.exists()){
			feedPath.delete();
		}
		super.unsubscribe(endSubscriptionMessage);
	}
	
	private String id2Path(String id){
		return id.replaceAll(":", "_")+ ".atom";
	}
	
	
}
