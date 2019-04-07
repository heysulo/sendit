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

import com.whileloop.sendit.client.SClient;
import com.whileloop.sendit.messages.SMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class SServerHandler extends ChannelInboundHandlerAdapter {

    private SClient remoteClient = null;
    private SServer attachedServer;

    public SServerHandler(SServer server) {
        this.attachedServer = server;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        attachedServer.getServerCallback().OnMessage(attachedServer, remoteClient, (SMessage) msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        attachedServer.getServerCallback().OnEvent(attachedServer, remoteClient, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        attachedServer.getServerCallback().OnError(attachedServer, remoteClient, cause);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        this.remoteClient = new SClient(ctx);
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        if (future.isSuccess()) {
                            attachedServer.getServerCallback().OnSSLHandshakeSuccess(attachedServer, remoteClient);
                        } else {
                            attachedServer.getServerCallback().OnSSLHandshakeFailure(attachedServer, remoteClient);
                        }
                    }
                });
        attachedServer.getServerCallback().OnConnect(attachedServer, remoteClient);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        attachedServer.getServerCallback().OnDisconnect(attachedServer, remoteClient);
    }

}
