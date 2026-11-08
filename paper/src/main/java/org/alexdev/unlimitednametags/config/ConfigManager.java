package org.alexdev.unlimitednametags.config;

import lombok.Getter;
import org.alexdev.unlimitednametags.UnlimitedNameTags;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Kokonut 고정 설정. settings.yml / advanced.yml을 읽거나 쓰지 않고, {@link Settings}와 {@link Advanced}의
 * 코드 기본값을 그대로 사용한다. 값을 바꾸려면 해당 클래스의 기본값을 수정하고 다시 빌드한다.
 * <p>
 * 명령어로 바꾼 값(회전 방식, 문자 형식)은 서버가 켜져 있는 동안만 유지되며, 리로드하면 기본값으로 돌아간다.
 */
@Getter
public class ConfigManager {

    private final UnlimitedNameTags plugin;
    private Settings settings;
    private volatile Advanced advanced = new Advanced();
    private boolean compiled;

    public ConfigManager(@NotNull final UnlimitedNameTags plugin) {
        this.plugin = plugin;
        this.loadUsage();
    }

    private void loadUsage() {
        try (@NotNull final InputStream inputStream = Objects.requireNonNull(getClass().getResourceAsStream("/plugin.yml"));
             final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            compiled = YamlConfiguration.loadConfiguration(reader).getBoolean("compiled", true);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public Optional<Throwable> loadConfigs() {
        try {
            reload();
            return Optional.empty();
        } catch (final Exception e) {
            return Optional.of(e);
        }
    }

    public void reload() {
        final Settings fresh = new Settings();
        SettingsValidator.validateAndFix(fresh, msg -> plugin.getLogger().warning(msg));
        settings = fresh;

        final Advanced freshAdvanced = new Advanced();
        sortHelmetHeightRules(freshAdvanced);
        advanced = freshAdvanced;
    }

    @NotNull
    public Advanced getAdvanced() {
        return advanced;
    }

    private static void sortHelmetHeightRules(@NotNull Advanced advanced) {
        final List<Advanced.HelmetHeightRule> rules = advanced.getHelmetHeightRules();
        if (rules == null || rules.size() < 2) {
            return;
        }
        rules.sort(Comparator.comparingInt(Advanced.HelmetHeightRule::getPriority).reversed());
    }

    /** 설정 파일을 쓰지 않으므로 아무것도 하지 않는다. 기존 명령어 호환용. */
    public void save() {
    }
}
