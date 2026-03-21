package org.hope.griefPreventionEasyGUI.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChatInputListener implements Listener {
    private final GriefPreventionEasyGUI plugin;
    private final Player player;
    private final Predicate<String> validator;
    private final Consumer<String> onSuccess;
    private final String errorMessage;
    private final String timeoutMessage;
    private final int maxFailures;
    private final BukkitTask timeoutTask;
    private final AtomicInteger failCount = new AtomicInteger(0);
    private volatile boolean unregistered = false;

    public ChatInputListener(GriefPreventionEasyGUI plugin, Player player, Predicate<String> validator,
                             Consumer<String> onSuccess, String errorMessage, String timeoutMessage,
                             long timeoutTicks) {
        this.plugin = plugin;
        this.player = player;
        this.validator = validator;
        this.onSuccess = onSuccess;
        this.errorMessage = errorMessage;
        this.timeoutMessage = timeoutMessage;
        this.maxFailures = plugin.getConfig().getInt("chat-input-max-failures", 2);

        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!unregistered) {
                player.sendMessage(timeoutMessage);
                unregister();
            }
        }, timeoutTicks);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
        if (unregistered) return;

        event.setCancelled(true);
        String input = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (validator.test(input)) {
            timeoutTask.cancel();
            unregister();
            Bukkit.getScheduler().runTask(plugin, () -> onSuccess.accept(input));
        } else {
            int fails = failCount.incrementAndGet();
            if (fails >= maxFailures) {
                timeoutTask.cancel();
                player.sendMessage(timeoutMessage);
                unregister();
            } else {
                player.sendMessage(errorMessage);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
        timeoutTask.cancel();
        unregister();
    }

    private synchronized void unregister() {
        if (!unregistered) {
            unregistered = true;
            HandlerList.unregisterAll(this);
        }
    }
}
