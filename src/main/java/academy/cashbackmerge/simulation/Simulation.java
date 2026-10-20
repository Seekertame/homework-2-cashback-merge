package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.game.Cashback;
import academy.cashbackmerge.game.GameState;
import academy.cashbackmerge.game.MoveEngine;
import academy.cashbackmerge.game.TileSpawner;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Много партий подряд со стратегией random. Один seed даёт один и тот же результат. */
public final class Simulation {

    private Simulation() {}

    public static SimulationResult run(CampaignConfig config, int players, long seed) {
        if (players <= 0) {
            throw new IllegalArgumentException("Число игроков должно быть больше нуля, сейчас " + players);
        }
        Random random = new Random(seed);
        TileSpawner spawner = new TileSpawner(config.spawnRules(), random);
        MoveEngine engine = new MoveEngine(spawner);
        RandomStrategy strategy = new RandomStrategy(random);

        Map<Cashback, Integer> maxTileCounts = new HashMap<>();
        long totalScore = 0;
        int maxScore = 0;
        int maxRewardPlayers = 0;
        for (int player = 0; player < players; player++) {
            GameState end = play(GameState.start(config.movesLimit(), spawner), engine, strategy);
            Cashback maxTile = end.board().maxTile();
            maxTileCounts.put(maxTile, maxTileCounts.getOrDefault(maxTile, 0) + 1);
            totalScore += end.score();
            maxScore = Math.max(maxScore, end.score());
            if (config.isMaxReward(maxTile)) {
                maxRewardPlayers++;
            }
        }
        return new SimulationResult(players, maxTileCounts, totalScore, maxScore, maxRewardPlayers);
    }

    /** Партия до конца. Ход без изменений не тратит лимит, поэтому стратегия просто выбирает снова. */
    private static GameState play(GameState start, MoveEngine engine, RandomStrategy strategy) {
        GameState state = start;
        while (!state.isOver()) {
            state = engine.applyMove(state, strategy.chooseMove()).state();
        }
        return state;
    }
}
