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

package sample.eventing;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.engine.AxisServer;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.savan.eventing.client.EventingClient;
import org.apache.savan.eventing.client.EventingClientBean;
import org.apache.savan.eventing.client.SubscriptionStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    boolean done = false;

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private final int MIN_OPTION = 1;
    private final int MAX_OPTION = 9;

    private final String SUBSCRIBER_1_ID = "subscriber1";
    private final String SUBSCRIBER_2_ID = "subscriber2";

    private EventingClient eventingClient = null;

    private String toAddressPart = "/axis2/services/PublisherService";
    private String listener1AddressPart = "/axis2/services/ListenerService1";
    private String listener2AddressPart = "/axis2/services/ListenerService2";

    private static String repo = null;
    private static int port = 8080;
    private static String serverIP = "127.0.0.1";

    private static final String portParam = "-p";
    private static final String repoParam = "-r";
    private static final String helpParam = "-h";

    public static void main(String[] args) throws Exception {

        for (String arg : args) {
            if (helpParam.equalsIgnoreCase(arg)) {
                displayHelp();
                System.exit(0);
            }
        }

        String portStr = getParam(portParam, args);
        if (portStr != null) {
            port = Integer.parseInt(portStr);
            System.out.println("Server Port was set to:" + port);
        }

        String repoStr = getParam(repoParam, args);
        if (repoStr != null) {
            repo = repoStr;
            System.out.println("Client Repository was set to:" + repo);
        }

        Client c = new Client();
        c.run();
    }

    private static void displayHelp() {
        System.out.println("Help page for the Eventing Client");
        System.out.println("---------------------------------");
        System.out.println("Set the client reposiory using the parameter -r");
        System.out.println("Set the server port using the parameter -p");
    }

    static void foo() throws Exception {
        ConfigurationContext ctx = ConfigurationContextFactory.createDefaultConfigurationContext();
        SimpleHTTPServer server = new SimpleHTTPServer(ctx, 7071);
        AxisConfiguration axisConfig = ctx.getAxisConfiguration();

//        AxisService service = new AxisService("ListenerService1");
//        svc.addParameter("ServiceClass", ListenerService1.class.getName());

        AxisService service = AxisService.createService(ListenerService1.class.getName(),
                                                        axisConfig);
        axisConfig.addService(service);
        server.start();
    }

    /**
     * This will check the given parameter in the array and will return, if available
     *
     * @param param
     * @param args
     * @return
     */
    private static String getParam(String param, String[] args) {
        if (param == null || "".equals(param)) {
            return null;
        }

        for (int i = 0; i < args.length; i = i + 2) {
            String arg = args[i];
            if (param.equalsIgnoreCase(arg) && (args.length >= (i + 1))) {
                return args[i + 1];
            }
        }
        return null;
    }

    public void run() throws Exception {

        System.out.println("\n");
        System.out.println("Welcome to Axis2 Eventing Sample");
        System.out.println("================================\n");

        foo();
        
        boolean validOptionSelected = false;
        int selectedOption = -1;
        while (!validOptionSelected) {
            displayMenu();
            selectedOption = getIntInput();
            if (selectedOption >= MIN_OPTION && selectedOption <= MAX_OPTION)
                validOptionSelected = true;
            else
                System.out.println("\nInvalid Option \n\n");
        }

        initClient();
        performAction(selectedOption);

        //TODO publish

//        System.out.println("Press enter to initialize the publisher service.");
//        reader.readLine();
//
//        options.setAction("uuid:DummyMethodAction");
//        serviceClient.fireAndForget(getDummyMethodRequestElement());

        while (!done) {
            validOptionSelected = false;
            selectedOption = -1;
            while (!validOptionSelected) {
                displayMenu();
                selectedOption = getIntInput();
                if (selectedOption >= MIN_OPTION && selectedOption <= MAX_OPTION)
                    validOptionSelected = true;
                else
                    System.out.println("\nInvalid Option \n\n");
            }

            performAction(selectedOption);

        }
    }

    private void displayMenu() {
        System.out.println("Press 1 to subscribe Listener Service 1");
        System.out.println("Press 2 to subscribe Listener Service 2");
        System.out.println("Press 3 to subscribe both listener services");
        System.out.println("Press 4 to unsubscribe Listener Service 1");
        System.out.println("Press 5 to unsubscribe Listener Service 2");
        System.out.println("Press 6 to unsubscribe both listener services");
        System.out.println("Press 7 to to get the status of the subscription to Service 1");
        System.out.println("Press 8 to to get the status of the subscription to Service 2");
        System.out.println("Press 9 to Exit");
    }

    private int getIntInput() throws IOException {
        String option = reader.readLine();
        try {
            return Integer.parseInt(option);
        } catch (NumberFormatException e) {
            //invalid option
            return -1;
        }
    }

    private void initClient() throws AxisFault {
        String CLIENT_REPO = null;

        if (repo != null) {
            CLIENT_REPO = repo;
        } else {
//			throw new AxisFault ("Please specify the client repository as a program argument.Use '-h' for help.");
        }

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(CLIENT_REPO, null);
        ServiceClient serviceClient = new ServiceClient(configContext, null);

        Options options = new Options();
        serviceClient.setOptions(options);
        serviceClient.engageModule("addressing");

        eventingClient = new EventingClient(serviceClient);

        String toAddress = "http://" + serverIP + ":" + port + toAddressPart;
        options.setTo(new EndpointReference(toAddress));
    }

    private void performAction(int action) throws Exception {

        switch (action) {
            case 1:
                doSubscribe(SUBSCRIBER_1_ID);
                break;
            case 2:
                doSubscribe(SUBSCRIBER_2_ID);
                break;
            case 3:
                doSubscribe(SUBSCRIBER_1_ID);
                doSubscribe(SUBSCRIBER_2_ID);
                break;
            case 4:
                doUnsubscribe(SUBSCRIBER_1_ID);
                break;
            case 5:
                doUnsubscribe(SUBSCRIBER_2_ID);
                break;
            case 6:
                doUnsubscribe(SUBSCRIBER_1_ID);
                doUnsubscribe(SUBSCRIBER_2_ID);
                break;
            case 7:
                doGetStatus(SUBSCRIBER_1_ID);
                break;
            case 8:
                doGetStatus(SUBSCRIBER_2_ID);
                break;
            case 9:
                done = true;
                break;
            default:
                break;
        }
    }

    private void doSubscribe(String ID) throws Exception {
        EventingClientBean bean = new EventingClientBean();

        String subscribingAddress = null;
        if (SUBSCRIBER_1_ID.equals(ID)) {
            subscribingAddress = "http://" + serverIP + ":" + 7070 + listener1AddressPart;
        } else if (SUBSCRIBER_2_ID.equals(ID)) {
            subscribingAddress = "http://" + serverIP + ":" + port + listener2AddressPart;
        }

        bean.setDeliveryEPR(new EndpointReference(subscribingAddress));

        //uncomment following to set an expiration time of 10 minutes.
//		Date date = new Date ();
//		date.setMinutes(date.getMinutes()+10);
//		bean.setExpirationTime(date);

        eventingClient.subscribe(bean, ID);
        Thread.sleep(1000);   //TODO remove if not sequired
    }

    private void doUnsubscribe(String ID) throws Exception {
        eventingClient.unsubscribe(ID);
        Thread.sleep(1000);   //TODO remove if not sequired
    }

    private void doGetStatus(String ID) throws Exception {
        SubscriptionStatus status = eventingClient.getSubscriptionStatus(ID);
        Thread.sleep(1000);   //TODO remove if not sequired

        String statusValue = status.getExpirationValue();
        System.out.println("Status of the subscriber '" + ID + "' is" + statusValue);
    }

//    private OMElement getDummyMethodRequestElement() {
//        OMFactory fac = OMAbstractFactory.getOMFactory();
//        OMNamespace namespace = fac.createOMNamespace(applicationNamespaceName, "ns1");
//        return fac.createOMElement(dummyMethod, namespace);
//    }

}
