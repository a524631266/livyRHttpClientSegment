package com.hw.transmitlayer.job;

import org.apache.livy.Job;
import org.apache.livy.JobContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

import java.util.ArrayList;
import java.util.List;

public class PiJob implements Job<Double>, Function<Integer, Integer>, Function2<Integer, Integer, Integer> {
    private final int samples;
    public PiJob(int samples) {
        this.samples = samples;
    }
    @Override
    public Double call(JobContext jobContext) throws Exception {
        JavaSparkContext sc = jobContext.sc();
        List<Integer> sampleList = new ArrayList<Integer>();
        for (int i = 0; i < samples; i++) {
            sampleList.add(i + 1);
        }
        return  4.0d * sc.parallelize(sampleList).map(this).reduce(this) / samples;
    }

    @Override
    public Integer call(Integer integer) throws Exception {
        double x = Math.random();
        double y = Math.random();
        return (x*x + y*y < 1) ? 1 : 0;
    }

    @Override
    public Integer call(Integer v1, Integer v2) throws Exception {
        return v1 + v2;
    }
}
