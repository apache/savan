package org.apache.savan.atom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.util.CommonUtil;

public class AtomMessageReceiver implements MessageReceiver{
	
	public static final String ATOM_NAME = "atom";

	public void receive(MessageContext messageCtx) throws AxisFault {
		
		try {
			//String resourcePath = messageCtx.getTo().getAddress();
			//http://127.0.0.1:5555/axis2/services/PublisherService/atom?a=urn_uuid_96C2CB953DABC98DFC1179904343537.atom

			
			
			HttpServletRequest request = (HttpServletRequest)messageCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
			if(request == null || HTTPConstants.HEADER_GET.equals(request.getMethod()) || HTTPConstants.HEADER_POST.equals(request.getMethod())){
				SOAPEnvelope envlope = messageCtx.getEnvelope();
				OMElement bodyContent = envlope.getBody().getFirstElement();
				
				OMElement feedID = bodyContent.getFirstElement();
				String pathWRTRepository = "atom/"+feedID.getText();
				
				File atomFile = messageCtx.getConfigurationContext().getRealPath(pathWRTRepository);
				if(pathWRTRepository.equals("atom/all.atom") && !atomFile.exists()){
					AtomSubscriber atomSubscriber = new AtomSubscriber();
					atomSubscriber.setId(new URI("All"));
					atomSubscriber.setAtomFile(atomFile);
					atomSubscriber.setAuthor("DefaultUser");
					atomSubscriber.setTitle("default Feed");
					
					String serviceAddress = messageCtx.getTo().getAddress();
					int cutIndex = serviceAddress.indexOf("services");
					if(cutIndex > 0){
						serviceAddress = serviceAddress.substring(0,cutIndex-1);
					}
					atomSubscriber.setFeedUrl(serviceAddress+"/services/"+messageCtx.getServiceContext().getAxisService().getName() +"/atom?feed=all.atom");
					
					
					SubscriberStore store = CommonUtil.getSubscriberStore(messageCtx.getAxisService());
					if (store == null)
						throw new AxisFault ("Cant find the Savan subscriber store");
					store.store(atomSubscriber);
				}

				
				if(!atomFile.exists()){
					throw new AxisFault("no feed exisits for "+feedID.getText() + " no file found "+ atomFile.getAbsolutePath());
				}
				FileInputStream atomIn =  new FileInputStream(atomFile);

	            SOAPFactory fac = getSOAPFactory(messageCtx);
	            SOAPEnvelope envelope = fac.getDefaultEnvelope();
	            
	            //add the content of the file to the response
	            XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader
		            (atomIn, MessageContext.DEFAULT_CHAR_SET_ENCODING);
		        StAXBuilder builder = new StAXOMBuilder(fac,xmlreader);
		        OMElement result = (OMElement) builder.getDocumentElement();
	            envelope.getBody().addChild(result);
				
	            //send beck the response
				 MessageContext outMsgContext = MessageContextBuilder.createOutMessageContext(messageCtx);
				 outMsgContext.getOperationContext().addMessageContext(outMsgContext);
				 outMsgContext.setEnvelope(envelope);
				 
				 AxisEngine engine =
				        new AxisEngine(
				        		outMsgContext.getConfigurationContext());
				engine.send(outMsgContext);

			}else if(HTTPConstants.HEADER_POST.equals(request.getMethod())){
				SOAPEnvelope envlope = messageCtx.getEnvelope();
				OMElement bodyContent = envlope.getBody().getFirstElement();
				
				OMElement feedID = bodyContent.getFirstElement();
				String pathWRTRepository = "atom/"+feedID.getText();

				//remove the file
				File atomFile = messageCtx.getConfigurationContext().getRealPath(pathWRTRepository);
				atomFile.delete();
				
				//remove the feed from subscriber store
				String feedIDAsUrn = feedID.getText().replaceAll("_", ":");
				SubscriberStore store = CommonUtil.getSubscriberStore(messageCtx.getAxisService());
				if (store == null)
					throw new AxisFault ("Cant find the Savan subscriber store");
				store.delete(feedIDAsUrn);
			}
			
		} catch (SOAPProcessingException e) {
			e.printStackTrace();
			throw new AxisFault(e);
			
		} catch (OMException e) {
			e.printStackTrace();
			throw new AxisFault(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new AxisFault(e);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new AxisFault(e);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new AxisFault(e);
		}
	}
	
	 public SOAPFactory getSOAPFactory(MessageContext msgContext) throws AxisFault {
	        String nsURI = msgContext.getEnvelope().getNamespace().getNamespaceURI();
	        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
	            return OMAbstractFactory.getSOAP12Factory();
	        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
	            return OMAbstractFactory.getSOAP11Factory();
	        } else {
	            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
	        }
	    }

}
