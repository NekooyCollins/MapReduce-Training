package solutions.assignment1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import examples.MapRedFileUtils;

public class MapRedSolution1
{
    /* your code goes in here*/
    public static class MapURL extends Mapper<LongWritable, Text, Text, IntWritable>
    {
        private final static IntWritable one = new IntWritable(1);
        private Text letter = new Text();
        
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
        {
            try
            {
                String valueString = value.toString();
                String[] singleData = valueString.split(" ");

                if (singleData.length > 6){
                    String target = singleData[6];
                    String newTarget = target.replaceAll("\"", "");

                    letter.set(newTarget);
                    context.write(letter, one);

                    // find all URLs start without "http://localhost"
                    // if(newTarget.startsWith("http://localhost") == false){

                    //     StringBuilder prefix = new StringBuilder();
                    //     prefix.append(newTarget);
                    //     prefix.insert(0, "http://localhost");

                    //     letter.set(prefix.toString());
                    //     context.write(letter, one);
                    //        
                    // } 
                    // URLs start with "http://localhost"
                    // else{
                    //     letter.set(newTarget);
                    //     context.write(letter, one);
                    // }
                }
            } catch(Exception e){
                System.out.println("errors occur in mapper proccess.");
            }
        }
    }


    public static class ReduceURL extends Reducer<Text, IntWritable, Text, IntWritable>
    {
        private IntWritable result = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values,
            Context context) throws IOException, InterruptedException
        {
            int sum = 0;
        
            for (IntWritable val : values)
                sum += val.get();
            
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();

        String[] otherArgs =
            new GenericOptionsParser(conf, args).getRemainingArgs();
        
        if (otherArgs.length != 2)
        {
            System.err.println("Usage: MapRedSolution1 <in> <out>");
            System.exit(2);
        }
        
        Job job = Job.getInstance(conf, "MapRed Solution #1");
        
        /* your code goes in here*/
        job.setMapperClass(MapURL.class);
        job.setCombinerClass(ReduceURL.class);
        job.setReducerClass(ReduceURL.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        
        MapRedFileUtils.deleteDir(otherArgs[1]);
        int exitCode = job.waitForCompletion(true) ? 0 : 1; 
        
        FileInputStream fileInputStream = new FileInputStream(new File(otherArgs[1]+"/part-r-00000"));
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fileInputStream);
        fileInputStream.close();
        
        String[] validMd5Sums = {"ca11be10a928d07204702b3f950fb353", "6a70a6176249b0f16bdaeee5996f74cb", 
            "54893b270934b63a25cd0dcfd42fba64", "d947988bd6f35078131ce64db48dfad2", "3c3ded703f60e117d48c3c37e2830866"};
        
        for (String validMd5 : validMd5Sums) 
        {
            if (validMd5.contentEquals(md5))
            {
                System.out.println("The result looks good :-)");
                System.exit(exitCode);
            }
        }
        System.out.println("The result does not look like what we expected :-(");
        System.exit(exitCode);
    }
}
