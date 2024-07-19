package net.gteam.wave.managers.themes;

import net.gteam.wave.Wave;
import net.gteam.wave.utils.MiscUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class BaseTheme {

    private final FileConfiguration data;

    public BaseTheme(final Wave plugin, final String themeName) {

        final File file = new File(plugin.getDataFolder() + "/themes", themeName + ".yml");

        try {

            file.createNewFile();

        } catch (final IOException ignored) {}

        this.data = MiscUtils.loadConfigurationUTF_8(file);

        create();

        get().options().copyDefaults(true);

        try {

            this.data.save(file);

        } catch (final IOException ignored) {}
    }

    protected FileConfiguration get() {
        return this.data;
    }

    public abstract void create();
}
