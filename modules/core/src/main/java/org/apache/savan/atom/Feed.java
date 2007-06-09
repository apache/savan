package org.apache.savan.atom;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

public class Feed {
	private String title;
	private String id;
	private Date lastUpdated;
	private String author;
	private ArrayList entries;
	private OMDocument document;
	private int entryCount;
	private OMFactory factory;
	private OMNamespace atomNs;
	
//	<feed xmlns="http://www.w3.org/2005/Atom">
//	  <id>http://www.example.org/myfeed</id>
//	  <title>My Podcast Feed</title>
//	  <updated>2005-07-15T12:00:00Z</updated>
//	  <author>
//	    <name>James M Snell</name>
//	  </author>
//	  <link href="http://example.org" />
//	  <link rel="self" href="http://example.org/myfeed" />
//	  <entry>
//	    <id>http://www.example.org/entries/1</id>
//	    <title>Atom 1.0</title>
//	    <updated>2005-07-15T12:00:00Z</updated>
//	    <link href="http://www.example.org/entries/1" />
//	    <summary>An overview of Atom 1.0</summary>
//	    <link rel="enclosure" 
//	          type="audio/mpeg"
//	          title="MP3"
//	          href="http://www.example.org/myaudiofile.mp3"
//	          length="1234" />
//	    <link rel="enclosure"
//	          type="application/x-bittorrent"
//	          title="BitTorrent"
//	          href="http://www.example.org/myaudiofile.torrent"
//	          length="1234" />
//	    <content type="xml">
//	      ..
//	    </content>
//	  </entry>
//	</feed>
	
	
	public Feed(String title, String id, String author,Date lastUpdated) {
		this.title = title;
		if(title != null){
			title = title.trim();
		}
		if(author != null){
			author = author.trim();
		}
		
		this.id = id;
		this.author = author;
		if(lastUpdated == null){
			lastUpdated = new Date();	
		}
		factory = OMAbstractFactory.getOMFactory();
		document = factory.createOMDocument();
		atomNs = factory.createOMNamespace(AtomConstants.ATOM_NAMESPACE,AtomConstants.ATOM_PREFIX);
		OMElement feedEle = factory.createOMElement("feed",atomNs,document);
		
		factory.createOMElement("id",atomNs,feedEle).setText(id);
		if(title != null){
			factory.createOMElement("title",atomNs,feedEle).setText(title);	
		}
		factory.createOMElement("updated",atomNs,feedEle).setText( new SimpleDateFormat("dd-mm-yy'T1'HH:MM:ssZ").format(lastUpdated));
		if(author != null){
			OMElement authorEle = factory.createOMElement("author",atomNs,feedEle);
			factory.createOMElement("name",atomNs,authorEle).setText(author);
		}
	}
	public void addEntry(OMElement entry){
		entryCount++;
		lastUpdated = new Date();
		OMElement entryEle = factory.createOMElement("entry",atomNs,document.getOMDocumentElement());
		factory.createOMElement("id",atomNs,entryEle).setText(id +"/" + entryCount);
		factory.createOMElement("title",atomNs,entryEle).setText("entry" + entryCount);
		
		factory.createOMElement("updated",atomNs,entryEle).setText( new SimpleDateFormat("dd-mm-yy'T1'HH:MM:ssZ").format(lastUpdated));
		
		OMElement contentEle =  factory.createOMElement("content",atomNs,entryEle);
		contentEle.addAttribute("type","text/xml",null);
		contentEle.addChild(entry);
		
		
		
		document.getOMDocumentElement().addChild(entryEle);
	}
	
	public void write(OutputStream out) throws XMLStreamException{
		document.serialize(out);
	}
	
//	public static void main(String[] args) throws Exception{
//		Feed feed = new Feed("testtitle","test_id","john");
//		StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream("<foo>bar</foo>".getBytes()));
//		feed.addEntry(builder.getDocumentElement());
//		feed.write(System.out);
//		System.out.flush();
//		
//	}
	
	public OMElement getFeedAsXml(){
		return document.getOMDocumentElement();
	}
	public OMFactory getFactory() {
		return factory;
	}
	
}
