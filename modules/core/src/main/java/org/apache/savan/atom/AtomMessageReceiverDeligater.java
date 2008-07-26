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

import com.wso2.eventing.atom.CreateFeedResponseDocument;
import com.wso2.eventing.atom.CreateFeedResponseDocument.CreateFeedResponse;
import com.wso2.eventing.atom.RenewFeedResponseDocument;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.messagereceiver.MessageReceiverDeligater;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.util.CommonUtil;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.Date;


public class AtomMessageReceiverDeligater extends MessageReceiverDeligater {
    private SOAPEnvelope findOrCreateSoapEnvelope(SavanMessageContext subscriptionMessage,
                                                  MessageContext outMessage) throws AxisFault {
        MessageContext subscriptionMsgCtx = subscriptionMessage.getMessageContext();

        SOAPEnvelope outMessageEnvelope = outMessage.getEnvelope();
        SOAPFactory factory = null;

        if (outMessageEnvelope != null) {
            factory = (SOAPFactory)outMessageEnvelope.getOMFactory();
        } else {
            factory = (SOAPFactory)subscriptionMsgCtx.getEnvelope().getOMFactory();
            outMessageEnvelope = factory.getDefaultEnvelope();
            outMessage.setEnvelope(outMessageEnvelope);
        }
        return outMessageEnvelope;
    }


    public void handleSubscriptionRequest(SavanMessageContext subscriptionMessage,
                                          MessageContext outMessage) throws SavanException {
        try {
            if (outMessage != null) {
                MessageContext subscriptionMsgCtx = subscriptionMessage.getMessageContext();

                SOAPEnvelope outMessageEnvelope =
                        findOrCreateSoapEnvelope(subscriptionMessage, outMessage);
                //setting the action
                outMessage.getOptions().setAction(AtomConstants.Actions.SubscribeResponse);

                CreateFeedResponseDocument createFeedResponseDocument =
                        CreateFeedResponseDocument.Factory.newInstance();
                CreateFeedResponse createFeedResponse =
                        createFeedResponseDocument.addNewCreateFeedResponse();
                EndpointReferenceType savenEpr = createFeedResponse.addNewSubscriptionManager();
                savenEpr.addNewAddress()
                        .setStringValue(subscriptionMsgCtx.getOptions().getTo().getAddress());

                String id = (String)subscriptionMessage
                        .getProperty(AtomConstants.TransferedProperties.SUBSCRIBER_UUID);
                if (id != null) {
                    XmlCursor c = savenEpr.addNewReferenceParameters().newCursor();
                    c.toNextToken();
                    addNameValuePair(c, new QName(AtomConstants.ATOM_NAMESPACE,
                                                  AtomConstants.ElementNames.Identifier), id);
                } else {
                    throw new SavanException("Subscription UUID is not set");
                }

//				HttpServletRequest request = (HttpServletRequest) subscriptionMessage.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
//				request.getServerPort()


                createFeedResponse.setFeedUrl(
                        (String)subscriptionMessage.getProperty(AtomConstants.Properties.feedUrl));
                outMessageEnvelope.getBody().addChild(CommonUtil.toOM(createFeedResponseDocument));
            } else {
                throw new SavanException(
                        "Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");
            }
            //setting the message type
            outMessage.setProperty(SavanConstants.MESSAGE_TYPE, new Integer(
                    SavanConstants.MessageTypes.SUBSCRIPTION_RESPONSE_MESSAGE));
        } catch (SOAPProcessingException e) {
            throw new SavanException(e);
        } catch (AxisFault e) {
            throw new SavanException(e);
        } catch (OMException e) {
            throw new SavanException(e);
        }
    }

    public void handleRenewRequest(SavanMessageContext renewMessage, MessageContext outMessage)
            throws SavanException {

        try {
            if (outMessage == null)
                throw new SavanException(
                        "Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");

            SOAPEnvelope outMessageEnvelope = findOrCreateSoapEnvelope(renewMessage, outMessage);
            RenewFeedResponseDocument renewFeedResponseDocument =
                    RenewFeedResponseDocument.Factory.newInstance();

            //setting the action
            outMessage.getOptions().setAction(AtomConstants.Actions.RenewResponse);
            String subscriberID = (String)renewMessage
                    .getProperty(AtomConstants.TransferedProperties.SUBSCRIBER_UUID);
            if (subscriberID == null) {
                String message = "SubscriberID TransferedProperty is not set";
                throw new SavanException(message);
            }

            SubscriberStore store = CommonUtil
                    .getSubscriberStore(renewMessage.getMessageContext().getAxisService());
            Subscriber subscriber = store.retrieve(subscriberID);
            AtomSubscriber atomSubscriber = (AtomSubscriber)subscriber;
            if (atomSubscriber == null) {
                String message = "Cannot find the AbstractSubscriber with the given ID";
                throw new SavanException(message);
            }

            Date expiration = atomSubscriber.getSubscriptionEndingTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expiration);
            renewFeedResponseDocument.addNewRenewFeedResponse().setExpires(calendar);
            outMessageEnvelope.getBody().addChild(CommonUtil.toOM(renewFeedResponseDocument));
            //setting the message type
            outMessage.setProperty(SavanConstants.MESSAGE_TYPE,
                                   new Integer(SavanConstants.MessageTypes.RENEW_RESPONSE_MESSAGE));
        } catch (AxisFault e) {
            throw new SavanException(e);
        }
    }

    public void handleEndSubscriptionRequest(SavanMessageContext renewMessage,
                                             MessageContext outMessage) throws SavanException {
        try {
            if (outMessage == null)
                throw new SavanException(
                        "Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");
            //setting the action
            outMessage.getOptions().setAction(AtomConstants.Actions.UnsubscribeResponse);
            SOAPEnvelope outMessageEnvelope = findOrCreateSoapEnvelope(renewMessage, outMessage);
            outMessageEnvelope.getBody().addChild(OMAbstractFactory.getOMFactory().createOMElement(
                    new QName(AtomConstants.ATOM_MSG_NAMESPACE,
                              AtomConstants.ElementNames.deleteFeedResponse)));
            outMessage.setProperty(SavanConstants.MESSAGE_TYPE, new Integer(
                    SavanConstants.MessageTypes.UNSUBSCRIPTION_RESPONSE_MESSAGE));
        } catch (AxisFault e) {
            throw new SavanException(e);
        } catch (OMException e) {
            throw new SavanException(e);
        }
    }

    public void handleGetStatusRequest(SavanMessageContext getStatusMessage,
                                       MessageContext outMessage) throws SavanException {

        if (outMessage == null)
            throw new SavanException(
                    "Eventing protocol need to sent the SubscriptionResponseMessage. But the outMessage is null");

        MessageContext subscriptionMsgCtx = getStatusMessage.getMessageContext();

        String id = (String)getStatusMessage
                .getProperty(AtomConstants.TransferedProperties.SUBSCRIBER_UUID);
        if (id == null)
            throw new SavanException("Cannot fulfil request. AbstractSubscriber ID not found");

        //setting the action
        outMessage.getOptions().setAction(AtomConstants.Actions.UnsubscribeResponse);

        SOAPEnvelope outMessageEnvelope = outMessage.getEnvelope();
        SOAPFactory factory = null;

        if (outMessageEnvelope != null) {
            factory = (SOAPFactory)outMessageEnvelope.getOMFactory();
        } else {
            factory = (SOAPFactory)subscriptionMsgCtx.getEnvelope().getOMFactory();
            outMessageEnvelope = factory.getDefaultEnvelope();

            try {
                outMessage.setEnvelope(outMessageEnvelope);
            } catch (AxisFault e) {
                throw new SavanException(e);
            }
        }

        SubscriberStore store = CommonUtil
                .getSubscriberStore(getStatusMessage.getMessageContext().getAxisService());


        if (store == null) {
            throw new SavanException("AbstractSubscriber Store was not found");
        }

        AtomSubscriber subscriber = (AtomSubscriber)store.retrieve(id);
        if (subscriber == null) {
            throw new SavanException("AbstractSubscriber not found");
        }

        OMNamespace ens =
                factory.createOMNamespace(AtomConstants.ATOM_NAMESPACE, AtomConstants.ATOM_PREFIX);
        OMElement getStatusResponseElement =
                factory.createOMElement(AtomConstants.ElementNames.GetStatusResponse, ens);

        Date expires = subscriber.getSubscriptionEndingTime();
        if (expires != null) {
            OMElement expiresElement =
                    factory.createOMElement(AtomConstants.ElementNames.Expires, ens);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expires);
            String expirationString = ConverterUtil.convertToString(calendar);
            expiresElement.setText(expirationString);
            getStatusResponseElement.addChild(expiresElement);
        }

        outMessageEnvelope.getBody().addChild(getStatusResponseElement);

        //setting the message type
        outMessage.setProperty(SavanConstants.MESSAGE_TYPE, new Integer(
                SavanConstants.MessageTypes.GET_STATUS_RESPONSE_MESSAGE));
    }

    public static void addNameValuePair(XmlCursor c, QName name, Object value) {
        c.beginElement(name);

        if (value instanceof String) {
            c.insertChars((String)value);
        } else {
            //Make sure this works, code is taken from
            //http://xmlbeans.apache.org/docs/2.0.0/guide/conHandlingAny.html
            XmlCursor cc = ((XmlObject)value).newCursor();
            cc.toFirstContentToken();
            cc.copyXml(c);
            cc.dispose();
        }
        c.toParent();
    }


    public void doProtocolSpecificProcessing(SavanMessageContext inSavanMessage,
                                             MessageContext outMessage) throws SavanException {
        int msgtype = ((Integer)inSavanMessage.getProperty(SavanConstants.MESSAGE_TYPE)).intValue();
        switch (msgtype) {
            case SavanConstants.MessageTypes.SUBSCRIPTION_MESSAGE:
                handleSubscriptionRequest(inSavanMessage, outMessage);
                break;
            case SavanConstants.MessageTypes.RENEW_MESSAGE:
                handleRenewRequest(inSavanMessage, outMessage);
                break;
            case SavanConstants.MessageTypes.UNSUBSCRIPTION_MESSAGE:
                handleEndSubscriptionRequest(inSavanMessage, outMessage);
                break;
            default:
                throw new SavanException("Unknow Message type [" + msgtype + "]");
        }
    }


    public void doProtocolSpecificProcessing(SavanMessageContext inSavanMessage)
            throws SavanException {
        // TODO Auto-generated method stub

    }


}
