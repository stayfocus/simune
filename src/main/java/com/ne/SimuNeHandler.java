package com.ne;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.util.SimuCmdCfg;
import com.ne.util.SimuNeCfg;
import com.ne.util.SimuNeConnCfg;
import com.ne.util.SimuNeConstant;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@ChannelHandler.Sharable
public class SimuNeHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(SimuNeHandler.class);

    private SimuNeServer neServer;

    private SimuNeCfg neCfg;

    private Map<String,SimuCmdCfg> mapCmdsCfg;
    //此Channel的超时时间
    private int timeoutChannel;
    //主要用于判断登录是否成功
    private int loginStep = SimuNeConstant.STEP_INIT;

    //private long receiveTime;

    private Charset charset;

    public SimuNeHandler(SimuNeServer neServer) {
        this.neServer = neServer;
        this.neCfg = neServer.getNeConfig().getNeCfg();
        this.mapCmdsCfg = neServer.getNeConfig().getMapCmdsCfg();
        this.charset = Charset.forName(neCfg.getCharset());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext arg0, String arg1) throws Exception {
        arg1 = arg1.trim();
        if (loginStep >= 0) {
            doLogin(arg0,arg1);
        } else {
            doCommand(arg0,arg1);
        }
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleEvent = (IdleStateEvent)evt;
            if (idleEvent.state().equals(IdleState.ALL_IDLE)) {
                //断掉
                logger.warn("channel {} is ilde for long time,so disconnect it.",ctx.channel());
                ctx.close();
            }
        }
    } 

    private void doLogin(ChannelHandlerContext ctx, String msg) {
        List<SimuNeConnCfg> loginCfg = neCfg.getLogin();
        int size = loginCfg.size();
        if (loginStep >= size) {
            logger.info("channel {} login already success.",ctx);
            loginStep = -1;
            return;
        }
        SimuNeConnCfg stepLogin = loginCfg.get(loginStep);
        int randomTimeOut = stepLogin.getRandomTimeout();
        if (-1 == randomTimeOut) {
            //本命令没有配置，取全局的
            randomTimeOut = this.timeoutChannel;
        }
        
        if (msg.equals(stepLogin.getRequest())) {
            doResponse(ctx,stepLogin.getSuccess(),randomTimeOut);
            loginStep++;
            if(loginStep >= size) {
                logger.info("channel {} login success.",ctx);
                loginStep = -1;
                return;
            }
        } else {
            logger.error("channel {} login fail,maybe the username or password is error.",ctx);
            doResponse(ctx,stepLogin.getFail(),randomTimeOut);
        }
    }

    private void doCommand(ChannelHandlerContext ctx, String msg) {
        //receiveTime = System.currentTimeMillis();
        logger.info("receive msg:{}.",msg);
        SimuCmdCfg cmdCfg = mapCmdsCfg.get(msg);
        if (null == cmdCfg) {
            logger.warn("the cmd is not found,it is {}.",msg);
            doResponse(ctx,this.neCfg.getNotMatch(),this.timeoutChannel);
            return;
        }
        int randomTimeOut = cmdCfg.getRandomTimeout();
        
        if (-1 == randomTimeOut) {
          //本命令没有配置，取全局的
          randomTimeOut = this.timeoutChannel;
        }
        //本通道超时时间+本命令的超时时间
        doResponse(ctx,cmdCfg.getResponse(), randomTimeOut);
    }
    
    public static List<String> splitCmd(String cmd,int piece) {
        int sectionLen = cmd.length() / piece;
        List<String> retCmd = new ArrayList<String>();
        int index = 0;
        for (; index < piece - 1 ;index++) {
            String tmp = cmd.substring(index * sectionLen,(index+1) * sectionLen);
            if (!tmp.isEmpty()) {
                retCmd.add(tmp);
            }
        }
        String tmp = cmd.substring(index * sectionLen);
        if (!tmp.isEmpty()) {
            retCmd.add(tmp);
        }
        return retCmd;
    }

    private void doResponse(ChannelHandlerContext ctx, String msg, int timeout) {
        String send = msg + neCfg.getResAppend();
        int piece = new Random().nextInt(3) + 1;        
        List<String> sendCmds = splitCmd(send,piece);
        if (0 == timeout) {
            doResponse(ctx,sendCmds,0);
            return;
        }
        //延迟发        
        neServer.getWorkerGroup().schedule(new Runnable() {
            @Override
            public void run() {
                doResponse(ctx,sendCmds,0);
            }
        }, timeout, TimeUnit.MILLISECONDS);
    }

    private void doResponse(ChannelHandlerContext ctx, List<String> lstMsg,int times) {       
        try {
            if (null == ctx || lstMsg.size() <= times) {
                logger.warn("channel is null.");
                return;
            }
            String sendTmp = lstMsg.get(times);
            //logger.info("begin to send the response");
            ctx.writeAndFlush(Unpooled.wrappedBuffer(sendTmp.getBytes(this.charset)));
            //logger.info("end to send the response,cost {} ms.",System.currentTimeMillis()-receiveTime);
            final int nextTime = times + 1;
            if (nextTime >= lstMsg.size()) {
                return;
            }
            Integer intervalTimeout = this.neCfg.getSendIntervalMS();
            if (null == intervalTimeout) {
                intervalTimeout = SimuNeConstant.DEFAULT_INTERVAL_MS;
            }
            neServer.getWorkerGroup().schedule(new Runnable() {
                @Override
                public void run() {
                    doResponse(ctx,lstMsg,nextTime);
                }
            }, intervalTimeout.longValue(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("channel {} writeAndFlush exception.",ctx,e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        if (loginStep == SimuNeConstant.STEP_DISCONN) {
            ;
        } else {
            loginStep = SimuNeConstant.STEP_DISCONN;
            neServer.decCurConn();
        }
        logger.error("channel {} socket exception {}.",ctx,cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (loginStep == SimuNeConstant.STEP_DISCONN) {
            ;
        } else {
            loginStep = SimuNeConstant.STEP_DISCONN;
            neServer.decCurConn();
        }
        logger.info("channel {} disconnect,the num of conntion is {}.",ctx,neServer.getCurConn());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //新连接连进来了
        boolean reachMax = neServer.reachMaxAfterInc();
        if (!reachMax) {
            this.timeoutChannel = neCfg.getTimeout();
            logger.info("channel {} connect success,the num of conntion is {}.",ctx,neServer.getCurConn());
        } else {
            //达到最大值了
            int maxPolicy = neCfg.getMaxPolicy();
            if (-1 >= maxPolicy) {
                logger.warn("channel {} reach limit,so disconnect it, the num of conntion is {}.",ctx,neServer.getCurConn());
                ctx.close();
                return;
            }
            logger.warn("channel {} reach limit,so slow response, the num of conntion is {}.",ctx,neServer.getCurConn());
            //如果maxPolicy配的是大于等于0的值
            this.timeoutChannel = neCfg.getTimeout() + maxPolicy;
        }
        //查看其有没有登录命令
        List<SimuNeConnCfg> neConnCfg = neCfg.getLogin();
        if (null == neConnCfg || neConnCfg.isEmpty()) {
            //没有登录命令，到此登录成功,将loginStep设置为-1
            logger.info("channel {} no need login, the num of conntion is {}.",ctx,neServer.getCurConn());
            loginStep = SimuNeConstant.STEP_LOGIN;
        }
    }

}
