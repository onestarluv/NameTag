package org.alexdev.unlimitednametags.placeholders;

import kr.gomsv.kokonutCore.api.util.ServerNicknameUtil;
import kr.gomsv.kokonutCore.api.title.TitleLookup;
import org.alexdev.unlimitednametags.UnlimitedNameTags;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlaceholderAPI 없이 Kokonut-Core를 직접 호출해 이름표 플레이스홀더를 채운다.
 * <ul>
 *     <li>{@code %kokonut_title%}, {@code %kokonut_title_plain%}, {@code %kokonut_title_legacy%}, {@code %kokonut_title_id%}:
 *     Core {@link TitleLookup} 서비스의 메모리 캐시를 읽는다 (DB 접근 없음).</li>
 *     <li>{@code %kokonut_nickname%}: Core의 공유 닉네임 캐시({@link ServerNicknameUtil#cachedNickname})를 읽는다.
 *     같은 서버의 변경은 즉시, 다른 서버의 변경은 최대 30초 뒤 반영된다. 닉네임이 없으면 마인크래프트 이름을 쓴다.</li>
 *     <li>{@code %kokonut_name%}, {@code %player_name%}: 마인크래프트 이름.</li>
 * </ul>
 * 모르는 플레이스홀더는 그대로 남긴다 (PAPI와 같은 동작).
 */
public final class KokonutPlaceholders {

    private static final Pattern PLACEHOLDER = Pattern.compile("%([a-zA-Z0-9_]+)%");

    private final UnlimitedNameTags plugin;
    private volatile TitleLookup titles;

    public KokonutPlaceholders(@NotNull UnlimitedNameTags plugin) {
        this.plugin = plugin;
    }

    /** 문자열 안의 모든 {@code %...%}를 치환한다. */
    @NotNull
    public String setPlaceholders(@NotNull Player player, @NotNull String text) {
        if (text.indexOf('%') == -1) {
            return text;
        }
        final Matcher matcher = PLACEHOLDER.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        final StringBuilder builder = new StringBuilder(text.length() + 16);
        int last = 0;
        do {
            builder.append(text, last, matcher.start());
            final String value = resolveKey(player, matcher.group(1));
            builder.append(value != null ? value : matcher.group());
            last = matcher.end();
        } while (matcher.find());
        builder.append(text, last, text.length());
        return builder.toString();
    }

    /** {@code %key%} 형태 하나를 치환한다. 모르는 값이면 원문을 돌려준다. */
    @NotNull
    public String resolve(@NotNull Player player, @NotNull String placeholder) {
        if (placeholder.length() < 3 || placeholder.charAt(0) != '%' || placeholder.charAt(placeholder.length() - 1) != '%') {
            return setPlaceholders(player, placeholder);
        }
        final String value = resolveKey(player, placeholder.substring(1, placeholder.length() - 1));
        return value != null ? value : placeholder;
    }

    @Nullable
    private String resolveKey(@NotNull Player player, @NotNull String rawKey) {
        final String key = rawKey.toLowerCase(Locale.ROOT);
        return switch (key) {
            case "kokonut_nickname" -> nickname(player);
            case "kokonut_name", "player_name" -> player.getName();
            case "kokonut_title" -> title(player, "title");
            case "kokonut_title_plain" -> title(player, "title_plain");
            case "kokonut_title_legacy" -> title(player, "title_legacy");
            case "kokonut_title_id" -> title(player, "title_id");
            default -> null;
        };
    }

    @NotNull
    private String title(@NotNull Player player, @NotNull String kind) {
        TitleLookup service = titles;
        if (service == null) {
            service = Bukkit.getServicesManager().load(TitleLookup.class);
            if (service == null) {
                return "";
            }
            titles = service;
        }
        return service.placeholder(player.getUniqueId(), kind);
    }

    @NotNull
    private String nickname(@NotNull Player player) {
        final UUID uuid = player.getUniqueId();
        Optional<String> nickname = ServerNicknameUtil.cachedNickname(uuid);
        if (nickname == null && !Bukkit.isPrimaryThread()) {
            // 첫 조회가 이미 비동기 스레드라면 바로 읽어서, 입장 직후 마크 이름이 잠깐 보이는 것을 막는다.
            try {
                nickname = ServerNicknameUtil.findNicknameByUuid(uuid);
            } catch (RuntimeException ignored) {
                nickname = Optional.empty();
            }
        }
        return nickname == null ? player.getName() : nickname.filter(value -> !value.isBlank()).orElse(player.getName());
    }

    /** 캐시는 Core가 관리하므로 할 일이 없다. 기존 호출부 호환용. */
    public void removePlayer(@NotNull UUID uuid) {
    }

    public void close() {
        titles = null;
    }
}
