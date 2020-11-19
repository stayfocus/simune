package com.ne;

import java.util.Map;

import com.ne.util.SimuCmdCfg;
import com.ne.util.SimuNeCfg;

public class NeConfig {
    
    private SimuNeCfg neCfg;

    private Map<String,SimuCmdCfg> mapCmdsCfg;

    public SimuNeCfg getNeCfg() {
        return this.neCfg;
    }

    public void setNeCfg(SimuNeCfg neCfg) {
        this.neCfg = neCfg;
    }

    public Map<String, SimuCmdCfg> getMapCmdsCfg() {
        return this.mapCmdsCfg;
    }

    public void setMapCmdsCfg(Map<String, SimuCmdCfg> mapCmdsCfg) {
        this.mapCmdsCfg = mapCmdsCfg;
    }
}
