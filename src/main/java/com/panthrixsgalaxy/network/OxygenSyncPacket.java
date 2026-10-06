package com.panthrixsgalaxy.network;

import com.panthrixsgalaxy.client.ClientOxygenData;
import com.panthrixsgalaxy.system.oxygen.OxygenState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Paquete servidor -> jugador con el estado del oxígeno (se envía cada segundo). */
public class OxygenSyncPacket {

    private final OxygenState state;
    private final int oxygen;
    private final int capacity;
    private final int graceSecondsLeft;

    public OxygenSyncPacket(OxygenState state, int oxygen, int capacity, int graceSecondsLeft) {
        this.state = state;
        this.oxygen = oxygen;
        this.capacity = capacity;
        this.graceSecondsLeft = graceSecondsLeft;
    }

    /** Leer el paquete (en el cliente). Mismo orden que encode. */
    public OxygenSyncPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(OxygenState.class), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    /** Escribir el paquete (en el servidor). */
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(state);
        buf.writeVarInt(oxygen);
        buf.writeVarInt(capacity);
        buf.writeVarInt(graceSecondsLeft);
    }

    /** Al llegar al cliente: guardar los datos para que el indicador los dibuje. */
    public void handle(Supplier<NetworkEvent.Context> context) {
        ClientOxygenData.update(state, oxygen, capacity, graceSecondsLeft);
        context.get().setPacketHandled(true);
    }
}
