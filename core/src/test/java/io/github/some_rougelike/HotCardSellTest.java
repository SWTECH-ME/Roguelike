package io.github.some_rougelike;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import io.github.some_rougelike.logic.ChapterGenerator;
import io.github.some_rougelike.logic.Enemy;
import io.github.some_rougelike.logic.HotCard;
import io.github.some_rougelike.logic.RunState;
import io.github.some_rougelike.save.RunSaveData;
import io.github.some_rougelike.GameConfig;

public class HotCardSellTest {

    @Test
    public void testBasePurchaseAndSellPrice() {
        RunState run = new RunState();
        int initialMoney = run.getMoney();

        // STRIKE_GOLD base price is 10 (5 * 2)
        HotCard strikeGold = HotCard.byId(HotCard.STRIKE_GOLD);
        assertNotNull(strikeGold);
        assertEquals(10, strikeGold.getPrice());

        assertTrue(run.addHotCard(strikeGold.getId()));
        assertEquals(10, run.getHotCardPurchasePrice(strikeGold.getId()));
        assertEquals(5, run.getHotCardSellPrice(strikeGold.getId()));

        int sellPrice = run.sellHotCard(strikeGold.getId());
        assertEquals(5, sellPrice);
        assertEquals(initialMoney + 5, run.getMoney());
        assertFalse(run.hasHotCard(strikeGold.getId()));
    }

    @Test
    public void testDiscountedPurchasePriceAndSell() {
        RunState run = new RunState();
        int initialMoney = run.getMoney();

        // Suppose purchased with Costume Jewelry (25% off 10 = 8)
        HotCard strikeGold = HotCard.byId(HotCard.STRIKE_GOLD);
        assertTrue(run.addHotCard(strikeGold.getId(), 8));

        assertEquals(8, run.getHotCardPurchasePrice(strikeGold.getId()));
        // 50% of 8 = 4
        assertEquals(4, run.getHotCardSellPrice(strikeGold.getId()));

        int sellPrice = run.sellHotCard(strikeGold.getId());
        assertEquals(4, sellPrice);
        assertEquals(initialMoney + 4, run.getMoney());
        assertFalse(run.hasHotCard(strikeGold.getId()));
    }

    @Test
    public void testSlotCyclingWhenFull() {
        RunState run = new RunState();

        // Fill all 5 slots
        assertTrue(run.addHotCard(HotCard.CROSSED_BLADES));
        assertTrue(run.addHotCard(HotCard.IRON_HEART));
        assertTrue(run.addHotCard(HotCard.HOUSE_OF_CARDS));
        assertTrue(run.addHotCard(HotCard.CARDIAC_KING));
        assertTrue(run.addHotCard(HotCard.BLEEDING_HEART));

        assertEquals(5, run.getHotCards().size());
        assertFalse(run.hasFreeHotCardSlot());

        // Attempting to add a 6th card fails
        assertFalse(run.addHotCard(HotCard.STRIKE_GOLD));

        // Sell one card to cycle
        int sellPrice = run.sellHotCard(HotCard.CROSSED_BLADES);
        assertTrue(sellPrice > 0);
        assertEquals(4, run.getHotCards().size());
        assertTrue(run.hasFreeHotCardSlot());

        // Now we can buy/add a new card
        assertTrue(run.addHotCard(HotCard.STRIKE_GOLD));
        assertEquals(5, run.getHotCards().size());
        assertTrue(run.hasHotCard(HotCard.STRIKE_GOLD));
        assertFalse(run.hasHotCard(HotCard.CROSSED_BLADES));
    }

    @Test
    public void testResetCardStateOnSell() {
        RunState run = new RunState();
        assertTrue(run.addHotCard(HotCard.DIG_DEEP));

        run.getHotState().digDeepSpades = 15;
        assertEquals(15, run.getHotState().digDeepSpades);

        run.sellHotCard(HotCard.DIG_DEEP);
        assertEquals(0, run.getHotState().digDeepSpades);
        assertFalse(run.hasHotCard(HotCard.DIG_DEEP));
    }

    @Test
    public void testRunSaveDataPersistence() {
        RunState run = new RunState();
        run.addHotCard(HotCard.STRIKE_GOLD, 8); // bought for 8
        run.addHotCard(HotCard.BULLION, 12);     // bought for 12

        RunSaveData data = RunSaveData.fromRunState(run, null);

        RunState restored = data.toRunState();
        assertTrue(restored.hasHotCard(HotCard.STRIKE_GOLD));
        assertTrue(restored.hasHotCard(HotCard.BULLION));

        assertEquals(8, restored.getHotCardPurchasePrice(HotCard.STRIKE_GOLD));
        assertEquals(4, restored.getHotCardSellPrice(HotCard.STRIKE_GOLD));

        assertEquals(12, restored.getHotCardPurchasePrice(HotCard.BULLION));
        assertEquals(6, restored.getHotCardSellPrice(HotCard.BULLION));
    }

    @Test
    public void testStrictlyIncreasingBlindTargetsAcrossAllChapters() {
        int previousTarget = 0;
        int totalBlindsPerChapter = GameConfig.NORMALS_PER_CHAPTER + 1;

        for (int ch = 1; ch <= GameConfig.MAX_CHAPTERS; ch++) {
            for (int b = 0; b < totalBlindsPerChapter; b++) {
                int target = ChapterGenerator.calculateTargetScore(ch, b);
                assertTrue(target > previousTarget,
                    "Target at chapter " + ch + " blind " + b + " (" + target + ") must be > previous (" + previousTarget + ")");
                previousTarget = target;
            }
        }
    }

    @Test
    public void testChapterGeneratorTargetAssignments() {
        RunState run = new RunState();
        assertEquals(1, run.getChapterIndex());

        List<Enemy> normals = run.getAvailableNormals();
        Enemy boss = run.getBossEnemy();

        assertEquals(2, normals.size());
        assertTrue(normals.get(0).getTargetScore() < normals.get(1).getTargetScore(),
            "Normal 1 (" + normals.get(0).getTargetScore() + ") should be < Normal 2 (" + normals.get(1).getTargetScore() + ")");
        assertTrue(normals.get(1).getTargetScore() < boss.getTargetScore(),
            "Normal 2 (" + normals.get(1).getTargetScore() + ") should be < Boss (" + boss.getTargetScore() + ")");

        int ch1BossTarget = boss.getTargetScore();

        // Advance to chapter 2
        for (Enemy n : normals) {
            n.setDefeated(true);
        }
        run.setActiveEnemy(boss);
        run.registerVictory();

        assertEquals(2, run.getChapterIndex());
        List<Enemy> ch2Normals = run.getAvailableNormals();
        Enemy ch2Boss = run.getBossEnemy();

        assertTrue(ch2Normals.get(0).getTargetScore() > ch1BossTarget,
            "Chapter 2 Normal 1 (" + ch2Normals.get(0).getTargetScore() + ") should be > Chapter 1 Boss (" + ch1BossTarget + ")");
        assertTrue(ch2Normals.get(0).getTargetScore() < ch2Normals.get(1).getTargetScore());
        assertTrue(ch2Normals.get(1).getTargetScore() < ch2Boss.getTargetScore());
    }
}
