/*
 * The MIT License
 *
 * Copyright 2019 Team whileLOOP.
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
package com.whileloop.sendit.server;

import com.whileloop.sendit.callbacks.SServerCallback;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLException;

public class SServer {

    private final ServerBootstrap bootstrap;
    private final SServerCallback serverCallback;
    private final NioEventLoopGroup clientEventGroup;
    private ChannelFuture completionFuture;

    public SServer(int port, EventLoopGroup parentGroup, NioEventLoopGroup childGroup, SServerCallback callback) throws CertificateException, SSLException, InterruptedException {
        this.serverCallback = callback;
        this.clientEventGroup = childGroup;
        this.bootstrap = new ServerBootstrap();
        this.bootstrap.group(parentGroup, childGroup);
        this.bootstrap.channel(NioServerSocketChannel.class);
        this.bootstrap.childHandler(new SecureSServerChannelHandler(this));
        this.bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        this.completionFuture = this.bootstrap.bind(port).sync();
    }

    public SServerCallback getServerCallback() {
        return serverCallback;
    }

    public void waitTillClose() throws InterruptedException {
        this.completionFuture.channel().closeFuture().sync();
    }

    public void closeServer() throws InterruptedException {
        this.completionFuture.channel().close().channel().closeFuture().sync();
    }

    public Channel getServerChannel() {
        return this.completionFuture.channel();
    }

    public NioEventLoopGroup getClientEventGroup() {
        return clientEventGroup;
    }
}
