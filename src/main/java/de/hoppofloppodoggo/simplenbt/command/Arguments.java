package de.hoppofloppodoggo.simplenbt.command;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Arguments {

    public final boolean isNewWord;
    public final String[] rawArgs;

    public Arguments(String raw) {
        this.isNewWord = raw.endsWith(" ");
        this.rawArgs = raw.split(" ");
    }

    public boolean isWord(int idx){
        return rawArgs.length == (idx) || (rawArgs.length == (idx+1) && !isNewWord);
    }

    public boolean hasWord(int idx){
        return idx < this.rawArgs.length;
    }

    public String getWord(int idx){
        if(idx < this.rawArgs.length)
            return rawArgs[idx];

        return "";
    }

    public String getWordEnum(int idx, String... allowed){
        if(!hasWord(idx)) return null;
        var rawW = getWord(idx);

        if(Arrays.asList(allowed).contains(rawW))
            return rawW;
        return null;
    }

    public CompletableFuture<Suggestions> suggest(SuggestionsBuilder sb, String... toAdd) {
        return CommandSource.suggestMatching(List.of(toAdd), sb);
    }
}
