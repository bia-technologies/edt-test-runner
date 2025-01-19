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
import ru.biatech.edt.junit.model.SessionsManager;
import ru.biatech.edt.junit.yaxunit.remote.dto.HelloMessage;
import ru.biatech.edt.junit.yaxunit.remote.dto.Message;
import ru.biatech.edt.junit.yaxunit.remote.dto.ReportMessage;
import ru.biatech.edt.junit.yaxunit.remote.dto.RunMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoteLauncherImpl implements RemoteLauncher, Handler, AutoCloseable {
  private final Map<WebSocket, ClientInfo> clients = new HashMap<>();
  private final Map<String, ClientInfo> clientsByKey = new HashMap<>();
  private final AtomicBoolean available = new AtomicBoolean(false);
  private final AtomicInteger lastMessageId = new AtomicInteger(0);
  private WebSocketServer server;

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
  public void launchTest(String module, String method, boolean isServer, boolean isClient, boolean isOrdinaryClient) throws ClientNotFound {
    if (clientsByKey.isEmpty()) {
      throw new ClientNotFound("Нет подключенных клиентов");
    }
    var clientKey = clientsByKey.keySet().stream().findFirst().get();
    launchTest(clientKey, module, method, isServer, isClient, isOrdinaryClient);
  }

  @Override
  public void launchTest(String clientKey, String module, String method, boolean isServer, boolean isClient, boolean isOrdinaryClient) throws ClientNotFound {
    var client = clientsByKey.getOrDefault(clientKey, null);
    if (client == null) {
      throw new ClientNotFound(clientKey);
    }
    var message = RunMessage.builder()
        .module(module)
        .method(method)
        .client(isClient)
        .server(isServer)
        .ordinaryClient(isOrdinaryClient)
        .build();
    setMessageId(message);

    server.send(client.socket, message);
  }

  private void setMessageId(Message<?> message) {
    message.setId(lastMessageId.getAndIncrement());
  }

  @Override
  public void onMessageReceive(WebSocket socket, Message<?> message) {
    if (message instanceof HelloMessage) {
      handleHandshake(socket, (HelloMessage) message);
    } else if (message instanceof ReportMessage) {
      handleReport((ReportMessage) message);
    }
  }

  private void handleReport(ReportMessage message) {
    SessionsManager.importSession(message.getData());
  }

  @SneakyThrows
  private void handleHandshake(WebSocket socket, HelloMessage message) {
    var client = new ClientInfo(message.getData().getKey(), message.getData().getProtocolVersion(), socket);
    addClient(client);
    launchTest(client.key, "Процедура ИсполняемыеСценарии() Экспорт\n" +
        "\n" +
        "\tЮТТесты\n" +
        "\t\t.ДобавитьТест(\"ТестУспешно\")\n" +
        "\t\t.ДобавитьТест(\"ТестОшибка\")\n" +
        "\t\t.ДобавитьТест(\"ТестСломан\")\n" +
        "\t;\n" +
        "\n" +
        "КонецПроцедуры\n" +
        "\n" +
        "Процедура ТестУспешно() Экспорт\n" +
        "\n" +
        "\tРезультат = СтрНайти(\"90\", \"9\");\n" +
        "\tЮТест.ОжидаетЧто(Результат).Равно(1);\n" +
        "\n" +
        "КонецПроцедуры\n" +
        "\n" +
        "Процедура ТестОшибка() Экспорт\n" +
        "\n" +
        "\tЮТест.ОжидаетЧто(1).Равно(2);\n" +
        "\n" +
        "КонецПроцедуры\n" +
        "\n" +
        "Процедура ТестСломан() Экспорт\n" +
        "\n" +
        "\tЮТест.ОжидаетЧто(1).ОтсутствующийМетод(2);\n" +
        "\n" +
        "КонецПроцедуры", "ИсполняемыеСценарии", true, false, false);
  }

  @Override
  public void onClientDisconnect(WebSocket socket) {
    var client = clients.getOrDefault(socket, null);
    if (client != null) {
      removeClient(client);
    }
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
    available.weakCompareAndSetPlain(!empty, empty);
  }

  public boolean isAvailable() {
    return available.get();
  }

  @Override
  public void close() throws Exception {
    stop();
  }

  @Value
  private static class ClientInfo {
    String key;
    String protocolVersion;
    WebSocket socket;
  }
}
