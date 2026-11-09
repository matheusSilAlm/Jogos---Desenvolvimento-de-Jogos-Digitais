package com.meuprojeto.tanks.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.meuprojeto.tanks.TankClient;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // necessário no macOS

        // IP opcional na linha de comando; senão a tela inicial já vem com "localhost".
        String host = args.length > 0 ? args[0] : "localhost";

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Batalha de Tanques - Multiplayer");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        // até 10 tanques atirando/explodindo ao mesmo tempo + motor + música: mais canais que o padrão (16)
        config.setAudioConfig(32, 512, 9);
        config.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        new Lwjgl3Application(new TankClient(host), config);
    }
}
