/*
 * The MIT License
 *
 * Copyright 2019 sulochana.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.whileloop.sendit.client;

import com.whileloop.sendit.callbacks.SClientCallback;
import com.whileloop.sendit.messages.SMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

public class SClient {

    private final String configuredHost;
    private final int configuredPort;
    private final SClientCallback clientCallback;
    private final boolean manualConnect;
    private Bootstrap bootstrap;
    private Channel channel = null;
    private ChannelHandlerContext context = null;
    private SClient self = this;

    public SClient(String host, int port, EventLoopGroup eventLoopGroup, SClientCallback callback, boolean connectManually)
            throws SSLException, InterruptedException {
        this.configuredHost = host;
        this.configuredPort = port;
        this.clientCallback = callback;
        this.manualConnect = connectManually;

        final SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup);
        this.bootstrap.channel(NioSocketChannel.class);
        this.bootstrap.handler(new SecureSClientChannelHandler(sslCtx, this));
        if (connectManually){
            return;
        }
        this._connect();
    }
    
    public final void connect() throws InterruptedException{
        if (!this.manualConnect){
            throw new RuntimeException("SClient not designed for manual connect");
        }
        if (this.isConnected()){
            throw new RuntimeException("SClient already connected");
        }
        
        _connect();
    }
    
    private void _connect() throws InterruptedException{
        ChannelFuture connection = this.bootstrap.connect(configuredHost, configuredPort);
        this.channel = connection.sync().channel();
        this.channel.pipeline().get(SslHandler.class).handshakeFuture().sync().addListener(new GenericFutureListener<Future<Channel>>() {
            @Override
            public void operationComplete(Future<Channel> future) throws Exception {
                if (future.isSuccess()) {
                    clientCallback.OnSSLHandshakeSuccess(self);
                } else {
                    clientCallback.OnSSLHandshakeFailure(self);
                }
            }
        });
    }

    public SClient(ChannelHandlerContext context) {
        this.context = context;
        this.configuredHost = ((InetSocketAddress) this.context.channel().remoteAddress()).getAddress().getHostAddress();
        this.configuredPort = ((InetSocketAddress) this.context.channel().remoteAddress()).getPort();
        this.clientCallback = null;
        this.manualConnect = false;
    }

    public String getConfiguredHost() {
        return this.configuredHost;
    }

    public int getConfiguredPort() {
        return this.configuredPort;
    }

    public SClientCallback getClientCallback() {
        return clientCallback;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public void Send(SMessage msg) {
        this.context.writeAndFlush(msg);
    }

    private SSLEngine getSSLEngine() {
        return this.context.pipeline().get(SslHandler.class).engine();
    }

    private SSLSession getSSLSession() {
        return this.getSSLEngine().getSession();
    }

    public String getCipherSuite() {
        return this.getSSLSession().getCipherSuite();
    }

    public String getProtocol() {
        return this.getSSLSession().getProtocol();
    }

    private InetSocketAddress getLocalAddress() {
        return ((InetSocketAddress) this.context.channel().localAddress());
    }

    private InetSocketAddress getRemoteAddress() {
        return ((InetSocketAddress) this.context.channel().remoteAddress());
    }

    public String getLocalHostAddress() {
        return getLocalAddress().getAddress().getHostAddress();
    }

    public int getLocalPort() {
        return getLocalAddress().getPort();
    }

    public String getLocalHostName() {
        return getLocalAddress().getHostName();
    }

    public String getRemoteHostAddress() {
        return getRemoteAddress().getAddress().getHostAddress();
    }

    public int getRemotePort() {
        return getRemoteAddress().getPort();
    }

    public String getRemoteHostName() {
        return getRemoteAddress().getHostName();
    }

    public boolean isConnected() {
        if (this.context == null) {
            return false;
        }
        return this.context.channel().isActive();
    }
    
    public void closeConnection(){
        this.context.channel().close();
    }

}
