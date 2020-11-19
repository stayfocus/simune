package com.ne.util;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import java.util.Random;

import javax.xml.bind.annotation.XmlAccessType;

@XmlAccessorType(XmlAccessType.NONE)
public class SimuNeConnCfg {
    
    @XmlElement(name="request")
    private String request;
    
    @XmlElement(name="success")
    private String success;
    
    @XmlElement(name="fail")
    private String fail;
    
    @XmlElement(name="timeout",required=false)
    private String timeout;
    
    private int[] rangeTimeout;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getFail() {
        return fail;
    }

    public void setFail(String fail) {
        this.fail = fail;
    }
    
    public String getTimeout() {
        return this.timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
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
