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
public class PlayerClientSettingsEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final GlowSession session;
    private ClientSettings clientSettings;
    private boolean cancelled;

    /**
     * Constructs a new instance of the {@code PlayerClientSettingsEvent} class with the specified session and client settings.
     *
     * @param glowSession    the session associated with the event
     * @param clientSettings the client settings associated with the event
     */
    public PlayerClientSettingsEvent(@NotNull GlowSession glowSession, @NotNull ClientSettings clientSettings) {
        super(glowSession.getPlayer());
        this.session = glowSession;
        this.clientSettings = clientSettings;
    }

    /**
     * Returns the client settings associated with this event.
     *
     * @return the client settings associated with this event
     */
    public @NotNull ClientSettings getClientSettings() {
        return clientSettings;
    }

    /**
     * Sets the client settings associated with this event.
     *
     * @param clientSettings the client settings to set
     */
    public void setClientSettings(@NotNull ClientSettings clientSettings) {
        this.clientSettings = clientSettings;
    }

    /**
     * Returns the session associated with this event.
     *
     * @return the session associated with this event
     */
    public @NotNull GlowSession getSession() {
        return session;
    }

    /**
     * Returns the player associated with this event.
     *
     * @return the player associated with this event
     */
    public @NotNull GlowPlayer getGlowPlayer() {
        return session.getPlayer();
    }

    /**
     * Returns the handler list for this event.
     *
     * @return the handler list for this event
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the handler list for this event.
     *
     * @return the handler list for this event
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns whether this event is cancelled.
     *
     * @return true if this event is cancelled; false otherwise
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether this event is cancelled.
     *
     * @param setCancelled true to cancel this event; false otherwise
     */
    @Override
    public void setCancelled(boolean setCancelled) {
        this.cancelled = setCancelled;
    }
}
