package de.hoppofloppodoggo.simplenbt.command;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.hoppofloppodoggo.simplenbt.SimpleNBTMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class DotCommandHandler {

    private final String SYNTAX = ".nbt <show/copy/paste>";

    private DotCommandHandler(){}
    private static DotCommandHandler INSTANCE;

    public static DotCommandHandler getInstance() {
        if(INSTANCE == null)
            INSTANCE = new DotCommandHandler();
        return INSTANCE;
    }

    public CompletableFuture<Suggestions> handleSuggestions(String str, SuggestionsBuilder sb) {
        var args = new Arguments(str);

        if(args.isWord(0))
            return args.suggest(sb, ".nbt");

        var wd = args.getWord(0);

        if(!wd.equalsIgnoreCase(".nbt"))
            return args.suggest(sb);

        if(args.isWord(1))
            return args.suggest(sb, "show","copy","paste");

        return args.suggest(sb);
    }

    protected void handlePaste(Arguments args, ClientPlayerEntity p) {
        if(!p.isCreative()) {
            p.sendMessage(Text.literal("§cYou must be in creative mode to create items."));
            return;
        }

        try {
            var raw = MinecraftClient.getInstance().keyboard.getClipboard();

            if(raw.startsWith("/give")){
                // Ensures its encoded correctly
                var isError = !raw.startsWith("/give @p decorated_pot[container=[{slot:0,item:{") || !raw.endsWith("}]] 1");

                if(isError){
                    p.sendMessage(Text.literal("§c/give is currently only partically supported. Please look at https://github.com/whiIe-true/simple-nbt-mod On how to use it."));
                    return;
                }

                raw = raw.substring("/give @p decorated_pot[container=[{slot:0,item:".length(),raw.length()-"}]] 1".length());
            }

            // Parses the nbt tag
            NbtCompound comp = StringNbtReader.parse(raw);

            var itmStack = ItemStack.fromNbtOrEmpty(MinecraftClient.getInstance().world.getRegistryManager(), comp);

            var nethandler = MinecraftClient.getInstance().getNetworkHandler();

            // Creates the item
            nethandler.sendPacket(new CreativeInventoryActionC2SPacket(p.getInventory().selectedSlot + 36, itmStack));

        }catch(Exception err){
            p.sendMessage(Text.literal("§cFailed to create item: "+err.getMessage()));
            SimpleNBTMod.LOGGER.info(err.getMessage());
        }
    }

    public void handleClientCommand(String rawStr){
        var p = MinecraftClient.getInstance().player;
        var w = MinecraftClient.getInstance().world;
        if(p == null || w == null) return;

        var args = new Arguments(rawStr);

        // Ensure .nbt is used
        if(!args.hasWord(0) || !args.getWord(0).equalsIgnoreCase(".nbt")){
            p.sendMessage(Text.literal("§cPlease use "+this.SYNTAX));
            return;
        }

        var action = args.hasWord(1) ? args.getWordEnum(1, "show","copy","paste") : "show";

        if(action == null){
            p.sendMessage(Text.literal("§cPlease use "+this.SYNTAX));
            return;
        }

        if(action.equals("paste")){
            handlePaste(args,p);
            return;
        }

        // Gets the nbt from the held item
        var itm = p.getMainHandStack();

        if(itm == null || itm.getItem() == Items.AIR){
            p.sendMessage(Text.literal("§cYou are not holding an item."));
            return;
        }

        // Gets the nbt
        var nbt = itm.encodeAllowEmpty(w.getRegistryManager()).asString();

        switch(action){
            case "show":
                p.sendMessage(Text.literal("§b"+nbt));
                break;
            case "copy":
                MinecraftClient.getInstance().keyboard.setClipboard(nbt);
                p.sendMessage(Text.literal("§bCopied nbt"));
                break;
        }
    }
}
