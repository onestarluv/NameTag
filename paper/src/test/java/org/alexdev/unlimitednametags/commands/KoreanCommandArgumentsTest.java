package org.alexdev.unlimitednametags.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KoreanCommandArgumentsTest {
    @Test
    void koreanCommandParsesToggleAndRejectsUnknownValue() throws Exception {
        CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
        dispatcher.register(LiteralArgumentBuilder.<Object>literal("네임태그")
                .then(LiteralArgumentBuilder.<Object>literal("설정")
                        .then(RequiredArgumentBuilder.<Object, Boolean>argument("상태", KoreanCommandArguments.toggle())
                                .executes(ctx -> ctx.getArgument("상태", Boolean.class) ? 1 : 0))));
        assertEquals(1, dispatcher.execute("네임태그 설정 활성", new Object()));
        assertEquals(0, dispatcher.execute("네임태그 설정 비활성", new Object()));
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("네임태그 설정 오타", new Object()));
        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("네임태그 설정 비", new Object())).get();
        assertTrue(suggestions.getList().stream().anyMatch(s -> s.getText().equals("비활성")));
    }

    @Test
    void koreanWordStopsAtWhitespaceAndMapsEnum() throws Exception {
        StringReader reader = new StringReader("미니메시지 다음값");
        assertEquals("MINIMESSAGE", KoreanCommandArguments.enumName(KoreanCommandArguments.word().parse(reader)));
        assertEquals(' ', reader.peek());
        assertEquals("중앙", KoreanCommandArguments.displayName("CENTER"));
        assertThrows(CommandSyntaxException.class, () -> KoreanCommandArguments.word().parse(new StringReader("")));
    }
}
