package com.tzunicom.soft.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * @author taop
 * @email tplovejava@sina.cn
 * @date 2020/6/18 15:28
 * @describe
 **/
@Component
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    /**
     * 端口号
     */
    @Value("${webSocket.netty.port:8888}")
    private int port;

    /**
     * webSocket路径
     */
    @Value("${webSocket.netty.path:/webSocket}")
    private String webSocketPath;

    @Autowired
    private WebSocketHandler webSocketHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        ServerBootstrap sb = new ServerBootstrap();
        sb.option(ChannelOption.SO_BACKLOG, 1024);
        sb.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline cp = socketChannel.pipeline();

                        // =====================以下为SSL处理代码=================================
                        /*log.info("开始设置https服务");
                        SSLContext sslcontext = SSLContext.getInstance("TLS");
                        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                        KeyStore ks = KeyStore.getInstance("PKCS12");
                        String keyStorePath = "config/dsmy.pfx";
                        String keyPassword = "dsmy@123456$";
                        ks.load(new FileInputStream(keyStorePath), keyPassword.toCharArray());
                        kmf.init(ks, keyPassword.toCharArray());
                        sslcontext.init(kmf.getKeyManagers(), null, null);
                        SSLEngine engine = sslcontext.createSSLEngine();
                        engine.setUseClientMode(false);
                        engine.setNeedClientAuth(false);
                        cp.addFirst("ssl", new SslHandler(engine));*/
                        // =====================以上为SSL处理代码=================================

                        cp.addLast(new HttpServerCodec());
                        cp.addLast(new ObjectEncoder());
                        // 以块的方式来写的处理器
                        cp.addLast(new ChunkedWriteHandler());

                        /**
                         * 说明：
                         * 1、http数据在传输过程中是分段的，HttpObjectAggregator可以将多个段聚合
                         * 2、这就是为什么，当浏览器发送大量数据时，就会发送多次http请求
                         */
                        cp.addLast(new HttpObjectAggregator(8192));

                        /**
                         *  说明：
                         *  1、对应webSocket，它的数据是以帧（frame）的形式传递
                         *  2、浏览器请求时 ws://localhost:58080/xxx 表示请求的uri
                         *  3、核心功能是将http协议升级为ws协议，保持长连接
                         */
                        cp.addLast(new WebSocketServerProtocolHandler(webSocketPath, "WebSocket", true, 65536 * 10));
                        // 自定义的handler，处理业务逻辑
                        cp.addLast(webSocketHandler);
                    }
                });

        // 服务器异步创建绑
        ChannelFuture cf = sb.bind(port).sync();
        log.info("Server started and listen on:{}",cf.channel().localAddress());
        // 关闭服务器通道
        cf.channel().closeFuture().sync();
    }

    /**
     * 释放资源
     * @throws InterruptedException
     */
    @PreDestroy
    public void destroy() throws InterruptedException {
        if(bossGroup != null){
            bossGroup.shutdownGracefully().sync();
        }
        if(workGroup != null){
            workGroup.shutdownGracefully().sync();
        }
    }

    @PostConstruct()
    public void init() {
        //需要开启一个新的线程来执行netty server 服务器
        new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
