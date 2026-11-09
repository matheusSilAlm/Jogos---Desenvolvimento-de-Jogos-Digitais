package com.meuprojeto.tanks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

/**
 * Música de batalha e efeitos sonoros do cliente (o servidor não toca nada).
 *
 * Os sons são posicionais: quanto mais longe do SEU tanque, mais baixo; e o
 * pan estéreo segue a posição X (tiro à esquerda sai no fone esquerdo).
 *
 * Fica no {@link TankClient} (um só para o jogo todo), para não recarregar os
 * arquivos a cada partida.
 */
public class AudioManager {

    private static final float MUSIC_VOLUME  = 0.45f;
    private static final float SFX_VOLUME    = 0.9f;
    private static final float ENGINE_VOLUME = 0.22f;
    private static final float MIN_DISTANCE_VOLUME = 0.35f;   // som do outro lado do mapa ainda é ouvido
    private static final float FADE_DISTANCE = 1500f;

    private Music battleMusic;
    private Sound shot, hit, explosion, shieldBreak;
    private Sound repair, armor, damage, powerUpSpawn;
    private Sound respawn, reloadReady, kill, engine;

    private boolean muted;
    private float listenerX = GameState.WORLD_WIDTH / 2f, listenerY = GameState.WORLD_HEIGHT / 2f;

    private long engineId = -1;
    private float engineLevel;    // 0..1, sobe/desce suave para não estalar

    public AudioManager() {
        try {
            battleMusic = Gdx.audio.newMusic(Gdx.files.internal("musica_batalha.wav"));
            battleMusic.setLooping(true);
            battleMusic.setVolume(MUSIC_VOLUME);
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Falha ao carregar a música de batalha", e);
        }

        shot         = load("sfx_tiro.wav");
        hit          = load("sfx_acerto.wav");
        explosion    = load("sfx_explosao.wav");
        shieldBreak  = load("sfx_escudo_quebra.wav");
        repair       = load("sfx_reparo.wav");
        armor        = load("sfx_escudo.wav");
        damage       = load("sfx_dano.wav");
        powerUpSpawn = load("sfx_powerup_surge.wav");
        respawn      = load("sfx_renascer.wav");
        reloadReady  = load("sfx_recarregado.wav");
        kill         = load("sfx_abate.wav");
        engine       = load("sfx_motor.wav");
    }

    private Sound load(String file) {
        try {
            return Gdx.audio.newSound(Gdx.files.internal(file));
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Falha ao carregar " + file, e);
            return null;
        }
    }

    // =========================================================================
    // MÚSICA
    // =========================================================================

    /**
     * Chamado a cada frame enquanto a partida está rolando: começa a música na
     * primeira vez e, se ela parar por qualquer motivo (fim do arquivo, falta de
     * canal de áudio, som reativado), volta a tocar.
     */
    public void keepBattleMusicPlaying() {
        if (battleMusic != null && !muted && !battleMusic.isPlaying()) battleMusic.play();
    }

    /** Fim da partida (saiu, caiu a conexão): para tudo e volta a música para o início. */
    public void stopMatchAudio() {
        if (battleMusic != null) battleMusic.stop();
        stopEngine();
    }

    // =========================================================================
    // EFEITOS
    // =========================================================================

    /** Posição de quem está ouvindo (o seu tanque). */
    public void setListener(float x, float y) {
        listenerX = x;
        listenerY = y;
    }

    public void playShot(float x, float y, boolean boosted) {
        if (boosted) playAt(shot, x, y, 1f, MathUtils.random(0.72f, 0.80f));
        else         playAt(shot, x, y, 0.6f, MathUtils.random(0.92f, 1.08f));
    }

    public void playHit(float x, float y)         { playAt(hit, x, y, 0.8f, MathUtils.random(0.95f, 1.05f)); }
    public void playExplosion(float x, float y)   { playAt(explosion, x, y, 1f, MathUtils.random(0.9f, 1.05f)); }
    public void playShieldBreak(float x, float y) { playAt(shieldBreak, x, y, 0.8f, 1f); }
    public void playPowerUpSpawn(float x, float y){ playAt(powerUpSpawn, x, y, 0.45f, 1f); }

    public void playPickup(int type, float x, float y) {
        switch (type) {
            case PowerUp.REPAIR: playAt(repair, x, y, 0.8f, 1f); break;
            case PowerUp.ARMOR:  playAt(armor,  x, y, 0.8f, 1f); break;
            case PowerUp.DAMAGE: playAt(damage, x, y, 0.9f, 1f); break;
            default: break;
        }
    }

    // sons só seus (sem posição)
    public void playRespawn()     { play(respawn, 0.6f, 1f, 0f); }
    public void playReloadReady() { play(reloadReady, 0.35f, 1f, 0f); }
    public void playKill()        { play(kill, 0.7f, 1f, 0f); }

    /** Ronco do motor do seu tanque: chamado todo frame, sobe quando anda e some quando para. */
    public void updateEngine(boolean moving, float delta) {
        if (engine == null) return;
        float target = moving && !muted ? 1f : 0f;
        float speed = target > engineLevel ? 6f : 3f;
        engineLevel += MathUtils.clamp(target - engineLevel, -speed * delta, speed * delta);

        if (engineLevel > 0.01f) {
            if (engineId == -1) engineId = engine.loop(0f);
            if (engineId != -1) {
                engine.setVolume(engineId, ENGINE_VOLUME * engineLevel);
                engine.setPitch(engineId, 0.85f + 0.2f * engineLevel);
            }
        } else {
            stopEngine();
        }
    }

    private void stopEngine() {
        if (engine != null && engineId != -1) engine.stop(engineId);
        engineId = -1;
        engineLevel = 0f;
    }

    private void playAt(Sound sound, float x, float y, float volume, float pitch) {
        float dist = (float) Math.hypot(x - listenerX, y - listenerY);
        float att = MathUtils.clamp(1f - dist / FADE_DISTANCE, MIN_DISTANCE_VOLUME, 1f);
        float pan = MathUtils.clamp((x - listenerX) / (GameState.WORLD_WIDTH * 0.5f), -1f, 1f) * 0.7f;
        play(sound, volume * att, pitch, pan);
    }

    private void play(Sound sound, float volume, float pitch, float pan) {
        if (muted || sound == null) return;
        sound.play(volume * SFX_VOLUME, pitch, pan);
    }

    // =========================================================================
    // MUDO
    // =========================================================================

    public void toggleMute() {
        muted = !muted;
        if (muted) {
            if (battleMusic != null && battleMusic.isPlaying()) battleMusic.pause();
            stopEngine();
        }
        // ao desmutar, a próxima chamada de keepBattleMusicPlaying() retoma de onde parou
    }

    public boolean isMuted() {
        return muted;
    }

    public void dispose() {
        stopMatchAudio();
        if (battleMusic != null) battleMusic.dispose();
        Sound[] all = { shot, hit, explosion, shieldBreak, repair, armor, damage, powerUpSpawn,
                        respawn, reloadReady, kill, engine };
        for (Sound s : all) if (s != null) s.dispose();
    }
}
