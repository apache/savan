package org.apache.savan.atom;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanException;

public class AtomDataSource {
	public static final String SQL_CREATE_FEEDS = "CREATE TABLE FEEDS(id CHAR(250) NOT NULL, " +
			"title CHAR(250), updated TIMESTAMP, author CHAR(250), PRIMARY KEY(id))";
	public static final String SQL_CREATE_ENTRIES = "CREATE TABLE ENTIES(feed CHAR(250), content VARCHAR(2000))";

	public static final String SQL_ADD_FEED = "INSERT INTO FEEDS(id,title, updated,author) VALUES(?,?,?,?)";
	public static final String SQL_ADD_ENTRY = "INSERT INTO ENTIES(feed, content) VALUES(?,?)";
	public static final String SQL_GET_ENTRIES_4_FEED = "SELECT content from ENTIES WHERE feed=?";
	public static final String SQL_GET_FEED_DATA = "SELECT id,title,updated,author from FEEDS WHERE id=?";

	
	
	
	public String framework = "embedded";
    public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    public String protocol = "jdbc:derby:";
    private Properties props;
	
	public AtomDataSource() throws SavanException{
		try {
			Class.forName(driver).newInstance();
			System.out.println("Loaded the appropriate driver.");

			props = new Properties();
			props.put("user", "user1");
			props.put("password", "user1");
			
			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			
			ResultSet feedtable = connection.getMetaData().getTables(null, null, "FEEDS", null);
			if(!feedtable.next()){
				statement.execute(SQL_CREATE_FEEDS);	
			}
			ResultSet entirestable = connection.getMetaData().getTables(null, null, "ENTIES", null);
			if(!entirestable.next()){
				statement.execute(SQL_CREATE_ENTRIES);
			}
			connection.close();
		} catch (InstantiationException e) {
			throw new SavanException(e);
		} catch (IllegalAccessException e) {
			throw new SavanException(e);
		} catch (ClassNotFoundException e) {
			throw new SavanException(e);
		} catch (SQLException e) {
			throw new SavanException(e);
		}

	}
	
	public Connection getConnection() throws SavanException{
        try {
		/*
		    The connection specifies create=true to cause
		    the database to be created. To remove the database,
		    remove the directory derbyDB and its contents.
		    The directory derbyDB will be created under
		    the directory that the system property
		    derby.system.home points to, or the current
		    directory if derby.system.home is not set.
		  */
		 return DriverManager.getConnection(protocol +
		         "derbyDB;create=true", props);
	} catch (SQLException e) {
		throw new SavanException(e);
	}
		
	}
	
	
	public void addFeed(String id,String title,Date lastEditedtime,String author) throws SavanException{
		
		try {
			Connection connection = getConnection();
			try{
				PreparedStatement statement = connection.prepareStatement(SQL_ADD_FEED);
				statement.setString(1,id );
				statement.setString(2,title );
				Timestamp t = new Timestamp(lastEditedtime.getTime());
				statement.setTimestamp(3, t);
				statement.setString(4, author);
				statement.executeUpdate();
			}finally{
				connection.close();
			}
		} catch (SQLException e) {
			throw new SavanException(e);
		}
		
		
	}
	
	public void addEntry(String id,OMElement entry) throws SavanException{
		try {
			StringWriter w = new StringWriter();
			entry.serialize(w);
			Connection connection = getConnection();
			try{
				PreparedStatement statement = connection.prepareStatement(SQL_ADD_ENTRY);
				statement.setString(1,id );
				statement.setString(2,w.getBuffer().toString() );
				statement.executeUpdate();
			}finally{
				connection.close();
			}
		} catch (SQLException e) {
			throw new SavanException(e);
		} catch (XMLStreamException e) {
			throw new SavanException(e);
		}
	}
	
	
	public OMElement getFeedAsXml(String feedId) throws SavanException{
		
		try {
			Connection connection = getConnection();
			try{
				PreparedStatement statement = connection.prepareStatement(SQL_GET_FEED_DATA);
				statement.setString(1,feedId );
				ResultSet results = statement.executeQuery();
				if(results.next()){
					String title = results.getString("title");
					Timestamp updatedTime = results.getTimestamp("updated");
					String author = results.getString("author");
					
					Feed feed = new Feed(title,feedId,author,updatedTime);
					
					statement.close();
					
					statement = connection.prepareStatement(SQL_GET_ENTRIES_4_FEED);
					statement.setString(1,feedId );
					results = statement.executeQuery();
					while(results.next()){
						String entryAsStr = results.getString("content");
						InputStream atomIn = new ByteArrayInputStream(entryAsStr.getBytes());
						XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader(atomIn, MessageContext.DEFAULT_CHAR_SET_ENCODING);
						StAXBuilder builder = new StAXOMBuilder(feed.getFactory(),xmlreader);
						feed.addEntry(builder.getDocumentElement());
					}
					return feed.getFeedAsXml();
				}else{
					throw new SavanException("No such feed "+feedId);
				}
			}finally{
				connection.close();
			}
		} catch (SQLException e) {
			throw new SavanException(e);
		} catch (XMLStreamException e) {
			throw new SavanException(e);
		}
	}
	
	
	
	
	
}
