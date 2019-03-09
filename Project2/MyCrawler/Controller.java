package MyCrawler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {

	public static void main(String[] args) throws Exception {
		 // TODO Auto-generated method stub
		 String crawlStorageFolder = "/Users/weifei/Dropbox/Development/Java/Ecilpse/CS572CrawlerProject/data/crawl";
		 
		 int numberOfCrawlers = 7;
		 int maxPage = 20000;
		 int maxDepth = 16;
		 
		 String countCnnFile = "CrawlReport_cnn.txt";
		 String fetchCnnFile = "fetch_cnn.csv";
		 String visitCnnFile = "visit_cnn.csv";
		 String urlsCnnFile = "urls_cnn.csv";
		  
		 CrawlConfig config = new CrawlConfig();
		 config.setCrawlStorageFolder(crawlStorageFolder);
		 config.setMaxPagesToFetch(maxPage);
		 config.setMaxDepthOfCrawling(maxDepth);
		 config.setIncludeHttpsPages(true);
		 config.setFollowRedirects(true);
		 config.setIncludeBinaryContentInCrawling(true);
		  
		 /*
		 * Instantiate the controller for this crawl.
		 */
		 PageFetcher pageFetcher = new PageFetcher(config);
		 RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		 RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		 CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		 
		 try{
	        BufferedWriter bufferWritter = new BufferedWriter(new FileWriter(crawlStorageFolder+fetchCnnFile));
	        bufferWritter.write("URL, Status Code\n");
	        bufferWritter.close();
	
	        bufferWritter = new BufferedWriter(new FileWriter(crawlStorageFolder+visitCnnFile));
	        bufferWritter.write("URLs Downloaded, size, # of outlinks found, content-type\n");
	        bufferWritter.close();
	
	        bufferWritter = new BufferedWriter(new FileWriter(crawlStorageFolder+urlsCnnFile));
	        bufferWritter.write("encountered URL, indicator\n");
	        bufferWritter.close();
		 } 
		 catch (IOException e){
            e.printStackTrace();
		 }
		 /*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		 controller.addSeed("https://www.cnn.com/");
		 controller.addSeed("http://www.cnn.com/");
		
		 /*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		 controller.start(MyCrawlers.class, numberOfCrawlers);
		 
		 try{
             BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(crawlStorageFolder + countCnnFile));
             
             bufferWriter.write("< 1KB: "+ MyCrawlers.fileSizeDistribution[0]);
             bufferWriter.write("\n");
             
             bufferWriter.write("1KB ~ <10KB: "+ MyCrawlers.fileSizeDistribution[1]);
             bufferWriter.write("\n");
             
             bufferWriter.write("10KB ~ <100KB: "+ MyCrawlers.fileSizeDistribution[2]);
             bufferWriter.write("\n");
             
             bufferWriter.write("100KB ~ <1MB:: "+ MyCrawlers.fileSizeDistribution[3]);
             bufferWriter.write("\n");
             
             bufferWriter.write(">= 1MB: "+ MyCrawlers.fileSizeDistribution[4]);
             bufferWriter.write("\n");
             
             bufferWriter.close();
         } 
		 catch (IOException e){
             e.printStackTrace();
         }
		 System.out.println("Fetch attemped: "+ MyCrawlers.counter);
	}

}
