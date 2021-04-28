package solutions.assignment2;

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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import examples.MapRedFileUtils;

public class MapRedSolution2
{
    /* your code goes in here*/
    public static class MapRecord extends Mapper<LongWritable, Text, Text, IntWritable>
    {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
        {
            IntWritable one = new IntWritable(1);
            String valueString = value.toString();
            String[] singleData = valueString.split(",");
            String startTime = singleData[1];
            String endTime = singleData[2];

            if ((!startTime.equals("tpep_pickup_datetime")) && (!endTime.equals("tpep_dropoff_datetime")))
            {
                int startHour = Integer.parseInt(startTime.substring(11, 13));
                // int endHour = Integer.parseInt(endTime.substring(11, 13));

                String word;
                Text record = new Text();
                int startHour12 = 0, endHour12 = 0;

                if(startHour < 12){
                    if(startHour == 0)
                        context.write(new Text("12am"), one);
                    else {
                        word = Integer.toString(startHour) + "am";
                        record.set(word);
                        context.write(record, one);
                    }

                    // if(endHour != startHour){
                    //     if(endHour == 12){
                    //         word = Integer.toString(endHour) + "pm";
                    //     } else if(endHour > 12){
                    //         endHour12 = endHour % 12;
                    //         word = Integer.toString(endHour12) + "pm";
                    //     } else {
                    //         word = Integer.toString(endHour) + "am";
                    //     }
                    //     record.set(word);
                    //     context.write(record, one);
                    // }
                }

                if (startHour == 12){
                    context.write(new Text("12pm"), one);

                    // if(endHour > startHour){
                    //     context.write(new Text("1pm"), one);
                    // } 
                }

                if (startHour > 12){
                    startHour12 = startHour % 12;
                    // endHour12 = endHour % 12;

                    word = Integer.toString(startHour12) + "pm";
                    record.set(word);
                    context.write(record, one);

                    // if (endHour != startHour){
                    //     word = Integer.toString(endHour12) + "pm";
                    //     record.set(word);
                    //     context.write(record, one);

                    //     if (endHour == 0){
                    //         context.write(new Text("12am"), one);
                    //     }
                    // }
                }
            }
        }
    }

    public static class ReduceRecords extends Reducer<Text, IntWritable, Text, IntWritable>
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
            System.err.println("Usage: MapRedSolution2 <in> <out>");
            System.exit(2);
        }
        
        Job job = Job.getInstance(conf, "MapRed Solution #2");
        
        /* your code goes in here*/
        job.setMapperClass(MapRecord.class);
        job.setCombinerClass(ReduceRecords.class);
        job.setReducerClass(ReduceRecords.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        
        MapRedFileUtils.deleteDir(otherArgs[1]);
        int exitCode = job.waitForCompletion(true) ? 0 : 1; 

        FileInputStream fileInputStream = new FileInputStream(new File(otherArgs[1]+"/part-r-00000"));
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fileInputStream);
        fileInputStream.close();
        
        String[] validMd5Sums = {"03357cb042c12da46dd5f0217509adc8", "ad6697014eba5670f6fc79fbac73cf83", "07f6514a2f48cff8e12fdbc533bc0fe5", 
            "e3c247d186e3f7d7ba5bab626a8474d7", "fce860313d4924130b626806fa9a3826", "cc56d08d719a1401ad2731898c6b82dd", 
            "6cd1ad65c5fd8e54ed83ea59320731e9", "59737bd718c9f38be5354304f5a36466", "7d35ce45afd621e46840627a79f87dac"};
        
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
