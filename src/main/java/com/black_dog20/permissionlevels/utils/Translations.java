package com.black_dog20.permissionlevels.utils;

import com.black_dog20.bml.utils.translate.ITranslation;
import com.black_dog20.permissionlevels.PermissionLevels;

public enum Translations implements ITranslation {
    LEVEL_TOO_HIGH("command.op.level_too_high"),
    OP_WITH_LEVEL("command.op.opped_with_level");

    private final String modId;
    private final String key;

    Translations(String key) {
        this.modId = PermissionLevels.MOD_ID;
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getModId() {
        return modId;
    }
}
