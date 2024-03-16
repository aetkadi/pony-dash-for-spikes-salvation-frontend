package ee.taltech.pony_dash_for_spikes_salvation;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import ee.taltech.pony_dash_for_spikes_salvation.packets.Packet;
import ee.taltech.pony_dash_for_spikes_salvation.packets.PacketLobby;
import ee.taltech.pony_dash_for_spikes_salvation.packets.PacketPlayerConnect;
import ee.taltech.pony_dash_for_spikes_salvation.packets.PacketSendCoordinates;

import java.util.ArrayList;
import java.util.List;

public class Network {

    private Network() {
        // Prevent instantiation
    }

    /**
     * Register endpoint to the network.
     *
     * @param endPoint The endpoint.
     */
    public static void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Packet.class);
        kryo.register(PacketPlayerConnect.class);
        kryo.register(PacketSendCoordinates.class);
        kryo.register(PacketLobby.class);
        kryo.register(ArrayList.class);
        kryo.register(List.class);
        kryo.register(ee.taltech.pony_dash_for_spikes_salvation.Player.class);
    }
}
