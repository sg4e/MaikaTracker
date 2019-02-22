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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg4e.ff4stats.fe.KeyItemLocation;

/**
 *
 * @author sg4e
 */
public class MaikaTracker extends javax.swing.JFrame {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private final TreasureAtlas atlas = new TreasureAtlas();
    private static final String EBLAN_CASTLE = "Eblan Castle";
    private static final String LUNAR_SUBTERRANE = "Lunar Subterrane";
    private static final String LUNAR_CORE = "Lunar Core";
    private static final String SYLPH_CAVE = "Sylph Cave";

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
        
        //add party characters
        for(int i = 0; i < 5; i++) {
            PartyLabel label = new PartyLabel();
            if(i != 0)
                label.setBorder(new EmptyBorder(0, 30, 0, 0));
            partyPanel.add(label);
        }
        
        //add maps
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
        final String lunar = "lunar";
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
        initMap(lunar, "paledim", LUNAR_SUBTERRANE, "Pale Dim",
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
        
        setTitle("MaikaTracker");
        pack();
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
        keyItemPanel = new javax.swing.JPanel();
        partyPanel = new javax.swing.JPanel();

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        for(KeyItemMetadata meta : KeyItemMetadata.values()) {
            keyItemPanel.add(new sg4e.maikatracker.KeyItemPanel(meta));
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane)
            .addComponent(keyItemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(partyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(partyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keyItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void positionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionComboBoxActionPerformed
        onBossComboBoxChanged();
    }//GEN-LAST:event_positionComboBoxActionPerformed

    private void bossComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bossComboBoxActionPerformed
        onBossComboBoxChanged();
    }//GEN-LAST:event_bossComboBoxActionPerformed

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
    private javax.swing.JComboBox<String> bossComboBox;
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
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel keyItemPanel;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JPanel mapPane;
    private javax.swing.JPanel partyPanel;
    private javax.swing.JComboBox<String> positionComboBox;
    // End of variables declaration//GEN-END:variables
}
