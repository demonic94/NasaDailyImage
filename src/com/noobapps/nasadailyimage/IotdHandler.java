package com.noobapps.nasadailyimage;


/**
 * IotdHandler
 * This handler will collect the Image Of The Day
 * 
 */

import org.xml.sax.helpers.DefaultHandler;
import android.annotation.SuppressLint;
import android.graphics.*;
import android.os.StrictMode;

import javax.xml.parsers.*;
import org.xml.sax.*;

import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;

@SuppressLint("NewApi")
public class IotdHandler extends DefaultHandler {
private String url = "http://www.nasa.gov/rss/image_of_the_day.rss";
private boolean inUrl = false;
private boolean inTitle = false;
private boolean inDescription = false;
private boolean inItem = false;
private boolean inDate = false;
private Bitmap image = null;
private String imageUrl=null;
private String title = null;
private StringBuffer description = new StringBuffer();
private String date = null;
private int count=0;

public void processFeed() {
try {
//This part is added to allow the network connection on a main GUI thread...    
StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
StrictMode.setThreadPolicy(policy); 
SAXParserFactory factory = SAXParserFactory.newInstance();
SAXParser parser = factory.newSAXParser();
XMLReader reader = parser.getXMLReader();
reader.setContentHandler(this);
URL urlObj = new URL(url);
InputStream inputStream = urlObj.openConnection().getInputStream();
reader.parse(new InputSource(inputStream));
} 
catch (Exception e) 
{
    e.printStackTrace();
    //System.out.println(new String("Got Exception General"));
}
}

private Bitmap getBitmap(String url) {
try {
    System.out.println(url);
HttpURLConnection connection =
(HttpURLConnection)new URL(url).openConnection();
connection.setDoInput(true);
connection.connect();
InputStream input = connection.getInputStream();
Bitmap bitmap = BitmapFactory.decodeStream(input);
input.close();
return bitmap;
} 
catch (IOException ioe) 
{
    //System.out.println(new String("IOException in reading Image"));
    return null;
}
catch (Exception ioe) 
{
    //System.out.println(new String("IOException GENERAL"));
    return null;
}
}

public void startElement(String uri, String localName, String qName,
Attributes attributes) throws SAXException 
{

if (localName.equals("enclosure"))
{
    //System.out.println(new String("characters Image"));
    imageUrl = attributes.getValue("","url");
    //System.out.println(imageUrl);
    inUrl = true; 
}
else { inUrl = false; }
if (localName.startsWith("item")) { inItem = true;}
else if (inItem) {
if (localName.equals("title")) { inTitle = true; }
else { inTitle = false; }
if (localName.equals("description") && count<1) { inDescription = true; count++;}
else { inDescription = false; }
if (localName.equals("pubDate")) { inDate = true; }
else { inDate = false; }
}
}

public void characters(char ch[], int start, int length) {
    //System.out.println(new String("characters"));
String chars = new String(ch).substring(start, start + length);
//System.out.println(chars);
if (inUrl && image == null) 
{
   // System.out.println(new String("IMAGE"));
   // System.out.println(imageUrl);
    image = getBitmap(imageUrl);
}
if (inTitle && title == null) {
   // System.out.println(new String("TITLE"));
    title = chars; }
if (inDescription) { description.append(chars); }
if (inDate && date == null) { date = chars; }
}

public Bitmap getImage() { return image; }
public String getTitle() { return title; }
public StringBuffer getDescription() { return description; }
public String getDate() { return date; }

}

