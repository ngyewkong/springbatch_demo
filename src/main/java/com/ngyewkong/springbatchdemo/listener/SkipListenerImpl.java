package com.ngyewkong.springbatchdemo.listener;

import com.ngyewkong.springbatchdemo.model.StudentCsv;
import com.ngyewkong.springbatchdemo.model.StudentJson;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

// using the skip listener interface from spring batch
// takes in input from reader and output from writer as generics
// different method from using annotations
// same outcome as using @SkipInRead, @SkipInProcess & @SkipInWrite annotation
@Component
public class SkipListenerImpl implements SkipListener<StudentCsv, StudentJson> {
    @Override
    public void onSkipInRead(Throwable throwable) {
        if (throwable instanceof FlatFileParseException) {
            // capturing the bad records to a flatfile
            createFile("/Users/ngyewkong/IdeaProjects/springbatch-demo/ItemReadersDemoJob/reader/SkipInRead.txt",
                    ((FlatFileParseException) throwable).getInput());
        }
    }

    @Override
    public void onSkipInWrite(StudentJson studentJson, Throwable throwable) {
        // create file
        createFile("/Users/ngyewkong/IdeaProjects/springbatch-demo/ItemReadersDemoJob/writer/SkipInWrite.txt",
                studentJson.toString());
    }

    @Override
    public void onSkipInProcess(StudentCsv studentCsv, Throwable throwable) {
        // create file
        createFile("/Users/ngyewkong/IdeaProjects/springbatch-demo/ItemReadersDemoJob/processor/SkipInProcess.txt",
                studentCsv.toString());
    }

    // file writer helper
    // create a file at the filePath and allows it to be appendable (multiple records can be written)
    public void createFile(String filePath, String data) {
        try (FileWriter fileWriter = new FileWriter(new File(filePath), true)) {
            fileWriter.write(data + " -> error occurred on " + new Date() + "\n");
        } catch (Exception e) {

        }
    }
}
