package com.rickweek.personalemclink.common.util;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Batches EMC syncs: at most one packet per player per tick.
 * <p>
 * Before, every single extraction (pipes pull from the links constantly) sent a sync packet,
 * which flooded the network and made ProjectE log "RECEIVED TRANSMUTATION EMC DATA CLIENTSIDE"
 * every time on the client.
 */
public final class EmcSyncScheduler {
    private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();

    private EmcSyncScheduler() {}

    public static void markDirty(ServerPlayer player) {
        PENDING.add(player.getUUID());
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) return;

        PlayerList players = event.getServer().getPlayerList();
        for (UUID id : PENDING) {
            ServerPlayer player = players.getPlayer(id);
            if (player != null) {
                IKnowledgeProvider provider = EmcHelper.getKnowledge(player);
                if (provider != null) provider.syncEmc(player);
            }
        }
        PENDING.clear();
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
