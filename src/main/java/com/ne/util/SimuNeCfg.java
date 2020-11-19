package com.ne.util;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;

//fastjson string-bean转化时，如果字符串中没有的话，会使用类中初始化的值
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "NeCfg")
public class SimuNeCfg {
    @XmlElement(name="ip",required=false)
    private String ip = "";//如果一个服务器有多网卡IP
    @XmlElement(name="port")
    private String port; //监听端口
    @XmlElement(name="maxConn",required=false)
    private int maxConn = 10; //最大连接数
    
    //到了最大连接数后的处理策略,断链还是延迟回应 小于等于0表示断链,大于0表示延迟的毫秒数
    @XmlElement(name="maxPolicy",required=false)
    private int maxPolicy = 0;
    @XmlElement(name="timeout",required=false)
    private int timeout = 0; //整网元的延迟,ms
    @XmlElement(name="pkgSplit")
    private String pkgSplit="\r\n"; //请求报文的分隔符
    @XmlElement(name="stripSplit",required=false)
    private boolean stripSplit = true;
    
    //private String cmdSplit; //命令的分隔符
    @XmlElement(name="charset",required=false)
    private String charset = SimuNeConstant.CHARSET_DEFAULT;//字符集编码
    @XmlElement(name="resAppend",required=false)
    private String resAppend = ""; //当没配置时，默认就是原样返回
    @XmlElement(name="notMatch",required=false)
    private String notMatch = ""; //当命令找不到时，如果回应
    @XmlElement(name="login",required=false)
    private List<SimuNeConnCfg> login; //网元配置的登录命令
    //空闲时间秒数
    @XmlElement(name="idleSeconds",required=false)
    private Integer idleSeconds;
    //返回报文发送时间间隔
    @XmlElement(name="sendIntervalMS",required=false)
    private Integer sendIntervalMS;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(int maxConn) {
        this.maxConn = maxConn;
    }

    public int getMaxPolicy() {
        return maxPolicy;
    }

    public void setMaxPolicy(int maxPolicy) {
        this.maxPolicy = maxPolicy;
    }

    public String getPkgSplit() {
        return pkgSplit;
    }

    public void setPkgSplit(String pkgSplit) {
        this.pkgSplit = pkgSplit;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public List<SimuNeConnCfg> getLogin() {
        return login;
    }

    public void setLogin(List<SimuNeConnCfg> login) {
        this.login = login;
    }

    public String getResAppend() {
        return resAppend;
    }

    public void setResAppend(String resAppend) {
        this.resAppend = resAppend;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getNotMatch() {
        return notMatch;
    }

    public void setNotMatch(String notMatch) {
        this.notMatch = notMatch;
    }

    public boolean isStripSplit() {
        return stripSplit;
    }

    public void setStripSplit(boolean stripSplit) {
        this.stripSplit = stripSplit;
    }

    public Integer getIdleSeconds() {
        return this.idleSeconds;
    }

    public void setIdleSeconds(Integer idleSeconds) {
        this.idleSeconds = idleSeconds;
    }

    public Integer getSendIntervalMS() {
        return this.sendIntervalMS;
    }

    public void setSendIntervalMS(Integer sendIntervalMS) {
        this.sendIntervalMS = sendIntervalMS;
    }
    
    public SimuNeCfg simpleCopyWithoutPort() {
        SimuNeCfg cloneCfg = new SimuNeCfg();
        cloneCfg.ip = this.ip;
        cloneCfg.maxConn = this.maxConn;
        cloneCfg.maxPolicy = this.maxPolicy;
        cloneCfg.timeout = this.timeout;
        cloneCfg.pkgSplit = this.pkgSplit;
        cloneCfg.stripSplit = this.stripSplit;
        cloneCfg.charset = this.charset;
        cloneCfg.resAppend = this.resAppend;
        cloneCfg.notMatch = this.notMatch;
        cloneCfg.login = this.login;
        cloneCfg.idleSeconds = this.idleSeconds;
        cloneCfg.sendIntervalMS = this.sendIntervalMS;
        return cloneCfg;
    }
    
    public void makeLoginTimeout() {
        for (SimuNeConnCfg connCfg : this.login) {
            connCfg.makeRangeTimeout();
        }
    }
}
