package de.hoppofloppodoggo.simplenbt.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.hoppofloppodoggo.simplenbt.command.DotCommandHandler;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow
    CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    final TextFieldWidget textField;

    @Shadow
    private static int getStartOfCurrentWord(String input) {
        return 0;
    }

    @Shadow public abstract void show(boolean narrateFirstSuggestion);

    protected ChatInputSuggestorMixin() {
        this.textField = null;
    }

    @Inject(at = @At("TAIL"), method = "refresh")
    protected void refresh(CallbackInfo ci) {
        String textStr = this.textField.getText();
        int cursorPos = this.textField.getCursor();
        String preStr = textStr.substring(0, cursorPos);
        if (!preStr.startsWith(".")) {
            return;
        }

        int wordStart = getStartOfCurrentWord(preStr);
        CompletableFuture<Suggestions> suggestions;
        try {
            suggestions = DotCommandHandler.getInstance().handleSuggestions(preStr, new SuggestionsBuilder(preStr, wordStart));
        }
        catch (Throwable e) {
            suggestions = null;
        }
        if (suggestions != null) {
            this.pendingSuggestions = suggestions;
            this.show(true);
        }
    }

}
