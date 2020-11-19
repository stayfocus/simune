package com.ne;



import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import com.ne.util.SimuNeCfg;
import com.ne.util.SimuNeConstant;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;


public class SimuChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    
    
    private SimuNeServer neServer;
    
    public SimuChannelInitializer(SimuNeServer neServer) {
        this.neServer = neServer;
    }

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		//添加编解码handler 以及 解析报文的handler
		ChannelPipeline p = ch.pipeline();
		p.addLast(new LoggingHandler(LogLevel.INFO));
		SimuNeCfg neCfg = neServer.getNeConfig().getNeCfg();
		String pkgCplit = neCfg.getPkgSplit();
		Integer idleSeconds = neCfg.getIdleSeconds();
		if (null != idleSeconds) {
		    if (0 > idleSeconds.compareTo(SimuNeConstant.MIN_IDLE_SECOND)) {
		        idleSeconds = SimuNeConstant.MIN_IDLE_SECOND;
		    }
		    //读写都空闲时
		    p.addLast(new IdleStateHandler(0,0,idleSeconds,TimeUnit.SECONDS));
		}

        p.addLast(new DelimiterBasedFrameDecoder(5000,neCfg.isStripSplit(),Unpooled.wrappedBuffer(pkgCplit.getBytes())));
        String charsetName = neServer.getNeConfig().getNeCfg().getCharset();
        if (null == charsetName || charsetName.isEmpty()) {
            charsetName = SimuNeConstant.CHARSET_DEFAULT;
        }
        p.addLast(new StringDecoder(Charset.forName(charsetName)));
        p.addLast("handler",new SimuNeHandler(neServer));
	}
}
