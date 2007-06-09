package org.apache.savan.atom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.savan.SavanException;
import org.apache.savan.filters.Filter;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.subscription.ExpirationBean;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

public class AtomSubscriber implements Subscriber{
	private static final Log log = LogFactory.getLog(AtomSubscriber.class);
	private Date subscriptionEndingTime = null;
	//private Feed feed; 
	private Filter filter = null;
	private File atomFile;
	private String feedUrl;
	private AtomDataSource atomDataSource;
	private URI id;
	
	public void init(AtomDataSource dataSource,URI id,String title,String author) throws SavanException{
		this.atomDataSource = dataSource;
		atomDataSource.addFeed(id.toString(), title, new Date(), author);
	}
	
	
	
	public URI getId() {
		return id;
	}
	public void renewSubscription(ExpirationBean bean) {
		throw new UnsupportedOperationException();
	}
	public void sendEventData(OMElement eventData) throws SavanException {
//		try {
			Date date = new Date ();
			
			boolean expired = false;
			if (subscriptionEndingTime!=null && date.after(subscriptionEndingTime))
				expired = true;
			
			if (expired) {
				String message = "Cant notify the listner since the subscription has been expired";
				log.debug(message);
			}
			
			atomDataSource.addEntry(id.toString(), eventData);
//			
//			if(feed == null){
//				feed = new Feed(title,id.toString(),author);
//			}
//			feed.addEntry(eventData);
//			
//			if(!atomFile.getParentFile().exists()){
//				atomFile.getParentFile().mkdir();
//			}
//			FileOutputStream out = new FileOutputStream(atomFile);
//			feed.write(out);
//			out.close();
//			System.out.println("Atom file at "+ atomFile + " is updated");
//		} catch (FileNotFoundException e) {
//			throw new SavanException(e);
//		} catch (XMLStreamException e) {
//			throw new SavanException(e);
//		} catch (IOException e) {
//			throw new SavanException(e);
//		}
	}
	public void setId(URI id) {
		this.id = id;
	}
	
	
	
	


	public void setSubscriptionEndingTime(Date subscriptionEndingTime) {
		this.subscriptionEndingTime = subscriptionEndingTime;
	}

	public void setEndToEPr(EndpointReferenceType endToEPR) {
		throw new UnsupportedOperationException();
	}

//	public String getAuthor() {
//		return author;
//	}
//
//	public void setAuthor(String author) {
//		this.author = author;
//	}
//
//
//	public String getTitle() {
//		return title;
//	}

//	public void setTitle(String title) {
//		this.title = title;
//	}
	
	
	public String getFeedUrl(){
		return feedUrl;
	}

	public Date getSubscriptionEndingTime() {
		return subscriptionEndingTime;
	}
	public Filter getFilter() {
		return filter;
	}
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	public void setAtomFile(File atomFile) {
		this.atomFile = atomFile;
	}
	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}
//	public Feed getFeed() {
//		return feed;
//	}
	public OMElement getFeedAsXml() throws SavanException {
		return atomDataSource.getFeedAsXml(id.toString());
	}
//	public void setAtomDataSource(AtomDataSource atomDataSource) {
//		this.atomDataSource = atomDataSource;
//	}
	
	
}
