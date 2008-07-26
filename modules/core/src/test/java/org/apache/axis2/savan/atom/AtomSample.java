package org.apache.axis2.savan.atom;

import com.wso2.eventing.atom.CreateFeedResponseDocument.CreateFeedResponse;
import org.apache.axiom.om.*;
import org.apache.savan.atom.AtomEventingClient;

public class AtomSample {


    /**
     * To run the sample <ol> <li>Install Axis2 with addressing Module</li> <li>Install some service,
     * engage Savan with that service</li> <li>Set up Axis2 client repository in client machine with
     * addressing module</li> <li>Run the sample with http://serviceHost:servicePort/services/<Service-Name>
     * and <client-repostiory-location></li> </ol>
     * <p/>
     * Samples shows how to Create,Delete, publish to with SOAP/REST, and retrive  a Feed.
     *
     * @param args
     */

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: serviceUrl clientRepository");
        } else {

            try {
                String serviceUrl = args[0];
                AtomEventingClient atomEventingClient = new AtomEventingClient(serviceUrl, args[1]);
                CreateFeedResponse createFeedResponse =
                        atomEventingClient.createFeed("test Title", "Srinath Perera");
                System.out.println(
                        "Created Feed " + createFeedResponse.getFeedUrl() + " Sucessfully");

                //publish to service using SOAP
                atomEventingClient
                        .publishWithSOAP(serviceUrl, getDummyMethodRequestElement(1), null);

                //publish service using REST
                atomEventingClient
                        .publishWithREST(serviceUrl, getDummyMethodRequestElement(2), null);

                //Get the feed using http GET
                OMElement feedAsXml = atomEventingClient.fetchFeed(createFeedResponse.getFeedUrl());
                feedAsXml.serialize(System.out, new OMOutputFormat());

                System.out.println("Fetch Feed using HTTP Get, copy and paste url " +
                                   createFeedResponse.getFeedUrl() +
                                   " in browser to retirve the feed ");
                System.out.println("Press any key to delete the feed");
                System.in.read();
                atomEventingClient.deleteFeed();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static final String applicationNamespaceName = "http://tempuri.org/";
    private static final String dummyMethod = "dummyMethod";

    private static OMElement getDummyMethodRequestElement(int i) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = fac.createOMNamespace(applicationNamespaceName, "ns1");
        OMElement de = fac.createOMElement(dummyMethod, namespace);
        de.setText(String.valueOf(i));
		return de;
	}
}
