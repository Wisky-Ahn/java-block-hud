package io.github.wiskyahn.blockhud.service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 효과음 재생 (클릭). 원본 UseClickSound + click.wav 대체. (DESIGN.md §8.3)
 * {@code javax.sound}를 사용해 추가 JavaFX 모듈(javafx.media) 의존을 피한다.
 */
public final class SoundService {

    private static final Logger log = LoggerFactory.getLogger(SoundService.class);

    private Clip clickClip;
    private volatile boolean enabled = true;

    public SoundService() {
        try (InputStream raw = SoundService.class.getResourceAsStream("/assets/audio/click.wav")) {
            if (raw != null) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
                clickClip = AudioSystem.getClip();
                clickClip.open(ais);
            }
        } catch (Exception e) {
            log.warn("클릭 사운드 로드 실패: {}", e.getMessage());
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void playClick() {
        if (!enabled || clickClip == null) {
            return;
        }
        clickClip.stop();
        clickClip.setFramePosition(0);
        clickClip.start();
    }
}
