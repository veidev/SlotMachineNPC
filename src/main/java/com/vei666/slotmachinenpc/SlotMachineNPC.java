package com.vei666.slotmachinenpc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SlotMachineNPC extends JavaPlugin implements Listener {

    private final Set<UUID> npcUUIDs = new HashSet<>();
    private final Map<UUID, Location> npcLocations = new HashMap<>();
    private final Map<UUID, SlotSession> sessions = new HashMap<>();

    private static final int[] REEL_SLOTS = {10, 11, 12, 13, 14};
    private static final int SPIN_BUTTON = 22;
    private static final int BET_DOWN = 21;
    private static final int BET_DISPLAY = 4;
    private static final int BET_UP = 23;
    private static final int INFO_SLOT = 15;
    private static final int BALANCE_SLOT = 5;

    // Weight = how often it appears (higher = more common)
    // Payout = multiplier on bet for 5-of-a-kind
    // Balanced for ~1/1000 chance for top wins
    private static final Symbol[] SYMBOLS = {
        new Symbol(Material.NETHERITE_INGOT, ChatColor.DARK_PURPLE + "Netherite", 8, 80),   // 8.5%, 80x
        new Symbol(Material.DIAMOND, ChatColor.AQUA + "Diamond", 10, 40),                        // 10.6%, 40x
        new Symbol(Material.GOLD_INGOT, ChatColor.GOLD + "Gold", 12, 20),                     // 12.8%, 20x
        new Symbol(Material.IRON_INGOT, ChatColor.GRAY + "Iron", 14, 8),                      // 14.9%, 8x
        new Symbol(Material.REDSTONE, ChatColor.RED + "Redstone", 14, 4),                      // 14.9%, 4x
        new Symbol(Material.COAL, ChatColor.DARK_GRAY + "Coal", 16, 2),                       // 17.0%, 2x
        new Symbol(Material.BARRIER, ChatColor.WHITE + "Bomb", 25, 0)                           // 26.6%, no payout
    };

    private static final int MAX_WIN_MULTIPLIER = 80; // max win = 80x bet
    private static final int[] BET_OPTIONS = {1, 2, 5, 10, 20, 50};

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("SlotMachineNPC v2 enabled! Use /slotnpc to spawn a villager.");
        loadNPCs();
    }

    private void loadNPCs() {
        saveDefaultConfig();
        if (getConfig().contains("npcs")) {
            for (String npcData : getConfig().getStringList("npcs")) {
                String[] parts = npcData.split(";");
                if (parts.length == 5) {
                    try {
                        UUID uuid = UUID.fromString(parts[0]);
                        String worldName = parts[1];
                        double x = Double.parseDouble(parts[2]);
                        double y = Double.parseDouble(parts[3]);
                        double z = Double.parseDouble(parts[4]);
                        
                        // Respawn NPC at saved location
                        if (Bukkit.getWorld(worldName) != null) {
                            Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
                            Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
                            villager.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Slot Machine");
                            villager.setCustomNameVisible(true);
                            villager.setProfession(Villager.Profession.NITWIT);
                            villager.setVillagerType(Villager.Type.PLAINS);
                            villager.setAI(false);
                            villager.setInvulnerable(true);
                            villager.setCollidable(false);
                            villager.setSilent(true);
                            npcUUIDs.add(villager.getUniqueId());
                            npcLocations.put(villager.getUniqueId(), loc);
                            getLogger().info("Respawned NPC at " + worldName + " " + x + "," + y + "," + z);
                        }
                    } catch (Exception e) {
                        getLogger().warning("Failed to load NPC: " + npcData);
                    }
                }
            }
        }
    }

    private void saveNPCs() {
        List<String> npcData = new ArrayList<>();
        for (UUID uuid : npcUUIDs) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) {
                Location loc = e.getLocation();
                String data = uuid + ";" + loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ();
                npcData.add(data);
            }
        }
        getConfig().set("npcs", npcData);
        saveConfig();
    }

    @Override
    public void onDisable() {
        saveNPCs();
        for (UUID uuid : npcUUIDs) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        }
        npcUUIDs.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("slotnpc")) {
            if (!player.hasPermission("slotnpc.admin")) {
                player.sendMessage(ChatColor.RED + "Yetkin yok!");
                return true;
            }
            Location loc = player.getLocation();
            Villager villager = (Villager) player.getWorld().spawnEntity(loc, EntityType.VILLAGER);
            villager.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Slot Machine");
            villager.setCustomNameVisible(true);
            villager.setProfession(Villager.Profession.NITWIT);
            villager.setVillagerType(Villager.Type.PLAINS);
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setCollidable(false);
            villager.setSilent(true);
            npcUUIDs.add(villager.getUniqueId());
            npcLocations.put(villager.getUniqueId(), loc);
            player.sendMessage(ChatColor.GREEN + "Slot Machine NPC created! Right-click the villager.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("slotnpcremove")) {
            if (!player.hasPermission("slotnpc.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }
            double closest = Double.MAX_VALUE;
            UUID toRemove = null;
            for (UUID uuid : npcUUIDs) {
                Entity e = Bukkit.getEntity(uuid);
                if (e != null && e.getWorld().equals(player.getWorld())) {
                    double dist = e.getLocation().distance(player.getLocation());
                    if (dist < closest && dist < 10) {
                        closest = dist;
                        toRemove = uuid;
                    }
                }
            }
            if (toRemove != null) {
                Entity e = Bukkit.getEntity(toRemove);
                if (e != null) e.remove();
                npcUUIDs.remove(toRemove);
                player.sendMessage(ChatColor.GREEN + "Nearest Slot Machine NPC removed.");
            } else {
                player.sendMessage(ChatColor.RED + "No Slot Machine NPC nearby.");
            }
            return true;
        }

        return true;
    }

    @EventHandler
    public void onVillagerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        if (!npcUUIDs.contains(villager.getUniqueId())) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (!player.hasPermission("slotnpc.play")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to play slots!");
            return;
        }

        openSlotMachine(player);
    }

    private void openSlotMachine(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "" + ChatColor.BOLD + "Slot Machine");

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        for (int slot : REEL_SLOTS) {
            inv.setItem(slot, createItem(Material.WHITE_STAINED_GLASS_PANE, ChatColor.WHITE + "?"));
        }

        inv.setItem(BET_DISPLAY, createItem(Material.EMERALD, ChatColor.GREEN + "Bet: 1 Emerald"));
        inv.setItem(BET_DOWN, createItem(Material.RED_CONCRETE, ChatColor.RED + "- Decrease Bet"));
        inv.setItem(BET_UP, createItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+ Increase Bet"));

        inv.setItem(INFO_SLOT, createItem(Material.PAPER,
                ChatColor.YELLOW + "" + ChatColor.BOLD + "=== SLOT PAYOUTS ===",
                ChatColor.DARK_PURPLE + "Netherite %8.1 | 5x:80x 4x:16x 3x:6x",
                ChatColor.AQUA + "Diamond %10.1 | 5x:40x 4x:8x 3x:3x",
                ChatColor.GOLD + "Gold %12.1 | 5x:20x 4x:4x 3x:2x",
                ChatColor.GRAY + "Iron %14.1 | 5x:8x 4x:3x 3x:2x",
                ChatColor.RED + "Redstone %14.1 | 5x:4x 4x:3x 3x:2x",
                ChatColor.DARK_GRAY + "Coal %16.2 | 5x:2x 4x:2x 3x:2x",
                ChatColor.WHITE + "Bomb %25.3 | No payout!",
                ChatColor.YELLOW + "2 match = 1.5x always",
                ChatColor.GOLD + "" + ChatColor.BOLD + "Max Win: 80x bet!"));

        inv.setItem(BALANCE_SLOT, createItem(Material.GOLD_INGOT,
                ChatColor.GOLD + "Your Emeralds: " + countEmeralds(player)));

        inv.setItem(SPIN_BUTTON, createItem(Material.LIME_CONCRETE, ChatColor.GREEN + "" + ChatColor.BOLD + "SPIN!"));

        player.openInventory(inv);
        sessions.put(player.getUniqueId(), new SlotSession());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.GOLD + "" + ChatColor.BOLD + "Slot Machine")) return;

        event.setCancelled(true);

        SlotSession session = sessions.get(player.getUniqueId());
        if (session == null || session.spinning) return;

        int slot = event.getRawSlot();
        if (slot >= 27) return;

        if (slot == SPIN_BUTTON) {
            int bet = BET_OPTIONS[session.betIndex];
            if (!hasEmeralds(player, bet)) {
                player.sendMessage(ChatColor.RED + "Not enough emeralds! Need " + bet + " emeralds.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            removeEmeralds(player, bet);
            player.sendMessage(ChatColor.YELLOW + String.valueOf(bet) + " Emeralds taken. Good luck!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);
            spin(player, session, bet);
        } else if (slot == BET_UP) {
            if (session.betIndex < BET_OPTIONS.length - 1) {
                session.betIndex++;
                updateBetDisplay(player, session);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
            }
        } else if (slot == BET_DOWN) {
            if (session.betIndex > 0) {
                session.betIndex--;
                updateBetDisplay(player, session);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.5f);
            }
        }
    }

    private void updateBetDisplay(Player player, SlotSession session) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        int bet = BET_OPTIONS[session.betIndex];
        inv.setItem(BET_DISPLAY, createItem(Material.EMERALD, ChatColor.GREEN + "Bet: " + bet + " Emerald"));
        inv.setItem(BALANCE_SLOT, createItem(Material.GOLD_INGOT,
                ChatColor.GOLD + "Your Emeralds: " + countEmeralds(player)));
    }

    private void spin(Player player, SlotSession session, int bet) {
        session.spinning = true;
        Inventory inv = player.getOpenInventory().getTopInventory();

        inv.setItem(SPIN_BUTTON, createItem(Material.ORANGE_CONCRETE, ChatColor.GOLD + "" + ChatColor.BOLD + "Spinning..."));
        inv.setItem(BET_DOWN, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(BET_UP, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));

        Symbol[] finalResults = new Symbol[5];
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            finalResults[i] = pickWeightedSymbol(random);
        }

        final int totalTicks = 50;
        final boolean[] reelStopped = new boolean[5];
        final int[] stopTimes = {15, 25, 35, 42, 50};

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= totalTicks) {
                    for (int i = 0; i < 5; i++) {
                        inv.setItem(REEL_SLOTS[i], createSymbolItem(finalResults[i]));
                    }
                    PayoutResult result = calculatePayout(finalResults, bet);
                    processPayout(player, result, inv, session, bet);
                    this.cancel();
                    return;
                }

                for (int i = 0; i < 5; i++) {
                    if (tick >= stopTimes[i]) {
                        if (!reelStopped[i]) {
                            reelStopped[i] = true;
                            inv.setItem(REEL_SLOTS[i], createSymbolItem(finalResults[i]));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1f + (i * 0.2f));
                        }
                    } else {
                        Symbol randomSym = pickWeightedSymbol(random);
                        inv.setItem(REEL_SLOTS[i], createSymbolItem(randomSym));
                    }
                }

                if (tick % 3 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.1f, 2f);
                }

                tick++;
            }
        }.runTaskTimer(this, 0L, 2L);
    }

    private Symbol pickWeightedSymbol(Random random) {
        int totalWeight = 0;
        for (Symbol s : SYMBOLS) totalWeight += s.weight;
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Symbol s : SYMBOLS) {
            cumulative += s.weight;
            if (roll < cumulative) return s;
        }
        return SYMBOLS[SYMBOLS.length - 1];
    }

    private PayoutResult calculatePayout(Symbol[] results, int bet) {
        Map<Symbol, Integer> counts = new HashMap<>();
        int bombCount = 0;
        for (Symbol s : results) {
            counts.merge(s, 1, Integer::sum);
            if (s.material == Material.BARRIER) bombCount++;
        }

        // 3+ bombs = total loss
        if (bombCount >= 3) {
            return new PayoutResult(PayoutType.BOMB, 0, "3 Bombs! Total loss!");
        }

        int maxMatch = 0;
        Symbol matchedSymbol = null;
        for (Map.Entry<Symbol, Integer> entry : counts.entrySet()) {
            if (entry.getKey().material == Material.BARRIER) continue;
            if (entry.getValue() > maxMatch) {
                maxMatch = entry.getValue();
                matchedSymbol = entry.getKey();
            }
        }

        int maxWin = bet * MAX_WIN_MULTIPLIER;

        if (maxMatch >= 5) {
            // 5 of a kind - JACKPOT (very rare)
            int payout = Math.min(bet * matchedSymbol.payoutMultiplier, maxWin);
            return new PayoutResult(PayoutType.JACKPOT, payout,
                    "JACKPOT! 5x " + matchedSymbol.displayName + "! " + payout + " Emeralds! (" + matchedSymbol.payoutMultiplier + "x)");
        } else if (maxMatch == 4) {
            // 4 of a kind - big win (rare)
            int mult = matchedSymbol.payoutMultiplier / 5;
            mult = Math.max(mult, 3);
            int payout = Math.min(bet * mult, maxWin);
            return new PayoutResult(PayoutType.BIG_WIN, payout,
                    "4x " + matchedSymbol.displayName + "! " + payout + " Emeralds! (" + mult + "x)");
        } else if (maxMatch == 3) {
            // 3 of a kind - moderate win
            int mult = matchedSymbol.payoutMultiplier / 12;
            mult = Math.max(mult, 2);
            int payout = Math.min(bet * mult, maxWin);
            return new PayoutResult(PayoutType.WIN, payout,
                    "3x " + matchedSymbol.displayName + "! " + payout + " Emeralds! (" + mult + "x)");
        } else if (maxMatch == 2) {
            // 2 of a kind - always pay 1.5x bet (rounded up)
            int payout = Math.max(1, (int) Math.ceil(bet * 1.5));
            payout = Math.min(payout, maxWin);
            return new PayoutResult(PayoutType.PARTIAL, payout,
                    "2x " + matchedSymbol.displayName + "! " + payout + " Emeralds! (1.5x)");
        } else if (bombCount >= 2) {
            return new PayoutResult(PayoutType.LOSS, 0, "2 Bombs! Unlucky...");
        } else {
            return new PayoutResult(PayoutType.LOSS, 0, "No match. Try again!");
        }
    }

    private void processPayout(Player player, PayoutResult result, Inventory inv, SlotSession session, int bet) {
        switch (result.type) {
            case JACKPOT:
                giveEmeralds(player, result.payout);
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + result.message);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                for (int i = 0; i < 5; i++) {
                    final int delay = i * 4;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f + (delay * 0.05f));
                        }
                    }.runTaskLater(this, delay);
                }
                break;
            case BIG_WIN:
                giveEmeralds(player, result.payout);
                player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + result.message);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                break;
            case WIN:
                giveEmeralds(player, result.payout);
                player.sendMessage(ChatColor.GREEN + result.message);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
                break;
            case PARTIAL:
                giveEmeralds(player, result.payout);
                player.sendMessage(ChatColor.YELLOW + result.message);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 0.5f, 1f);
                break;
            case BOMB:
                player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + result.message);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.5f);
                break;
            case LOSS:
                player.sendMessage(ChatColor.RED + result.message);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
                break;
        }

        inv.setItem(SPIN_BUTTON, createItem(Material.LIME_CONCRETE, ChatColor.GREEN + "" + ChatColor.BOLD + "SPIN!"));
        inv.setItem(BET_DOWN, createItem(Material.RED_CONCRETE, ChatColor.RED + "- Decrease Bet"));
        inv.setItem(BET_UP, createItem(Material.LIME_CONCRETE, ChatColor.GREEN + "+ Increase Bet"));
        inv.setItem(BALANCE_SLOT, createItem(Material.GOLD_INGOT,
                ChatColor.GOLD + "Your Emeralds: " + countEmeralds(player)));

        session.spinning = false;
    }

    private int countEmeralds(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private boolean hasEmeralds(Player player, int amount) {
        return countEmeralds(player) >= amount;
    }

    private void removeEmeralds(Player player, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.EMERALD) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
        player.updateInventory();
    }

    private void giveEmeralds(Player player, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int stack = Math.min(remaining, 64);
            player.getInventory().addItem(new ItemStack(Material.EMERALD, stack));
            remaining -= stack;
        }
        player.updateInventory();
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSymbolItem(Symbol symbol) {
        ItemStack item = new ItemStack(symbol.material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(symbol.displayName);
            int totalWeight = 0;
            for (Symbol s : SYMBOLS) totalWeight += s.weight;
            double chance = (symbol.weight * 100.0) / totalWeight;
            String chanceStr = String.format("%.1f%%", chance);
            if (symbol.payoutMultiplier > 0) {
                int mult5 = symbol.payoutMultiplier;
                int mult4 = Math.max(symbol.payoutMultiplier / 5, 3);
                int mult3 = Math.max(symbol.payoutMultiplier / 12, 2);
                meta.setLore(Arrays.asList(
                        ChatColor.GOLD + "5x: " + mult5 + "x bet",
                        ChatColor.AQUA + "4x: " + mult4 + "x bet",
                        ChatColor.GREEN + "3x: " + mult3 + "x bet",
                        ChatColor.GRAY + "2x: 1.5x bet",
                        ChatColor.YELLOW + "Spawn chance: " + chanceStr
                ));
            } else {
                meta.setLore(Arrays.asList(
                        ChatColor.RED + "Bomb! No payout",
                        ChatColor.YELLOW + "Spawn chance: " + chanceStr
                ));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static class Symbol {
        final Material material;
        final String displayName;
        final int weight;
        final int payoutMultiplier;

        Symbol(Material material, String displayName, int weight, int payoutMultiplier) {
            this.material = material;
            this.displayName = displayName;
            this.weight = weight;
            this.payoutMultiplier = payoutMultiplier;
        }
    }

    private enum PayoutType {
        JACKPOT, BIG_WIN, WIN, PARTIAL, LOSS, BOMB
    }

    private static class PayoutResult {
        final PayoutType type;
        final int payout;
        final String message;

        PayoutResult(PayoutType type, int payout, String message) {
            this.type = type;
            this.payout = payout;
            this.message = message;
        }
    }

    private static class SlotSession {
        boolean spinning = false;
        int betIndex = 0;
    }
}
