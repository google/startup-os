package com.google.startupos.tools.aa.commands;

import com.google.startupos.common.CommonModule;
import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.aa.Protos.Config;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;
import javax.inject.Singleton;

@Module(includes = CommonModule.class)
public class ConfigProvider {
    private static String CONFIG_FILENAME = "~/aaconfig.prototxt";

    @Inject
    public ConfigProvider() {
    }

    @Provides public static Config getConfig(FileUtils fileUtils) {
        return (Config) fileUtils.readPrototxtUnchecked(
                fileUtils.expandHomeDirectory(CONFIG_FILENAME),
                Config.newBuilder());

    }
}
