package com.ne;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ne.util.SimuNeCfg;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

//每个模拟网元的业务承载类
public class SimuNeServer  {

    private static final Logger logger = LoggerFactory.getLogger(SimuNeServer.class);

    private NeConfig neConfig;

    private AtomicInteger curConn = new AtomicInteger(0); //当前连接数

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(2);

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public SimuNeServer() {

    }

    public NeConfig getNeConfig() {
        return this.neConfig;
    }

    public void setNeConfig(NeConfig neConfig) {
        this.neConfig = neConfig;
    }

    public int incCurConn() {
        return curConn.incrementAndGet();
    }

    public boolean reachMaxAfterInc() {
        int now = curConn.incrementAndGet();
        if (now > this.neConfig.getNeCfg().getMaxConn()) {
            return true;
        } else {
            return false;
        }
    }

    public void decCurConn() {
        curConn.decrementAndGet();
    }

    public AtomicInteger getCurConn() {
        return curConn;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void init() throws Exception {
        //配置服务端的NIO线程组        
        startNetty();
    }

    private void startNetty() throws Exception {
       
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
        .channel((Class<? extends ServerChannel>) NioServerSocketChannel.class)        
        .option(ChannelOption.SO_BACKLOG, 1024)
        //如果服务非正常停止掉，那么操作系统一般过一会才会释放这个连接 TIME_WAIT
        .option(ChannelOption.SO_REUSEADDR,Boolean.valueOf(true))
        //.option(ChannelOption.SO_RCVBUF,32 * 1024)
        //.option(ChannelOption.SO_SNDBUF,32 * 1024)
        .childHandler(new SimuChannelInitializer(this));
        try {
            //绑定端口，同步等待成功
            SimuNeCfg neCfg = this.neConfig.getNeCfg();
            String localIp = neCfg.getIp();
            int port = Integer.parseInt(neCfg.getPort());            
            if(null == localIp || localIp.isEmpty()) {
                localIp = "";
                b.bind(port);
            } else {
                SocketAddress local = new InetSocketAddress(localIp,port);
                b.bind(local);
            }
            logger.info("SimuNe begin to listen the clients,the simuNe is {}:{}.",localIp,port);
        }
        catch (Exception e) {
            logger.error("SimuNe server start exception",e);
            throw new Exception("server start exception.");
        }
    }

}
