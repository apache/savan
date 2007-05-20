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

package org.apache.savan.eventing.subscribers;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanException;
import org.apache.savan.eventing.Delivery;
import org.apache.savan.filters.Filter;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.subscription.ExpirationBean;
import org.apache.savan.util.CommonUtil;

/**
 * Defines methods common to all eventing subscribers.
 */
public class EventingSubscriber implements Subscriber {
	
	
	private URI id;
	private Filter filter = null;
	private EndpointReference endToEPr;
	private Delivery delivery;
	private ConfigurationContext configurationContext;
	
	/**
	 * The time at which further notification of messages should be avaoded
	 * to this subscriber.
	 */
	private Date subscriptionEndingTime = null;


	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public URI getId() {
		return id;
	}
	
	public void setId(URI id) {
		this.id = id;
	}
	
	public Delivery getDelivery() {
		return delivery;
	}

	public EndpointReference getEndToEPr() {
		return endToEPr;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public void setEndToEPr(EndpointReference errorReportingEPR) {
		this.endToEPr = errorReportingEPR;
	}

	public Date getSubscriptionEndingTime () {
		return subscriptionEndingTime;
	}
	
	public void setSubscriptionEndingTime () {
	}
	
	public ConfigurationContext getConfigurationContext() {
		return configurationContext;
	}

	public void setConfigurationContext(ConfigurationContext configurationContext) {
		this.configurationContext = configurationContext;
	}

	public void setSubscriptionEndingTime(Date subscriptionEndingTime) {
		this.subscriptionEndingTime = subscriptionEndingTime;
	}
	
	/**
	 * This method first checks weather the passed message complies with the current filter.
	 * If so message is sent, and the subscriberID is added to the PublicationReport.
	 * Else message is ignored.
	 * 
	 * @param smc
	 * @param report
	 * @throws SavanException
	 */
	public void sendEventData (OMElement eventData) throws SavanException {

		Date date = new Date ();
		boolean expired = false;
		if (subscriptionEndingTime!=null && date.after(subscriptionEndingTime))
			expired = true;
		
		if (expired) {
			String message = "Cant notify the listner since the subscription has been expired";
			throw new SavanException (message);
		}
		
		if (doesEventDataBelongToTheFilter(eventData)) {
			sendThePublication (eventData);
		}
	}

	private boolean doesEventDataBelongToTheFilter(OMElement eventData) throws SavanException {
		if (filter!=null) {
			return filter.checkCompliance (eventData);
		} else 
			return true;
	}
	
	private void sendThePublication(OMElement eventData) throws SavanException {
		
		EndpointReference deliveryEPR  = delivery.getDeliveryEPR();
		try {
			ServiceClient sc = new ServiceClient (configurationContext,null);
			Options options = new Options ();
			sc.setOptions(options);
			options.setTo(deliveryEPR);
			options.setProperty(MessageContext.TRANSPORT_NON_BLOCKING, Boolean.FALSE);
			sc.fireAndForget(eventData);
		} catch (AxisFault e) {
			throw new SavanException (e);
		}
	}
	
	public void renewSubscription (ExpirationBean bean) {
		if (bean.isDuration()) {
			if (subscriptionEndingTime==null) {
				Calendar calendar = Calendar.getInstance();
				CommonUtil.addDurationToCalendar(calendar,bean.getDurationValue());
				subscriptionEndingTime = calendar.getTime();
			} else {
				Calendar expiration = Calendar.getInstance();
				expiration.setTime(subscriptionEndingTime);
				CommonUtil.addDurationToCalendar(expiration,bean.getDurationValue());
				subscriptionEndingTime = expiration.getTime();
			}
		} else
			subscriptionEndingTime = bean.getDateValue();
	}
	
}
