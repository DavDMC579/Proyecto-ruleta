// Retro Web Audio Synthesizer for 8-bit Casino Roulette
class RetroAudio {
    constructor() {
        this.ctx = null;
        this.muted = false;
    }

    init() {
        if (!this.ctx) {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (AudioContext) {
                this.ctx = new AudioContext();
            }
        }
        if (this.ctx && this.ctx.state === 'suspended') {
            this.ctx.resume();
        }
    }

    toggleMute() {
        this.muted = !this.muted;
        return this.muted;
    }

    playTone(freq, type = 'square', duration = 0.1, gainVal = 0.15, decay = 0.08) {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        osc.type = type;
        osc.frequency.setValueAtTime(freq, this.ctx.currentTime);

        gain.gain.setValueAtTime(gainVal, this.ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + decay);

        osc.connect(gain);
        gain.connect(this.ctx.destination);

        osc.start();
        osc.stop(this.ctx.currentTime + duration);
    }

    playChip() {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        osc.type = 'triangle';
        osc.frequency.setValueAtTime(1400 + Math.random() * 400, now);
        osc.frequency.exponentialRampToValueAtTime(400, now + 0.04);

        gain.gain.setValueAtTime(0.25, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.04);

        osc.connect(gain);
        gain.connect(this.ctx.destination);

        osc.start(now);
        osc.stop(now + 0.05);
    }

    playBallTick(speedRatio = 1) {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        const baseFreq = 900 + (speedRatio * 500) + Math.random() * 100;
        osc.type = 'square';
        osc.frequency.setValueAtTime(baseFreq, now);
        osc.frequency.exponentialRampToValueAtTime(180, now + 0.025);

        gain.gain.setValueAtTime(0.12 * Math.min(1.2, speedRatio + 0.3), now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.025);

        osc.connect(gain);
        gain.connect(this.ctx.destination);

        osc.start(now);
        osc.stop(now + 0.03);
    }

    playBallDrop() {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        const now = this.ctx.currentTime;
        // Two low thuds for pocket settling
        [0, 0.06, 0.12].forEach((offset, idx) => {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(320 - idx * 60, now + offset);
            osc.frequency.exponentialRampToValueAtTime(60, now + offset + 0.05);

            gain.gain.setValueAtTime(0.3 / (idx + 1), now + offset);
            gain.gain.exponentialRampToValueAtTime(0.001, now + offset + 0.05);

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start(now + offset);
            osc.stop(now + offset + 0.06);
        });
    }

    playWin() {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        // Upbeat victory arpeggio
        const notes = [440, 554.37, 659.25, 880]; // A major
        notes.forEach((freq, idx) => {
            setTimeout(() => {
                this.playTone(freq, 'square', 0.18, 0.2, 0.16);
            }, idx * 70);
        });
    }

    playJackpot() {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        // Big fanfare for quota cleared
        const melody = [
            { f: 523.25, d: 90 }, // C5
            { f: 659.25, d: 90 }, // E5
            { f: 783.99, d: 90 }, // G5
            { f: 1046.50, d: 200 }, // C6
            { f: 880.00, d: 100 }, // A5
            { f: 1046.50, d: 350 }  // C6 long
        ];
        let delay = 0;
        melody.forEach(item => {
            setTimeout(() => {
                this.playTone(item.f, 'triangle', item.d / 1000 + 0.05, 0.28, item.d / 1000);
            }, delay);
            delay += item.d;
        });
    }

    playPowerSelect() {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        // Mystical ascending synth chord
        const freqs = [330, 415, 495, 660, 830, 990];
        freqs.forEach((freq, i) => {
            setTimeout(() => {
                this.playTone(freq, 'sine', 0.25, 0.2, 0.22);
            }, i * 45);
        });
    }

    playClairvoyance() {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        // Ethereal magic reveal
        const chimeNotes = [784, 880, 988, 1175, 1318, 1568];
        chimeNotes.forEach((n, idx) => {
            setTimeout(() => {
                this.playTone(n, 'triangle', 0.3, 0.18, 0.25);
            }, idx * 55);
        });
    }

    playShield() {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        // Metal shield deflection
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(600, now);
        osc.frequency.linearRampToValueAtTime(1200, now + 0.08);
        osc.frequency.exponentialRampToValueAtTime(200, now + 0.25);

        gain.gain.setValueAtTime(0.2, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.25);

        osc.connect(gain);
        gain.connect(this.ctx.destination);

        osc.start(now);
        osc.stop(now + 0.26);
    }

    playGameOver() {
        if (this.muted) return;
        this.init();
        if (!this.ctx) return;

        // Sad minor descending arpeggio
        const notes = [440, 415, 392, 349, 293];
        notes.forEach((freq, idx) => {
            setTimeout(() => {
                this.playTone(freq, 'sawtooth', 0.3, 0.2, 0.28);
            }, idx * 110);
        });
    }
}

window.soundEngine = new RetroAudio();
