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
package com.whileloop.sendit.callbacks;

import com.whileloop.sendit.client.SClient;
import com.whileloop.sendit.messages.SMessage;
import com.whileloop.sendit.server.SServer;

public interface SServerCallback {

    public void OnConnect(SServer server, SClient client);

    public void OnDisconnect(SServer server, SClient client);

    public void OnMessage(SServer server, SClient client, SMessage msg);

    public void OnError(SServer server, SClient client, Throwable cause);

    public void OnEvent(SServer server, SClient client, Object event);

    public void OnSSLHandshakeSuccess(SServer server, SClient client);

    public void OnSSLHandshakeFailure(SServer server, SClient client);
}
