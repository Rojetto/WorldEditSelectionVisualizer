package com.rojel.wesv;

import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldEditSelectionChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Region region;

    public WorldEditSelectionChangeEvent(Player player, Region region) {
        this.player = player;
        this.region = region;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Region getRegion() {
        return region;
    }
}
