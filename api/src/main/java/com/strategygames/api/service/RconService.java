package com.strategygames.api.service;

import com.strategygames.api.config.RconProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal RCON client for sending commands to the Minecraft server.
 * Implements the Source RCON protocol used by Minecraft.
 */
@Service
public class RconService {

    private static final Logger log = LoggerFactory.getLogger(RconService.class);
    private static final int RCON_TYPE_AUTH = 3;
    private static final int RCON_TYPE_COMMAND = 2;

    private final RconProperties props;
    private final AtomicInteger requestId = new AtomicInteger(1);

    public RconService(RconProperties props) {
        this.props = props;
    }

    /**
     * Sends a command to the Minecraft server via RCON.
     * Opens a new connection per call (stateless, safe for k8s).
     *
     * @param command the Minecraft command to execute (without leading slash)
     * @return server response, or empty string if disabled / on error
     */
    public String sendCommand(String command) {
        if (!props.isEnabled()) {
            log.debug("RCON disabled, skipping command: {}", command);
            return "";
        }
        try (Socket socket = new Socket(props.getHost(), props.getPort())) {
            socket.setSoTimeout(5000);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            int id = requestId.getAndIncrement();
            sendPacket(out, id, RCON_TYPE_AUTH, props.getPassword());
            RconPacket authResponse = readPacket(in);
            if (authResponse.id == -1) {
                log.error("RCON authentication failed");
                return "";
            }

            sendPacket(out, id, RCON_TYPE_COMMAND, command);
            RconPacket response = readPacket(in);
            return response.body;
        } catch (IOException e) {
            log.error("RCON command failed: {}", e.getMessage());
            return "";
        }
    }

    private void sendPacket(OutputStream out, int id, int type, String body) throws IOException {
        byte[] bodyBytes = body.getBytes("UTF-8");
        int length = 4 + 4 + bodyBytes.length + 2; // id + type + body + 2 null terminators
        ByteBuffer buf = ByteBuffer.allocate(4 + length).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(length);
        buf.putInt(id);
        buf.putInt(type);
        buf.put(bodyBytes);
        buf.put((byte) 0);
        buf.put((byte) 0);
        out.write(buf.array());
        out.flush();
    }

    private RconPacket readPacket(InputStream in) throws IOException {
        byte[] lenBuf = in.readNBytes(4);
        int length = ByteBuffer.wrap(lenBuf).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byte[] data = in.readNBytes(length);
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int id = buf.getInt();
        int type = buf.getInt();
        byte[] bodyBytes = new byte[length - 10]; // length - id(4) - type(4) - nulls(2)
        buf.get(bodyBytes);
        return new RconPacket(id, type, new String(bodyBytes, "UTF-8"));
    }

    private record RconPacket(int id, int type, String body) {}
}
