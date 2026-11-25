package com.roulette.view;

import com.roulette.factory.BetFactory;
import com.roulette.model.bet.Bet;
import com.roulette.model.entity.GameSession;
import com.roulette.model.perk.PerkType;
import com.roulette.model.roulette.PocketColor;
import com.roulette.service.GameSessionService;
import com.roulette.dto.SpinResultDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console / Terminal User Interface for the Roulette Game (RQ07, RQ10).
 */
public class ConsoleView {
    private final GameSessionService gameSessionService;
    private final BetFactory betFactory;
    private final Scanner scanner;

    public ConsoleView(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
        this.betFactory = new BetFactory();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=========================================================");
        System.out.println("      🎰 RULETA ROGUELIKE: RETO DE CUOTAS PROGRESIVAS    ");
        System.out.println("=========================================================");
        System.out.print("Ingresa tu nombre de jugador: ");
        String input = scanner.nextLine().trim();
        String username = input.isEmpty() ? "Player1" : input;

        GameSession session = gameSessionService.getActiveSession(username)
                .orElseGet(() -> gameSessionService.createSession(username));

        boolean running = true;
        while (running && "ACTIVE".equals(session.getStatus())) {
            displayHUD(session);
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. 🎲 Colocar Apuestas y Girar Ruleta");
            System.out.println("2. 🔮 Usar Ventaja Clairvoyance");
            System.out.println("3. 🛡️ Alternar Escudo Loss Mitigation");
            System.out.println("4. 📖 Ver Panel Informativo de Pagos (RQ07)");
            System.out.println("5. 💾 Guardar Partida");
            System.out.println("6. 🚪 Salir");
            System.out.print("Selecciona una opción: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleSpinTurn(session);
                    break;
                case "2":
                    handleClairvoyance(session);
                    break;
                case "3":
                    handleLossMitigation(session);
                    break;
                case "4":
                    displayRules();
                    break;
                case "5":
                    gameSessionService.saveSession(session);
                    System.out.println("✓ Partida guardada correctamente.");
                    break;
                case "6":
                    running = false;
                    System.out.println("¡Gracias por jugar!");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    private void displayHUD(GameSession session) {
        System.out.println("\n---------------------------------------------------------");
        System.out.printf(" SALDO: $%,d  |  CUOTA: $%,d  |  TIRADAS: %d/6  |  RONDA: %d\n",
                session.getCurrentBalance(), session.getCurrentQuota(), session.getSpinsLeft(), session.getCurrentRound());
        System.out.println("---------------------------------------------------------");
    }

    private void handleSpinTurn(GameSession session) {
        List<Bet> bets = new ArrayList<>();
        boolean placing = true;

        while (placing) {
            System.out.println("\n--- TIPO DE APUESTA ---");
            System.out.println("1. Número directo (Pleno 35:1)");
            System.out.println("2. Color (Rojo / Negro 1:1)");
            System.out.println("3. Paridad (Par / Impar 1:1)");
            System.out.println("4. Docena (1st 12, 2nd 12, 3rd 12 2:1)");
            System.out.println("5. Finalizar apuestas y GIRAR RULETA");
            System.out.print("Opción: ");

            String type = scanner.nextLine().trim();
            if ("5".equals(type)) {
                if (bets.isEmpty()) {
                    System.out.println("Debes colocar al menos una apuesta.");
                    continue;
                }
                placing = false;
                break;
            }

            System.out.print("Monto a apostar: $");
            long amount;
            try {
                amount = Long.parseLong(scanner.nextLine().trim());
                if (amount <= 0) {
                    System.out.println("El monto debe ser positivo.");
                    continue;
                }
                long currentTotal = bets.stream().mapToLong(Bet::getAmount).sum();
                if (currentTotal + amount > session.getCurrentBalance()) {
                    System.out.printf("Saldo insuficiente. Saldo disponible para apostar: $%,d\n", (session.getCurrentBalance() - currentTotal));
                    continue;
                }
            } catch (Exception e) {
                System.out.println("Monto inválido.");
                continue;
            }

            try {
                switch (type) {
                    case "1":
                        System.out.print("Ingresa el número (0, 00, o 1 a 36): ");
                        String num = scanner.nextLine().trim();
                        bets.add(betFactory.createNumberBet(num, amount));
                        break;
                    case "2":
                        System.out.print("Color (ROJO o NEGRO): ");
                        String col = scanner.nextLine().trim().toUpperCase();
                        bets.add(betFactory.createColorBet(PocketColor.valueOf(col), amount));
                        break;
                    case "3":
                        System.out.print("Paridad (EVEN u ODD): ");
                        String par = scanner.nextLine().trim().toUpperCase();
                        bets.add(betFactory.createParityBet(com.roulette.model.bet.ParityBet.Parity.valueOf(par), amount));
                        break;
                    case "4":
                        System.out.print("Docena (FIRST, SECOND, THIRD): ");
                        String doc = scanner.nextLine().trim().toUpperCase();
                        bets.add(betFactory.createDozenBet(com.roulette.model.bet.DozenBet.Dozen.valueOf(doc), amount));
                        break;
                }
                System.out.println("✓ Apuesta añadida.");
            } catch (Exception ex) {
                System.out.println("Error en la apuesta: " + ex.getMessage());
            }
        }

        System.out.println("\n🎲 ¡Girando la ruleta!...");
        SpinResultDTO result = gameSessionService.processSpin(session, bets);

        if (result.getWinningPocket() == null) {
            System.out.println("❌ " + (result.getMessage() != null ? result.getMessage() : "Error al procesar la tirada"));
            return;
        }

        System.out.println("Número ganador: " + result.getWinningPocket() + " (" + result.getPocketColor() + ")");
        System.out.printf("Ganancia del turno: +$%,d | Saldo actualizado: $%,d\n", result.getTotalPayout(), result.getNewBalance());

        if (result.isGameOver()) {
            System.out.println("\n" + result.getMessage());
        } else if (result.isQuotaReached()) {
            System.out.println("\n🎉 " + result.getMessage());
            choosePerk(session);
        }
    }

    private void choosePerk(GameSession session) {
        System.out.println("\n--- ELIGE TU VENTAJA ESTRATÉGICA (RQ05) ---");
        System.out.println("1. 🌟 Money Boost (+50% balance base)");
        System.out.println("2. 🔮 Clairvoyance (Revela número)");
        System.out.println("3. 🛡️ Loss Mitigation (-50% pérdidas)");
        System.out.print("Opción (1-3): ");

        String opt = scanner.nextLine().trim();
        PerkType selected = "2".equals(opt) ? PerkType.CLAIRVOYANCE : "3".equals(opt) ? PerkType.LOSS_MITIGATION : PerkType.MONEY_BOOST;
        gameSessionService.grantPerkReward(session, selected);
        System.out.println("✓ Ventaja obtenida: " + selected.getDisplayName());
        System.out.printf("Avanzaste a la Ronda %d con Cuota de: $%,d\n", session.getCurrentRound(), session.getCurrentQuota());
    }

    private void handleClairvoyance(GameSession session) {
        String revealed = gameSessionService.revealNextNumber(session);
        if (revealed != null) {
            System.out.println("🔮 VISIÓN: ¡El próximo número será " + revealed + "!");
        } else {
            System.out.println("No tienes cargas de Clairvoyance disponibles.");
        }
    }

    private void handleLossMitigation(GameSession session) {
        var perk = session.getPerk(PerkType.LOSS_MITIGATION);
        if (perk != null && perk.getQuantity() > 0) {
            perk.setActive(!perk.isActive());
            System.out.println("Escudo Loss Mitigation: " + (perk.isActive() ? "ACTIVADO" : "DESACTIVADO"));
        } else {
            System.out.println("No tienes cargas de Loss Mitigation.");
        }
    }

    private void displayRules() {
        System.out.println("\n=========================================================");
        System.out.println("        TABLA DE MULTIPLICADORES Y PAGOS (RQ07)          ");
        System.out.println("=========================================================");
        System.out.println("• Pleno (Número específico): Paga 35 a 1 (Devuelve 36x)");
        System.out.println("• Docenas (1st 12, 2nd 12, 3rd 12): Paga 2 a 1 (Devuelve 3x)");
        System.out.println("• Color (Rojo / Negro): Paga 1 a 1 (Devuelve 2x)");
        System.out.println("• Paridad (Par / Impar): Paga 1 a 1 (Devuelve 2x)");
        System.out.println("=========================================================");
    }
}
