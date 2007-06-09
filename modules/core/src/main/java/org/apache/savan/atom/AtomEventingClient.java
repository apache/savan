package org.apache.savan.atom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.http.HttpStatus;
import org.apache.savan.SavanException;
import org.apache.savan.eventing.EventingConstants;
import org.apache.savan.filters.XPathBasedFilter;
import org.apache.savan.util.CommonUtil;
import org.apache.xmlbeans.XmlException;

import com.wso2.eventing.atom.CreateFeedDocument;
import com.wso2.eventing.atom.CreateFeedResponseDocument;
import com.wso2.eventing.atom.FilterType;
import com.wso2.eventing.atom.CreateFeedDocument.CreateFeed;
import com.wso2.eventing.atom.CreateFeedResponseDocument.CreateFeedResponse;

/**
 * This class take provide client interface for Savan atom support
 * 
 * @author Srinath Perera(hemapani@apache.org)
 * 
 */
public class AtomEventingClient {
	private ServiceClient serviceClient = null;

	private EndpointReference feedEpr;

	public AtomEventingClient(ServiceClient serviceClient) {
		this.serviceClient = serviceClient;
	}

	public CreateFeedResponse createFeed(String title, String author)
			throws AxisFault {
		return createFeed(title, author, null, null);
	}

	public CreateFeedResponse createFeed(String title, String author,
			Calendar expiredTime, String xpathFilter) throws AxisFault {
		try {
			serviceClient.getOptions().setAction(
					AtomConstants.Actions.Subscribe);

			CreateFeedDocument createFeedDocument = CreateFeedDocument.Factory
					.newInstance();
			CreateFeed createFeed = createFeedDocument.addNewCreateFeed();

			createFeed.setAuthor(author);
			createFeed.setTitle(title);

			if (expiredTime != null) {
				createFeed.setExpires(expiredTime);
			}
			if (xpathFilter != null) {
				FilterType filter = createFeed.addNewFilter();
				filter.setDialect(XPathBasedFilter.XPATH_BASED_FILTER);
				filter.setStringValue(xpathFilter);
			}

			OMElement request = CommonUtil.toOM(createFeedDocument);
			request.build();
			OMElement element = serviceClient.sendReceive(request);
			CreateFeedResponseDocument createFeedResponseDocument = CreateFeedResponseDocument.Factory
					.parse(element.getXMLStreamReader());
			System.out.println(createFeedDocument.xmlText());

			// read epr for subscription from response and store it
			OMElement responseAsOM = CommonUtil
					.toOM(createFeedResponseDocument);
			OMElement eprAsOM = responseAsOM.getFirstChildWithName(new QName(
					AtomConstants.ATOM_MSG_NAMESPACE, "SubscriptionManager"));

			feedEpr = new EndpointReference(eprAsOM.getFirstElement().getText());
			OMElement referanceParameters = eprAsOM
					.getFirstChildWithName(new QName(eprAsOM.getFirstElement()
							.getNamespace().getNamespaceURI(),
							AddressingConstants.EPR_REFERENCE_PARAMETERS));
			Iterator refparams = referanceParameters.getChildElements();
			while (refparams.hasNext()) {
				feedEpr.addReferenceParameter((OMElement) refparams.next());
			}

			return createFeedResponseDocument.getCreateFeedResponse();
		} catch (XmlException e) {
			throw new AxisFault(e);
		}
	}

	public void deleteFeed(EndpointReference epr) throws AxisFault {
		serviceClient.getOptions().setAction(AtomConstants.Actions.Unsubscribe);
		serviceClient.getOptions().setTo(epr);

		OMElement request = OMAbstractFactory.getOMFactory().createOMElement(
				new QName(AtomConstants.ATOM_MSG_NAMESPACE, "DeleteFeed"));
		serviceClient.sendReceive(request);
	}

	public void deleteFeed() throws AxisFault {
		if (feedEpr != null) {
			deleteFeed(feedEpr);
		} else {
			throw new AxisFault(
					"No feed epr alreday stored, you must have create a feed using same AtomEventingClient Object");
		}
	}

	public OMElement fetchFeed(String url) throws SavanException {
		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();

		// Create a method instance.
		GetMethod method = new GetMethod(url);

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new SavanException("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			byte[] responseBody = method.getResponseBody();

			StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(
					responseBody));
			return builder.getDocumentElement();
		} catch (IOException e) {
			throw new SavanException(e);
		} catch (XMLStreamException e) {
			throw new SavanException(e);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
	}

	public void publishWithREST(String serviceurl, final OMElement content,String topic)
			throws SavanException {
		// Create an instance of HttpClient.
		HttpClient client = new HttpClient();

		StringBuffer queryUrl = new StringBuffer(serviceurl);
		
		if(!serviceurl.endsWith("/")){
			queryUrl.append("/");
		}
		queryUrl.append("publish");
		if(topic != null ){
			queryUrl.append("?").append(EventingConstants.ElementNames.Topic).append("=").append(topic);	
		}
		PostMethod method = new PostMethod(queryUrl.toString());
		// Request content will be retrieved directly
		// from the input stream
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			content.serialize(out);
			out.flush();
			final byte[] data = out.toByteArray();

			RequestEntity entity = new RequestEntity() {

				public void writeRequest(OutputStream outstream)
						throws IOException {
					outstream.write(data);
				}

				public boolean isRepeatable() {
					return false;
				}

				public String getContentType() {
					return "text/xml";
				}

				public long getContentLength() {
					return data.length;
				}

			};
			method.setRequestEntity(entity);

			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_ACCEPTED) {
				throw new SavanException("Method failed: " + method.getStatusLine());
			}

		} catch (IOException e) {
			throw new SavanException(e);
		} catch (XMLStreamException e) {
			throw new SavanException(e);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
	}
	
	public void publishWithSOAP(String serviceurl, final OMElement content,String topic) throws SavanException{
		try {
			Options options = serviceClient.getOptions();
			EndpointReference to = new EndpointReference(serviceurl);
			if(topic != null){
				to.addReferenceParameter(new QName(EventingConstants.EXTENDED_EVENTING_NAMESPACE,
						EventingConstants.ElementNames.Topic), topic);
			}
			options.setAction(EventingConstants.Actions.Publish);
			serviceClient.fireAndForget(content);
		} catch (AxisFault e) {
			throw new SavanException(e);
		}
	}
	

}
