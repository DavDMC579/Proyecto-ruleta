package com.roulette.service.impl;

import com.roulette.dao.GameSessionDao;
import com.roulette.dao.PlayerDao;
import com.roulette.dto.GameSessionDTO;
import com.roulette.dto.SpinResultDTO;
import com.roulette.model.bet.Bet;
import com.roulette.model.entity.GameSession;
import com.roulette.model.entity.Player;
import com.roulette.model.perk.Perk;
import com.roulette.model.perk.PerkType;
import com.roulette.model.roulette.RoulettePocket;
import com.roulette.service.BettingService;
import com.roulette.service.GameSessionService;
import com.roulette.service.QuotaService;
import com.roulette.service.RouletteService;

import java.util.*;

/**
 * Implementation of GameSessionService complying with SOLID principles.
 */
public class GameSessionServiceImpl implements GameSessionService {
    private final PlayerDao playerDao;
    private final GameSessionDao sessionDao;
    private final RouletteService rouletteService;
    private final BettingService bettingService;
    private final QuotaService quotaService;

    public GameSessionServiceImpl(PlayerDao playerDao,
                                  GameSessionDao sessionDao,
                                  RouletteService rouletteService,
                                  BettingService bettingService,
                                  QuotaService quotaService) {
        this.playerDao = Objects.requireNonNull(playerDao);
        this.sessionDao = Objects.requireNonNull(sessionDao);
        this.rouletteService = Objects.requireNonNull(rouletteService);
        this.bettingService = Objects.requireNonNull(bettingService);
        this.quotaService = Objects.requireNonNull(quotaService);
    }

    @Override
    public GameSession createSession(String username) {
        Player player = playerDao.findByUsername(username)
                .orElseGet(() -> playerDao.save(new Player(null, username)));

        GameSession session = new GameSession();
        session.setPlayerId(player.getId());
        session.setCurrentBalance(100);
        session.setCurrentQuota(1000);
        session.setCurrentRound(1);
        session.setSpinsLeft(6);
        session.setStatus("ACTIVE");

        return sessionDao.save(session);
    }

    @Override
    public Optional<GameSession> getActiveSession(String username) {
        return playerDao.findByUsername(username)
                .flatMap(player -> sessionDao.findActiveSessionByPlayerId(player.getId()));
    }

    @Override
    public void saveSession(GameSession session) {
        if (session.getId() == null) {
            sessionDao.save(session);
        } else {
            sessionDao.update(session);
        }
    }

    @Override
    public SpinResultDTO processSpin(GameSession session, List<Bet> bets) {
        SpinResultDTO result = new SpinResultDTO();
        long minBet = quotaService.getMinimumMandatoryBet(session.getCurrentRound());

        // RQ03: Check minimum mandatory bet
        if (session.getCurrentBalance() < minBet && bettingService.calculateTotalBet(bets) < minBet) {
            session.setStatus("GAME_OVER");
            saveSession(session);
            result.setGameOver(true);
            result.setMessage("GAME OVER: Fondos insuficientes para cubrir la apuesta mínima obligatoria ($" + minBet + ")");
            return result;
        }

        long totalBet = bettingService.calculateTotalBet(bets);
        if (totalBet < minBet) {
            result.setMessage("Error: La apuesta total debe ser al menos $" + minBet);
            return result;
        }

        if (totalBet > session.getCurrentBalance()) {
            result.setMessage("Error: Saldo insuficiente");
            return result;
        }

        // Deduct bet from balance
        session.setCurrentBalance(session.getCurrentBalance() - totalBet);
        session.setSpinsLeft(session.getSpinsLeft() - 1);

        // Track perks used
        List<String> perksUsed = new ArrayList<>();
        Perk clairvoyancePerk = session.getPerk(PerkType.CLAIRVOYANCE);
        if (clairvoyancePerk != null && clairvoyancePerk.isActive()) {
            perksUsed.add("Clairvoyance");
            clairvoyancePerk.setActive(false);
            clairvoyancePerk.decrementQuantity();
        }

        // Spin the wheel
        RoulettePocket pocket = rouletteService.spin();
        result.setWinningPocket(pocket.getValue());
        result.setPocketColor(pocket.getColor().name());
        result.setTotalBetAmount(totalBet);

        // Evaluate payout
        long basePayout = bettingService.evaluatePayouts(bets, pocket);

        // Apply Loss Mitigation if active
        long finalPayout = basePayout;
        Perk lossPerk = session.getPerk(PerkType.LOSS_MITIGATION);
        if (lossPerk != null && lossPerk.isActive()) {
            if (basePayout < totalBet) {
                finalPayout = bettingService.applyLossMitigation(totalBet, basePayout);
                perksUsed.add("Loss Mitigation (-50% pérdidas)");
            }
            lossPerk.setActive(false);
            lossPerk.decrementQuantity();
        }

        result.setActivePerksUsed(perksUsed);
        result.setTotalPayout(finalPayout);
        long netProfit = finalPayout - totalBet;
        result.setNetProfit(netProfit);

        // Update balance
        session.setCurrentBalance(session.getCurrentBalance() + finalPayout);
        result.setNewBalance(session.getCurrentBalance());

        // Check if round quota is reached (RQ02, RQ04)
        if (quotaService.isQuotaReached(session.getCurrentBalance(), session.getCurrentQuota())) {
            result.setQuotaReached(true);
            result.setMessage("¡Felicidades! Has superado la cuota de $" + session.getCurrentQuota());
        } else if (session.getSpinsLeft() <= 0) {
            session.setStatus("GAME_OVER");
            result.setGameOver(true);
            result.setMessage("GAME OVER: Te has quedado sin tiradas para alcanzar la cuota");
        } else if (session.getCurrentBalance() < quotaService.getMinimumMandatoryBet(session.getCurrentRound())) {
            session.setStatus("GAME_OVER");
            result.setGameOver(true);
            result.setMessage("GAME OVER: Fondos insuficientes para la apuesta mínima de la siguiente ronda");
        }

        saveSession(session);
        return result;
    }

    @Override
    public void grantPerkReward(GameSession session, PerkType perkType) {
        if (perkType == PerkType.MONEY_BOOST) {
            // Money Boost (RQ05): +50% of initial balance ($100 * 0.50 = +$50)
            long boost = 50;
            session.setCurrentBalance(session.getCurrentBalance() + boost);
        } else {
            Perk perk = session.getPerk(perkType);
            if (perk != null) {
                perk.incrementQuantity();
            }
        }

        // Advance to next round and calculate progressive quota (RQ04)
        long nextQuota = quotaService.calculateNextQuota(session.getCurrentQuota(), session.getCurrentBalance());
        session.setCurrentQuota(nextQuota);
        session.setCurrentRound(session.getCurrentRound() + 1);
        session.setSpinsLeft(6);

        saveSession(session);
    }

    @Override
    public void togglePerk(GameSession session, PerkType perk, boolean active) {
        Perk p = session.getPerk(perk);
        if (p != null && (p.getQuantity() > 0 || !active)) {
            p.setActive(active);
        }
    }

    @Override
    public String revealNextNumber(GameSession session) {
        Perk perk = session.getPerk(PerkType.CLAIRVOYANCE);
        if (perk != null && perk.getQuantity() > 0) {
            perk.setActive(true);
            RoulettePocket nextPocket = rouletteService.peekNextPocket();
            return nextPocket.toString();
        }
        return null;
    }

    @Override
    public GameSessionDTO toDTO(GameSession session, Player player) {
        GameSessionDTO dto = new GameSessionDTO();
        dto.setId(session.getId());
        dto.setPlayerId(session.getPlayerId());
        dto.setPlayerName(player != null ? player.getUsername() : "Player");
        dto.setCurrentBalance(session.getCurrentBalance());
        dto.setCurrentQuota(session.getCurrentQuota());
        dto.setCurrentRound(session.getCurrentRound());
        dto.setSpinsLeft(session.getSpinsLeft());
        dto.setStatus(session.getStatus());
        dto.setUpdatedAt(session.getUpdatedAt());

        Map<PerkType, Integer> perkMap = new EnumMap<>(PerkType.class);
        for (Map.Entry<PerkType, Perk> entry : session.getPerks().entrySet()) {
            perkMap.put(entry.getKey(), entry.getValue().getQuantity());
        }
        dto.setPerkInventory(perkMap);

        return dto;
    }
}
