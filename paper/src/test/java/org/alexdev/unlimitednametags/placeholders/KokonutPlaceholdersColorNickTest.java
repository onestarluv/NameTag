package org.alexdev.unlimitednametags.placeholders;

import kr.gomsv.kokonutCore.api.nickname.ColorNickLookup;
import kr.gomsv.kokonutCore.api.nickname.NickColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.alexdev.unlimitednametags.UnlimitedNameTags;
import org.alexdev.unlimitednametags.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/** {@code %kokonut_colornick%}와 {@code %kokonut_colornick_legacy%}가 돌려주는 문자열 형식. */
class KokonutPlaceholdersColorNickTest {
    @Test
    void defaultNametagUsesCoreColorOnAccountNameWhenNoNicknameIsSet() {
        var player = mock(Player.class);
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn("MinecraftName");
        var services = mock(ServicesManager.class);
        when(services.load(ColorNickLookup.class)).thenReturn(uuid ->
                uuid.equals(id) ? NickColor.byId("aqua") : null);

        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getServicesManager).thenReturn(services);
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            var placeholders = new KokonutPlaceholders(mock(UnlimitedNameTags.class));
            var tag = new Settings().resolveNametag(permission -> false);
            String nameLine = tag.displayGroups().getFirst().lines().getLast().text();
            String nameTag = placeholders.resolve(player, nameLine);
            assertEquals(Component.text("MinecraftName", NamedTextColor.AQUA), MiniMessage.miniMessage().deserialize(nameTag));
        }
    }

    private static String mini(String id, String name) {
        return KokonutPlaceholders.ColorNicks.format(NickColor.byId(id), name, false);
    }

    private static String legacy(String id, String name) {
        return KokonutPlaceholders.ColorNicks.format(NickColor.byId(id), name, true);
    }

    @Test
    void vanillaColorWrapsTheName() {
        assertEquals("<aqua>핑퐁", mini("aqua", "핑퐁"));
        assertEquals("§b핑퐁", legacy("aqua", "핑퐁"));
    }

    @Test
    void hexColorUsesHexTagAndXCodes() {
        assertEquals("<#FF7200>핑퐁", mini("ff7200", "핑퐁"));
        assertEquals("§x§f§f§7§2§0§0핑퐁", legacy("ff7200", "핑퐁"));
    }

    @Test
    void gradientUsesGradientTagAndColorsEachLetter() {
        assertEquals("<gradient:#2D0000:#FF0000>핑퐁", mini("gradient11", "핑퐁"));
        String codes = legacy("gradient11", "핑퐁");
        assertTrue(codes.startsWith("§x§2§d§0§0§0§0핑§x"), codes);
        assertTrue(codes.endsWith("퐁"), codes);
    }

    @Test
    void noColorLeavesThePlainName() {
        assertEquals("핑퐁", mini(null, "핑퐁"));
        assertEquals("핑퐁", legacy(null, "핑퐁"));
    }

    @Test
    void miniMessageTagsInNamesAreEscaped() {
        assertEquals("<aqua>\\<red>이름", mini("aqua", "<red>이름"));
        assertEquals("\\<red>이름", mini(null, "<red>이름"));
    }
}
