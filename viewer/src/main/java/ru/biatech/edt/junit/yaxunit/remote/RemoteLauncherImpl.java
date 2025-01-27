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

import lombok.SneakyThrows;
import lombok.Value;
import org.java_websocket.WebSocket;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.SessionsManager;
import ru.biatech.edt.junit.model.TestSuiteElement;
import ru.biatech.edt.junit.yaxunit.remote.dto.HelloMessage;
import ru.biatech.edt.junit.yaxunit.remote.dto.Message;
import ru.biatech.edt.junit.yaxunit.remote.dto.ReportFileMessage;
import ru.biatech.edt.junit.yaxunit.remote.dto.ReportMessage;
import ru.biatech.edt.junit.yaxunit.remote.dto.RunMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoteLauncherImpl implements RemoteLauncher, Handler, AutoCloseable {
  private final Map<WebSocket, ClientInfo> clients = new HashMap<>();
  private final Map<String, ClientInfo> clientsByKey = new HashMap<>();
  private final AtomicBoolean available = new AtomicBoolean(false);
  private final AtomicInteger lastMessageId = new AtomicInteger(0);
  private WebSocketServer server;

  private final Map<String, CompletableFuture<TestSuiteElement[]>> runs = new HashMap<>();

  @Override
  @SneakyThrows
  public void start() {
    server = new WebSocketServer(0, this);
    server.start();
    while (!server.isStarted()) {
      Thread.sleep(100);
    }
    System.out.println("Remote launcher start on " + server.getPort());
  }

  @SneakyThrows
  @Override
  public void stop() {
    clients.clear();
    clientsByKey.clear();
    if (server != null) {
      server.stop();
    }
    server = null;
  }

  @Override
  public int getPort() {
    return server != null ? server.getPort() : 0;
  }

  @Override
  public CompletableFuture<TestSuiteElement[]> launchTest(String module, String moduleName, List<String> methods, boolean isServer, boolean isClient, boolean isOrdinaryClient) throws ClientNotFound {
    if (clientsByKey.isEmpty()) {
      throw new ClientNotFound("Нет подключенных клиентов");
    }
    var clientKey = clientsByKey.keySet().stream().findFirst().get();
    return launchTest(clientKey, module, moduleName, methods, isServer, isClient, isOrdinaryClient);
  }

  @Override
  public void onMessageReceive(WebSocket socket, Message<?> message) {
    if (message instanceof HelloMessage) {
      handleHandshake(socket, (HelloMessage) message);
    } else if (message instanceof ReportMessage) {
      handleReport((ReportMessage) message);
    } else if (message instanceof ReportFileMessage) {
      handleReportFile((ReportFileMessage) message);
    }
  }

  private void handleReportFile(ReportFileMessage message) {
    SessionsManager.getInstance().importActiveSession(message.getData().getReportFile().toFile());
  }

  private void handleReport(ReportMessage message) {
    var runKey = runKey(clientKey(), message.getId());
    var future = runs.getOrDefault(runKey, null);
    if (future == null) {
      TestViewerPlugin.log().logError("Получен отчет о тестировании, но не обнаружен запуск");
      return;
    }
    runs.remove(runKey);
    future.complete(message.getData());
  }

  @SneakyThrows
  private void handleHandshake(WebSocket socket, HelloMessage message) {
    var client = new ClientInfo(message.getData().getKey(), message.getData().getProtocolVersion(), socket);
    addClient(client);
  }

  @Override
  public void onClientDisconnect(WebSocket socket) {
    var client = clients.getOrDefault(socket, null);
    if (client != null) {
      removeClient(client);
    }
  }

  private CompletableFuture<TestSuiteElement[]> launchTest(String clientKey, String module, String moduleName, List<String> methods, boolean isServer, boolean isClient, boolean isOrdinaryClient) throws ClientNotFound {

    var client = clientsByKey.getOrDefault(clientKey, null);
    if (client == null) {
      throw new ClientNotFound(clientKey);
    }
    var message = RunMessage.builder()
        .module(module)
        .moduleName(moduleName)
        .methods(methods)
        .client(isClient)
        .server(isServer)
        .ordinaryClient(isOrdinaryClient)
        .build();
    setMessageId(message);


    if (!client.socket.isOpen()) {
      removeClient(client);
      return CompletableFuture.failedFuture(new Exception("Web socket is closed"));
    }
    server.send(client.socket, message);
    var runKey = runKey(clientKey, message.getId());
    var future = new CompletableFuture<TestSuiteElement[]>();
    runs.put(runKey, future);
    return future;
  }

  private void setMessageId(Message<?> message) {
    message.setId(lastMessageId.getAndIncrement());
  }

  synchronized private void addClient(ClientInfo client) {
    clients.put(client.socket, client);
    clientsByKey.put(client.getKey(), client);
    available.weakCompareAndSetPlain(false, true);
  }

  synchronized private void removeClient(ClientInfo client) {
    clients.remove(client.socket);
    clientsByKey.remove(client.key);
    var empty = clients.isEmpty();
    available.weakCompareAndSetPlain(empty, !empty);
  }

  public boolean isAvailable() {
    return available.get();
  }

  @Override
  public void close() {
    stop();
  }

  @Value
  private static class ClientInfo {
    String key;
    String protocolVersion;
    WebSocket socket;
  }

  String clientKey() {
    return clientsByKey.keySet().stream().findFirst().orElse(null);
  }

  private String runKey(String clientKey, int operationId) {
    return String.format("%s----%d", clientKey, operationId);
  }
}
