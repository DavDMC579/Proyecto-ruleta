package com.roulette;

import com.roulette.config.DatabaseConfig;
import com.roulette.dao.GameSessionDao;
import com.roulette.dao.PlayerDao;
import com.roulette.dao.impl.InMemoryGameSessionDao;
import com.roulette.dao.impl.InMemoryPlayerDao;
import com.roulette.dao.impl.PostgreSqlGameSessionDao;
import com.roulette.dao.impl.PostgreSqlPlayerDao;
import com.roulette.service.BettingService;
import com.roulette.service.GameSessionService;
import com.roulette.service.QuotaService;
import com.roulette.service.RouletteService;
import com.roulette.service.impl.BettingServiceImpl;
import com.roulette.service.impl.GameSessionServiceImpl;
import com.roulette.service.impl.QuotaServiceImpl;
import com.roulette.service.impl.RouletteServiceImpl;
import com.roulette.view.ConsoleView;
import com.roulette.view.GameGuiView;

import javax.swing.*;
import java.awt.*;

/**
 * Main application entry point for Roulette Roguelike Game (RQ07, RQ08, RQ10, RQ11).
 */
public class RouletteApplication {

    public static void main(String[] args) {
        System.out.println("Starting Roulette Roguelike Game...");

        // 1. Initialize DAO Layer with PostgreSQL or In-Memory fallback (RQ08, RQ11)
        PlayerDao playerDao;
        GameSessionDao sessionDao;

        if (DatabaseConfig.isConnected()) {
            System.out.println("✓ Connected to PostgreSQL database successfully.");
            playerDao = new PostgreSqlPlayerDao();
            sessionDao = new PostgreSqlGameSessionDao();
        } else {
            System.out.println("ℹ PostgreSQL connection not detected. Using In-Memory persistent DAO fallback.");
            playerDao = new InMemoryPlayerDao();
            sessionDao = new InMemoryGameSessionDao();
        }

        // 2. Initialize Service Layer complying with SOLID / Dependency Inversion
        RouletteService rouletteService = new RouletteServiceImpl();
        BettingService bettingService = new BettingServiceImpl();
        QuotaService quotaService = new QuotaServiceImpl();

        GameSessionService gameSessionService = new GameSessionServiceImpl(
                playerDao,
                sessionDao,
                rouletteService,
                bettingService,
                quotaService
        );

        // 3. Launch Presentation Layer (GUI or Console)
        boolean forceConsole = args.length > 0 && "--console".equalsIgnoreCase(args[0]);

        if (!GraphicsEnvironment.isHeadless() && !forceConsole) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ignored) {}
                GameGuiView gui = new GameGuiView(gameSessionService, "Player1");
                gui.setVisible(true);
            });
        } else {
            ConsoleView console = new ConsoleView(gameSessionService);
            console.start();
        }
    }
}
