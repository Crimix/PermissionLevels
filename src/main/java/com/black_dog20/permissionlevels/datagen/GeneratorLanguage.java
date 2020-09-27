package com.black_dog20.permissionlevels.datagen;

import com.black_dog20.permissionlevels.PermissionLevels;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class GeneratorLanguage extends LanguageProvider {

    private final String modId;

    public GeneratorLanguage(DataGenerator gen) {
        super(gen, PermissionLevels.MOD_ID, "en_us");
        this.modId = PermissionLevels.MOD_ID;
    }

    @Override
    protected void addTranslations() {

        addPrefixed("commands.op.success", "Made %s a server operator with permission level %d");
    }

    protected void addPrefixed(String key, String text) {
        this.add(String.format("%s.%s", this.modId, key), text);
    }
}
