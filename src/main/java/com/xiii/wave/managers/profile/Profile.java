package com.xiii.wave.managers.profile;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.xiii.wave.Wave;
import com.xiii.wave.checks.CheckHolder;
import com.xiii.wave.enums.Permissions;
import com.xiii.wave.exempt.Exempt;
import com.xiii.wave.files.Config;
import com.xiii.wave.managers.threads.ProfileThread;
import com.xiii.wave.playerdata.data.impl.*;
import com.xiii.wave.processors.packet.client.ClientPlayPacket;
import com.xiii.wave.processors.packet.server.ServerPlayPacket;
import com.xiii.wave.utils.TaskUtils;
import com.xiii.wave.utils.versionutils.VersionUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Profile {

    //-------------------------------------------
    private final ActionData actionData;
    private final CombatData combatData;
    private final ConnectionData connectionData;
    private final FishingData fishingData;
    private final MovementData movementData;
    private final RotationData rotationData;
    private final TeleportData teleportData;
    private final VelocityData velocityData;
    private final VehicleData vehicleData;
    //-------------------------------------------

    //--------------------------------------
    private final CheckHolder checkHolder;
    //--------------------------------------

    //--------------------------------------
    private final String version;
    private final ClientVersion clientVersion;
    private String clientBrand = "Unknown";
    private final boolean bypass;
    //--------------------------------------

    //------------------------------------------
    private final ProfileThread profileThread;
    private final Player player;
    private final UUID uuid;
    //------------------------------------------

    //---------------------------
    private final Exempt exempt;
    //---------------------------

    public Profile(final Player player) {

        //Player Object
        this.player = player;

        //UUID
        this.uuid = player.getUniqueId();

        //Version
        this.version = VersionUtils.getClientVersionAsString(player);
        this.clientVersion = VersionUtils.getClientVersion(player);

        //Bypass
        this.bypass = !Config.Setting.DISABLE_BYPASS_PERMISSION.getBoolean() && player.hasPermission(Permissions.BYPASS.getPermission());

        //Data
        this.actionData = new ActionData(this);
        this.combatData = new CombatData(this);
        this.connectionData = new ConnectionData();
        this.movementData = new MovementData(this);
        this.fishingData = new FishingData(this);
        this.rotationData = new RotationData(this);
        this.teleportData = new TeleportData();
        this.velocityData = new VelocityData();
        this.vehicleData = new VehicleData(this);

        //Check Holder
        this.checkHolder = new CheckHolder(this);

        //Exempt
        this.exempt = new Exempt(this);

        //Thread
        this.profileThread = Wave.getInstance().getThreadManager().getAvailableProfileThread();

        //Initialize Checks
        reloadChecks();
    }

    public boolean isBypassing() {
        return bypass;
    }

    public void handle(final ClientPlayPacket clientPlayPacket) {

        if (this.player == null) return;

        this.connectionData.process(clientPlayPacket);
        this.actionData.process(clientPlayPacket);
        this.combatData.process(clientPlayPacket);
        this.fishingData.process(clientPlayPacket);
        this.movementData.process(clientPlayPacket);
        this.rotationData.process(clientPlayPacket);
        this.teleportData.process(clientPlayPacket);
        this.velocityData.process(clientPlayPacket);
        this.vehicleData.process(clientPlayPacket);

        this.exempt.handleExempts(clientPlayPacket.getTimeStamp());

        this.checkHolder.runChecks(clientPlayPacket);
    }

    public void handle(final ServerPlayPacket serverPlayPacket) {

        if (this.player == null) return;

        this.connectionData.process(serverPlayPacket);
        this.actionData.process(serverPlayPacket);
        this.combatData.process(serverPlayPacket);
        this.fishingData.process(serverPlayPacket);
        this.movementData.process(serverPlayPacket);
        this.rotationData.process(serverPlayPacket);
        this.teleportData.process(serverPlayPacket);
        this.velocityData.process(serverPlayPacket);
        this.vehicleData.process(serverPlayPacket);

        this.exempt.handleExempts(serverPlayPacket.getTimeStamp());

        this.checkHolder.runChecks(serverPlayPacket);
    }

    public void kickPlayer(final String reason) {

        if (this.player == null) return;

        TaskUtils.task(() -> this.player.kickPlayer(reason));
    }

    public void handleTick(long currentTime) {
        //Handle the tick here
    }

    public String getVersionAsString() {
        return version;
    }

    public ClientVersion getVersion() {
        return clientVersion;
    }

    public String getClientBrand() {
        return clientBrand;
    }

    public void setClientBrand(final String clientBrand) {
        this.clientBrand = clientBrand;
    }

    public Player getPlayer() {
        return this.player;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void reloadChecks() {
        this.checkHolder.registerAll();
    }

    public TeleportData getTeleportData() { return teleportData; }

    public ActionData getActionData() {
        return actionData;
    }

    public CombatData getCombatData() {
        return combatData;
    }

    public ConnectionData getConnectionData() {
        return connectionData;
    }

    public FishingData getFishingData() {
        return fishingData;
    }

    public MovementData getMovementData() {
        return movementData;
    }

    public RotationData getRotationData() {
        return rotationData;
    }

    public VelocityData getVelocityData() {
        return velocityData;
    }

    public VehicleData getVehicleData() {
        return vehicleData;
    }

    public CheckHolder getCheckHolder() {
        return checkHolder;
    }

    public Exempt isExempt() {
        return exempt;
    }

    public ProfileThread getProfileThread() {
        return profileThread;
    }
}
