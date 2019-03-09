import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndexJob {
  public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
    if (args.length != 2) {
      System.err.println("Usage: Word Count <input path> <output path>");
      System.exit(-1);
    }
    Job job = new Job();
    job.setJarByClass(InvertedIndexJob.class);
    job.setJobName("Inverted Index Job");

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    job.setMapperClass(wordFrequencyMap.class);
    job.setReducerClass(wordFrequencyReduce.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.waitForCompletion(true);
  }

  public static class wordFrequencyMap extends Mapper<Object, Text, Text, Text> {
    private Text word = new Text();
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      Text docID = new Text();
      //split input documents by tab, retrive document id and document contents, respectively
      docID.set(value.toString().split("\t")[0]);
      //String[] document = new String[2];
      StringTokenizer contents = new StringTokenizer(value.toString());

      //distrubute word-docID pairs into context
      while (contents.hasMoreTokens()) {
        //remove all special characters except alphabets, and then convert all rest strings to lower case
        String[] filtedWords = contents.nextToken().replaceAll("[^a-zA-Z]", " ").toLowerCase().split(" ");
        for(String filtedWord : filtedWords) {
          word.set(filtedWord);
          context.write(word, docID);
        }
      }
    }
  }

  public static class wordFrequencyReduce extends Reducer<Text, Text, Text, Text> {
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      //use a hash map to store document id and corresponding frequency
      HashMap<String, Integer> map = new HashMap<>();
      //use a StringBuilder to generate final output of map key-value pairs
      StringBuilder newStr = new StringBuilder();
      //convert String type to a Text type variable
      Text textOutput = new Text();

      //count word frequency in each document
      for (Text value : values) {
        map.put(value.toString(), map.getOrDefault(value.toString(), 0) + 1);
      }
      //generate string from map results
      for(String curKey : map.keySet()) {
        newStr.append(curKey + ":" + map.get(curKey) + "\t");
      }

      //write outputs
      textOutput.set(newStr.toString());
      context.write(key, textOutput);
    }
  }
}
