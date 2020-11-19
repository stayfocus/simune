package com.ne;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.ne.util.SimuCmdCfg;
import com.ne.util.SimuCmdsCfg;
import com.ne.util.SimuNeCfg;

@Service
public class SimuNeService {
    //同一个配置文件支持多个模拟网元
    private static final Logger logger = LoggerFactory.getLogger(SimuNeService.class);
    
    private List<String> lstConfDir = new ArrayList<String>();
    
    private Map<String,NeConfig> mapCfgs = new ConcurrentHashMap<String,NeConfig>();
    
    @PostConstruct
    public void init() throws Exception {
        //配置服务端的NIO线程组
        readNeCfgs();
        startNeServer();
    }
    
    private void startNeServer() {
        for (NeConfig wholeCfg : mapCfgs.values()) {
            new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        SimuNeServer neServer = new SimuNeServer();
                        neServer.setNeConfig(wholeCfg);
                        neServer.init();
                    } catch (Exception ex) {
                        logger.warn("server start excetion",ex);
                    }
                }
                
            }).start(); 
        }
    }
    
    //首先读取配置
    private void readNeCfgs() throws IOException {

        /*
         * String userDir = System.getProperty("user.dir");
         * if(!userDir.endsWith(File.separator)) { userDir = userDir + File.separator; }
         */
        String[] filePath = new String[] {"file:./conf/","classpath:/conf/"};
        Resource resource  = null;
        boolean exist = false;
        for (String tmp : filePath) {
            resource = new PathMatchingResourcePatternResolver().getResource(tmp);
            exist = resource.exists();
            if (exist) {
                break;
            }            
        }
        if (null == resource) {
            logger.error("there are no config file.");
            throw new RuntimeException("there are no config file.");
        }
        
        boolean isFile = resource.isFile();
        if (!isFile) {
            logger.error("there are no config file.");
            throw new RuntimeException("there are no config file.");
        }        
        String userDir = resource.getFile().getPath();
        if(!userDir.endsWith(File.separator)) {
            userDir = userDir + File.separator;
        }
        //userDir = userDir + "conf" + File.separator;
        File file = new File(userDir);     //获取其file对象
        logger.info("the conf dir is {}.",userDir);
        File[] fs = file.listFiles();//遍历path下的文件和目录，放在File数组中
        for (File f : fs) {
            //加一层目录的目的是为了好复制，但是只支持一个网元
            if (f.isDirectory()) {
                String conf = f.getAbsolutePath() + File.separator;
                this.lstConfDir.add(conf);
            }
        }
        if (this.lstConfDir.isEmpty()) {
            throw new RuntimeException("there is no simune config files");
        }
        
        for (String dirTmp : this.lstConfDir) {
            
            logger.info("the simune config dir is {}.",dirTmp);            
            Map<String,SimuCmdCfg> mapCmdCfg = readCmdsXml(dirTmp);
            SimuNeCfg neCfg = readNeXml(dirTmp);
            neCfg.makeLoginTimeout();
            List<Integer> ports = splitPorts(neCfg.getPort(),dirTmp);
            if (ports.size() > 1) {
                for (Integer portTmp : ports) {
                    SimuNeCfg cloneCfg = neCfg.simpleCopyWithoutPort();
                    cloneCfg.setPort(portTmp.toString());
                    putCfgMap(cloneCfg,mapCmdCfg);
                }             
                
            } else {
                putCfgMap(neCfg,mapCmdCfg);
            }
        }
    }
    
    private void putCfgMap(SimuNeCfg neCfg,Map<String,SimuCmdCfg> mapCmdsCfg) {
        
        NeConfig wholeCfg = new NeConfig();
        wholeCfg.setNeCfg(neCfg);
        wholeCfg.setMapCmdsCfg(mapCmdsCfg);        
        this.mapCfgs.put(makeKey(neCfg), wholeCfg);
    }
    
    private String makeKey(SimuNeCfg neCfg) {
        String ip = neCfg.getIp();
        if (null == ip) {
            ip = "";
        }
        return ip + ":" + neCfg.getPort();        
    }
    
    private List<Integer> splitPorts(String lstPort,String dir) {
        List<Integer> retPorts = new ArrayList<Integer>();
        String[] commaSplit = lstPort.split(",");
        try {            
            for (String tmp : commaSplit) {
                if (tmp.isEmpty()) {
                    continue;
                }
                String[] interval = tmp.split("-");
                if (interval.length > 2) {
                    logger.warn("error port style,{},{}.",lstPort,dir);
                    return null;
                } else if (2 == interval.length) {
                    int start = Integer.parseInt(interval[0]);
                    int end = Integer.parseInt(interval[1]);
                    for (; start <= end; start++) {
                        retPorts.add(start);
                    }
                } else {
                    //只有一个
                    int onePort = Integer.parseInt(interval[0]);
                    retPorts.add(onePort);
                }
                
            }
            return retPorts;
        } catch (Exception e) {
            logger.warn("error port style,{},{}.",lstPort,dir);
            return null;
        }
    }
    
    private SimuNeCfg readNeXml(String confDir) {
        String filePath = confDir + "ne.xml";
        try {
            JAXBContext context = JAXBContext.newInstance(SimuNeCfg.class);
            Unmarshaller umar = context.createUnmarshaller();
            File file = new File(filePath);
            InputStream inputStream = new FileInputStream(file);//通过输入流读取配置文件
            return (SimuNeCfg)umar.unmarshal(inputStream);
        } catch (JAXBException  e) {
            logger.error("readCfg JAXBException {}",e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("the file is not found,file name is {}.",filePath);
        }
        return null;
    }

    private Map<String,SimuCmdCfg> readCmdsXml(String confDir) {
        
        List<SimuCmdsCfg> cmdCfgs = new ArrayList<SimuCmdsCfg>();
        String cmdsPath = confDir + "cmds";
        File cmdsDir = new File(cmdsPath);     //获取其file对象
        
        // 获取cmds文件夹所在的路径地址
        String absCmdDir = cmdsDir.getAbsolutePath();
        if (!absCmdDir.endsWith(File.separator)) {
            absCmdDir = absCmdDir + File.separator;
        }
        absCmdDir = absCmdDir + "file" + File.separator;
        
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        };
        File[] fs = cmdsDir.listFiles(filter);   //遍历path下的文件和目录，放在File数组中
        for (File f : fs) {
            if (f.isDirectory()) {
                //不支持目录递归
                continue;
            }
            //读取此文件
            String cmdsFile = f.getAbsolutePath();
            try {
                JAXBContext context = JAXBContext.newInstance(SimuCmdsCfg.class);
                Unmarshaller umar = context.createUnmarshaller();
                File file = new File(cmdsFile);
                InputStream inputStream = new FileInputStream(file);//通过输入流读取配置文件
                SimuCmdsCfg cmdsCfg = (SimuCmdsCfg)umar.unmarshal(inputStream);
                if(null == cmdsCfg || null == cmdsCfg.getCmds()) {
                    logger.error("the xml of cmds has error,{}.",cmdsFile);
                    continue;
                }
                //对命令进行处理
                cmdsCfg.makeCmdsTimeout();
                cmdsCfg.makeCmdsResfile(absCmdDir);
                cmdCfgs.add(cmdsCfg);
            } catch (Exception e) {
                logger.error("the xml of cmds has exception,{}.",cmdsFile,e);
            }
        }
        //所有文件读取完成,将
        Map<String,SimuCmdCfg> mapCmdsCfg = new HashMap<String,SimuCmdCfg>();
        for (SimuCmdsCfg cmdsTmp : cmdCfgs) {
            List<SimuCmdCfg> lstCmd = cmdsTmp.getCmds();
            if (null == lstCmd) {
                continue;
            }
            for (SimuCmdCfg cmdTmp : lstCmd) {
                mapCmdsCfg.put(cmdTmp.getRequest(), cmdTmp);
            }
        }
        return mapCmdsCfg;
    }
}
