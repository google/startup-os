package com.google.startupos.tools.aa.commands;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.aa.Protos.Config;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;

@Module(includes = CommonModule.class)
public class ConfigProvider {
    @Inject
    public ConfigProvider() {
    }

    @Provides public static Config getConfig(FileUtils fileUtils, String configFileName) {
        return (Config) fileUtils.readPrototxtUnchecked(
                fileUtils.expandHomeDirectory(configFileName),
                Config.newBuilder());

    }
}
