import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class Converter {
    public static void main(String args[])
            throws IOException, TikaException, SAXException {
        Converter converter = new Converter();
        converter.parseHtmlFiles();
    }

    public void parseHtmlFiles()
            throws IOException, TikaException, SAXException {
        // get all html files from nypost folder, store in "files"
        String inputPath = "/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/data/nypost";
        File file = new File(inputPath);
        File[] files = file.listFiles();

        ArrayList<String> wordsList = new ArrayList();
        for (File curFile: files) {
            // allocate memory for all required parameters for html parser
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();
            FileInputStream fileInputStream = new FileInputStream(curFile);
            BodyContentHandler bodyContentHandler = new BodyContentHandler(-1);

            // configure and allocate new memory for new html parser with corresponding parameters
            HtmlParser htmlparser = new HtmlParser();
            htmlparser.parse(fileInputStream, bodyContentHandler, metadata, parseContext);

            // generate new bigList from current html file
            ArrayList newBigList = new ArrayList(Arrays.asList(bodyContentHandler.toString().split("\\W+")));
            wordsList.addAll(newBigList);
            System.out.println(newBigList);
        }

        // write generated word list to big.txt
        String outputFilePath = "/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW5/src/big.txt";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFilePath));
        for (String word: wordsList) {
            bufferedWriter.write(word);
            bufferedWriter.write("\n");
            System.out.println(word);
        }
    }
}

