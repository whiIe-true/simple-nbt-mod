package de.hoppofloppodoggo.simplenbt.mixin;

import de.hoppofloppodoggo.simplenbt.command.DotCommandHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class SendChatMessageMixin {

    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
    public void sendPacket(String content, CallbackInfo ci) {
        if(content.startsWith(".")){
            DotCommandHandler.getInstance().handleClientCommand(content);
            ci.cancel();
        }
    }
}
