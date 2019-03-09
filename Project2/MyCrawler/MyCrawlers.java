package MyCrawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawlers extends WebCrawler {
	 private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|feed|rss|svg|js|mp3|zip|gz|vcf|xml|php|json|ico|jpg))$");
	 private final static Pattern MATCHES = Pattern.compile(".*(\\.(html|pdf|jpeg|png|gif))$");
	 
	 public static int[] fileSizeDistribution = new int[5];
	 public static int counter = 0;
	 
	 String crawlStorageFolder = "/Users/weifei/Dropbox/Development/Java/Ecilpse/CS572CrawlerProject/data/crawl";
	 String fetchCnnFile = "fetch_cnn.csv";
	 String visitCnnFile = "visit_cnn.csv";
	 String urlsCnnFile = "urls_cnn.csv";
	 
	 /**
	 * This method receives two parameters. The first parameter is the page
	 * in which we have discovered this new url and the second parameter is
	 * the new url. You should implement this function to specify whether
	 * the given url should be crawled or not (based on your crawling logic).
	 * In this example, we are instructing the crawler to ignore urls that
	 * have css, js, git, ... extensions and to only accept urls that start
	 * with "http://www.viterbi.usc.edu/". In this case, we didn't need the
	 * referringPage parameter to make the decision.
	 */
	 @Override
		
	 public boolean shouldVisit(Page referringPage, WebURL url) {
		 String href = url.getURL().toLowerCase();
		 try{
			 synchronized(this){
				 BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(crawlStorageFolder + urlsCnnFile, true));
				 if(href.startsWith("https://www.cnn.com/") || (href.startsWith("http://www.cnn.com/"))) {
					 bufferWriter.write(url.getURL().replace(",", "_") + ", OK");
				 	 bufferWriter.write("\n");
				 }
				 else {
					 bufferWriter.write(url.getURL().replace(",", "_")+ ", N_OK");
					 bufferWriter.write("\n");
				 }
				 bufferWriter.close();
			 }
		 }
		 catch(IOException e){
			 e.printStackTrace();
		 }
		 
		 if (!(href.startsWith("https://www.cnn.com/") || href.startsWith("http://www.cnn.com/"))) {
			 return false;
		 }
		 
		 return !(FILTERS.matcher(href).matches());
	 }
		 
	 @Override
	 protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		 counter++;
		 try{
		     synchronized(this){
		         	BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(crawlStorageFolder + fetchCnnFile, true));
		         	bufferWriter.write(webUrl.getURL().replace(",", "_") + "," + statusCode + "\n");
		         	bufferWriter.close();
		         }
		     }  
		 catch(IOException e){
		        e.printStackTrace();
		 }
	 }
	 
	 /**
	  * This function is called when a page is fetched and ready
	  * to be processed by your program.
	  */
	  @Override
	  public void visit(Page page) {
		  String url = page.getWebURL().getURL();
		  System.out.println("URL: " + url);
//		  if (page.getParseData() instanceof HtmlParseData) {
//			  HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
//			  String text = htmlParseData.getText();
//			  String html = htmlParseData.getHtml();
//			  Set<WebURL> links = htmlParseData.getOutgoingUrls();
//			  
//			  System.out.println("Text length: " + text.length());
//			  System.out.println("Html length: " + html.length());
//			  System.out.println("Number of outgoing links: " + links.size());
//		  }
		  
         int pageLength = page.getContentData().length;
         int stdSize = pageLength / 1024;
         int outlinkSize = page.getParseData().getOutgoingUrls().size();
         String contentType = page.getContentType();
         if (contentType.toLowerCase().indexOf(";") > -1) {
        	 contentType = contentType.replace(contentType.substring(contentType.indexOf(";"), contentType.length()), "");
         }

         try{
             synchronized (this){
                 if(stdSize < 1) {
                	 fileSizeDistribution[0]++;
                 }
                 else if(1 <= stdSize && stdSize < 10) {
                   	 fileSizeDistribution[1]++;
                 }
                 else if(10 <= stdSize && stdSize < 100) {
                	 fileSizeDistribution[2]++;
                 } 	 
                 else if(100 <= stdSize && stdSize <1024) {
                	 fileSizeDistribution[3]++;
                 }
                 else {
                	 fileSizeDistribution[4]++;
                 }
                 BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(crawlStorageFolder + visitCnnFile, true));
                 bufferWriter.write(url.replace(",", "_") + "," + pageLength + "," + outlinkSize + "," + contentType);
                 bufferWriter.write("\n");
                 bufferWriter.close();
                 System.out.println(crawlStorageFolder + visitCnnFile);
             }
         }
         catch(IOException e){
        	 e.printStackTrace();
         }   
	  }	 
}
