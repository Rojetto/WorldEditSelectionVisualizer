package com.rojel.wesv;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.darkblade12.particleeffect.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

public class ProtocolLibHelper {
    private final JavaPlugin plugin;
    private final Configuration config;
    private final boolean protocolLibInstalled;
    private ProtocolManager protocolManager;
    private EnumWrappers.Particle particleType;

    public ProtocolLibHelper(JavaPlugin plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;

        this.protocolLibInstalled = plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null;
        if (protocolLibInstalled) {
            protocolManager = ProtocolLibrary.getProtocolManager();
            particleType = fromParticleEffect(config.particle());
        }
    }

    public void sendParticle(Player player, Location loc) {
        PacketContainer particle = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        particle.getParticles().write(0, particleType);
        particle.getBooleans().write(0, true);
        particle.getFloat().write(0, (float) loc.getX());
        particle.getFloat().write(1, (float) loc.getY());
        particle.getFloat().write(2, (float) loc.getZ());

        try {
            protocolManager.sendServerPacket(player, particle);
        } catch (InvocationTargetException e) {
            plugin.getLogger().warning("Failed to send particle.");
        }
    }

    private EnumWrappers.Particle fromParticleEffect(ParticleEffect effect) {
        EnumWrappers.Particle particle = EnumWrappers.Particle.getById(effect.getId());
        if (particle != null)
            return particle;

        return EnumWrappers.Particle.REDSTONE;
    }

    public boolean canUseProtocolLib() {
        return protocolLibInstalled && config.useProtocolLib();
    }

    public boolean isProtocolLibInstalled() {
        return protocolLibInstalled;
    }
}
