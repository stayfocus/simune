package com.ne.util;

import java.util.Random;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class SimuCmdCfg {
    
    @XmlElement(name="request")
    private String request;//请求报文,全部匹配
    
    @XmlElement(name="response")
    private String response = "";
    
    @XmlElement(name="resfile")
    private String resfile = "";
    //可单独设置每个命令的超时时间,整命令时间是在这个基础上再加上网元超时时间
    @XmlElement(name="timeout",required=false)
    private String timeout;
    
    private int[] rangeTimeout;
    
    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getTimeout() {
        return this.timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
    
    
    public String getResfile() {
        return this.resfile;
    }

    public void setResfile(String resfile) {
        this.resfile = resfile;
    }

    public void makeRangeTimeout() {
        if (null == timeout || timeout.isEmpty()) {
            rangeTimeout = new int[0];
            return;
        }
        String[] splitTime = this.timeout.split("-");
        int len = splitTime.length;
        rangeTimeout = new int[len];
        for (int i = 0 ;i < len ;i++) {
            this.rangeTimeout[i] = Integer.parseInt(splitTime[i]);
        }
    }
    
    public int getRandomTimeout() {
        int len = rangeTimeout.length;
        if (0 == len) {
            return -1;
        } else if (1 == len) {
            return rangeTimeout[0];
        }
        int start = rangeTimeout[0];
        int end = rangeTimeout[1];
        //int randNumber = rand.nextInt(MAX - MIN + 1) + MIN;
        Random rand = new Random();
        return rand.nextInt(end - start + 1) + start;
    }    
}
