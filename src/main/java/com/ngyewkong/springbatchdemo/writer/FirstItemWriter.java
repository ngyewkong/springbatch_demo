package com.ngyewkong.springbatchdemo.writer;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirstItemWriter implements ItemWriter<Long> {
    // item writer takes in items based on chunk size not 1 by 1
    @Override
    public void write(List<? extends Long> itemList) throws Exception {
        System.out.println("Inside Item Writer");

        // print out each item
        itemList.stream().forEach(System.out::println);
    }
}
