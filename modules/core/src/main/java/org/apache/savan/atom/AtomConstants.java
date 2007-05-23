
package org.apache.savan.atom;

public class AtomConstants {
	public static String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	public static String ATOM_PREFIX = "atom";
	
	public static String ATOM_MSG_NAMESPACE = "http://wso2.com/eventing/atom/";
	
	public static String ENDTO_ELEMENT = "EndTo";
	public static String EXPIRES_ELEMENT = "Expires";
	public static String FILTER_ELEMENT = "Filter";
	public static String TITLE_ELEMENT = "title";
	public static String ID_ELEMENT = "id";
	public static String AUTHOR_ELEMENT = "author";
	public static String DIALECT_ELEMENT = "Dialect";
	
	public static String RENEW_FEED = "renewFeed";
	public static String IDEDNTIFIER_ELEMENT = "Identifier";
	
	public static String DEFAULT_FILTER_IDENTIFIER = FilterDialects.XPath;
	
	interface FilterDialects {
		String XPath = "http://www.w3.org/TR/1999/REC-xpath-19991116";
	}
	
	
	interface Actions {
		String Subscribe = "http://wso2.com/eventing/Subscribe";
		String SubscribeResponse = "http://wso2.com/eventing/SubscribeResponse";
		String Renew = "http://wso2.com/eventing/Renew";
		String RenewResponse = "http://wso2.com/eventing/RenewResponse";
		String Unsubscribe = "http://wso2.com/eventing/Unsubscribe";
		String UnsubscribeResponse = "http://wso2.com/eventing/UnsubscribeResponse";
		String GetStatus = "http://wso2.com/eventing/GetStatus";
		String GetStatusResponse = "http://wso2.com/eventing/GetStatusResponse";
	}
	
	interface TransferedProperties {
		String SUBSCRIBER_UUID = "SAVAN_EVENTING_SUBSCRIBER_UUID";
	}
	
	interface ElementNames {
		String Subscribe = "Subscribe";
		String EndTo = "EndTo";
		String Delivery = "Delivery";
		String Mode = "Mode";
		String NotifyTo = "NotifyTo";
		String Expires = "Expires";
		String Filter = "Filter";
		String Dialect = "Dialect";
		String SubscribeResponse = "SubscribeResponse";
		String SubscriptionManager = "SubscriptionManager";
		String Renew = "Renew";
		String RenewResponse = "RenewResponse";
		String Identifier = "Identifier";
		String Unsubscribe = "Unsubscribe";
		String GetStatus = "GetStatus";
		String GetStatusResponse = "GetStatusResponse";
		String FeedUrl = "FeedUrl";
		
		String deleteFeedResponse = "DeleteFeedResponse";
	}
	
	interface Properties {
		String SOAPVersion = "SOAPVersion";
		String feedUrl = "feedUrl";
	}
	
}
