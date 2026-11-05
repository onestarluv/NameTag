package org.alexdev.unlimitednametags.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

final class KoreanCommandArguments {
    private KoreanCommandArguments() {}

    static ArgumentType<String> word() {
        return new CustomArgumentType<String, String>() {
            @Override
            public ArgumentType<String> getNativeType() {
                return StringArgumentType.word();
            }

            @Override
            public String parse(StringReader reader) throws CommandSyntaxException {
            int start = reader.getCursor();
            while (reader.canRead() && !Character.isWhitespace(reader.peek())) reader.skip();
            if (start == reader.getCursor()) {
                throw new SimpleCommandExceptionType(new LiteralMessage("값을 입력하세요.")).createWithContext(reader);
            }
            return reader.getString().substring(start, reader.getCursor());
            }
        };
    }

    static ArgumentType<Boolean> toggle() {
        return new CustomArgumentType<Boolean, String>() {
            @Override
            public ArgumentType<String> getNativeType() {
                return StringArgumentType.word();
            }

            @Override
            public Boolean parse(StringReader reader) throws CommandSyntaxException {
                int start = reader.getCursor();
                String value = word().parse(reader);
                if (value.equals("활성") || value.equals("true")) return true;
                if (value.equals("비활성") || value.equals("false")) return false;
                reader.setCursor(start);
                throw new SimpleCommandExceptionType(new LiteralMessage("활성 또는 비활성을 입력하세요.")).createWithContext(reader);
            }

            @Override
            public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
                for (String value : getExamples()) {
                    if (value.startsWith(builder.getRemaining())) builder.suggest(value);
                }
                return builder.buildFuture();
            }

            @Override
            public Collection<String> getExamples() {
                return List.of("활성", "비활성");
            }
        };
    }

    static String displayName(String value) {
        return switch (value) {
            case "CENTER" -> "중앙";
            case "HORIZONTAL" -> "수평";
            case "VERTICAL" -> "수직";
            case "FIXED" -> "고정";
            case "MINIMESSAGE" -> "미니메시지";
            case "LEGACY" -> "레거시";
            case "UNIVERSAL" -> "통합";
            default -> value;
        };
    }

    static String enumName(String value) {
        return switch (value) {
            case "중앙" -> "CENTER";
            case "수평" -> "HORIZONTAL";
            case "수직" -> "VERTICAL";
            case "고정" -> "FIXED";
            case "미니메시지" -> "MINIMESSAGE";
            case "레거시" -> "LEGACY";
            case "통합" -> "UNIVERSAL";
            default -> value.toUpperCase(java.util.Locale.ROOT);
        };
    }
}
