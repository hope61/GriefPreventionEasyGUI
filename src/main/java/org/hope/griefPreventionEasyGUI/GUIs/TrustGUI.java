package org.hope.griefPreventionEasyGUI.GUIs;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;
import org.hope.griefPreventionEasyGUI.listeners.ChatInputListener;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrustGUI {
    private final GriefPreventionEasyGUI plugin;
    private final Player player;
    private final Claim claim;
    private final Origin origin;
    private final Window window;

    public TrustGUI(GriefPreventionEasyGUI plugin, Player player, Claim claim, Origin origin) {
        this.plugin = plugin;
        this.player = player;
        this.claim = claim;
        this.origin = origin;

        FileConfiguration lang = plugin.getLang();

        // Gather all trusted players with their trust levels
        ArrayList<String> buildList = new ArrayList<>(), containerList = new ArrayList<>(),
                accessList = new ArrayList<>(), manageList = new ArrayList<>();
        claim.getPermissions(buildList, containerList, accessList, manageList);

        List<Item> trustedItems = new ArrayList<>();

        for (String entry : buildList) {
            trustedItems.add(createTrustedItem(entry, "§eBuild Access"));
        }
        for (String entry : containerList) {
            trustedItems.add(createTrustedItem(entry, "§6Container Access"));
        }
        for (String entry : accessList) {
            trustedItems.add(createTrustedItem(entry, "§bBasic Access"));
        }
        for (String entry : manageList) {
            trustedItems.add(createTrustedItem(entry, "§cFull Access"));
        }

        Item backItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.back", "§7Back"))))
                .addClickHandler(click -> new ClaimInfoGUI(plugin, player, claim, origin).getWindow().open())
                .build();

        Item addPlayerItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.EMERALD)
                        .setName(text(lang.getString("gui.trust.add_name", "§a§lAdd Player")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§7Click and type the name in chat."),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> startAddPlayer())
                .build();

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        Item prevItem = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.previous_page", "§7Previous Page"))))
                .addClickHandler((item, gui, click) -> {
                    if (gui.getPage() > 0) gui.setPage(gui.getPage() - 1);
                })
                .build();

        Item nextItem = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.next_page", "§7Next Page"))))
                .addClickHandler((item, gui, click) -> {
                    if (gui.getPage() < gui.getPageCount() - 1) gui.setPage(gui.getPage() + 1);
                })
                .build();

        PagedGui<Item> gui = PagedGui.itemsBuilder()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "< . . A B . . . >"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', prevItem)
                .addIngredient('>', nextItem)
                .addIngredient('A', addPlayerItem)
                .addIngredient('B', backItem)
                .addIngredient('.', filler)
                .setContent(trustedItems)
                .build();

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.trust.title", "§6§lTrusted Players")))
                .setUpperGui(gui)
                .setViewer(player)
                .build();
    }

    public Window getWindow() {
        return window;
    }

    private Item createTrustedItem(String entry, String trustLevel) {
        String displayName = resolveName(entry);
        Material headMaterial = displayName.equals(entry) && isUUID(entry)
                ? Material.SKELETON_SKULL : Material.PLAYER_HEAD;

        return Item.builder()
                .setItemProvider(new ItemBuilder(headMaterial)
                        .setName(text("§f" + displayName))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§eLevel: " + trustLevel),
                                text("§cClick to remove"),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> {
                    claim.dropPermission(entry);
                    GriefPrevention.instance.dataStore.saveClaim(claim);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                    new TrustGUI(plugin, player, claim, origin).getWindow().open();
                })
                .build();
    }

    private String resolveName(String entry) {
        try {
            UUID uuid = UUID.fromString(entry);
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            return name != null ? name : entry;
        } catch (IllegalArgumentException e) {
            return entry;
        }
    }

    private boolean isUUID(String s) {
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void startAddPlayer() {
        FileConfiguration lang = plugin.getLang();
        player.closeInventory();

        player.sendMessage(text(lang.getString("gui.trust.add_prompt",
                "§eType the name of the player you want to trust:")));

        long timeoutTicks = plugin.getConfig().getInt("chat-input-timeout-seconds", 60) * 20L;
        new ChatInputListener(plugin, player,
                input -> !input.isBlank() && input.length() <= 16 && input.matches("[a-zA-Z0-9_]+"),
                input -> new TrustLevelGUI(plugin, player, claim, input, origin).getWindow().open(),
                lang.getString("gui.trust.add_error", "§cInvalid name! Try again."),
                lang.getString("gui.messages.input_cancelled", "§cInput cancelled."),
                timeoutTicks
        );
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }
}
