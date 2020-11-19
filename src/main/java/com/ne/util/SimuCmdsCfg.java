package com.ne.util;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "cmdlist")
public class SimuCmdsCfg {
    
    @XmlElement(name="cmds")
    private List<SimuCmdCfg> cmds;

    public List<SimuCmdCfg> getCmds() {
        return cmds;
    }

    public void setCmds(List<SimuCmdCfg> cmds) {
        this.cmds = cmds;
    }
    
    public void makeCmdsTimeout() {
        for (SimuCmdCfg cmdTmp : this.cmds) {
            cmdTmp.makeRangeTimeout();
        }
    }
    
    public void makeCmdsResfile(String absDir) {
        for (SimuCmdCfg cmdTmp : this.cmds) {
            String resFile = cmdTmp.getResfile();
            if (null != resFile && !resFile.isEmpty()) {
                //将绝对路径加上
                String fileName = absDir + resFile;
                cmdTmp.setResfile(fileName);
                //读取文件
                String fileContent = SimuFileUtils.readFile(fileName);
                cmdTmp.setResponse(fileContent);                
            }
        }
    }
}
