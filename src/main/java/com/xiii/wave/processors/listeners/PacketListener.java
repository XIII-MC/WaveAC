package com.xiii.wave.processors.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.xiii.wave.Wave;
import com.xiii.wave.managers.profile.Profile;
import com.xiii.wave.processors.Packet;
import com.xiii.wave.utils.ChatUtils;
import com.xiii.wave.utils.MoveUtils;
import com.xiii.wave.utils.TaskUtils;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class PacketListener extends SimplePacketListenerAbstract {

    private final Wave plugin;

    public PacketListener(Wave plugin) {
        super(PacketListenerPriority.LOWEST);

        this.plugin = plugin;

        PacketEvents.getAPI().getEventManager().registerListener(this);

    }

    @Override
    public void onPacketPlayReceive(PacketPlayReceiveEvent e) {
        if (e.isCancelled() || e.getPlayer() == null) return;

        final Player player = (Player) e.getPlayer();

        final Packet packet = new Packet(e.getPacketType(), e, System.currentTimeMillis());

        /*
        Check for server crashers - exploit attempts
        We have to do this on the netty thread in order to cancel the packet
         */
        final String crashAttempt = checkCrasher(packet);

        if (crashAttempt != null) {

            e.setCancelled(true);

            ChatUtils.log(Level.SEVERE, "Kicking " + player.getName() + " for attempting to crash the server, Module: " + crashAttempt);

            //Kick the player on the main thread
            TaskUtils.task(() -> player.kickPlayer("Invalid Packet"));

            return;
        }

        final Profile profile = this.plugin.getProfileManager().getProfile(player);

        if (profile == null) return;

        profile.getProfileThread().execute(() -> profile.handle(packet));
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent e) {
        if (e.isCancelled() || e.getPlayer() == null) return;

        final Player player = (Player) e.getPlayer();

        final Profile profile = this.plugin.getProfileManager().getProfile(player);

        if (profile == null) return;

        /*
        ---------------------------------------------------------------------------
        Validate serverbound packets to make sure they're being sent to the player
        ---------------------------------------------------------------------------
         */
        final int playerId = player.getEntityId();

        switch (e.getPacketType()) {

            case ENTITY_VELOCITY:

                final WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(e);

                if (velocity.getEntityId() != playerId) return;

                break;

                /*
                Validate more
                 */
        }
        /*
        ---------------------------------------------------------------------------
         */

        //profile.getProfileThread().execute(() -> profile.handle(packet));
    }

    private String checkCrasher(Packet packet) {

        double x = 0D, y = 0D, z = 0D;
        float yaw = 0F, pitch = 0F;

        switch (packet.getType()) {

            case PLAYER_POSITION:

                WrapperPlayClientPlayerPosition pos = packet.getPositionWrapper();

                x = Math.abs(pos.getPosition().getX());
                y = Math.abs(pos.getPosition().getY());
                z = Math.abs(pos.getPosition().getZ());

                break;

            case PLAYER_POSITION_AND_ROTATION:

                WrapperPlayClientPlayerPositionAndRotation posLook = packet.getPositionLookWrapper();

                x = Math.abs(posLook.getPosition().getX());
                y = Math.abs(posLook.getPosition().getY());
                z = Math.abs(posLook.getPosition().getZ());
                yaw = Math.abs(posLook.getYaw());
                pitch = Math.abs(posLook.getPitch());

                break;

            case PLAYER_ROTATION:

                WrapperPlayClientPlayerRotation look = packet.getLookWrapper();

                yaw = Math.abs(look.getYaw());
                pitch = Math.abs(look.getPitch());

                break;
        }

        final double invalidValue = 3.0E7D;

        //This messes our threading system and potentially causes damage to the server.
        final boolean invalid = x > invalidValue || y > invalidValue || z > invalidValue
                || yaw > 3.4028235e+35F
                || pitch > MoveUtils.MAXIMUM_PITCH;

        //It's impossible for these values to be NaN or Infinite.
        final boolean impossible = !Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(z)
                || !Float.isFinite(yaw)
                || !Float.isFinite(pitch);

        if (invalid || impossible) {

            return "Invalid Position, X: " + x + " Y: " + y + " Z: " + z + " Yaw: " + yaw + " Pitch: " + pitch;
        }

        return null;
    }
}
