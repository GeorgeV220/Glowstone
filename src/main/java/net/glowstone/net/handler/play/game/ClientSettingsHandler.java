package net.glowstone.net.handler.play.game;

import com.flowpowered.network.MessageHandler;
import net.glowstone.EventFactory;
import net.glowstone.entity.meta.ClientSettings;
import net.glowstone.events.player.PlayerClientOptionsChangeEvent;
import net.glowstone.net.GlowSession;
import net.glowstone.net.message.play.game.ClientSettingsMessage;

public final class ClientSettingsHandler implements
        MessageHandler<GlowSession, ClientSettingsMessage> {

    @Override
    public void handle(GlowSession session, ClientSettingsMessage message) {
        PlayerClientOptionsChangeEvent event = new PlayerClientOptionsChangeEvent(session, new ClientSettings(message));
        EventFactory.getInstance().callEvent(event);
        if (!event.isCancelled()) {
            event.getGlowPlayer().setSettings(event.getClientSettings());
        }
    }
}
