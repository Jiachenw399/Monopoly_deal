package GUI;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class MusicPlayer {
    private Clip clip;
    private boolean muted;

    // Creates a MusicPlayer instance.
    public MusicPlayer() {
        muted = false;
        createClip();
    }

    // Plays this operation.
    public void play() {
        if (clip == null || muted || clip.isRunning()) {
            return;
        }

        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    // Toggles mute.
    public void toggleMute() {
        muted = !muted;

        if (clip == null) {
            return;
        }

        if (muted) {
            clip.stop();
        } else {
            play();
        }
    }

    public boolean isMuted() {
        return muted;
    }

    // Creates clip.
    private void createClip() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            byte[] data = createLoop(format);

            clip = AudioSystem.getClip();
            clip.open(format, data, 0, data.length);
        } catch (Exception e) {
            clip = null;
            muted = true;
        }
    }

    // Creates loop.
    private byte[] createLoop(AudioFormat format) {
        int sampleRate = (int) format.getSampleRate();
        int seconds = 16;
        byte[] data = new byte[sampleRate * seconds * 2];
        double[] melody = {
                523.25, 659.25, 783.99, 659.25,
                587.33, 739.99, 880.00, 739.99,
                659.25, 783.99, 987.77, 783.99,
                587.33, 659.25, 783.99, 1046.50
        };
        double[] bass = {
                130.81, 130.81, 174.61, 174.61,
                196.00, 196.00, 146.83, 146.83
        };

        for (int i = 0; i < data.length / 2; i++) {
            double time = i / format.getSampleRate();
            double beatTime = time * 2.4;
            int beat = (int) beatTime % melody.length;
            double note = melody[beat];
            double bassNote = bass[((int) (beatTime / 2)) % bass.length];
            double local = beatTime % 1.0;
            double attack = Math.min(1.0, local / 0.12);
            double release = Math.max(0.0, 1.0 - local);
            double envelope = attack * (0.25 + 0.75 * release);
            double lead = softTriangle(note, time) * envelope;
            double harmony = softTriangle(note * 1.25, time) * envelope * 0.28;
            double bassLine = Math.sin(2 * Math.PI * bassNote * time) * 0.22;
            double sparkle = Math.sin(2 * Math.PI * note * 2 * time) * envelope * 0.08;
            double kick = local < 0.08 ? Math.sin(2 * Math.PI * 90 * time) * (1.0 - local / 0.08) * 0.35 : 0.0;
            short value = (short) ((lead + harmony + bassLine + sparkle + kick) * 5200);

            data[i * 2] = (byte) (value & 0xff);
            data[i * 2 + 1] = (byte) ((value >> 8) & 0xff);
        }

        return data;
    }

    // Runs soft triangle.
    private double softTriangle(double frequency, double time) {
        double phase = (frequency * time) % 1.0;
        double triangle = 4.0 * Math.abs(phase - 0.5) - 1.0;
        return triangle * 0.55 + Math.sin(2 * Math.PI * frequency * time) * 0.45;
    }
}
