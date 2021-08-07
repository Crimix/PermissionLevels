package com.black_dog20.permissionlevels.datagen;

import com.black_dog20.bml.datagen.BaseLanguageProvider;
import com.black_dog20.permissionlevels.PermissionLevels;
import net.minecraft.data.DataGenerator;

import static com.black_dog20.permissionlevels.utils.Translations.LEVEL_TOO_HIGH;
import static com.black_dog20.permissionlevels.utils.Translations.OP_WITH_LEVEL;

public class GeneratorLanguageEnglish extends BaseLanguageProvider {

    public GeneratorLanguageEnglish(DataGenerator gen) {
        super(gen, PermissionLevels.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addPrefixed(LEVEL_TOO_HIGH, "Cannot op player to higher level than your level");
        addPrefixed(OP_WITH_LEVEL, "Made %s a server operator with permission level %d");
    }
}
