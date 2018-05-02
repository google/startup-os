package com.google.startupos.tools.aa.commands;

import com.google.startupos.common.FileUtils;
import com.google.startupos.tools.aa.Protos.Config;

import javax.inject.Inject;

public class ConfigProvider {
    private FileUtils fileUtils;
    private static String CONFIG_FILENAME = "~/aaconfig.prototxt";

    @Inject
    public ConfigProvider(FileUtils utils) {
        this.fileUtils = utils;
    }

    public Config getConfig() {
        return (Config) fileUtils.readPrototxtUnchecked(
                fileUtils.expandHomeDirectory(CONFIG_FILENAME),
                Config.newBuilder());

    }
}
