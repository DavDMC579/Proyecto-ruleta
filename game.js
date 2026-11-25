// Progressive Roulette Game Logic & Canvas Animation

// American Roulette standard order (38 pockets)
const POCKETS = [
    '0', '28', '9', '26', '30', '11', '7', '20', '32', '17', 
    '5', '22', '34', '15', '3', '24', '36', '13', '1', '00', 
    '27', '10', '25', '29', '12', '8', '19', '31', '18', '6', 
    '21', '33', '16', '4', '23', '35', '14', '2'
];

const RED_NUMBERS = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36];
const BLACK_NUMBERS = [2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35];

function getPocketColor(val) {
    if (val === '0' || val === '00') return 'green';
    const num = parseInt(val, 10);
    if (RED_NUMBERS.includes(num)) return 'red';
    if (BLACK_NUMBERS.includes(num)) return 'black';
    return 'green';
}

class ProgressiveRouletteGame {
    constructor() {
        // Initial Game State
        this.balance = 100;
        this.currentQuota = 1000;
        this.round = 1;
        this.maxSpins = 6;
        this.spinsLeft = 6;

        // Betting state
        this.currentBets = {}; // cellId -> amount
        this.previousBets = null;
        this.selectedChip = 10;
        this.isSpinning = false;

        // Powers inventory & active for current spin
        this.powersInventory = {
            double_win: 0,
            reveal_number: 0,
            loss_shield: 0
        };

        this.activePowers = {
            double_win: false,
            reveal_number: false,
            loss_shield: false
        };

        this.predeterminedWinner = null;

        // Canvas & wheel assets
        this.canvas = document.getElementById('rouletteCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.rotorImg = new Image();
        this.rotorImg.src = 'wheel_rotor.png';
        this.bowlImg = new Image();
        this.bowlImg.src = 'wheel_bowl_hollow.png';

        // Wheel animation state
        this.rotorAngle = 0; // radians
        this.rotorSpeed = 0.005; // idle spin
        this.ballAngle = 0;
        this.ballRadius = 117;
        this.ballSpeed = 0;
        this.ballLocked = true;
        this.lastWinningPocket = '0';
        this.lastResultColor = 'green';

        this.init();
    }

    init() {
        this.bindEvents();
        this.updateHUD();
        this.renderBettingGrid();
        this.renderChipTray();
        this.updatePowerButtons();
        this.startAnimationLoop();
    }

    // --- HUD & UI Updates ---
    updateHUD() {
        document.getElementById('hudBalance').textContent = `$${this.balance.toLocaleString()}`;
        document.getElementById('hudQuota').textContent = `$${this.currentQuota.toLocaleString()}`;
        document.getElementById('hudSpins').textContent = `${this.spinsLeft} / ${this.maxSpins}`;
        document.getElementById('hudRound').textContent = `Nivel ${this.round}`;

        const totalBet = this.getTotalBet();
        const minBet = this.getMinimumBet();
        document.getElementById('hudTotalBet').textContent = `$${totalBet.toLocaleString()} (Mín: $${minBet.toLocaleString()})`;

        // Quota Progress Bar
        const progressPct = Math.min(100, Math.max(0, (this.balance / this.currentQuota) * 100));
        const fillEl = document.getElementById('quotaProgressFill');
        if (fillEl) {
            fillEl.style.width = `${progressPct.toFixed(1)}%`;
        }

        const spinBtn = document.getElementById('btnSpin');
        if (spinBtn) {
            spinBtn.disabled = this.isSpinning || totalBet <= 0;
        }

        const clearBtn = document.getElementById('btnClear');
        if (clearBtn) {
            clearBtn.disabled = this.isSpinning || totalBet <= 0;
        }

        const doubleBtn = document.getElementById('btnDouble');
        if (doubleBtn) {
            doubleBtn.disabled = this.isSpinning || totalBet <= 0 || this.balance < totalBet;
        }

        const repeatBtn = document.getElementById('btnRepeat');
        if (repeatBtn) {
            repeatBtn.disabled = this.isSpinning || !this.previousBets || this.balance < this.getBetsTotal(this.previousBets);
        }
    }

    getTotalBet() {
        return Object.values(this.currentBets).reduce((sum, val) => sum + val, 0);
    }

    getMinimumBet() {
        return Math.max(10, this.round * 10);
    }

    getBetsTotal(betsObj) {
        if (!betsObj) return 0;
        return Object.values(betsObj).reduce((sum, val) => sum + val, 0);
    }

    // --- Build Interactive Betting Board ---
    renderBettingGrid() {
        const board = document.getElementById('rouletteBoard');
        board.innerHTML = '';

        // 1. Zeros: 00 and 0
        const cell00 = document.createElement('div');
        cell00.className = 'bet-cell cell-00';
        cell00.dataset.bet = '00';
        cell00.textContent = '00';
        board.appendChild(cell00);

        const cell0 = document.createElement('div');
        cell0.className = 'bet-cell cell-0';
        cell0.dataset.bet = '0';
        cell0.textContent = '0';
        board.appendChild(cell0);

        // 2. Numbers 1 to 36 arranged in 3 rows x 12 columns
        // Row 1: 3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36
        // Row 2: 2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35
        // Row 3: 1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34
        const rows = [
            [3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36],
            [2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35],
            [1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34]
        ];

        rows.forEach((rowNums, rowIdx) => {
            rowNums.forEach((num, colIdx) => {
                const cell = document.createElement('div');
                const color = RED_NUMBERS.includes(num) ? 'red' : 'black';
                cell.className = `bet-cell ${color}`;
                cell.dataset.bet = num.toString();
                cell.textContent = num.toString();
                cell.style.gridRow = (rowIdx + 1).toString();
                cell.style.gridColumn = (colIdx + 2).toString();
                board.appendChild(cell);
            });
        });

        // 3. Dozens (Row 4)
        const dozens = [
            { id: '1st12', label: '1ST 12', cls: 'cell-1st12' },
            { id: '2nd12', label: '2ND 12', cls: 'cell-2nd12' },
            { id: '3rd12', label: '3RD 12', cls: 'cell-3rd12' }
        ];
        dozens.forEach(d => {
            const cell = document.createElement('div');
            cell.className = `bet-cell ${d.cls}`;
            cell.dataset.bet = d.id;
            cell.textContent = d.label;
            board.appendChild(cell);
        });

        // 4. Outside Bets (Row 5)
        const outside = [
            { id: '1-18', label: '1-18', cls: 'cell-1-18' },
            { id: 'even', label: 'EVEN', cls: 'cell-even' },
            { id: 'red', label: '', cls: 'cell-red-bet', icon: 'diamond-red' },
            { id: 'black', label: '', cls: 'cell-black-bet', icon: 'diamond-black' },
            { id: 'odd', label: 'ODD', cls: 'cell-odd' },
            { id: '19-36', label: '19-36', cls: 'cell-19-36' }
        ];
        outside.forEach(o => {
            const cell = document.createElement('div');
            cell.className = `bet-cell ${o.cls}`;
            cell.dataset.bet = o.id;
            if (o.label) {
                cell.textContent = o.label;
            } else if (o.icon) {
                const diamond = document.createElement('div');
                diamond.className = `diamond-icon ${o.icon}`;
                cell.appendChild(diamond);
            }
            board.appendChild(cell);
        });

        // Click handler for all betting cells
        board.addEventListener('click', (e) => {
            const cell = e.target.closest('.bet-cell');
            if (!cell || this.isSpinning) return;
            const betId = cell.dataset.bet;
            this.placeBet(betId);
        });

        // Right-click or shift-click to remove chip
        board.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            const cell = e.target.closest('.bet-cell');
            if (!cell || this.isSpinning) return;
            const betId = cell.dataset.bet;
            this.removeBet(betId);
        });
    }

    renderChipTray() {
        const chipValues = [10, 25, 50, 100, 500, 1000, 5000];
        const tray = document.getElementById('chipTray');
        tray.innerHTML = '';

        chipValues.forEach(val => {
            const btn = document.createElement('button');
            btn.className = `chip-btn chip-${val} ${this.selectedChip === val ? 'selected' : ''}`;
            btn.textContent = val >= 1000 ? `${val / 1000}K` : `$${val}`;
            btn.addEventListener('click', () => {
                this.selectedChip = val;
                window.soundEngine.playChip();
                this.renderChipTray();
            });
            tray.appendChild(btn);
        });
    }

    // Place bet on a cell
    placeBet(betId) {
        if (this.balance < this.selectedChip) {
            window.soundEngine.playTone(200, 'sawtooth', 0.1, 0.15, 0.08); // Error buzz
            return;
        }

        this.balance -= this.selectedChip;
        this.currentBets[betId] = (this.currentBets[betId] || 0) + this.selectedChip;
        window.soundEngine.playChip();
        this.updateBoardChips();
        this.updateHUD();
    }

    removeBet(betId) {
        if (!this.currentBets[betId] || this.currentBets[betId] <= 0) return;

        const refund = Math.min(this.selectedChip, this.currentBets[betId]);
        this.currentBets[betId] -= refund;
        this.balance += refund;

        if (this.currentBets[betId] <= 0) {
            delete this.currentBets[betId];
        }

        window.soundEngine.playChip();
        this.updateBoardChips();
        this.updateHUD();
    }

    updateBoardChips() {
        // Clear old badges
        document.querySelectorAll('.chip-badge').forEach(el => el.remove());

        // Add chip badges
        Object.entries(this.currentBets).forEach(([betId, amount]) => {
            const cell = document.querySelector(`.bet-cell[data-bet="${betId}"]`);
            if (cell && amount > 0) {
                const badge = document.createElement('div');
                badge.className = `chip-badge ${amount >= 1000 ? 'chip-1000' : amount >= 500 ? 'chip-500' : amount >= 100 ? 'chip-100' : amount >= 50 ? 'chip-50' : 'chip-10'}`;
                badge.textContent = amount >= 1000 ? `${(amount / 1000).toFixed(amount % 1000 === 0 ? 0 : 1)}K` : amount;
                cell.appendChild(badge);
            }
        });
    }

    clearBets() {
        if (this.isSpinning) return;
        const total = this.getTotalBet();
        this.balance += total;
        this.currentBets = {};
        window.soundEngine.playChip();
        this.updateBoardChips();
        this.updateHUD();
    }

    doubleBets() {
        if (this.isSpinning) return;
        const total = this.getTotalBet();
        if (total === 0 || this.balance < total) return;

        this.balance -= total;
        Object.keys(this.currentBets).forEach(betId => {
            this.currentBets[betId] *= 2;
        });
        window.soundEngine.playChip();
        this.updateBoardChips();
        this.updateHUD();
    }

    repeatBets() {
        if (this.isSpinning || !this.previousBets) return;
        const needed = this.getBetsTotal(this.previousBets);
        if (this.balance < needed) return;

        // Refund any current bets
        this.clearBets();

        // Copy previous
        this.balance -= needed;
        this.currentBets = { ...this.previousBets };
        window.soundEngine.playChip();
        this.updateBoardChips();
        this.updateHUD();
    }

    // --- Powers Handling ---
    togglePower(powerType) {
        if (this.isSpinning) return;
        if (this.powersInventory[powerType] <= 0 && !this.activePowers[powerType]) return;

        this.activePowers[powerType] = !this.activePowers[powerType];

        if (this.activePowers[powerType]) {
            if (powerType === 'reveal_number') {
                window.soundEngine.playClairvoyance();
                this.activateOracleReveal();
            } else if (powerType === 'loss_shield') {
                window.soundEngine.playShield();
            } else {
                window.soundEngine.playPowerSelect();
            }
        } else {
            if (powerType === 'reveal_number') {
                this.deactivateOracleReveal();
            }
        }

        this.updatePowerButtons();
    }

    activateOracleReveal() {
        // Predetermine winning number for next spin
        if (!this.predeterminedWinner) {
            const randomIndex = Math.floor(Math.random() * POCKETS.length);
            this.predeterminedWinner = POCKETS[randomIndex];
        }

        const oracleBanner = document.getElementById('oracleBanner');
        const oracleNum = document.getElementById('oracleNumber');
        const color = getPocketColor(this.predeterminedWinner);

        oracleNum.textContent = `${this.predeterminedWinner} (${color.toUpperCase()})`;
        oracleNum.style.backgroundColor = color === 'red' ? 'var(--red)' : color === 'black' ? 'var(--black)' : 'var(--green)';
        oracleBanner.style.display = 'block';
    }

    deactivateOracleReveal() {
        const oracleBanner = document.getElementById('oracleBanner');
        oracleBanner.style.display = 'none';
        // Note: we can keep or reset predeterminedWinner
    }

    updatePowerButtons() {
        ['double_win', 'reveal_number', 'loss_shield'].forEach(pt => {
            const btn = document.getElementById(`btnPower_${pt}`);
            const countEl = document.getElementById(`countPower_${pt}`);
            if (btn && countEl) {
                const count = this.powersInventory[pt];
                countEl.textContent = count;
                btn.disabled = count <= 0 && !this.activePowers[pt];
                if (this.activePowers[pt]) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            }
        });
    }

    // --- Spinning & Roulette Physics ---
    spin() {
        if (this.isSpinning) return;

        const totalBet = this.getTotalBet();
        const minBet = this.getMinimumBet();

        if (totalBet <= 0) {
            const banner = document.getElementById('turnResultBanner');
            if (banner) {
                banner.textContent = "Coloca al menos una ficha en el tapete para girar.";
                banner.style.display = 'block';
                setTimeout(() => { banner.style.display = 'none'; }, 2500);
            }
            return;
        }

        if (totalBet < minBet) {
            const banner = document.getElementById('turnResultBanner');
            if (banner) {
                banner.textContent = `⚠️ Apuesta mínima requerida: $${minBet.toLocaleString()} (Tienes $${totalBet.toLocaleString()})`;
                banner.style.display = 'block';
                setTimeout(() => { banner.style.display = 'none'; }, 3000);
            }
            return;
        }

        window.soundEngine.init();
        this.isSpinning = true;
        this.spinsLeft--;
        this.updateHUD();

        // Save current bets as previous
        this.previousBets = { ...this.currentBets };

        // Determine winning pocket
        let winningPocket;
        if (this.activePowers.reveal_number && this.predeterminedWinner) {
            winningPocket = this.predeterminedWinner;
        } else {
            const randomIndex = Math.floor(Math.random() * POCKETS.length);
            winningPocket = POCKETS[randomIndex];
        }

        this.targetPocket = winningPocket;
        const targetIndex = POCKETS.indexOf(winningPocket);

        // Consume activated powers
        const powersUsed = { ...this.activePowers };
        ['double_win', 'reveal_number', 'loss_shield'].forEach(pt => {
            if (this.activePowers[pt]) {
                this.powersInventory[pt] = Math.max(0, this.powersInventory[pt] - 1);
                this.activePowers[pt] = false;
            }
        });
        this.deactivateOracleReveal();
        this.predeterminedWinner = null;
        this.updatePowerButtons();

        // Spin animation parameters:
        // Rotor spins counter-clockwise or clockwise
        // Ball launches at high speed in opposite direction
        const pocketAngleSize = (Math.PI * 2) / 38;
        // In our rotor sprite, pocket '0' is at +PI/2 + half-pocket (94.74 deg, bottom)
        const pocketZeroOffset = (Math.PI / 2) + (Math.PI / 38);
        const targetPocketAngleOnRotor = pocketZeroOffset + (targetIndex * pocketAngleSize);

        // Physics parameters for smooth retro arcade spin
        const spinDuration = 4500; // ms
        const startTime = performance.now();
        const startBallAngle = this.ballAngle;
        const startRotorAngle = this.rotorAngle;

        // Ball makes multiple full revolutions
        const ballRevolutions = 8 + Math.random() * 2;
        // Rotor makes 2 to 3 revolutions
        const rotorRevolutions = -(2 + Math.random() * 0.5);

        this.ballLocked = false;
        let lastTickTime = 0;

        const animateSpin = (now) => {
            const elapsed = now - startTime;
            const progress = Math.min(1, elapsed / spinDuration);

            // Easing curves
            // Ball uses cubic ease out
            const ballEase = 1 - Math.pow(1 - progress, 3);
            // Rotor uses quadratic ease out
            const rotorEase = 1 - Math.pow(1 - progress, 2);

            // Compute current angles
            this.rotorAngle = startRotorAngle + (rotorRevolutions * Math.PI * 2 * rotorEase);

            // Target landing angle for the ball so that ballAngle - rotorAngle == targetPocketAngleOnRotor
            const finalRotorAngle = startRotorAngle + (rotorRevolutions * Math.PI * 2);
            const finalBallAngle = finalRotorAngle + targetPocketAngleOnRotor + (ballRevolutions * Math.PI * 2);

            this.ballAngle = startBallAngle + ((finalBallAngle - startBallAngle) * ballEase);

            // Ball radius drops from outer wooden track (~162) to pocket ring (~117) in last 35% of animation
            if (progress < 0.65) {
                this.ballRadius = 162;
            } else {
                const dropProgress = (progress - 0.65) / 0.35;
                // Add subtle bounce oscillation
                const bounce = Math.sin(dropProgress * Math.PI * 3) * 5 * (1 - dropProgress);
                this.ballRadius = 162 - (45 * dropProgress) + bounce;
            }

            // Audio tick as ball passes frets
            const speedRatio = (1 - progress);
            const tickInterval = 50 + (1 - speedRatio) * 250;
            if (now - lastTickTime > tickInterval && progress < 0.9) {
                window.soundEngine.playBallTick(speedRatio);
                lastTickTime = now;
            }

            if (progress < 1) {
                requestAnimationFrame(animateSpin);
            } else {
                // Ball drops into pocket
                this.ballRadius = 117;
                this.ballLocked = true;
                this.lastWinningPocket = winningPocket;
                this.lastResultColor = getPocketColor(winningPocket);
                window.soundEngine.playBallDrop();

                setTimeout(() => {
                    this.evaluateOutcome(winningPocket, powersUsed);
                }, 400);
            }
        };

        requestAnimationFrame(animateSpin);
    }

    // --- Payout Evaluation & Power Resolution ---
    evaluateOutcome(winningPocket, powersUsed) {
        const winNum = winningPocket === '0' || winningPocket === '00' ? null : parseInt(winningPocket, 10);
        const winColor = getPocketColor(winningPocket);

        let totalPayout = 0;
        let totalBet = this.getTotalBet();

        // Evaluate each placed bet
        Object.entries(this.currentBets).forEach(([betId, amount]) => {
            let won = false;
            let multiplier = 0;

            // Straight up
            if (betId === winningPocket) {
                won = true;
                multiplier = 36; // 35:1 + original bet returned
            }
            // Dozens (2:1 + original = 3x)
            else if (winNum !== null) {
                if (betId === '1st12' && winNum >= 1 && winNum <= 12) won = true;
                else if (betId === '2nd12' && winNum >= 13 && winNum <= 24) won = true;
                else if (betId === '3rd12' && winNum >= 25 && winNum <= 36) won = true;

                if (won) multiplier = 3;

                // Even chances (1:1 + original = 2x)
                if (betId === '1-18' && winNum >= 1 && winNum <= 18) { won = true; multiplier = 2; }
                if (betId === '19-36' && winNum >= 19 && winNum <= 36) { won = true; multiplier = 2; }
                if (betId === 'even' && winNum % 2 === 0) { won = true; multiplier = 2; }
                if (betId === 'odd' && winNum % 2 !== 0) { won = true; multiplier = 2; }
                if (betId === 'red' && winColor === 'red') { won = true; multiplier = 2; }
                if (betId === 'black' && winColor === 'black') { won = true; multiplier = 2; }
            }

            if (won) {
                totalPayout += amount * multiplier;
            }
        });

        // Compute net win or loss
        let netProfit = totalPayout - totalBet;

        // Apply Power 1: Double Win (Ganancias Dobles)
        if (powersUsed.double_win && netProfit > 0) {
            // Net profit is doubled
            const bonus = netProfit;
            totalPayout += bonus;
            netProfit *= 2;
        }

        // Apply Power 3: Loss Shield (Escudo de Pérdidas)
        if (powersUsed.loss_shield && netProfit < 0) {
            // Loss is cut in half -> 50% of the loss is refunded
            const lossAmount = Math.abs(netProfit);
            const refund = Math.floor(lossAmount * 0.5);
            totalPayout += refund;
        }

        // Credit payout to balance
        this.balance += totalPayout;

        // Update result display
        this.showResultBadge(winningPocket, winColor, totalPayout, netProfit, powersUsed);

        // Clear table bets
        this.currentBets = {};
        this.updateBoardChips();
        this.updateHUD();

        // Sound trigger
        if (totalPayout > totalBet) {
            window.soundEngine.playWin();
        }

        this.isSpinning = false;
        this.updateHUD();

        // Check Round Completion or Game Over
        setTimeout(() => {
            this.checkProgressionStatus();
        }, 1200);
    }

    showResultBadge(pocket, color, payout, netProfit, powersUsed) {
        const badge = document.getElementById('lastResultBadge');
        const valEl = document.getElementById('lastResultValue');
        valEl.textContent = `${pocket} ${color.toUpperCase()}`;
        valEl.style.backgroundColor = color === 'red' ? 'var(--red)' : color === 'black' ? 'var(--black)' : 'var(--green)';

        // Flash banner with profit or loss
        const resultBanner = document.getElementById('turnResultBanner');
        if (resultBanner) {
            let msg = `Número ganador: ${pocket} ${color.toUpperCase()}`;
            if (payout > 0) {
                msg += ` | ¡Ganaste +$${payout.toLocaleString()}!`;
                if (powersUsed.double_win) msg += ` (🔥 Ganancias x2)`;
            } else {
                msg += ` | No hubo acierto`;
                if (powersUsed.loss_shield) msg += ` (🛡️ Escudo protegió 50%)`;
            }
            resultBanner.textContent = msg;
            resultBanner.style.display = 'block';
            setTimeout(() => {
                resultBanner.style.display = 'none';
            }, 3500);
        }
    }

    // --- Progressive Quota Progression & Game Over ---
    checkProgressionStatus() {
        // Condition 1: Reached or exceeded the quota!
        if (this.balance >= this.currentQuota) {
            window.soundEngine.playJackpot();
            this.showRoundVictoryModal();
            return;
        }

        // Condition 2: Ran out of spins without reaching quota
        if (this.spinsLeft <= 0 && this.balance < this.currentQuota) {
            window.soundEngine.playGameOver();
            this.showGameOverModal('¡Te has quedado sin tiradas para alcanzar la cuota!');
            return;
        }

        // Condition 3: Total bankruptcy (0 balance and 0 bets)
        if (this.balance <= 0 && this.getTotalBet() <= 0) {
            window.soundEngine.playGameOver();
            this.showGameOverModal('¡Te has quedado sin fichas! Bancarrota total.');
            return;
        }
    }

    showRoundVictoryModal() {
        const modal = document.getElementById('victoryModal');
        const title = document.getElementById('victoryTitle');
        const desc = document.getElementById('victoryDesc');

        // Progressive quota scaling as requested by user:
        // "ejemplo se inicia con 100 la cuota serian 1000 y digamos si el jugador supera los 1000 llega a 2000 la proxima cuota son 6000"
        const achievedQuota = this.currentQuota;
        const currentBal = this.balance;

        // Progressive formula: 3x current balance or at least 2.5x previous quota
        const nextQuota = Math.max(
            Math.round(achievedQuota * 2.5 / 100) * 100,
            Math.round(currentBal * 3 / 100) * 100
        );

        this.nextCalculatedQuota = nextQuota;

        title.textContent = `¡CUOTA DE $${achievedQuota.toLocaleString()} SUPERADA!`;
        desc.innerHTML = `
            ¡Excelente jugada! Has acumulado <strong>$${currentBal.toLocaleString()}</strong>.<br>
            La próxima cuota para el Nivel ${this.round + 1} es de: <strong style="color:var(--gold); font-size:11px;">$${nextQuota.toLocaleString()}</strong>.<br><br>
            <em>Como recompensa, selecciona 1 de los 3 Poderes Sagrados:</em>
        `;

        modal.style.display = 'flex';
    }

    selectPowerReward(powerType) {
        window.soundEngine.playPowerSelect();
        this.powersInventory[powerType]++;

        // Advance level
        this.round++;
        this.currentQuota = this.nextCalculatedQuota;
        this.spinsLeft = this.maxSpins;

        document.getElementById('victoryModal').style.display = 'none';

        this.updatePowerButtons();
        this.updateHUD();
    }

    showGameOverModal(reason) {
        const modal = document.getElementById('gameOverModal');
        const desc = document.getElementById('gameOverDesc');
        desc.innerHTML = `
            ${reason}<br><br>
            Llegaste hasta el <strong>Nivel ${this.round}</strong> con saldo final de <strong>$${this.balance.toLocaleString()}</strong>.<br>
            Cuota exigida: $${this.currentQuota.toLocaleString()}
        `;
        modal.style.display = 'flex';
    }

    restartGame() {
        window.soundEngine.init();
        this.balance = 100;
        this.currentQuota = 1000;
        this.round = 1;
        this.spinsLeft = 6;
        this.currentBets = {};
        this.previousBets = null;
        this.powersInventory = { double_win: 0, reveal_number: 0, loss_shield: 0 };
        this.activePowers = { double_win: false, reveal_number: false, loss_shield: false };
        this.predeterminedWinner = null;
        this.deactivateOracleReveal();

        document.getElementById('gameOverModal').style.display = 'none';
        document.getElementById('victoryModal').style.display = 'none';

        this.updateBoardChips();
        this.updatePowerButtons();
        this.updateHUD();
    }

    // --- Canvas Wheel Render Loop ---
    startAnimationLoop() {
        const render = () => {
            // Idle rotation when not locked
            if (!this.isSpinning) {
                this.rotorAngle += 0.003;
                if (this.ballLocked) {
                    // Ball rotates in lockstep with the rotor
                    const targetIndex = POCKETS.indexOf(this.lastWinningPocket);
                    const pocketAngleSize = (Math.PI * 2) / 38;
                    const pocketZeroOffset = (Math.PI / 2) + (Math.PI / 38);
                    this.ballAngle = this.rotorAngle + pocketZeroOffset + (targetIndex * pocketAngleSize);
                }
            }

            this.drawWheel();
            requestAnimationFrame(render);
        };
        requestAnimationFrame(render);
    }

    drawWheel() {
        const ctx = this.ctx;
        const w = this.canvas.width;
        const h = this.canvas.height;
        const cx = w / 2;
        const cy = h / 2;

        ctx.clearRect(0, 0, w, h);
        ctx.imageSmoothingEnabled = false;

        // 1. Draw spinning rotor (408x408 aligned 1:1 with canvas)
        if (this.rotorImg.complete) {
            ctx.save();
            ctx.translate(cx, cy);
            ctx.rotate(this.rotorAngle);
            ctx.drawImage(this.rotorImg, -cx, -cy, w, h);
            ctx.restore();
        }

        // 2. Draw stationary outer wooden bowl with diamonds (408x408 aligned 1:1)
        if (this.bowlImg.complete) {
            ctx.drawImage(this.bowlImg, 0, 0, w, h);
        }

        // 3. Draw pixel-art ivory ball
        const bx = cx + Math.cos(this.ballAngle) * (this.ballRadius * (w / 408));
        const by = cy + Math.sin(this.ballAngle) * (this.ballRadius * (h / 408));

        // Ball shadow
        ctx.beginPath();
        ctx.arc(bx + 2, by + 2, 6, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
        ctx.fill();

        // Ball dark outline
        ctx.beginPath();
        ctx.arc(bx, by, 6, 0, Math.PI * 2);
        ctx.fillStyle = '#222222';
        ctx.fill();

        // Ball body
        ctx.beginPath();
        ctx.arc(bx, by, 4.8, 0, Math.PI * 2);
        ctx.fillStyle = '#f5f5f5';
        ctx.fill();

        // Ball highlight
        ctx.beginPath();
        ctx.arc(bx - 1.5, by - 1.5, 1.8, 0, Math.PI * 2);
        ctx.fillStyle = '#ffffff';
        ctx.fill();
    }

    bindEvents() {
        // Spin
        document.getElementById('btnSpin').addEventListener('click', () => this.spin());

        // Table controls
        document.getElementById('btnClear').addEventListener('click', () => this.clearBets());
        document.getElementById('btnDouble').addEventListener('click', () => this.doubleBets());
        document.getElementById('btnRepeat').addEventListener('click', () => this.repeatBets());

        // Powers
        document.getElementById('btnPower_double_win').addEventListener('click', () => this.togglePower('double_win'));
        document.getElementById('btnPower_reveal_number').addEventListener('click', () => this.togglePower('reveal_number'));
        document.getElementById('btnPower_loss_shield').addEventListener('click', () => this.togglePower('loss_shield'));

        // Power Reward Selection
        document.getElementById('cardPower_double_win').addEventListener('click', () => this.selectPowerReward('double_win'));
        document.getElementById('cardPower_reveal_number').addEventListener('click', () => this.selectPowerReward('reveal_number'));
        document.getElementById('cardPower_loss_shield').addEventListener('click', () => this.selectPowerReward('loss_shield'));

        // Restart button
        document.getElementById('btnRestart').addEventListener('click', () => this.restartGame());

        // Sound Mute
        document.getElementById('btnMute').addEventListener('click', (e) => {
            const isMuted = window.soundEngine.toggleMute();
            e.currentTarget.textContent = isMuted ? '🔇' : '🔊';
        });

        // Rules modal toggle
        document.getElementById('btnRules').addEventListener('click', () => {
            document.getElementById('rulesModal').style.display = 'flex';
        });
        document.getElementById('btnCloseRules').addEventListener('click', () => {
            document.getElementById('rulesModal').style.display = 'none';
        });
    }
}

// Start Game on load
window.addEventListener('DOMContentLoaded', () => {
    window.game = new ProgressiveRouletteGame();
});
