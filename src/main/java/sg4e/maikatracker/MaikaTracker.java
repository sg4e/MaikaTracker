/*
 * Copyright (C) 2019 sg4e
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sg4e.maikatracker;

import sg4e.ff4stats.Enemy;
import sg4e.ff4stats.Battle;
import sg4e.ff4stats.Formation;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg4e.ff4stats.fe.KeyItem;
import sg4e.ff4stats.fe.KeyItemLocation;
import sg4e.ff4stats.party.PartyMember;

/**
 *
 * @author sg4e
 */
public class MaikaTracker extends javax.swing.JFrame {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private final TreasureAtlas atlas = new TreasureAtlas();
    private final Set<KeyItemLocation> locationsVisited = new HashSet<>();
    private static final String TOWER_OF_ZOT = "Zot";
    private static final String EBLAN_CASTLE = "Eblan Castle";
    private static final String EBLAN_CAVE = "Eblan Cave";
    private static final String UPPER_BABIL = "Upper Bab-il";
    private static final String LOWER_BABIL = "Lower Bab-il";
    private static final String LAND_OF_SUMMONED_MONSTERS = "Land of Monsters";
    private static final String GIANT_OF_BABIL = "Giant of Bab-il";
    private static final String LUNAR_PATH = "Lunar Path";
    private static final String LUNAR_SUBTERRANE = "Lunar Subterrane";
    private static final String LUNAR_CORE = "Lunar Core";
    private static final String SYLPH_CAVE = "Sylph Cave";
    private static final int D_MACHIN_XP = 41500;
    
    private final JPanel logicPanel;

    /**
     * Creates new form MaikaTracker
     */
    public MaikaTracker() {
        initComponents();
        Map<Battle, Formation> bosses = Battle.getAllBosses();
        List<String> bossNames = bosses.keySet().stream().map(Battle::getBoss).distinct().collect(Collectors.toList());
        List<String> positions = bosses.keySet().stream().map(Battle::getPosition).distinct().collect(Collectors.toList());
        Collections.sort(bossNames);
        Collections.sort(positions);
        AutoCompleteSupport.install(bossComboBox, GlazedLists.eventList(bossNames));
        AutoCompleteSupport.install(positionComboBox, GlazedLists.eventList(positions));
        
        logicPanel = new JPanel();
        logicPanel.setLayout(new GridLayout(0, 2));
        logicTabPanel.add(logicPanel);
        
        //add party characters
        PartyTableModel partyTableModel = (PartyTableModel) xpTable.getModel();
        Runnable newPartyMemberCallback = () -> partyTableModel.setPartyMembers(getPartyMembers());
        for(int i = 0; i < 5; i++) {
            PartyLabel label = new PartyLabel(null);
            label.setOnPartyChangeAction(newPartyMemberCallback);
            if(i != 0)
                label.setBorder(new EmptyBorder(0, 30, 0, 0));
            partyPanel.add(label);
        }
        
        //add boss icons
        LayoutManager bossIconLayout = new GridLayout(3, 12);
        bossIconPanel.setLayout(bossIconLayout);
        loadBossIcon("FFIVFE-Bosses-1MistD-Gray.png", "FFIVFE-Bosses-1MistD-Color.png", "D. Mist");
        loadBossIcon("FFIVFE-Bosses-2Soldier-Gray.png", "FFIVFE-Bosses-2Soldier-Color.png", "Baron Soldiers");
        loadBossIcon("FFIVFE-Bosses-3Octo-Gray.png", "FFIVFE-Bosses-3Octo-Color.png", "Octomam");
        loadBossIcon("FFIVFE-Bosses-4Antlion-Gray.png", "FFIVFE-Bosses-4Antlion-Color.png", "Antlion");
        loadBossIcon("FFIVFE-Bosses-5WHag-Gray.png", "FFIVFE-Bosses-5WHag-Color.png", "Waterhag");
        loadBossIcon("FFIVFE-Bosses-6Mombomb-Gray.png", "FFIVFE-Bosses-6Mombomb-Color.png", "Mombomb");
        loadBossIcon("FFIVFE-Bosses-7Gauntlet-Gray.png", "FFIVFE-Bosses-7Gauntlet-Color.png", "Fabul Guantlet");
        loadBossIcon("FFIVFE-Bosses-8Milon-Gray.png", "FFIVFE-Bosses-8Milon-Color.png", "Milon");
        loadBossIcon("FFIVFE-Bosses-9MilonZ-Gray.png", "FFIVFE-Bosses-9MilonZ-Color.png", "MilonZ");
        loadBossIcon("FFIVFE-Bosses-10DKCecil-Gray.png", "FFIVFE-Bosses-10DKCecil-Color.png", "Dark Knight Cecil");
        loadBossIcon("FFIVFE-Bosses-11Guards-Gray.png", "FFIVFE-Bosses-11Guards-Color.png", "Baron Guards");
        loadBossIcon("FFIVFE-Bosses-12Yang-Gray.png", "FFIVFE-Bosses-12Yang-Color.png", "Karate");
        loadBossIcon("FFIVFE-Bosses-13Baigan-Gray.png", "FFIVFE-Bosses-13Baigan-Color.png", "Baigan");
        loadBossIcon("FFIVFE-Bosses-14Kainazzo-Gray.png", "FFIVFE-Bosses-14Kainazzo-Color.png", "Kainazzo");
        loadBossIcon("FFIVFE-Bosses-15DElf-Gray.png", "FFIVFE-Bosses-15DElf-Color.png", "Dark Elf");
        loadBossIcon("FFIVFE-Bosses-16MagusSis-Gray.png", "FFIVFE-Bosses-16MagusSis-Color.png", "Magus Sisters");
        loadBossIcon("FFIVFE-Bosses-17Valvalis-Gray.png", "FFIVFE-Bosses-17Valvalis-Color.png", "Valvalis");
        loadBossIcon("FFIVFE-Bosses-18Calcabrina-Gray.png", "FFIVFE-Bosses-18Calcabrina-Color.png", "Calcabrina");
        loadBossIcon("FFIVFE-Bosses-19Golbez-Gray.png", "FFIVFE-Bosses-19Golbez-Color.png", "Golbez");
        loadBossIcon("FFIVFE-Bosses-20Lugae-Gray.png", "FFIVFE-Bosses-20Lugae-Color.png", "Dr. Lugae");
        loadBossIcon("FFIVFE-Bosses-21DarkImps-Gray.png", "FFIVFE-Bosses-21DarkImps-Color.png", "Dark Imps");
        loadBossIcon("FFIVFE-Bosses-21Eblan-Gray.png", "FFIVFE-Bosses-21Eblan-Color.png", "Eblan King & Queen");
        loadBossIcon("FFIVFE-Bosses-22Rubicante-Gray.png", "FFIVFE-Bosses-22Rubicante-Color.png", "Rubicante");
        loadBossIcon("FFIVFE-Bosses-23EvilWall-Gray.png", "FFIVFE-Bosses-23EvilWall-Color.png", "Evil Wall");
        loadBossIcon("FFIVFE-Bosses-24Fiends-Gray.png", "FFIVFE-Bosses-24Fiends-Color.png", "Elements");
        loadBossIcon("FFIVFE-Bosses-25CPU-Gray.png", "FFIVFE-Bosses-25CPU-Color.png", "CPU");
        loadBossIcon("FFIVFE-Bosses-26Odin-Gray.png", "FFIVFE-Bosses-26Odin-Color.png", "Odin");
        loadBossIcon("FFIVFE-Bosses-28Leviath-Gray.png", "FFIVFE-Bosses-28Leviath-Color.png", "Leviatan");
        loadBossIcon("FFIVFE-Bosses-27Asura-Gray.png", "FFIVFE-Bosses-27Asura-Color.png", "Asura");
        loadBossIcon("FFIVFE-Bosses-29Bahamut-Gray.png", "FFIVFE-Bosses-29Bahamut-Color.png", "Bahamut");
        loadBossIcon("FFIVFE-Bosses-30PaleDim-Gray.png", "FFIVFE-Bosses-30PaleDim-Color.png", "Pale Dim");
        loadBossIcon("FFIVFE-Bosses-31LunarD-Gray.png", "FFIVFE-Bosses-31LunarD-Color.png", "Lunar D.");
        loadBossIcon("FFIVFE-Bosses-32Plague-Gray.png", "FFIVFE-Bosses-32Plague-Color.png", "Plague");
        loadBossIcon("FFIVFE-Bosses-33Ogopogo-Gray.png", "FFIVFE-Bosses-33Ogopogo-Color.png", "Ogopogo");
        loadBossIcon("FFIVFE-Bosses-34Wyvern-Gray.png", "FFIVFE-Bosses-34Wyvern-Color.png", "Wyvern");
        
        //add maps
        final String zot = "zot";
        initMap(zot, "1", TOWER_OF_ZOT, "1F",
                new TreasureChest("Z1", 4, 3));
        initMap(zot, "2", TOWER_OF_ZOT, "2F",
                new TreasureChest("Z2", 15, 7));
        initMap(zot, "5", TOWER_OF_ZOT, "5F",
                new TreasureChest("Z3", 9, 11),
                new TreasureChest("Z4", 23, 10),
                new TreasureChest("Z5", 10, 11),
                new TreasureChest("Z6", 14, 19));
        final String ebcast = "eblan-castle";
        initMap(ebcast, "1", EBLAN_CASTLE, "Left 1F",
                new TreasureChest("E1", 5, 11),
                new TreasureChest("E6", 5, 1));
        initMap(ebcast, "2", EBLAN_CASTLE, "Left 2F",
                new TreasureChest("E2", 8, 8),
                new TreasureChest("E3", 5, 10),
                new TreasureChest("E4", 5, 11),
                new TreasureChest("E5", 4, 4));
        initMap(ebcast, "3", EBLAN_CASTLE, "Center Foyer",
                new TreasureChest("E7", 4, 13));
        initMap(ebcast, "4", EBLAN_CASTLE, "Center Hall",
                new TreasureChest("E8", 3, 4),
                new TreasureChest("E9", 4, 4),
                new TreasureChest("E10", 3, 14),
                new TreasureChest("E11", 4, 15),
                new TreasureChest("E12", 4, 14));
        initMap(ebcast, "6", EBLAN_CASTLE, "Right 1F",
                new TreasureChest("E13", 5, 2),
                new TreasureChest("E19", 5, 12));
        initMap(ebcast, "7", EBLAN_CASTLE, "Right 2F",
                new TreasureChest("E14", 5, 1),
                new TreasureChest("E15", 6, 1),
                new TreasureChest("E16", 3, 6),
                new TreasureChest("E17", 5, 11),
                new TreasureChest("E18", 6, 11));
        initMap(ebcast, "8", EBLAN_CASTLE, "Basement",
                new TreasureChest("E19", 11, 2),
                new TreasureChest("E20", 11, 3),
                new TreasureChest("E21", 12, 8));
        final String ebcave = "eblan-cave";
        initMap(ebcave, "1", EBLAN_CAVE, "Entrance",
                new TreasureChest("V1", 4, 18),
                new TreasureChest("V2", 3, 22),
                new TreasureChest("V3", 25, 26));
        initMap(ebcave, "infirmary", EBLAN_CAVE, "Infirmary",
                new TreasureChest("V4", 7, 14),
                new TreasureChest("V5", 5, 2));
        initMap(ebcave, "p1", EBLAN_CAVE, "Pass 1",
                new TreasureChest("V6", 28, 8),
                new TreasureChest("V7", 10, 4),
                new TreasureChest("V8", 26, 10),
                new TreasureChest("V11", 12, 20),
                new TreasureChest("V12", 14, 20),
                new TreasureChest("V13", 11, 12),
                new TreasureChest("V14", 11, 16),
                new TreasureChest("V15", 12, 16),
                new TreasureChest("V16", 28, 15),
                new TreasureChest("V17", 27, 27),
                new TreasureChest("V18", 27, 28),
                new TreasureChest("V19", 27, 29));
        initMap(ebcave, "p2", EBLAN_CAVE, "Pass 2",
                new TreasureChest("V9", 8, 14),
                new TreasureChest("V10", 22, 18),
                new TreasureChest("V20", 16, 18),
                new TreasureChest("V21", 23, 29),
                new TreasureChest("V22", 9, 17));
        final String upperBabil = "upper-babil";
        initMap(upperBabil, "1f", UPPER_BABIL, "1F",
                new TreasureChest("U1", 4, 18),
                new TreasureChest("U2", 12, 26));
        initMap(upperBabil, "b1f", UPPER_BABIL, "B1F",
                new TreasureChest("U3", 20, 14));
        initMap(upperBabil, "b2f", UPPER_BABIL, "B2F",
                new TreasureChest("U4", 21, 20));
        initMap(upperBabil, "b3f", UPPER_BABIL, "B3F",
                new TreasureChest("U5", 18, 12));
        initMap(upperBabil, "b4f", UPPER_BABIL, "B4F",
                new TreasureChest("U6", 15, 5));
        final String lowerBabil = "lower-babil";
        initMap(lowerBabil, "1f", LOWER_BABIL, "1F",
                new TreasureChest("B1", 10, 5),
                new TreasureChest("B2", 20, 6),
                new TreasureChest("B3", 12, 26));
        initMap(lowerBabil, "2f", LOWER_BABIL, "2F",
                new TreasureChest("B4", 7, 8),
                new TreasureChest("B5", 22, 15),
                new TreasureChest("B6", 16, 23));
        initMap(lowerBabil, "3f", LOWER_BABIL, "3F",
                new TreasureChest("B9", 19, 26),
                new TreasureChest("B10", 11, 13),
                new TreasureChest("B11", 25, 15));
        initMap(lowerBabil, "4f", LOWER_BABIL, "4F",
                new TreasureChest("B7", 19, 16),
                new TreasureChest("B8", 23, 29),
                new TreasureChest("B12", 17, 26),
                new TreasureChest("B13", 8, 27));
        initMap(lowerBabil, "5f", LOWER_BABIL, "5F",
                new TreasureChest("B14", 12, 6),
                new TreasureChest("B15", 22, 29));
        initMap(lowerBabil, "7f", LOWER_BABIL, "7F",
                new TreasureChest("B16", 24, 16));
        final String sm = "sm";
        initMap(sm, "1", LAND_OF_SUMMONED_MONSTERS, "B1F",
                new TreasureChest("M1", 6, 26),
                new TreasureChest("M2", 6, 8),
                new TreasureChest("M3", 21, 25));
        initMap(sm, "2", LAND_OF_SUMMONED_MONSTERS, "B2F",
                new TreasureChest("M4", 25, 24),
                new TreasureChest("M5", 5, 9));
        initMap(sm, "3", LAND_OF_SUMMONED_MONSTERS, "B3F",
                new TreasureChest("M6", 21, 3),
                new TreasureChest("M7", 21, 4),
                new TreasureChest("M8", 21, 5),
                new TreasureChest("M9", 23, 11),
                new TreasureChest("M10", 21, 25),
                new TreasureChest("M11", 6, 18));
        final String sylph = "sylph";
        initMap(sylph, "s1", SYLPH_CAVE, "B1",
                new TreasureChest("S1", 5, 28),
                new TreasureChest("S2", 6, 28),
                new TreasureChest("S3", 5, 29),
                new TreasureChest("S4", 18, 3),
                new TreasureChest("S5", 18, 4),
                new TreasureChest("S6", 4, 5),
                new TreasureChest("S7", 4, 6),
                new TreasureChest("S8", 4, 7),
                new TreasureChest("S9", 5, 10),
                new TreasureChest("S10", 6, 10));
        initMap(sylph, "s2", SYLPH_CAVE, "B2",
                new TreasureChest("S11", 6, 11),
                new TreasureChest("S18", 13, 23+2),
                new TreasureChest("S19", 15, 23+2),
                new TreasureChest("S20", 13, 21+2),
                new TreasureChest("S21", 15, 21+2),
                new TreasureChest("S22", 28, 29),
                new TreasureChest("S23", 29, 29),
                new TreasureChest("S24", 28, 25),
                new TreasureChest("S25", 27, 23),
                new TreasureChest("S26", 29, 23));
        initMap(sylph, "s3", SYLPH_CAVE, "B3",
                new TreasureChest("S12", 9, 1),
                new TreasureChest("S15", 4, 10),
                new TreasureChest("S16", 5, 11),
                new TreasureChest("S17", 4, 11));
        initMap(sylph, "house", SYLPH_CAVE, "House",
                new TreasureChest("S13", 3, 13),
                new TreasureChest("S14", 5, 13));
        initMap(sylph, "treasure", SYLPH_CAVE, "Treasure Room",
                new TreasureChest("S27", 5, 7),
                new TreasureChest("S28", 5+2, 7),
                new TreasureChest("S29", 5, 7+2),
                new TreasureChest("S30", 5+2, 7+2),
                new TreasureChest("S31", 5, 7+4),
                new TreasureChest("S32", 5+2, 7+4));
        final String giant = "Giant";
        initMap(giant, "2", GIANT_OF_BABIL, "Chest",
                new TreasureChest("G1", 8, 9),
                new TreasureChest("G2", 19, 11),
                new TreasureChest("G3", 20, 21),
                new TreasureChest("G4", 6, 27),
                new TreasureChest("G5", 18, 14));
        initMap(giant, "3", GIANT_OF_BABIL, "Stomach",
                new TreasureChest("G6", 9, 19),
                new TreasureChest("G7", 6, 26));
        initMap(giant, "4", GIANT_OF_BABIL, "Passage",
                new TreasureChest("G8", 21, 14));
        final String lunar = "lunar";
        initMap(lunar, "path", LUNAR_PATH, "1F",
                new TreasureChest("P1", 22, 26),
                new TreasureChest("P2", 6, 28),
                new TreasureChest("P3", 7, 29));
        initMap(lunar, "b1", LUNAR_SUBTERRANE, "B1", new TreasureChest("L1", 24, 6));
        initMap(lunar, "b2", LUNAR_SUBTERRANE, "B2",
                new TreasureChest("L2", 12, 26),
                new TreasureChest("L3", 23, 25), 
                new TreasureChest("L4", 4, 10));
        initMap(lunar, "b3", LUNAR_SUBTERRANE, "B3",
                new TreasureChest("L5", 16, 25),
                new TreasureChest("L6", 24, 23),
                new TreasureChest("L7", 28, 14));
        initMap(lunar, "b4", LUNAR_SUBTERRANE, "B4",
                new TreasureChest("L8", 7, 28),
                new TreasureChest("L9", 13, 8),
                new TreasureChest("L12", 24, 6));
        initMap(lunar, "b4passage", LUNAR_SUBTERRANE, "B4 Passage",
                new TreasureChest("L10", 5, 23),
                new TreasureChest("L11", 14, 2));
        initMap(lunar, "b5", LUNAR_SUBTERRANE, "B5",
                new TreasureChest("L13", 9, 7),
                new TreasureChest("L14", 13, 11),
                new TreasureChest("L15", 11, 26),
                new TreasureChest("L16", 16, 27),
                new TreasureChest("L17", 20, 14),
                new TreasureChest("L18", 20, 5),
                new TreasureChest("L19", 27, 23),
                new TreasureChest("L24", 15, 1));
        initMap(lunar, "b6", LUNAR_SUBTERRANE, "B6",
                new TreasureChest("L20", 5, 15),
                new TreasureChest("L21", 5, 25),
                new TreasureChest("L22", 17, 18),
                new TreasureChest("L23", 27, 6),
                new TreasureChest("L24", 21, 22));
        initMap(lunar, "dlunar", LUNAR_SUBTERRANE, "D Lunar",
                new TreasureChest("L25", 5, 2),
                new TreasureChest("L26", 5, 4));
        initMap(lunar, "c1", LUNAR_CORE, "B1",
                new TreasureChest("C1", 11, 7));
        initMap(lunar, "c2", LUNAR_CORE, "B2",
                new TreasureChest("C2", 10, 21),
                new TreasureChest("C3", 24, 13));
        initMap(lunar, "c3", LUNAR_CORE, "B3",
                new TreasureChest("C4", 14, 24),
                new TreasureChest("C5", 15, 7));
        dungeonComboBox.setModel(new DefaultComboBoxModel<>(atlas.getAllDungeons().toArray(new String[0])));
        dungeonComboBox.addActionListener((ae) -> {
            String dungeon = (String) dungeonComboBox.getSelectedItem();
            floorComboBox.setModel(new DefaultComboBoxModel<>(atlas.getFloorsInDungeon(dungeon).toArray(new String[0])));
            showFloor();
        });
        floorComboBox.addActionListener((ae) -> {
            showFloor();
        });
        dungeonComboBox.setSelectedIndex(0);
        floorComboBox.setSelectedItem(0);
        atlas.showFloor((String) dungeonComboBox.getSelectedItem(), (String) floorComboBox.getSelectedItem());
        
        updateKeyItemCountLabel();
        updateLogic();
        
        setTitle("MaikaTracker");
        pack();
    }
    
    public List<PartyMember> getPartyMembers() {
        return Arrays.stream(partyPanel.getComponents()).map(c -> (PartyLabel) c)
                        .filter(PartyLabel::hasPartyMember)
                        .map(PartyLabel::getPartyMember).collect(Collectors.toList());
    }
    
    private void showFloor() {
        String dungeon = (String) dungeonComboBox.getSelectedItem();
        String floor = (String) floorComboBox.getSelectedItem();
        atlas.showFloor(dungeon, floor);
    }
    
    private void initMap(String directory, String filename, String dungeonName, String floorName, TreasureChest... chests) {
        String fileUrl = new StringBuilder("maps/").append(directory).append("/").append(filename).append(".png").toString();
        BufferedImage image = loadImageResource(fileUrl);
        atlas.add(new TreasureMap(dungeonName, floorName, image, chests));
        mapPane.add(atlas);
    }
    
    public void loadBossIcon(String off, String on, String bossName) {
        JPanel holder = new JPanel(new FlowLayout());
        JLabel label = new StativeLabel(new ImageIcon(MaikaTracker.loadImageResource("bosses/grayscale/" + off)), new ImageIcon(MaikaTracker.loadImageResource("bosses/color/" + on)));
        label.setToolTipText(bossName);
        holder.add(label);
        bossIconPanel.add(holder);
    }
    
    public static InputStream loadResource(String path) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        return classLoader.getResourceAsStream(path);
    }
    
    public static BufferedImage loadImageResource(String path) {
        try {
            return ImageIO.read(loadResource(path));
        }
        catch(IOException ex) {
            LOG.error("Error loading resource", ex);
        }
        return null;
    }
    
    public void updateKeyItemLocation(KeyItemMetadata keyItem, String chestId) {
        //first, find the KeyItemPanel
        getPanelForKeyItem(keyItem).setLocationInChest(chestId);
        //then, update the chest in the atlas
        atlas.setChestContents(chestId, keyItem);
    }
    
    public void updateLogic() {
        List<KeyItemLocation> locs = KeyItemLocation.getAccessibleLocations(getAcquiredKeyItems().stream()
                .map(KeyItemMetadata::getEnum).collect(Collectors.toSet()));
        locs.removeIf(locationsVisited::contains);
        logicPanel.removeAll();
        locs.forEach(l -> {
            LocationPanel panel = new LocationPanel(l);
            panel.setButtonListener((ae) -> {
                locationsVisited.add(panel.getKeyItemLocation());
                logicPanel.remove(panel);
                logicPanel.revalidate();
                logicPanel.repaint();
            });
            logicPanel.add(panel);
        });
        logicPanel.revalidate();
        logicPanel.repaint();
    }
    
    public Set<KeyItemMetadata> getAcquiredKeyItems() {
        return Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .filter(KeyItemPanel::isAcquired)
                .map(KeyItemPanel::getKeyItem)
                .collect(Collectors.toSet());
    }
    
    public void resetKeyItemLocation(KeyItemMetadata keyItem, String chestId) {
        getPanelForKeyItem(keyItem).reset();
        atlas.clearChestContents(chestId);
    }
    
    public static MaikaTracker getTrackerFromChild(Component child) {
        return (MaikaTracker) SwingUtilities.getWindowAncestor(child);
    }
    
    private KeyItemPanel getPanelForKeyItem(KeyItemMetadata keyItem) {
        return Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .filter(kip -> keyItem.equals(kip.getKeyItem()))
                .findAny()
                .get();
    }
    
    public JPopupMenu getAvailableLocationsMenu(Consumer<KeyItemLocation> actionOnEachItem) {
        JPopupMenu locationMenu = new JPopupMenu("Locations");
        Set<KeyItemLocation> knownLocations = Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .map(KeyItemPanel::getItemLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Arrays.stream(KeyItemLocation.values())
                .filter(ki -> !knownLocations.contains(ki))
                .forEach(ki -> locationMenu.add(ki.getLocation()).addActionListener((ae) -> actionOnEachItem.accept(ki)));
        return locationMenu;
    }
    
    public List<KeyItemMetadata> getUnknownKeyItems() {
        return Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .filter(kip -> !kip.isKnown())
                .map(KeyItemPanel::getKeyItem)
                .collect(Collectors.toList());
    }
    
    public JPopupMenu getUnknownKeyItemMenu(Consumer<KeyItemMetadata> actionOnEachItem) {
        JPopupMenu kiMenu = new JPopupMenu("Key Items");
        getUnknownKeyItems().forEach(ki -> kiMenu.add(ki.getEnum().toString()).addActionListener((ae) -> actionOnEachItem.accept(ki)));
        return kiMenu;
    }
    
    public void updateKeyItemCountLabel() {
        keyItemCountLabel.setText("Key Items: " + getKeyItemCount());
    }
        
    public int getKeyItemCount() {
        Set<KeyItemPanel> acquired = Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .filter(KeyItemPanel::isAcquired).collect(Collectors.toSet());
        int count = acquired.size();
        //pass doesn't count as a key item since 0.3
        return count - (acquired.stream().map(KeyItemPanel::getKeyItem).map(KeyItemMetadata::getEnum).anyMatch(ki -> ki == KeyItem.PASS) ? 1 : 0);
    }
    
    public void setMapComboBoxes(String dungeon, String floor) {
        dungeonComboBox.setSelectedItem(dungeon);
        floorComboBox.setSelectedItem(floor);
        mainTabbedPane.setSelectedComponent(mapPane);
    }
    
    public TreasureAtlas getAtlas() {
        return atlas;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainTabbedPane = new javax.swing.JTabbedPane();
        bossPane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        bossComboBox = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        positionComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        bossTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        enemyScriptTextArea = new javax.swing.JTextArea();
        mapPane = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        dungeonComboBox = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        floorComboBox = new javax.swing.JComboBox<>();
        xpPane = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        xpTable = new javax.swing.JTable();
        addDMachinButton = new javax.swing.JButton();
        logicTabPanel = new javax.swing.JPanel();
        keyItemPanel = new javax.swing.JPanel();
        partyPanel = new javax.swing.JPanel();
        keyItemCountLabel = new javax.swing.JLabel();
        bossIconPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        bossComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        bossComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bossComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(bossComboBox);

        jLabel1.setText("@");
        jPanel1.add(jLabel1);

        positionComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        positionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionComboBoxActionPerformed(evt);
            }
        });
        jPanel1.add(positionComboBox);

        bossTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Enemy", "HP", "Attack", "Min Speed", "Max Speed", "Spell Power"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        bossTable.setFocusable(false);
        bossTable.setRowSelectionAllowed(false);
        jScrollPane1.setViewportView(bossTable);

        jLabel2.setText("Script:");

        enemyScriptTextArea.setEditable(false);
        enemyScriptTextArea.setColumns(20);
        enemyScriptTextArea.setLineWrap(true);
        enemyScriptTextArea.setRows(3);
        enemyScriptTextArea.setAutoscrolls(false);
        enemyScriptTextArea.setFocusable(false);
        enemyScriptTextArea.setRequestFocusEnabled(false);
        jScrollPane3.setViewportView(enemyScriptTextArea);

        javax.swing.GroupLayout bossPaneLayout = new javax.swing.GroupLayout(bossPane);
        bossPane.setLayout(bossPaneLayout);
        bossPaneLayout.setHorizontalGroup(
            bossPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .addGroup(bossPaneLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3))
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        bossPaneLayout.setVerticalGroup(
            bossPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bossPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bossPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(285, Short.MAX_VALUE))
        );

        mainTabbedPane.addTab("Bosses", bossPane);

        mapPane.setLayout(new javax.swing.BoxLayout(mapPane, javax.swing.BoxLayout.Y_AXIS));

        dungeonComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(dungeonComboBox);

        jLabel3.setText("Floor:");
        jPanel2.add(jLabel3);

        floorComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(floorComboBox);

        mapPane.add(jPanel2);

        mainTabbedPane.addTab("Maps", mapPane);

        xpTable.setModel(new PartyTableModel());
        jScrollPane2.setViewportView(xpTable);

        addDMachinButton.setText("Add D. Machin");
        addDMachinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDMachinButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout xpPaneLayout = new javax.swing.GroupLayout(xpPane);
        xpPane.setLayout(xpPaneLayout);
        xpPaneLayout.setHorizontalGroup(
            xpPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .addGroup(xpPaneLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(addDMachinButton))
        );
        xpPaneLayout.setVerticalGroup(
            xpPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xpPaneLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addDMachinButton)
                .addGap(0, 370, Short.MAX_VALUE))
        );

        mainTabbedPane.addTab("XP", xpPane);
        mainTabbedPane.addTab("Logic", logicTabPanel);

        for(KeyItemMetadata meta : KeyItemMetadata.values()) {
            keyItemPanel.add(new sg4e.maikatracker.KeyItemPanel(meta));
        }

        keyItemCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        keyItemCountLabel.setText("jLabel4");

        javax.swing.GroupLayout bossIconPanelLayout = new javax.swing.GroupLayout(bossIconPanel);
        bossIconPanel.setLayout(bossIconPanelLayout);
        bossIconPanelLayout.setHorizontalGroup(
            bossIconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        bossIconPanelLayout.setVerticalGroup(
            bossIconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane)
            .addComponent(keyItemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(partyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(keyItemCountLabel))
                    .addComponent(bossIconPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(partyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keyItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keyItemCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bossIconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void positionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionComboBoxActionPerformed
        onBossComboBoxChanged();
    }//GEN-LAST:event_positionComboBoxActionPerformed

    private void bossComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bossComboBoxActionPerformed
        onBossComboBoxChanged();
    }//GEN-LAST:event_bossComboBoxActionPerformed

    private void addDMachinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDMachinButtonActionPerformed
        calculateXp(D_MACHIN_XP);
    }//GEN-LAST:event_addDMachinButtonActionPerformed

    private void calculateXp(int xpGained) {
        int kiMultipler = getKeyItemCount() >= 10 ? 2 : 1;
        List<PartyMember> members = getPartyMembers();
        PartyTableModel model = (PartyTableModel) xpTable.getModel();
        List<Integer> partyLevels = members.stream().map(p -> model.getStartingLevel(p)).collect(Collectors.toList());
        Collections.sort(partyLevels);
        int medianPartyLevel = partyLevels.get(partyLevels.size()/2);
        members.forEach(p -> {
            int startingLevel = model.getStartingLevel(p);
            if(p.getXp() == 0) {
                p.gainXp(model.getStartingXp(p));
            }
            //xp slingshot logic
            int slingshotMuliplier = 1;
            if(members.size() == 5) {
                if(startingLevel <= medianPartyLevel - 5)
                    slingshotMuliplier += 1;
                if(startingLevel <= medianPartyLevel - 10)
                    slingshotMuliplier += 1;
            }
            p.gainXp(xpGained * kiMultipler * slingshotMuliplier);
        });
    }
    
    private void onBossComboBoxChanged() {
        updateBossTable();
    }
    
    private void updateBossTable() {
        Object selectedBossObj = bossComboBox.getSelectedItem();
        Object selectedPositionObj = positionComboBox.getSelectedItem();
        if(selectedBossObj == null || selectedPositionObj == null)
            return;
        String boss = (String) selectedBossObj;
        String pos = (String) selectedPositionObj;
        if(boss.length() == 0 || pos.length() == 0) {
            return;
        }
        Formation formation = Formation.getFor(boss, pos);
        if(formation == null)
            return;
        List<Enemy> enemies = formation.getAllEnemies();
        clearTable(bossTable);
        StringBuilder scripts = new StringBuilder();
        for(int i = 0, size = enemies.size(); i < size; i++) {
            Enemy enemy = enemies.get(i);
            TableModel model = bossTable.getModel();
            model.setValueAt(enemy.getName(), i, 0);
            model.setValueAt(NUMBER_FORMAT.format(enemy.getHp()), i, 1);
            model.setValueAt(NUMBER_FORMAT.format(enemy.getAttack()), i, 2);
            model.setValueAt(NUMBER_FORMAT.format(enemy.getMinSpeed()), i, 3);
            model.setValueAt(NUMBER_FORMAT.format(enemy.getMaxSpeed()), i, 4);
            model.setValueAt(NUMBER_FORMAT.format(enemy.getSpellPower()), i, 5);
            enemy.getScriptValues().forEach(scr -> {
                if(scripts.length() > 0)
                    scripts.append("\n");
                scripts.append(scr);
            });
        }
        enemyScriptTextArea.setText(scripts.toString());
    }
    
    private static void clearTable(JTable table) {
        TableModel model = table.getModel();
        final int rows = model.getRowCount();
        final int columns = model.getColumnCount();
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                model.setValueAt("", i, j);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        //</editor-fold>
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MaikaTracker().setVisible(true);
            }
        });
    }
    
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDMachinButton;
    private javax.swing.JComboBox<String> bossComboBox;
    private javax.swing.JPanel bossIconPanel;
    private javax.swing.JPanel bossPane;
    private javax.swing.JTable bossTable;
    private javax.swing.JComboBox<String> dungeonComboBox;
    private javax.swing.JTextArea enemyScriptTextArea;
    private javax.swing.JComboBox<String> floorComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel keyItemCountLabel;
    private javax.swing.JPanel keyItemPanel;
    private javax.swing.JPanel logicTabPanel;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JPanel mapPane;
    private javax.swing.JPanel partyPanel;
    private javax.swing.JComboBox<String> positionComboBox;
    private javax.swing.JPanel xpPane;
    private javax.swing.JTable xpTable;
    // End of variables declaration//GEN-END:variables
}
