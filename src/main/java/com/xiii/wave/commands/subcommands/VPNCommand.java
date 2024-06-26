package com.xiii.wave.commands.subcommands;

import com.xiii.wave.Wave;
import com.xiii.wave.commands.SubCommand;
import com.xiii.wave.enums.MsgType;
import com.xiii.wave.enums.Permissions;
import com.xiii.wave.utils.HTTPUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class VPNCommand extends SubCommand {

    private final Wave plugin;

    public VPNCommand(Wave plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "vpn";
    }

    @Override
    protected String getDescription() {
        return "Check for VPN/Proxy on a player";
    }

    @Override
    protected String getSyntax() {
        return "vpn";
    }

    @Override
    protected String getPermission() {
        return Permissions.VPN_COMMAND.getPermission();
    }

    @Override
    protected int maxArguments() {
        return 2;
    }

    @Override
    protected boolean canConsoleExecute() {
        return true;
    }

    @Override
    protected void perform(CommandSender sender, String[] args) {

        if(args[1] != null && args[1].length() > 0) {

            Player target = Bukkit.getPlayer(args[1]);

            if (target != null) {

                final String vpnKey = Wave.getInstance().getConfiguration().getString("vpn-checker-key");

                if (vpnKey != null && !vpnKey.equalsIgnoreCase("DISABLED")) {

                    final String httpResponse = HTTPUtils.readUrl("https://proxycheck.io/v2/" + Objects.requireNonNull(target.getAddress()).getHostName() + "?key=" + vpnKey + "&vpn=3");
                    //final String riskLevel = httpResponse.substring(httpResponse.indexOf("\"risk\":"));
                    final String riskLevel = "Unknown";
                    // TODO: Fix riskLevel

                    if (httpResponse.contains("\"proxy\": \"yes\"") || httpResponse.contains("\"vpn\": \"yes\"") || httpResponse.contains("\"WaveACVPNCheckResult\": \"REJECTED\"") || httpResponse.contains("blacklist") || httpResponse.contains("compromised")) {

                        sender.sendMessage(MsgType.PREFIX.getMessage() + " §cVPN/Proxy detected for §3" + target.getName() + " §crisk level is §9" + riskLevel);
                    } else
                        sender.sendMessage(MsgType.PREFIX.getMessage() + " §aNo VPN/Proxy were detected for §3" + target.getName() + " §arisk level is §9" + riskLevel);
                } else sender.sendMessage(MsgType.PREFIX.getMessage() + " §cError! VPN checker is disabled.");
            } else sender.sendMessage(MsgType.PREFIX.getMessage() + " §cError! Player isn't valid.");
        } else sender.sendMessage(MsgType.PREFIX.getMessage() + " §cError! Player isn't valid.");
    }
}
