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
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg4e.ff4stats.fe.FlagSet;
import sg4e.ff4stats.fe.KeyItem;
import sg4e.ff4stats.fe.KeyItemLocation;
import sg4e.ff4stats.party.LevelData;
import sg4e.ff4stats.party.PartyMember;

/**
 *
 * @author sg4e
 */
public final class MaikaTracker extends javax.swing.JFrame {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private final TreasureAtlas atlas = new TreasureAtlas();    
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
    
    private final Map<String, ShopPanel> shopMap = new HashMap<>();
    
    private final JPanel logicPanel;
    private final StativeLabel dmistLabel;
    
    private static final List<BossLabel> bossLabels = new ArrayList<>();
    
    private final Preferences prefs;
    private static final String RESET_ONLY_ID = "AllowResetOnlyWhenKeyItemSet";
    
    public final Set<KeyItemLocation> locationsVisited = new HashSet<>();
    
    private int grindXP = 0;
    
    public FlagSet flagset = null;
    
    public static MaikaTracker tracker;

    /**
     * Creates new form MaikaTracker
     */
    public MaikaTracker() {
        tracker = this;
        initComponents();        
        prefs = Preferences.userRoot().node(this.getClass().getName());
        Map<Battle, Formation> bosses = Battle.getAllBosses();
        List<String> bossNames = bosses.keySet().stream().map(Battle::getBoss).distinct().collect(Collectors.toList());
        List<String> positions = bosses.keySet().stream().map(Battle::getPosition).distinct().collect(Collectors.toList());
        Collections.sort(bossNames);
        Collections.sort(positions);
        AutoCompleteSupport.install(bossComboBox, GlazedLists.eventList(bossNames));
        AutoCompleteSupport.install(positionComboBox, GlazedLists.eventList(positions));
        
        flagsTextField.setLineWrap(true);
        
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
            PartyLabel.PartyMembers.add(label);
        }
        
        //add boss icons
        LayoutManager bossIconLayout = new GridLayout(3, 12);
        bossIconPanel.setLayout(bossIconLayout);
        dmistLabel = loadBossIcon("1MistD", "D. Mist");
        dmistLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    updateLogic();
                }
            }
        });
        loadBossIcon("2Soldier", "Baron Soldiers");
        loadBossIcon("3Octo", "Octomam");
        loadBossIcon("4Antlion", "Antlion");
        loadBossIcon("5WHag", "Waterhag");
        loadBossIcon("6Mombomb", "Mombomb");
        loadBossIcon("7Gauntlet", "Fabul Guantlet");
        loadBossIcon("8Milon", "Milon");
        loadBossIcon("9MilonZ", "MilonZ");
        loadBossIcon("10DKCecil", "Dark Knight Cecil");
        loadBossIcon("11Guards", "Baron Guards");
        loadBossIcon("12Yang", "Karate");
        loadBossIcon("13Baigan", "Baigan");
        loadBossIcon("14Kainazzo", "Kainazzo");
        loadBossIcon("15DElf", "Dark Elf");
        loadBossIcon("16MagusSis", "Magus Sisters");
        loadBossIcon("17Valvalis", "Valvalis");
        loadBossIcon("18Calcabrina", "Calcabrina");
        loadBossIcon("19Golbez", "Golbez");
        loadBossIcon("20Lugae", "Dr. Lugae");
        loadBossIcon("21DarkImps", "Dark Imps");
        loadBossIcon("21Eblan", "Eblan King & Queen");
        loadBossIcon("22Rubicante", "Rubicante");
        loadBossIcon("23EvilWall", "Evil Wall");
        loadBossIcon("24Fiends", "Elements");
        loadBossIcon("25CPU", "CPU");
        loadBossIcon("26Odin", "Odin");
        loadBossIcon("28Leviath", "Leviatan");
        loadBossIcon("27Asura", "Asura");
        loadBossIcon("29Bahamut", "Bahamut");
        loadBossIcon("30PaleDim", "Pale Dim");
        loadBossIcon("31LunarD", "Lunar D.");
        loadBossIcon("32Plague", "Plague");
        loadBossIcon("33Ogopogo", "Ogopogo");
        loadBossIcon("34Wyvern", "Wyvern");
        
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
        initMap(giant, "GrindFightManip", GIANT_OF_BABIL, "D-Machine Grind");
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
                new TreasureChest("L25", 21, 22));
        initMap(lunar, "dlunar", LUNAR_SUBTERRANE, "D Lunar",
                new TreasureChest("L26", 5, 2),
                new TreasureChest("L27", 5, 4));
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
        
        initShop(agartShopLabel, false);
        initShop(baronShopLabel, true);
        initShop(eblanCaveShopLabel, false);
        initShop(fabulShopLabel, false);
        initShop(kaipoShopLabel, false);
        initShop(mysidiaShopLabel, false);
        initShop(silveraShopLabel, false);
        initShop(troiaItemShopLabel, false);
        initShop(troiaPubShopLabel, false);
        initShop(dwarfCastleShopLabel, false);
        initShop(feymarchShopLabel, false);
        initShop(tomaraShopLabel, false);
        initShop(hummingwayShopLabel, false);
        
        ShopPanel.knownLocationsPanel = initShop(knownShopLocationsLabel, false);
        
        //shopPanel2.setVisible(false);
        
        updateKeyItemCountLabel();
        resetButtonActionPerformed(null);
        updateLogic();
        mainTabbedPane.setSelectedComponent(resetPane);
        
        setTitle("MaikaTracker");
        pack();
        
        resetOnly.setSelected(prefs.getBoolean(RESET_ONLY_ID, resetOnly.isSelected()));
        setTextColor(false);
        setBackgroundColor(false);
        setTenKeyItemColor(false);
        xpErrorLabel.setText("");
    }
    
    public void setXpErrorLabel(String text) {
        xpErrorLabel.setText(text);
    }
    
    public List<PartyMember> getPartyMembers() {
        return Arrays.stream(partyPanel.getComponents()).map(c -> (PartyLabel) c)
                        .filter(PartyLabel::hasPartyMember)
                        .map(PartyLabel::getPartyMember).collect(Collectors.toList());
    }
    
    public List<PartyLabel> getPartyLabels() {
        return Arrays.stream(partyPanel.getComponents()).map(c -> (PartyLabel) c)
                        .filter(PartyLabel::hasPartyMember)
                        .collect(Collectors.toList());
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
    
    private ShopPanel initShop(JLabel shopLocation, Boolean visible)
    {
        if(shopMap.containsKey(shopLocation.getText())) {
            return shopMap.get(shopLocation.getText());
        }
        
        ShopPanel shopPanel = new ShopPanel(shopLocation);
        shopPanelsPanel.add(shopPanel);
        shopPanel.setVisible(visible);
        shopMap.put(shopLocation.getText(), shopPanel);
        
        setTextColor(false);
        setBackgroundColor(false);
        ShopPanel.UpdateFlags();

        return shopPanel;
        //shopPanes.add(shopLocation, new ShopPanel(shopLocation));
    }
    
    public void showShopPanel(JLabel label) {
        mainTabbedPane.setSelectedComponent(shopPane);
        shopMap.values().forEach(panel -> panel.setVisible(false));
        shopMap.get(label.getText()).setVisible(true);

        Rectangle bounds = activeShopPointerLabel.getBounds();
        activeShopPointerLabel.setBounds(bounds.x, label.getBounds().y, bounds.width, bounds.height);
    }
    
    public StativeLabel loadBossIcon(String imageName, String bossName) {
        BossLabel label = new BossLabel(imageName, bossName);        
        bossIconPanel.add(label.getHolder());
        bossLabels.add(label);
        return label;
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
        KeyItemPanel panel = getPanelForKeyItem(keyItem);        
        //then, update the chest in the atlas
        panel.setLocationInChest(atlas.setChestContents(chestId, keyItem));
    }
    
    public void createLocationPanel(KeyItemLocation l, Boolean createIfTrue) {
        if(!createIfTrue) return;
        
        LocationPanel panel = new LocationPanel(l);
        
        if(l.equals(KeyItemLocation.MIST)) {
            panel.setButtonEnabled(dmistLabel.isActive());
        }

        panel.setButtonListener((ae) -> {
            Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .filter(ki -> ki.getItemLocation() != null)
                .filter(ki -> ki.getItemLocation().equals(panel.getKeyItemLocation()))
                .forEach(ki -> ki.setActive(true));
            
            locationsVisited.add(panel.getKeyItemLocation());
            logicPanel.remove(panel);
            logicPanel.revalidate();
            logicPanel.repaint();
            handleLogic(panel.getKeyItemLocation(), true);
        });
        logicPanel.add(panel);
    }
    
    public void handleLogic(KeyItemLocation loc, boolean completed) {
        if(loc == null)
            return;
        if (loc.equals(KeyItemLocation.ORDEALS)) {
            PartyLabel.MtOrdealsComplete = completed;
            getPartyLabels().forEach(member -> member.setPartyMember(member.getData()));
        }
        if (loc.equals(KeyItemLocation.DWARF_CASTLE)) {
            PartyLabel.DwarfCastleComplete = completed;
            getPartyLabels().forEach(member -> member.setPartyMember(member.getData()));
        }
    }
    
    public void updateLogic() {
        
        List<KeyItemLocation> locs = KeyItemLocation.getAccessibleLocations(getAcquiredKeyItems().stream()
                .map(KeyItemMetadata::getEnum).collect(Collectors.toSet()));
        locs.removeIf(locationsVisited::contains);
        logicPanel.removeAll();
        locs.forEach(l -> {
            switch (l) {
                case ASURA:
                case LEVIATAN:
                case ODIN:
                case SYLPH:
                case BAHAMUT:
                    createLocationPanel(l, flagsetContains("Kq"));
                    break;
                case PALE_DIM:
                case PLAGUE:
                case DLUNAR:
                case OGOPOGO:
                case WYVERN:
                    createLocationPanel(l, flagsetContains("Km"));
                    break;
                case TOROIA:
                    createLocationPanel(l, flagset == null || !flagsetContains("Nk"));
                    break;
                case MIST:
                    createLocationPanel(l, flagsetContains("Nk"));
                    break;
                case KOKKOL:
                    createLocationPanel(l, flagsetContains("V1"));
                    break;
                case ZEROMUS:
                    break;
                    
                case FABUL:
                    createLocationPanel(l, flagsetContains("K"));
                    break;
                    
                case BARON_CASTLE:
                    createLocationPanel(l, flagsetContainsAny("K", "Pk"));
                    break;
                    
                default:
                    createLocationPanel(l, true);
                    break;
            }
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
    
    public KeyItemPanel getPanelForKeyItem(KeyItemMetadata keyItem) {
        return keyItem == null ? null : Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .filter(kip -> keyItem.equals(kip.getKeyItem()))
                .findAny()
                .get();
    }
    
    public void getAvailableLocationsMenu(Consumer<KeyItemLocation> actionOnEachItem, JPopupMenu locationMenu) {
        JMenu keyItemsMenu = new JMenu("Vanilla location");
        JMenu summonsMenu = new JMenu("Summon location");
        JMenu lunarMenu = new JMenu("Lunar location");
        
        List<KeyItemLocation> dLunar = Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .map(KeyItemPanel::getItemLocation)
                .filter(Objects::nonNull)
                .filter(ki -> ki.equals(KeyItemLocation.DLUNAR))
                .collect(Collectors.toList());               
        
        
        Set<KeyItemLocation> knownLocations = Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .map(KeyItemPanel::getItemLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if(!flagsetContains("Nk"))
            knownLocations.add(KeyItemLocation.MIST);
        
        if (flagsetContains(false, "Nk"))
            knownLocations.add(KeyItemLocation.TOROIA);
        
        if(!flagsetContains("Kq")) {
            knownLocations.add(KeyItemLocation.ASURA);
            knownLocations.add(KeyItemLocation.LEVIATAN);
            knownLocations.add(KeyItemLocation.SYLPH);
            knownLocations.add(KeyItemLocation.ODIN);
            knownLocations.add(KeyItemLocation.BAHAMUT);
        }
        
        if (!flagsetContains("Km")) {
            knownLocations.add(KeyItemLocation.PALE_DIM);
            knownLocations.add(KeyItemLocation.PLAGUE);
            knownLocations.add(KeyItemLocation.DLUNAR);
            knownLocations.add(KeyItemLocation.OGOPOGO);
            knownLocations.add(KeyItemLocation.WYVERN);
        }
        else if(dLunar.size() < 2)
            knownLocations.remove(KeyItemLocation.DLUNAR);
        
        if (!flagsetContains("K")) {
            knownLocations.add(KeyItemLocation.FABUL);
            if(!flagsetContains("Pk"))
                knownLocations.add(KeyItemLocation.BARON_CASTLE);
        }
        
        if (!flagsetContains("V1"))
            knownLocations.add(KeyItemLocation.KOKKOL);
        
        //Always.  Crystal location set there automatically on K0 seeds.
        if(flagset != null)
            knownLocations.add(KeyItemLocation.ZEROMUS);
        
        Arrays.stream(KeyItemLocation.values())
                .filter(ki -> !knownLocations.contains(ki))
                .forEach(ki -> {
                    //locationMenu.add(ki.getLocation()).addActionListener((ae) -> actionOnEachItem.accept(ki))
                    switch(ki) {
                        case ASURA:
                        case LEVIATAN:
                        case SYLPH:
                        case ODIN:
                        case BAHAMUT:
                            summonsMenu.add(ki.getLocation()).addActionListener((ae) -> actionOnEachItem.accept(ki));
                            break;
                        case PALE_DIM:
                        case PLAGUE:
                        case DLUNAR:
                        case OGOPOGO:
                        case WYVERN:
                            lunarMenu.add(ki.getLocation()).addActionListener((ae) -> actionOnEachItem.accept(ki));
                            break;
                        default:
                            keyItemsMenu.add(ki.getLocation()).addActionListener((ae) -> actionOnEachItem.accept(ki));
                            break;
                    }
                });
        if(keyItemsMenu.getItemCount() > 0)
            locationMenu.add(keyItemsMenu);
        if(summonsMenu.getItemCount() > 0)
            locationMenu.add(summonsMenu);
        if(lunarMenu.getItemCount() > 0)
            locationMenu.add(lunarMenu);
    }
    
    public List<KeyItemPanel> getKeyItemPanels() {
        return Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .collect(Collectors.toList());
    }
    
    public List<KeyItemMetadata> getUnknownKeyItems() {
        return Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c)
                .filter(kip -> !kip.isKnown())
                .map(KeyItemPanel::getKeyItem)
                .collect(Collectors.toList());
    }
    
    public Boolean isResetOnly() {
        return resetOnly.isSelected();
    }
    
    public boolean isItemAllowedInChest(KeyItemMetadata ki) {
        switch(ki) {
            case PASS:
                return flagsetContains("Pt") || flagsetContainsAll("Pk", "Kt");
            case CRYSTAL:
                return flagsetContains("Kt") && !flagsetContains(false, "V1");
            default:
                return flagsetContains("Kt");
        }
    }
    
    public JPopupMenu getUnknownKeyItemMenu(Consumer<KeyItemMetadata> actionOnEachItem) {
        JPopupMenu kiMenu = new JPopupMenu("Key Items");
        getUnknownKeyItems().stream().filter(ki -> isItemAllowedInChest(ki)).forEachOrdered(ki -> 
            kiMenu.add(ki.getEnum().toString()).addActionListener((ae) -> actionOnEachItem.accept(ki))
        );
        return kiMenu;
    }
    
    public void updateKeyItemCountLabel() {
        keyItemCountLabel.setText("Key Items: " + getKeyItemCount());
        Boolean keyItemBonusXP = flagsetContains("Xk");
        keyItemCountLabel.setForeground(getKeyItemCount() >= 10 && keyItemBonusXP ? tenKeyItemColorLabel.getForeground() : textColorLabel.getForeground());
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
    
    public Color selectColor(Boolean showPicker, String prefsNode, String colorPickerTitle, Component colorPanel, Boolean background) {
        Color color = getColor(prefsNode, background ? colorPanel.getBackground() : colorPanel.getForeground());
        
        if(showPicker) {
            color = JColorChooser.showDialog(this, colorPickerTitle, color);
            if(color != null)
                putColor(prefsNode, color);
        }
        return color;
    }
    
    public Color getColor(String prefsNode, Color defaultColor)
    {
        int red = prefs.node(prefsNode).getInt("Red", defaultColor.getRed());
        int green = prefs.node(prefsNode).getInt("Green", defaultColor.getGreen());
        int blue = prefs.node(prefsNode).getInt("Blue", defaultColor.getBlue());
        return new Color(red, green, blue);
    }
    
    public void putColor(String prefsNode, Color color) {
        prefs.node(prefsNode).putInt("Red", color.getRed());
        prefs.node(prefsNode).putInt("Green", color.getGreen());
        prefs.node(prefsNode).putInt("Blue", color.getBlue());
    }
    
    public void setTextColor(Boolean showPicker) {
        Color textColor = selectColor(showPicker, "TextColor", "Choose Text Color", textColorLabel, false);
        if(textColor == null)
            return;

        textColorLabel.setForeground(textColor);
        updateKeyItemCountLabel();
        floorLabel.setForeground(textColor);
        bossAtLabel.setForeground(textColor);
        scriptLabel.setForeground(textColor);
        LocationPanel.textColor = textColor;
        getKeyItemPanels().forEach((panel) -> {
            panel.setTextColor(textColor);
        });
        ShopPanel.setTextColor(textColor);
        
        agartShopLabel.setForeground(textColor);
        baronShopLabel.setForeground(textColor);
        eblanCaveShopLabel.setForeground(textColor);
        fabulShopLabel.setForeground(textColor);
        kaipoShopLabel.setForeground(textColor);
        mysidiaShopLabel.setForeground(textColor);
        silveraShopLabel.setForeground(textColor);
        troiaItemShopLabel.setForeground(textColor);
        troiaPubShopLabel.setForeground(textColor);
        dwarfCastleShopLabel.setForeground(textColor);
        feymarchShopLabel.setForeground(textColor);
        tomaraShopLabel.setForeground(textColor);
        hummingwayShopLabel.setForeground(textColor);
        activeShopPointerLabel.setForeground(textColor);
        knownShopLocationsLabel.setForeground(textColor);
        xpErrorLabel.setForeground(textColor);
        xpLabel.setForeground(textColor);
        
        updateLogic();
    }
    
    public void setBackgroundColor(Boolean showPicker) {
        Color backgroundColor = selectColor(showPicker, "BackgroundColor", "Choose Background Color", backgroundColorPanel, true);
        if(backgroundColor == null)
            return;
        backgroundColorPanel.setBackground(backgroundColor);
        shopLocationsPanel.setBackground(backgroundColor);
        shopPanelsPanel.setBackground(backgroundColor);
        getContentPane().setBackground(backgroundColor);
        logicPanel.setBackground(backgroundColor);
        bossPane.setBackground(backgroundColor);
        mapPane.setBackground(backgroundColor);
        xpPane.setBackground(backgroundColor);
        logicTabPanel.setBackground(backgroundColor);
        shopPane.setBackground(backgroundColor);
        resetPane.setBackground(backgroundColor);
        bossIconPanel.setBackground(backgroundColor);
        partyPanel.setBackground(backgroundColor);
        keyItemPanel.setBackground(backgroundColor);
        mapSelectionPanel.setBackground(backgroundColor);
        bossSelectionPanel.setBackground(backgroundColor);
        LocationPanel.backgroundColor = backgroundColor;
        bossLabels.forEach(boss -> boss.setBackground(backgroundColor));
        getKeyItemPanels().forEach((panel) -> {
            panel.setBackgroundColor(backgroundColor);
        });
        ShopPanel.setBackgroundColor(backgroundColor);
        updateLogic();
    }
    
    public void setTenKeyItemColor(Boolean showPicker) {
        Color color = selectColor(showPicker, "TenKeyItemColor", "Choose Color for Ten+ Key items", tenKeyItemColorLabel, false);
        if(color == null)
            return;
        tenKeyItemColorLabel.setForeground(color);
        updateKeyItemCountLabel();        
    }
    
    public void SetStartingMember() {
        if(flagset == null) return;
        PartyLabel label = PartyLabel.PartyMembers.get(0);
        if(flagset.contains("-startcecil"))
            label.setPartyMember(LevelData.DARK_KNIGHT_CECIL);
        else if (flagset.contains("-startkain"))
            label.setPartyMember(LevelData.KAIN);
        else if (flagset.contains("-startrydia"))
            label.setPartyMember(LevelData.RYDIA);
        else if (flagset.contains("-startedward"))
            label.setPartyMember(LevelData.EDWARD);
        else if (flagset.contains("-startrosa"))
            label.setPartyMember(LevelData.ROSA);
        else if (flagset.contains("-starttellah"))
            label.setPartyMember(LevelData.TELLAH);
        else if (flagset.contains("-startyang"))
            label.setPartyMember(LevelData.YANG);
        else if (flagset.contains("-startpalom"))
            label.setPartyMember(LevelData.PALOM);
        else if (flagset.contains("-startporom"))
            label.setPartyMember(LevelData.POROM);
        else if (flagset.contains("-startedge"))
            label.setPartyMember(LevelData.EDGE);
        else if (flagset.contains("-startcid"))
            label.setPartyMember(LevelData.CID);
        else if (flagset.contains("-startfusoya"))
            label.setPartyMember(LevelData.FUSOYA);
    }
    
    public boolean flagsetContains(boolean allowNullFlagset, String firstFlag) {
        return (allowNullFlagset || flagset != null) && flagsetContains(firstFlag);
    }
    
    public boolean flagsetContains(String firstFlag) {
        return flagsetContainsAll(firstFlag);
    }
    
    public boolean flagsetContainsAll(boolean allowNullFlagset, String firstFlag, String ...flags) {
        return (allowNullFlagset || flagset != null) && flagsetContainsAll(firstFlag, flags);
    }
    
    public boolean flagsetContainsAll(String firstFlag, String ...flags) {
        if(flagset == null)
            return true;
        List<String> flagsList = new ArrayList<>();
        flagsList.add(firstFlag);
        flagsList.addAll(Arrays.asList(flags));
        return flagsList.stream().allMatch(x -> flagset.contains(x));
    }
    
    public boolean flagsetContainsAny(boolean allowNullFlagset, String firstFlag, String ...flags) {
        return (allowNullFlagset || flagset != null) && flagsetContainsAny(firstFlag, flags);
    }
    
    public boolean flagsetContainsAny(String firstFlag, String ...flags) {
        if(flagset == null)
            return true;
        List<String> flagsList = new ArrayList<>();
        flagsList.add(firstFlag);
        flagsList.addAll(Arrays.asList(flags));
        return flagsList.stream().anyMatch(x -> flagset.contains(x));
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
        bossSelectionPanel = new javax.swing.JPanel();
        bossComboBox = new javax.swing.JComboBox<>();
        bossAtLabel = new javax.swing.JLabel();
        positionComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        bossTable = new javax.swing.JTable();
        scriptLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        enemyScriptTextArea = new javax.swing.JTextArea();
        mapPane = new javax.swing.JPanel();
        mapSelectionPanel = new javax.swing.JPanel();
        dungeonComboBox = new javax.swing.JComboBox<>();
        floorLabel = new javax.swing.JLabel();
        floorComboBox = new javax.swing.JComboBox<>();
        xpPane = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        xpTable = new javax.swing.JTable();
        addDMachinButton = new javax.swing.JButton();
        finishGrindButton = new javax.swing.JButton();
        xpLabel = new javax.swing.JLabel();
        xpErrorLabel = new javax.swing.JLabel();
        logicTabPanel = new javax.swing.JPanel();
        shopPane = new javax.swing.JPanel();
        shopPanelsPanel = new javax.swing.JPanel();
        shopLocationsPanel = new javax.swing.JPanel();
        agartShopLabel = new javax.swing.JLabel();
        baronShopLabel = new javax.swing.JLabel();
        eblanCaveShopLabel = new javax.swing.JLabel();
        fabulShopLabel = new javax.swing.JLabel();
        kaipoShopLabel = new javax.swing.JLabel();
        mysidiaShopLabel = new javax.swing.JLabel();
        silveraShopLabel = new javax.swing.JLabel();
        troiaItemShopLabel = new javax.swing.JLabel();
        troiaPubShopLabel = new javax.swing.JLabel();
        dwarfCastleShopLabel = new javax.swing.JLabel();
        feymarchShopLabel = new javax.swing.JLabel();
        tomaraShopLabel = new javax.swing.JLabel();
        hummingwayShopLabel = new javax.swing.JLabel();
        knownShopLocationsLabel = new javax.swing.JLabel();
        activeShopPointerLabel = new javax.swing.JLabel();
        resetPane = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        resetLabel = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        flagsTextField = new javax.swing.JTextArea();
        applyFlagsButton = new javax.swing.JButton();
        flagErrorLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        resetOnly = new javax.swing.JCheckBox();
        backgroundColorPanel = new javax.swing.JPanel();
        tenKeyItemColorLabel = new javax.swing.JLabel();
        textColorLabel = new javax.swing.JLabel();
        backgroundColorButton = new javax.swing.JButton();
        textColorButton = new javax.swing.JButton();
        tenKeyItemColorButton = new javax.swing.JButton();
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
        bossSelectionPanel.add(bossComboBox);

        bossAtLabel.setText("@");
        bossSelectionPanel.add(bossAtLabel);

        positionComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        positionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionComboBoxActionPerformed(evt);
            }
        });
        bossSelectionPanel.add(positionComboBox);

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

        scriptLabel.setText("Script:");

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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
            .addGroup(bossPaneLayout.createSequentialGroup()
                .addComponent(scriptLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3))
            .addComponent(bossSelectionPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        bossPaneLayout.setVerticalGroup(
            bossPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bossPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bossSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bossPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scriptLabel)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(235, Short.MAX_VALUE))
        );

        mainTabbedPane.addTab("Bosses", bossPane);

        mapPane.setLayout(new javax.swing.BoxLayout(mapPane, javax.swing.BoxLayout.Y_AXIS));

        dungeonComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        mapSelectionPanel.add(dungeonComboBox);

        floorLabel.setText("Floor:");
        mapSelectionPanel.add(floorLabel);

        floorComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        mapSelectionPanel.add(floorComboBox);

        mapPane.add(mapSelectionPanel);

        mainTabbedPane.addTab("Maps", mapPane);

        xpTable.setModel(new PartyTableModel());
        jScrollPane2.setViewportView(xpTable);

        addDMachinButton.setText("Add D. Machin");
        addDMachinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDMachinButtonActionPerformed(evt);
            }
        });

        finishGrindButton.setText("Finish Grind");
        finishGrindButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishGrindButtonActionPerformed(evt);
            }
        });

        xpLabel.setText("XP: 0");

        xpErrorLabel.setText("jLabel1");

        javax.swing.GroupLayout xpPaneLayout = new javax.swing.GroupLayout(xpPane);
        xpPane.setLayout(xpPaneLayout);
        xpPaneLayout.setHorizontalGroup(
            xpPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, xpPaneLayout.createSequentialGroup()
                .addComponent(addDMachinButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(finishGrindButton)
                .addContainerGap())
            .addGroup(xpPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(xpPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xpLabel)
                    .addComponent(xpErrorLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        xpPaneLayout.setVerticalGroup(
            xpPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(xpPaneLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(xpPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(finishGrindButton)
                    .addComponent(addDMachinButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xpLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xpErrorLabel)
                .addGap(0, 335, Short.MAX_VALUE))
        );

        mainTabbedPane.addTab("XP", xpPane);
        mainTabbedPane.addTab("Logic", logicTabPanel);

        shopPanelsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        shopLocationsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        shopLocationsPanel.setLayout(null);

        agartShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        agartShopLabel.setText("Agart");
        agartShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(agartShopLabel);
        agartShopLabel.setBounds(50, 10, 49, 22);

        baronShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        baronShopLabel.setText("Baron");
        baronShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(baronShopLabel);
        baronShopLabel.setBounds(50, 30, 54, 22);

        eblanCaveShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        eblanCaveShopLabel.setText("Eblan Cave");
        eblanCaveShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(eblanCaveShopLabel);
        eblanCaveShopLabel.setBounds(50, 50, 99, 22);

        fabulShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        fabulShopLabel.setText("Fabul");
        fabulShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(fabulShopLabel);
        fabulShopLabel.setBounds(50, 70, 49, 22);

        kaipoShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        kaipoShopLabel.setText("Kaipo");
        kaipoShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(kaipoShopLabel);
        kaipoShopLabel.setBounds(50, 90, 51, 22);

        mysidiaShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        mysidiaShopLabel.setText("Mysidia");
        mysidiaShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(mysidiaShopLabel);
        mysidiaShopLabel.setBounds(50, 110, 67, 22);

        silveraShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        silveraShopLabel.setText("Silvera");
        silveraShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(silveraShopLabel);
        silveraShopLabel.setBounds(50, 130, 61, 22);

        troiaItemShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        troiaItemShopLabel.setText("Troia [Item]");
        troiaItemShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(troiaItemShopLabel);
        troiaItemShopLabel.setBounds(50, 150, 111, 22);

        troiaPubShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        troiaPubShopLabel.setText("Troia [Pub]");
        troiaPubShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(troiaPubShopLabel);
        troiaPubShopLabel.setBounds(50, 170, 102, 22);

        dwarfCastleShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        dwarfCastleShopLabel.setText("Dwarf Castle");
        dwarfCastleShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(dwarfCastleShopLabel);
        dwarfCastleShopLabel.setBounds(50, 210, 116, 22);

        feymarchShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        feymarchShopLabel.setText("Feymarch");
        feymarchShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(feymarchShopLabel);
        feymarchShopLabel.setBounds(50, 230, 89, 22);

        tomaraShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        tomaraShopLabel.setText("Tomara");
        tomaraShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(tomaraShopLabel);
        tomaraShopLabel.setBounds(50, 250, 69, 22);

        hummingwayShopLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        hummingwayShopLabel.setText("Hummingway");
        hummingwayShopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(hummingwayShopLabel);
        hummingwayShopLabel.setBounds(50, 290, 125, 22);

        knownShopLocationsLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        knownShopLocationsLabel.setText("Known Locations");
        knownShopLocationsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                shopLabelClicked(evt);
            }
        });
        shopLocationsPanel.add(knownShopLocationsLabel);
        knownShopLocationsLabel.setBounds(50, 330, 155, 22);

        activeShopPointerLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        activeShopPointerLabel.setText("-->");
        shopLocationsPanel.add(activeShopPointerLabel);
        activeShopPointerLabel.setBounds(10, 30, 31, 22);

        javax.swing.GroupLayout shopPaneLayout = new javax.swing.GroupLayout(shopPane);
        shopPane.setLayout(shopPaneLayout);
        shopPaneLayout.setHorizontalGroup(
            shopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shopPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(shopLocationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(shopPanelsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                .addContainerGap())
        );
        shopPaneLayout.setVerticalGroup(
            shopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shopPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(shopPanelsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(shopLocationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(161, Short.MAX_VALUE))
        );

        mainTabbedPane.addTab("Shop", shopPane);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Reset Everything"));

        resetLabel.setText("<html>Are you sure you would like to reset everything?<br> <br> This Action cannot be undone");

        resetButton.setText("Reset & Apply Flags");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetButton)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(resetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetButton))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Flags"));

        flagsTextField.setColumns(20);
        flagsTextField.setRows(5);
        flagsTextField.setWrapStyleWord(true);
        jScrollPane5.setViewportView(flagsTextField);

        applyFlagsButton.setText("Apply Flags");
        applyFlagsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyFlagsButtonActionPerformed(evt);
            }
        });

        flagErrorLabel.setForeground(new java.awt.Color(255, 0, 0));
        flagErrorLabel.setText("jLabel5");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(flagErrorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(applyFlagsButton))
            .addComponent(jScrollPane5)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(flagErrorLabel)
                    .addComponent(applyFlagsButton)))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Other Options"));

        resetOnly.setText("Show only Reset when key item set");
        resetOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetOnlyActionPerformed(evt);
            }
        });

        backgroundColorPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tenKeyItemColorLabel.setForeground(new java.awt.Color(0, 153, 51));
        tenKeyItemColorLabel.setText("10+ Key Items Color");

        textColorLabel.setText("Text Color");

        javax.swing.GroupLayout backgroundColorPanelLayout = new javax.swing.GroupLayout(backgroundColorPanel);
        backgroundColorPanel.setLayout(backgroundColorPanelLayout);
        backgroundColorPanelLayout.setHorizontalGroup(
            backgroundColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundColorPanelLayout.createSequentialGroup()
                .addGap(136, 136, 136)
                .addComponent(textColorLabel)
                .addGap(39, 39, 39)
                .addComponent(tenKeyItemColorLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        backgroundColorPanelLayout.setVerticalGroup(
            backgroundColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundColorPanelLayout.createSequentialGroup()
                .addGroup(backgroundColorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tenKeyItemColorLabel)
                    .addComponent(textColorLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        backgroundColorButton.setText("Background Color");
        backgroundColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundColorButtonActionPerformed(evt);
            }
        });

        textColorButton.setText("Text Color");
        textColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textColorButtonActionPerformed(evt);
            }
        });

        tenKeyItemColorButton.setText("10+ Key Items Color");
        tenKeyItemColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tenKeyItemColorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(resetOnly)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(backgroundColorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textColorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tenKeyItemColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(backgroundColorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(resetOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tenKeyItemColorButton)
                    .addComponent(backgroundColorButton)
                    .addComponent(textColorButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backgroundColorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {backgroundColorButton, tenKeyItemColorButton, textColorButton});

        javax.swing.GroupLayout resetPaneLayout = new javax.swing.GroupLayout(resetPane);
        resetPane.setLayout(resetPaneLayout);
        resetPaneLayout.setHorizontalGroup(
            resetPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resetPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resetPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(resetPaneLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        resetPaneLayout.setVerticalGroup(
            resetPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resetPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resetPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(264, 264, 264))
        );

        mainTabbedPane.addTab("Misc.", resetPane);

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
                .addComponent(mainTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 562, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        grindXP += D_MACHIN_XP;
        calculateXp(grindXP, false);
        xpLabel.setText("XP: " + grindXP);
    }//GEN-LAST:event_addDMachinButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        applyFlagsButtonActionPerformed(evt);
        bossLabels.forEach(boss -> boss.reset());
        atlas.reset();
        getPartyLabels().forEach((member -> member.clearLabel()));
        Arrays.stream(keyItemPanel.getComponents())
                .map(c -> (KeyItemPanel) c).forEach(panel -> panel.reset(true));
        locationsVisited.clear();
        updateLogic();
        updateKeyItemCountLabel();
        PartyLabel.MtOrdealsComplete = false;
        PartyLabel.DwarfCastleComplete = false;
        ShopPanel.reset();
        SetStartingMember();        
    }//GEN-LAST:event_resetButtonActionPerformed

    private void applyFlagsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyFlagsButtonActionPerformed
        flagErrorLabel.setText("");
        String text = flagsTextField.getText().trim();
        try {
            if(text.startsWith("ff4fe.com") || text.startsWith("http://") || text.startsWith("https://")) { //maybe SSL support one day
                flagset = FlagSet.fromUrl(text);
            }
            else if(text.startsWith("b"))
                flagset = FlagSet.fromBinary(text);
            else
                flagset = FlagSet.fromString(text);
            flagsTextField.setText(flagset.toString());
        }
        catch (IllegalArgumentException ex) {
            flagErrorLabel.setText(ex.getMessage());
            flagset = null;
        }        
        ShopPanel.UpdateFlags();
        PartyLabel.flagset = flagset;
        updateLogic();
        updateKeyItemCountLabel();
    }//GEN-LAST:event_applyFlagsButtonActionPerformed

    private void resetOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetOnlyActionPerformed
        prefs.putBoolean(RESET_ONLY_ID, resetOnly.isSelected());
    }//GEN-LAST:event_resetOnlyActionPerformed

    private void textColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textColorButtonActionPerformed
        setTextColor(true);
    }//GEN-LAST:event_textColorButtonActionPerformed

    private void backgroundColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundColorButtonActionPerformed
        setBackgroundColor(true);
    }//GEN-LAST:event_backgroundColorButtonActionPerformed

    private void tenKeyItemColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tenKeyItemColorButtonActionPerformed
        setTenKeyItemColor(true);
    }//GEN-LAST:event_tenKeyItemColorButtonActionPerformed

    private void shopLabelClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_shopLabelClicked
        if(evt.getSource() instanceof JLabel) {
            JLabel label = (JLabel)evt.getSource();
            showShopPanel(label);
        }
    }//GEN-LAST:event_shopLabelClicked

    private void finishGrindButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishGrindButtonActionPerformed
        calculateXp(grindXP, true);
        grindXP = 0;
        xpLabel.setText("XP: 0");
    }//GEN-LAST:event_finishGrindButtonActionPerformed

    private void calculateXp(int xpGained, boolean commit) {
        List<PartyMember> members = getPartyMembers();
        int kiMultipler = getKeyItemCount() >= 10 && (flagsetContains("Xk")) ? 2 : 1;
        boolean slingshot = (flagsetContains("Xb"));
        int sharedXP = (flagsetContains("Xs")) ? xpGained : xpGained / members.size();        
        PartyTableModel model = (PartyTableModel) xpTable.getModel();
        List<Integer> partyLevels = members.stream().map(p -> model.getStartingLevel(p)).collect(Collectors.toList());
        Collections.sort(partyLevels);
        int medianPartyLevel = partyLevels.get(partyLevels.size()/2);
        members.forEach(p -> {
            p.resetXp();
            int startingLevel = model.getStartingLevel(p);
            if(p.getXp() == 0) {
                p.gainXp(model.getStartingXp(p));
            }
            //xp slingshot logic
            int slingshotMuliplier = 1;
            if((members.size() == 5) && slingshot) {
                if(startingLevel <= medianPartyLevel - 5)
                    slingshotMuliplier += 1;
                if(startingLevel <= medianPartyLevel - 10)
                    slingshotMuliplier += 1;
            }
            
            p.gainXp(sharedXP * kiMultipler * slingshotMuliplier);
            
            if(commit) {
                p.setStartingLevel(p.getLevel());
                p.setStartingXp(p.getXp());
            }
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
                        
            StringBuilder enemyScript = new StringBuilder();
            enemy.getScriptValues().forEach(scr -> {
                if(scr.length() > 0) {
                    if(enemyScript.length() > 0)
                        enemyScript.append("\n");
                    enemyScript.append(scr);
                }
            });
            
            if(scripts.length() > 0 && enemyScript.length() > 0)
                scripts.append("\n-----------\n");
            scripts.append(enemyScript);
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
    private javax.swing.JLabel activeShopPointerLabel;
    private javax.swing.JButton addDMachinButton;
    private javax.swing.JLabel agartShopLabel;
    private javax.swing.JButton applyFlagsButton;
    private javax.swing.JButton backgroundColorButton;
    private javax.swing.JPanel backgroundColorPanel;
    private javax.swing.JLabel baronShopLabel;
    private javax.swing.JLabel bossAtLabel;
    private javax.swing.JComboBox<String> bossComboBox;
    private javax.swing.JPanel bossIconPanel;
    private javax.swing.JPanel bossPane;
    private javax.swing.JPanel bossSelectionPanel;
    private javax.swing.JTable bossTable;
    private javax.swing.JComboBox<String> dungeonComboBox;
    private javax.swing.JLabel dwarfCastleShopLabel;
    private javax.swing.JLabel eblanCaveShopLabel;
    private javax.swing.JTextArea enemyScriptTextArea;
    private javax.swing.JLabel fabulShopLabel;
    private javax.swing.JLabel feymarchShopLabel;
    private javax.swing.JButton finishGrindButton;
    private javax.swing.JLabel flagErrorLabel;
    private javax.swing.JTextArea flagsTextField;
    private javax.swing.JComboBox<String> floorComboBox;
    private javax.swing.JLabel floorLabel;
    private javax.swing.JLabel hummingwayShopLabel;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel kaipoShopLabel;
    private javax.swing.JLabel keyItemCountLabel;
    private javax.swing.JPanel keyItemPanel;
    private javax.swing.JLabel knownShopLocationsLabel;
    private javax.swing.JPanel logicTabPanel;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JPanel mapPane;
    private javax.swing.JPanel mapSelectionPanel;
    private javax.swing.JLabel mysidiaShopLabel;
    private javax.swing.JPanel partyPanel;
    private javax.swing.JComboBox<String> positionComboBox;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel resetLabel;
    private javax.swing.JCheckBox resetOnly;
    private javax.swing.JPanel resetPane;
    private javax.swing.JLabel scriptLabel;
    private javax.swing.JPanel shopLocationsPanel;
    private javax.swing.JPanel shopPane;
    private javax.swing.JPanel shopPanelsPanel;
    private javax.swing.JLabel silveraShopLabel;
    private javax.swing.JButton tenKeyItemColorButton;
    private javax.swing.JLabel tenKeyItemColorLabel;
    private javax.swing.JButton textColorButton;
    private javax.swing.JLabel textColorLabel;
    private javax.swing.JLabel tomaraShopLabel;
    private javax.swing.JLabel troiaItemShopLabel;
    private javax.swing.JLabel troiaPubShopLabel;
    private javax.swing.JLabel xpErrorLabel;
    private javax.swing.JLabel xpLabel;
    private javax.swing.JPanel xpPane;
    private javax.swing.JTable xpTable;
    // End of variables declaration//GEN-END:variables
}
