package com.ngyewkong.springbatchdemo.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class FirstItemProcessor implements ItemProcessor<Integer, Long> {
    @Override
    public Long process(Integer item) throws Exception {
        System.out.println("Inside Item Processor");

        // take in 1, 2 ...  to 10 integer
        // return 101, 102 ... to 110 long value
        return Long.valueOf(item + 100);
    }
}
