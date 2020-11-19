package com.ne.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimuFileUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SimuFileUtils.class);
    
    public static String readFile(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        FileInputStream in = null;
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            in = new FileInputStream(file);
            in.read(filecontent);
            return new String(filecontent, encoding);
        } catch (Exception e) {
            logger.error("readFile exception,{}.",fileName);
            return null;
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("close file exception,{}.",fileName);
                }
            }
        }
    }

}
