package com.ngyewkong.springbatchdemo.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class FirstItemReader implements ItemReader<Integer> {

    // setting up the data to be read (source)
    // a list of integers
    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    int index = 0;

    // implement the read method which spring batch will use to read in the source values
    @Override
    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        System.out.println("Inside Item Reader");
        Integer item;

        // cannot use a for/while loop
        // the read method runs everytime based on chunk size
        if (index < list.size()) {
            item = list.get(index);
            index++;
            return item;
        }
        // reset the index as index is a class level variable
        // useful if we are reusing the itemReader
        index = 0;
        // the return null is indicating to itemReader that there are no more records to read from source
        return null;
    }
}
