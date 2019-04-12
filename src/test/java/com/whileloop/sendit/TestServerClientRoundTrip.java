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
package com.whileloop.sendit;

import com.whileloop.sendit.callbacks.SClientCallback;
import com.whileloop.sendit.callbacks.SServerCallback;
import com.whileloop.sendit.client.SClient;
import com.whileloop.sendit.messages.SMessage;
import com.whileloop.sendit.server.SServer;
import io.netty.channel.nio.NioEventLoopGroup;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestServerClientRoundTrip implements SServerCallback, SClientCallback {

    SServer serverInstance;
    SClient clientInstance;
    SClient connectedClient;
    TestHeartbeatMsg hbMsg;

    public TestServerClientRoundTrip() {
        this.clientInstance = null;
        this.serverInstance = null;
        this.connectedClient = null;
        this.hbMsg = null;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void TestServerClient() {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            serverInstance = new SServer(3000, boosGroup, workerGroup, this);
            clientInstance = new SClient("127.0.0.1", 3000, clientGroup, this, true);
            clientInstance.connect();
            serverInstance.waitTillClose();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("An exception was thrown");
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            clientGroup.shutdownGracefully();
        }
    }

    @Override
    public void OnConnect(SServer server, SClient client) {
        this.connectedClient = client;
        assertEquals(serverInstance, server, "Server instances are different");
        assertEquals(client.getRemoteHostAddress(), clientInstance.getLocalHostAddress());
        assertEquals(client.getRemotePort(), clientInstance.getLocalPort());
        assertEquals(client.getRemoteHostName(), clientInstance.getLocalHostName());
        assertEquals(client.isConnected(), true);
    }

    @Override
    public void OnDisconnect(SServer server, SClient client) {
        assertEquals(connectedClient, client, "Connected client instances are different");
        assertEquals(serverInstance, server, "Server instances are different");
        assertEquals(client.isConnected(), false);
    }

    @Override
    public void OnMessage(SServer server, SClient client, SMessage msg) {
        assertEquals(connectedClient, client, "Connected client instances are different");
        assertEquals(serverInstance, server, "Server instances are different");
        assertEquals(hbMsg.getCreationTime(), ((TestHeartbeatMsg) msg).getCreationTime());
        client.Send(msg);
        client.closeConnection();
    }

    @Override
    public void OnError(SServer server, SClient client, Throwable cause) {
        assertEquals(connectedClient, client, "Connected client instances are different");
        assertEquals(serverInstance, server, "Server instances are different");
    }

    @Override
    public void OnEvent(SServer server, SClient client, Object event) {
        assertEquals(connectedClient, client, "Connected client instances are different");
        assertEquals(serverInstance, server, "Server instances are different");
    }

    @Override
    public void OnSSLHandshakeSuccess(SServer server, SClient client) {
        assertEquals(connectedClient, client, "Connected client instances are different");
        assertEquals(serverInstance, server, "Server instances are different");
    }

    @Override
    public void OnSSLHandshakeFailure(SServer server, SClient client) {
        fail("OnSSLHandshakeFailure");
    }

    @Override
    public void OnConnect(SClient client) {
        assertEquals(clientInstance, client, "Client instances are different");
    }

    @Override
    public void OnDisconnect(SClient client) {
        try {
            assertEquals(clientInstance, client, "Client instances are different");
            serverInstance.closeServer();
        } catch (InterruptedException ex) {
            fail("Exsception thrown on disconnect");
        }
    }

    @Override
    public void OnMessage(SClient client, SMessage msg) {
        assertEquals(clientInstance, client, "Client instances are different");
        assertEquals(hbMsg.getCreationTime(), ((TestHeartbeatMsg) msg).getCreationTime());
    }

    @Override
    public void OnError(SClient client, Throwable cause) {
        assertEquals(clientInstance, client, "Client instances are different");
    }

    @Override
    public void OnEvent(SClient client, Object event) {
        assertEquals(clientInstance, client, "Client instances are different");
    }

    @Override
    public void OnSSLHandshakeSuccess(SClient client) {
        assertEquals(clientInstance, client, "Client instances are different");
        assertEquals(client.getCipherSuite(), clientInstance.getCipherSuite());
        assertEquals(client.getProtocol(), clientInstance.getProtocol());
        this.hbMsg = new TestHeartbeatMsg();
        client.Send(hbMsg);
    }

    @Override
    public void OnSSLHandshakeFailure(SClient client) {
        assertEquals(clientInstance, client, "Client instances are different");
        fail("SSLHandshake failed");
    }
}
