package net.glowstone.events.player;

import net.glowstone.entity.GlowPlayer;
import net.glowstone.entity.meta.ClientSettings;
import net.glowstone.net.GlowSession;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event that is called when a player's client settings are changed.
 */
public class PlayerClientOptionsChangeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final GlowSession glowSession;
    private final GlowPlayer glowPlayer;
    private final String locale;
    private final int viewDistance;
    private final int chatFlags;
    private final boolean chatColors;
    private final int skinParts;
    private final int mainHand;
    private ClientSettings clientSettings;
    private boolean cancelled = false;

    public PlayerClientOptionsChangeEvent(@NotNull GlowSession session, @NotNull String locale, int viewDistance,
                                          int chatFlags, boolean chatColors, int skinParts, int mainHand) {
        super(session.getPlayer());
        this.glowSession = session;
        this.glowPlayer = session.getPlayer();
        this.clientSettings = new ClientSettings(locale, viewDistance, chatFlags, chatColors,
                skinParts, mainHand);
        this.locale = locale;
        this.viewDistance = viewDistance;
        this.chatFlags = chatFlags;
        this.chatColors = chatColors;
        this.skinParts = skinParts;
        this.mainHand = mainHand;
    }

    public PlayerClientOptionsChangeEvent(@NotNull GlowSession session, @NotNull ClientSettings clientSettings) {
        super(session.getPlayer());
        this.glowSession = session;
        this.glowPlayer = session.getPlayer();
        this.clientSettings = clientSettings;
        this.locale = clientSettings.getLocale();
        this.viewDistance = clientSettings.getViewDistance();
        this.chatFlags = clientSettings.getChatFlags();
        this.chatColors = clientSettings.isChatColors();
        this.skinParts = clientSettings.getSkinFlags();
        this.mainHand = clientSettings.getMainHand();
    }

    public ClientSettings getClientSettings() {
        return clientSettings;
    }

    public void setClientSettings(ClientSettings clientSettings) {
        this.clientSettings = clientSettings;
    }

    @NotNull
    public String getLocale() {
        return locale;
    }

    public boolean hasLocaleChanged() {
        return !locale.equals(glowPlayer.getSettings().getLocale());
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public boolean hasViewDistanceChanged() {
        return viewDistance != glowPlayer.getSettings().getViewDistance();
    }

    public boolean getChatVisibility() {
        return chatFlags == ClientSettings.CHAT_ENABLED;
    }

    public boolean hasChatVisibilityChanged() {
        return chatFlags != glowPlayer.getSettings().getChatFlags();
    }

    public boolean hasChatColorsEnabled() {
        return chatColors;
    }

    public boolean hasChatColorsEnabledChanged() {
        return chatColors != glowPlayer.getSettings().isChatColors();
    }

    public int getSkinParts() {
        return skinParts;
    }

    public boolean hasSkinPartsChanged() {
        return skinParts != glowPlayer.getSettings().getSkinFlags();
    }


    public int getMainHand() {
        return mainHand;
    }

    public boolean hasMainHandChanged() {
        return mainHand != glowPlayer.getSettings().getMainHand();
    }

    public GlowPlayer getGlowPlayer() {
        return glowPlayer;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
