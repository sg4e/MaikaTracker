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
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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
    private static final String LUNAR_SUBTERRANE = "Lunar Subterrane";

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
        
        //add maps
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            BufferedImage b2Image = ImageIO.read(classLoader.getResourceAsStream("maps/lunar/b2.png"));
            atlas.add(LUNAR_SUBTERRANE, "B2", new TreasureMap(b2Image, 
                    new TreasureChest("L2", 12, 26),
                    new TreasureChest("L3", 23, 25), 
                    new TreasureChest("L4", 4, 10)));
        }
        catch(IOException ex) {
            LOG.error("Error loading maps", ex);
        }
        mapPane.add(atlas);
        
        setTitle("MaikaTracker");
        pack();
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
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        keyItemPanel = new javax.swing.JPanel();

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

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(jComboBox1);

        jLabel3.setText("Floor:");
        jPanel2.add(jLabel3);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(jComboBox2);

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
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
    private javax.swing.JTextArea enemyScriptTextArea;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
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
    private javax.swing.JComboBox<String> positionComboBox;
    // End of variables declaration//GEN-END:variables
}
