package org.apache.savan.messagereceiver;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.savan.publication.client.PublicationClient;

public class PublishingMessageReceiver implements MessageReceiver{
	public void receive(MessageContext messageCtx) throws AxisFault {
		SOAPEnvelope requestEnvelope = messageCtx.getEnvelope();
		ServiceContext serviceContext = messageCtx.getServiceContext();
		PublicationClient client = new PublicationClient(serviceContext.getConfigurationContext());
		client.sendPublication(requestEnvelope.getBody().getFirstElement(),serviceContext.getAxisService(),null);
	}
}
