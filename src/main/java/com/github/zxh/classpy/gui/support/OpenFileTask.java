package com.github.zxh.classpy.gui.support;

import com.github.zxh.classpy.classfile.ClassFileParser;
import com.github.zxh.classpy.common.FileComponent;
import com.github.zxh.classpy.gui.parsed.HexText;
import com.github.zxh.classpy.helper.UrlHelper;
import com.github.zxh.classpy.lua.binarychunk.BinaryChunkParser;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.concurrent.Task;

public class OpenFileTask extends Task<Object> {

    private final URL url;

    public OpenFileTask(URL url) {
        this.url = url;
    }
    
    @Override
    protected Object call() throws Exception {
        System.out.println("loading " + url + "...");

        byte[] data = UrlHelper.readData(url);
        String fileName = UrlHelper.getFileName(url);

        HexText hex = new HexText(data);
        FileComponent fc = parse(data);
        fc.setName(fileName);

        System.out.println("finish loading");
        return new Object[] {fc, hex};
    }

    private FileComponent parse(byte[] data) {
        if (url.toString().endsWith(".class")) {
            return new ClassFileParser().parse(data);
        } else {
            // todo
            return new BinaryChunkParser().parse(data);
        }
    }
    
    public void setOnSucceeded(BiConsumer<FileComponent, HexText> callback) {
        super.setOnSucceeded(e -> {
            Object[] arr = (Object[]) e.getSource().getValue();
            FileComponent fc = (FileComponent) arr[0];
            HexText hex = (HexText) arr[1];
            
            callback.accept(fc, hex);
        });
    }
    
    public void setOnFailed(Consumer<Throwable> callback) {
        super.setOnFailed(event -> {
            Throwable err = event.getSource().getException();
            err.printStackTrace(System.err);
            
            callback.accept(err);
        });
    }
    
    public void startInNewThread() {
        new Thread(this).start();
    }
    
}
