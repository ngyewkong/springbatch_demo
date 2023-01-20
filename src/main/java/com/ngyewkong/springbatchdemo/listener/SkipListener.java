package com.ngyewkong.springbatchdemo.listener;

import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

// to capture the bad records that were skipped during read
@Component
public class SkipListener {

    // use the @OnSkipInRead annotation from spring batch core
    // the method takes in a Throwable input argument
    // ((FlatFileParseException) throwable).getInput() returns the data of record that gave the exception
    @OnSkipInRead
    public void skipInRead(Throwable throwable) {
        if (throwable instanceof FlatFileParseException) {
            // capturing the bad records to a flatfile
            createFile("/Users/ngyewkong/IdeaProjects/springbatch-demo/ItemReadersDemoJob/reader/SkipInRead.txt",
                    ((FlatFileParseException) throwable).getInput());
        }
    }

    // filewriter helper
    // create a file at the filePath and allows it to be appendable (multiple records can be written)
    public void createFile(String filePath, String data) {
        try (FileWriter fileWriter = new FileWriter(new File(filePath), true)) {
            fileWriter.write(data + " -> error occurred on " + new Date() + "\n");
        } catch (Exception e) {

        }
    }
}
