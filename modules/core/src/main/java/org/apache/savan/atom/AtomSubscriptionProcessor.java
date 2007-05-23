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
			
//		AbstractSubscriber subscriber = utilFactory.createSubscriber();  //eventing only works on leaf subscriber for now.
			
			String subscriberName = protocol.getDefaultSubscriber();
			Subscriber subscriber = configurationManager.getSubscriberInstance(subscriberName);
			
			if (!(subscriber instanceof AtomSubscriber)) {
				String message = "Savan only support implementations of Atom subscriber as Subscribers";
				throw new SavanException (message);
			}

			//find the real path for atom feeds
			File repositoryPath = smc.getConfigurationContext().getRealPath("/"); 
			File realAtomPath = new File(repositoryPath.getAbsoluteFile(),"../atom");
			
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
			String atomFeedPath = id.replaceAll(":", "_")+ ".atom";
			atomSubscriber.setAtomFile(new File(realAtomPath,atomFeedPath));
			atomSubscriber.setFeedUrl(serviceAddress +"/atom/"+ atomFeedPath);
			
			SOAPBody body = envelope.getBody();
			CreateFeedDocument createFeedDocument = CreateFeedDocument.Factory.parse(body.getFirstElement().getXMLStreamReader());
			CreateFeed createFeed = createFeedDocument.getCreateFeed();
			//TODO Srinath
//			if(createFeed.getEndTo() != null){
//				atomSubscriber.setEndToEPr(createFeed.getEndTo());	
//			}
//			if(createFeed.getExpires() != null){
//				atomSubscriber.setSubscriptionEndingTime(createFeed.getExpires().getTime());	
//			}
			
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
			
			
			
			
			
			//TODO -Srinath
			atomSubscriber.setAuthor(createFeed.getAuthor());
			atomSubscriber.setTitle(createFeed.getTitle());
			
			smc.setProperty(AtomConstants.Properties.feedUrl, atomSubscriber.getFeedUrl());
			
			return atomSubscriber;
			
			
//			SOAPBody body = envelope.getBody();
//			
//			OMElement createFeed = findElement("createFeed", body, true) ;
//			OMElement endToElement = findElement(AtomConstants.ElementNames.EndTo, body, true);
//			
//			EndpointReference endToEPR = null;
//			if(endToElement != null){
//				endToEPR = EndpointReferenceHelper.fromOM(endToElement);
//			}
//			atomSubscriber.setEndToEPr(endToEPR);
//			
//			
//			String expiresText = findValue(AtomConstants.EXPIRES_ELEMENT, createFeed, true);
//			if (expiresText==null){
//				String message = "Expires Text is null";
//				throw new SavanException (message);
//			}
//			expiresText = expiresText.trim();
//			Date expiration = getExpirationBeanFromString(expiresText);
//			if (expiration==null) {
//				String message = "Cannot understand the given date-time value for the Expiration";
//				throw new SavanException (message);
//			}
//			atomSubscriber.setSubscriptionEndingTime(expiration);
//			
//			OMElement filterElement = findElement(AtomConstants.ElementNames.Filter, createFeed, true);
//			if (filterElement!= null) {
//				OMNode filterNode = filterElement.getFirstOMChild();
//				OMAttribute dialectAttr = filterElement.getAttribute(new QName (AtomConstants.ElementNames.Dialect));
//				Filter filter = null;
//				String filterKey = AtomConstants.DEFAULT_FILTER_IDENTIFIER;
//				if (dialectAttr!=null) {
//					filterKey = dialectAttr.getAttributeValue();
//				}
//				filter = configurationManager.getFilterInstanceFromId(filterKey);
//				if (filter==null)
//					throw new SavanException ("The Filter defined by the dialect is not available");
//				
//				filter.setUp (filterNode);
//				atomSubscriber.setFilter(filter);
//			}
//			
//			atomSubscriber.setId(findValue(AtomConstants.ID_ELEMENT, createFeed,true));
//			atomSubscriber.setAuthor(findValue(AtomConstants.AUTHOR_ELEMENT, createFeed,true));
//			atomSubscriber.setTitle(findValue(AtomConstants.TITLE_ELEMENT, createFeed,true));
//			
//			smc.setProperty(AtomConstants.Properties.feedUrl, atomSubscriber.getFeedUrl());
//			
//			return atomSubscriber;
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
	
	private String findValue(String localName,OMElement parent,boolean throwfault) throws SavanException{
		return findValue(AtomConstants.ATOM_NAMESPACE, localName, parent, throwfault);
	}
	
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
	
	private OMElement findElement(String localName,OMElement parent,boolean throwfault) throws SavanException{
		QName name = new QName (AtomConstants.ATOM_NAMESPACE,AtomConstants.ID_ELEMENT);
		OMElement ele = parent.getFirstChildWithName(name);
		if(ele != null){
			return ele;
		}else{
			if(throwfault){
				throw new SavanException (localName + " element is not defined");	
			}else{
				return null;
			}
		}
	}

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
	
//	private Date getExpirationBeanFromString (String expiresStr) throws SavanException {
//
//		
//		
//		//expires can be a duration or a date time.
//		//Doing the conversion using the ConverUtil helper class.
//		
//		boolean isDuration = CommonUtil.isDuration(expiresStr);
//		if (isDuration) {
//			try {
//				Date currentTime = new Date();
//				Duration duration = ConverterUtil.convertToDuration(expiresStr);
//				return new Date(currentTime.getTime()+ (int)(1000* duration.getSeconds()));
//			} catch (IllegalArgumentException e) {
//				String message = "Cannot convert the Expiration value to a valid duration";
//				throw new SavanException (message,e);
//			}
//		} else {
//			try {
//			    Calendar calendar = ConverterUtil.convertToDateTime(expiresStr);
//			    return calendar.getTime();
//			} catch (Exception e) {
//				String message = "Cannot convert the Expiration value to a valid DATE/TIME";
//				throw new SavanException (message,e);
//			}
//		}
//	}

	public void doProtocolSpecificEndSubscription(Subscriber subscriber, String reason, ConfigurationContext configurationContext) throws SavanException {
		throw new UnsupportedOperationException();
//		String SOAPVersion = (String) subscriber.getProperty(AtomConstants.Properties.SOAPVersion);
//		if (SOAPVersion==null) 
//			throw new SavanException ("Cant find the SOAP version of the subscriber");
//		
//		SOAPFactory factory = null;
//		if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
//			factory = OMAbstractFactory.getSOAP11Factory();
//		else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(SOAPVersion))
//			factory = OMAbstractFactory.getSOAP12Factory();
//		else
//			throw new SavanException ("The subscriber has a unknown SOAP version property set");
//		
//		SOAPEnvelope envelope = factory.getDefaultEnvelope();
	}
	
//	private boolean deliveryModesupported() {
//		return true;
//	}
//	
//	private boolean isInvalidDiration (Duration duration) {
//		return false;
//	}
//	
//	private boolean isDateInThePast (Date date) {
//		return false;
//	}
//	
//	private boolean filterDilalectSupported (String filterDialect){ 
//		return true;
//	}
	
}
