package org.apache.savan.messagereceiver;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.savan.atom.AtomConstants;
import org.apache.savan.eventing.EventingConstants;
import org.apache.savan.publication.client.PublicationClient;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * This Message reciver handles the publish requests. It will received all messages sent to SOAP/WS
 * action http://ws.apache.org/ws/2007/05/eventing-extended/Publish, or request URL
 * http://<host>:port//services/<service-name>/publish. It will search for topic in URL query
 * parameter "topic" or Soap Header <eevt::topic xmlns="http://ws.apache.org/ws/2007/05/eventing-extended">...</topic>
 *
 * @author Srinath Perera (hemapani@apache.org)
 */
public class PublishingMessageReceiver implements MessageReceiver {

    public void receive(MessageContext messageCtx) throws AxisFault {
        try {
            String toAddress = messageCtx.getTo().getAddress();
            //Here we try to locate the topic. It can be either a query parameter of the input address or a header
            //in the SOAP evvelope
            URI topic = null;

            SOAPEnvelope requestEnvelope = messageCtx.getEnvelope();
            int querySeperatorIndex = toAddress.indexOf('?');
            if (querySeperatorIndex > 0) {
                String queryString = toAddress.substring(querySeperatorIndex + 1);
                HashMap map = new HashMap();
                StringTokenizer t = new StringTokenizer(queryString, "=&");
                while (t.hasMoreTokens()) {
                    map.put(t.nextToken(), t.nextToken());
                }
                if (map.containsKey(EventingConstants.ElementNames.Topic)) {
                    topic = new URI((String)map.get(EventingConstants.ElementNames.Topic));
                }
            } else {
                OMElement topicHeader = requestEnvelope.getHeader().getFirstChildWithName(
                        new QName(EventingConstants.EXTENDED_EVENTING_NAMESPACE,
                                  EventingConstants.ElementNames.Topic));
                if (topicHeader != null) {
                    topic = new URI(topicHeader.getText());
                }
            }

            //Here we locate the content of the Event. If this is APP we unwrap APP wrapping elements.
            OMElement eventData = requestEnvelope.getBody().getFirstElement();
            if (AtomConstants.ATOM_NAMESPACE.equals(eventData.getNamespace().getNamespaceURI()) &&
                AtomConstants.ElementNames.Entry.equals(eventData.getLocalName())) {
                OMElement content = eventData.getFirstChildWithName(new QName(
                        AtomConstants.ATOM_NAMESPACE, AtomConstants.ElementNames.Content));
                if (content != null && content.getFirstElement() != null) {
                    eventData.getFirstElement();
                }
            }
            //Use in memory API to publish the event
            ServiceContext serviceContext = messageCtx.getServiceContext();
            PublicationClient client =
                    new PublicationClient(serviceContext.getConfigurationContext());
            client.sendPublication(eventData, serviceContext.getAxisService(), topic);
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (URISyntaxException e) {
            throw AxisFault.makeFault(e);
        }
    }
}
