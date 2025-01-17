/*******************************************************************************
 * Copyright (c) 2025 BIA-Technologies Limited Liability Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package ru.biatech.edt.junit.yaxunit.remote;

import lombok.Getter;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import ru.biatech.edt.junit.Logger;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.yaxunit.remote.dto.Message;

import java.net.InetSocketAddress;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer {
  private final Serializer serializer = new Serializer();
  private final Handler handler;
  @Getter
  private volatile boolean started;
  private final Logger log;

  public WebSocketServer(int port, Handler handler) {
    super(new InetSocketAddress(port));
    this.handler = handler;
    log = TestViewerPlugin.log();
  }

  public void send(Message<?> message) {
    broadcast(serializer.writeMessage(message));
  }

  public void send(WebSocket socket, Message<?> message) {
    var messageText = serializer.writeMessage(message);
    log.debug("Send message to {0}\n{1}", socket.getRemoteSocketAddress().getAddress(),messageText);
    socket.send(messageText);
  }

  @Override
  public void onOpen(WebSocket socket, ClientHandshake clientHandshake) {
    log.debug("Connect from {0}}", socket.getRemoteSocketAddress().getAddress());
  }

  @Override
  public void onClose(WebSocket socket, int code, String reason, boolean remote) {
    log.debug("Disconnect from {0}", socket.getRemoteSocketAddress().getAddress());
    handler.onClientDisconnect(socket);
  }

  @Override
  public void onMessage(WebSocket socket, String messageText) {
    log.debug("Message receive from {0}\n{1}", socket.getRemoteSocketAddress().getAddress(), messageText);
    try {
      var message = serializer.readMessage(messageText);
      handler.onMessageReceive(socket, message);
      System.out.println(socket + ": " + message);
    } catch (Exception ex) {
      onError(socket, ex);
    }
  }

  @Override
  public void onError(WebSocket socket, Exception ex) {
    log.logError("Web socket error", ex);
  }

  @Override
  public void onStart() {
    log.debug("Web socket server started on {0}", getPort());
    started = true;
  }
}
