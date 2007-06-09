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

//todo

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.savan.atom.AtomEventingClient;
import org.apache.savan.eventing.client.EventingClient;

import com.wso2.eventing.atom.CreateFeedResponseDocument.CreateFeedResponse;

public class AtomTest extends UtilServerBasedTestCase  {

	private static final Log log = LogFactory.getLog(AtomTest.class);
    protected QName transportName = new QName("http://localhost/my",
            "NullTransport");
    private final int MIN_OPTION = 1;
    private final int MAX_OPTION = 9;
    
    private final String SUBSCRIBER_1_ID = "subscriber1";
    private final String SUBSCRIBER_2_ID = "subscriber2";
    
    private final String AXIS2_REPO = "target/repository/";
    
    private ServiceClient serviceClient = null;
    private Options options = null;
    private EventingClient eventingClient = null;
    
    private String toAddressPart = "/axis2/services/PublisherService";
    private String listner1AddressPart = "/axis2/services/ListnerService1";
    private String listner2AddressPart = "/axis2/services/ListnerService2";
    
	private final String applicationNamespaceName = "http://tempuri.org/"; 
	private final String dummyMethod = "dummyMethod";
    
	private static String repo = null;
	private static int port = 5556;
	private static String serverIP = "127.0.0.1";    

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;
    private QName serviceName = new QName("PublisherService");

    protected boolean finish = false;

    public AtomTest() {
        super(AtomTest.class.getName());
    }

    public AtomTest(String testName) {
        super(testName);
    }

    

    protected void setUp() throws Exception {
    	UtilServer.start(AXIS2_REPO);
//        service = Utils.createSimpleService(serviceName,
//        		PublisherService.class.getName(),
//        		new QName("dummyMethod"));
//        service.addModuleref("savan");
//        
//        UtilServer.deployService(service);
        
//        AxisService service1 = Utils.createSimpleService(new QName("ListnerService1"),
//        		SavenTest.class.getName(),
//        		new QName("publish"));
//        UtilServer.deployService(service1);
        
    }

    protected void tearDown() throws Exception {
        //UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }


    public void testAtomSubcription()throws Exception{
    	//Thread.sleep(1000*60*100);
    	ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(AXIS2_REPO,AXIS2_REPO+"/conf/axis2.xml");
		serviceClient = new ServiceClient (configContext,null); //TODO give a repo
		
		options = new Options ();
		serviceClient.setOptions(options);
		serviceClient.engageModule(new QName ("addressing"));
		
		eventingClient = new EventingClient (serviceClient);
		
		String toAddress = "http://" + serverIP + ":" + port + toAddressPart;
		
		//String toAddress = "http://" + serverIP + ":" + port + "/axis2/services/RMSampleService";
		options.setTo(new EndpointReference (toAddress));
		
		
		AtomEventingClient atomEventingClient =  new AtomEventingClient(serviceClient);
		CreateFeedResponse createFeedResponse = atomEventingClient.createFeed("test Title","Srinath Perera");
		
		options.setAction("http://wso2.com/eventing/dummyMethod");
		serviceClient.fireAndForget(getDummyMethodRequestElement (0));
		
//		options.setAction(EventingConstants.Actions.Publish);
//		serviceClient.fireAndForget(getDummyMethodRequestElement ());
		
		atomEventingClient.publishWithSOAP(toAddress, getDummyMethodRequestElement (1), null);
		atomEventingClient.publishWithREST(toAddress, getDummyMethodRequestElement (2), null);
		//Thread.sleep(1000*10*1000);
		
//		int i = 0;
//		while(i<1){
			System.out.println(createFeedResponse.getFeedUrl());
			OMElement feedAsXml = atomEventingClient.fetchFeed(createFeedResponse.getFeedUrl());
			feedAsXml.serialize(System.out,new OMOutputFormat());
			
//			URL url = new URL(createFeedResponse.getFeedUrl());
//			System.out.println(readFromStream(url.openStream()));
//			Thread.sleep(1000*10);	
//			i++;
//		}
//		
		feedAsXml = atomEventingClient.fetchFeed(createFeedResponse.getFeedUrl());
		feedAsXml.serialize(System.out,new OMOutputFormat());	
			
			
		atomEventingClient.deleteFeed();
		
		
    }
    
    
    
//    public void testEvents() throws Exception{
//    	initClient ();
//		performAction (1);
//    }
    
    
    public void publish(OMElement param) throws Exception {
		System.out.println("\n");
		System.out.println("'1' got a new publication...");
		System.out.println(param);
		System.out.println("\n");
	}
    
    
//    private void initClient () throws AxisFault {
//
////		String CLIENT_REPO = null;
////		String AXIS2_XML = null;
////		
////		if (repo!=null) {
////			CLIENT_REPO = repo;
////			AXIS2_XML = repo + File.separator + "axis2.xml";
////		} else {
//////			throw new AxisFault ("Please specify the client repository as a program argument.Use '-h' for help.");
////		}
//		
//		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(AXIS2_REPO,AXIS2_REPO+"/conf/axis2.xml");
//		serviceClient = new ServiceClient (configContext,null); //TODO give a repo
//		
//		options = new Options ();
//		serviceClient.setOptions(options);
//		serviceClient.engageModule(new QName ("addressing"));
//		
//		eventingClient = new EventingClient (serviceClient);
//		
//		String toAddress = "http://" + serverIP + ":" + port + toAddressPart;
//		
//		//String toAddress = "http://" + serverIP + ":" + port + "/axis2/services/RMSampleService";
//		options.setTo(new EndpointReference (toAddress));
//	}
//	
//	private void performAction (int action) throws Exception {
//		
//		switch (action) {
//		case 1:
//			doSubscribe(SUBSCRIBER_1_ID);
//			break;
//		case 2:
//			doSubscribe(SUBSCRIBER_2_ID);
//			break;
//		case 3:
//			doSubscribe(SUBSCRIBER_1_ID);
//			doSubscribe(SUBSCRIBER_2_ID);
//			break;
//		case 4:
//			doUnsubscribe(SUBSCRIBER_1_ID);
//			break;
//		case 5:
//			doUnsubscribe(SUBSCRIBER_2_ID);
//			break;
//		case 6:
//			doUnsubscribe(SUBSCRIBER_1_ID);
//			doUnsubscribe(SUBSCRIBER_2_ID);
//			break;
//		case 7:
//			doGetStatus(SUBSCRIBER_1_ID);
//			break;
//		case 8:
//			doGetStatus(SUBSCRIBER_2_ID);
//			break;
//		case 9:
//			System.exit(0);
//			break;
//		default:
//			break;
//		}
//	}
	
//	private void doSubscribe (String ID) throws Exception {
//		EventingClientBean bean = new EventingClientBean ();
//		
//		String subscribingAddress = null;
//		if (SUBSCRIBER_1_ID.equals(ID)) {
//            subscribingAddress = "http://" + serverIP + ":" + port + listner1AddressPart;
//		} else if (SUBSCRIBER_2_ID.equals(ID)) {
//            subscribingAddress = "http://" + serverIP + ":" + port + listner2AddressPart;
//		}
//	
//		bean.setDeliveryEPR(new EndpointReference (subscribingAddress));
//	
//		//uncomment following to set an expiration time of 10 minutes.
////		Date date = new Date ();
////		date.setMinutes(date.getMinutes()+10);
////		bean.setExpirationTime(date);
//		
//		eventingClient.subscribe(bean,ID);
//		Thread.sleep(1000);   //TODO remove if not sequired
//	}
//	
//	private void doUnsubscribe (String ID) throws Exception {
//		eventingClient.unsubscribe(ID);
//		Thread.sleep(1000);   //TODO remove if not sequired
//	}
//	
//	private void doGetStatus (String ID) throws Exception {
//		SubscriptionStatus status  = eventingClient.getSubscriptionStatus(ID);
//		Thread.sleep(1000);   //TODO remove if not sequired
//		
//		String statusValue = status.getExpirationValue();
//		System.out.println("Status of the subscriber '" + ID +"' is" + statusValue);
//	}
	
	private OMElement getDummyMethodRequestElement(int i) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = fac.createOMNamespace(applicationNamespaceName,"ns1");
		OMElement de =  fac.createOMElement(dummyMethod, namespace);
		de.setText(String.valueOf(i));
		return de;
	}
    
	public static String readFromStream(InputStream in) throws Exception{
    	try {
			StringBuffer wsdlStr = new StringBuffer();
			
			int read;
			
			byte[] buf = new byte[1024];
			while((read = in.read(buf)) > 0){
				wsdlStr.append(new String(buf,0,read));
			}
			in.close();
			return wsdlStr.toString();
		} catch (IOException e) {
			throw new Exception(e);
		}
    }

}
