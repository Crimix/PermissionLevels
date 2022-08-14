package com.black_dog20.permissionlevels.datagen;

import com.black_dog20.bml.datagen.BaseLanguageProvider;
import com.black_dog20.permissionlevels.PermissionLevels;
import net.minecraft.data.DataGenerator;

import static com.black_dog20.permissionlevels.utils.Translations.*;

public class GeneratorLanguageEnglish extends BaseLanguageProvider {

    public GeneratorLanguageEnglish(DataGenerator gen) {
        super(gen, PermissionLevels.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addPrefixed(LEVEL_TOO_HIGH, "Cannot op player to higher level than your level");
        addPrefixed(OP_WITH_LEVEL, "Made %s a server operator with permission level %d");
        addPrefixed(OP_WITH_LEVEL_BYPASS, "Made %s a server operator with permission level %d and allowed to bypass player limit");
        addPrefixed(PLAYER_BYPASS_FAILED, "Nothing changed");
        addPrefixed(PLAYER_BYPASS_ALLOWED, "Allowed %s to bypass player limit");
        addPrefixed(PLAYER_BYPASS_DENIED, "Denied %s to bypass player limit");
    }
}
