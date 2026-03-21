package org.hope.griefPreventionEasyGUI;

import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hope.griefPreventionEasyGUI.commands.ClaimHelpCommand;
import org.hope.griefPreventionEasyGUI.listeners.ClaimVisualizer;
import org.hope.griefPreventionEasyGUI.listeners.PlayerJoinListener;

import java.io.File;
import java.io.IOException;

public final class GriefPreventionEasyGUI extends JavaPlugin {
    private FileConfiguration langConfig;
    private FileConfiguration cooldownsConfig;
    private FileConfiguration onboardedConfig;
    private File cooldownsFile;
    private File onboardedFile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadLangConfig();
        cooldownsFile = new File(getDataFolder(), "cooldowns.yml");
        onboardedFile = new File(getDataFolder(), "onboarded.yml");
        reloadCooldownsConfig();
        reloadOnboardedConfig();
        new Metrics(this, 30352);

        var claimhelpCmd = getCommand("claimhelp");
        if (claimhelpCmd != null) claimhelpCmd.setExecutor(new ClaimHelpCommand(this));

        var reloadCmd = getCommand("gpegui-reload");
        if (reloadCmd != null) reloadCmd.setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("gpegui.admin")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }
            reloadConfig();
            reloadLangConfig();
            reloadCooldownsConfig();
            reloadOnboardedConfig();
            sender.sendMessage(getLang().getString("gui.messages.reloaded", "§aGriefPreventionEasyGUI configuration reloaded!"));
            return true;
        });

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        ClaimVisualizer visualizer = new ClaimVisualizer(this);
        long intervalTicks = getConfig().getLong("visualizer-interval-ticks", 10);
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (!isGlobalVisualizerEnabled()) return;
            for (Player player : getServer().getOnlinePlayers()) {
                if (isVisualizerEnabled(player)) {
                    visualizer.renderForPlayer(player);
                }
            }
        }, 0L, intervalTicks);
    }

    @Override
    public void onDisable() {
        saveCooldownsConfig();
        saveOnboardedConfig();
    }

    public boolean isGlobalVisualizerEnabled() {
        return getConfig().getBoolean("visualizer-enabled", true);
    }

    public boolean isVisualizerEnabled(Player player) {
        return getCooldowns().getBoolean("visualizer." + player.getUniqueId(), true);
    }

    public void setVisualizerEnabled(Player player, boolean enabled) {
        getCooldowns().set("visualizer." + player.getUniqueId(), enabled);
        saveCooldownsConfig();
    }

    public void reloadLangConfig() {
        String language = getConfig().getString("language", "en");
        String langFileName = language.equals("en") ? "lang.yml" : "lang_" + language + ".yml";
        String langResource = langFileName;

        // Always save both default lang files
        saveResourceIfMissing("lang.yml");
        saveResourceIfMissing("lang_bg.yml");

        File langFile = new File(getDataFolder(), langFileName);
        if (!langFile.exists()) {
            getLogger().warning("Language file '" + langFileName + "' not found, falling back to lang.yml");
            langFile = new File(getDataFolder(), "lang.yml");
            langResource = "lang.yml";
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
        try (java.io.InputStream inputStream = getResource(langResource)) {
            if (inputStream != null) {
                YamlConfiguration internal = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8));
                for (String key : internal.getKeys(true)) {
                    if (!langConfig.contains(key)) {
                        langConfig.set(key, internal.get(key));
                    }
                }
                langConfig.save(langFile);
            }
        } catch (IOException e) {
            getLogger().warning("Could not merge lang keys!");
        }
    }

    private void saveResourceIfMissing(String name) {
        if (!new File(getDataFolder(), name).exists()) {
            saveResource(name, false);
        }
    }

    public void reloadCooldownsConfig() {
        cooldownsConfig = YamlConfiguration.loadConfiguration(cooldownsFile);
    }

    public void saveCooldownsConfig() {
        try {
            cooldownsConfig.save(cooldownsFile);
        } catch (IOException e) {
            getLogger().severe("Could not save cooldowns.yml!");
        }
    }

    public void reloadOnboardedConfig() {
        onboardedConfig = YamlConfiguration.loadConfiguration(onboardedFile);
    }

    public void saveOnboardedConfig() {
        try {
            onboardedConfig.save(onboardedFile);
        } catch (IOException e) {
            getLogger().severe("Could not save onboarded.yml!");
        }
    }

    public FileConfiguration getLang() {
        return langConfig;
    }

    public FileConfiguration getCooldowns() {
        return cooldownsConfig;
    }

    public FileConfiguration getOnboarded() {
        return onboardedConfig;
    }
}
