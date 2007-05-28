package org.apache.savan.atom;

import java.util.Calendar;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.savan.filters.XPathBasedFilter;
import org.apache.savan.util.CommonUtil;
import org.apache.xmlbeans.XmlException;

import com.wso2.eventing.atom.CreateFeedDocument;
import com.wso2.eventing.atom.CreateFeedResponseDocument;
import com.wso2.eventing.atom.FilterType;
import com.wso2.eventing.atom.CreateFeedDocument.CreateFeed;
import com.wso2.eventing.atom.CreateFeedResponseDocument.CreateFeedResponse;

public class AtomEventingClient {
	private ServiceClient serviceClient = null;
	private EndpointReference feedEpr;
	
	public AtomEventingClient(ServiceClient serviceClient){
		this.serviceClient = serviceClient;
	}
	
	public CreateFeedResponse createFeed(String title,String author) throws AxisFault{
		return createFeed(title, author,null,null);
	}
	public CreateFeedResponse createFeed(String title,String author,Calendar expiredTime,String xpathFilter) throws AxisFault{
		try {
			serviceClient.getOptions().setAction(AtomConstants.Actions.Subscribe);
			
			CreateFeedDocument createFeedDocument = CreateFeedDocument.Factory.newInstance();
			CreateFeed createFeed = createFeedDocument.addNewCreateFeed();
			
			createFeed.setAuthor(author);
			createFeed.setTitle(title);
			
			if(expiredTime != null){
				createFeed.setExpires(expiredTime);	
			}
			if(xpathFilter != null){
				FilterType filter = createFeed.addNewFilter();
				filter.setDialect(XPathBasedFilter.XPATH_BASED_FILTER);
				filter.setStringValue(xpathFilter);
			}
			
			OMElement request = CommonUtil.toOM(createFeedDocument);
			request.build();
			OMElement element = serviceClient.sendReceive(request);
			CreateFeedResponseDocument createFeedResponseDocument = CreateFeedResponseDocument.Factory.parse(element.getXMLStreamReader());
			System.out.println(createFeedDocument.xmlText());

			//read epr for subscription from response and store it
			OMElement responseAsOM = CommonUtil.toOM(createFeedResponseDocument);
			OMElement eprAsOM = responseAsOM.getFirstChildWithName(new QName(AtomConstants.ATOM_MSG_NAMESPACE,"SubscriptionManager"));
			
			feedEpr = new EndpointReference(eprAsOM.getFirstElement().getText());
			OMElement referanceParameters = eprAsOM.getFirstChildWithName(new QName(eprAsOM.getFirstElement().getNamespace().getNamespaceURI(),
					AddressingConstants.EPR_REFERENCE_PARAMETERS));
			Iterator refparams = referanceParameters.getChildElements();
			while(refparams.hasNext()){
				feedEpr.addReferenceParameter((OMElement)refparams.next());	
			}
			
			return createFeedResponseDocument.getCreateFeedResponse();
		} catch (XmlException e) {
			throw new AxisFault(e);
		} 
	}
	
	public void deleteFeed(EndpointReference epr)throws AxisFault{
		serviceClient.getOptions().setAction(AtomConstants.Actions.Unsubscribe);
		serviceClient.getOptions().setTo(epr);
		
		OMElement request = OMAbstractFactory.getOMFactory().createOMElement(new QName(AtomConstants.ATOM_MSG_NAMESPACE,"DeleteFeed"));
		serviceClient.sendReceive(request);
	}
	
	
	public void deleteFeed()throws AxisFault{
		if(feedEpr != null){
			deleteFeed(feedEpr);
		}else{
			throw new AxisFault("No feed epr alreday stored, you must have create a feed using same AtomEventingClient Object");
		}
	}
}
