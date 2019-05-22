/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg4e.maikatracker;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import sg4e.ff4stats.fe.FlagSet;

/**
 *
 * @author CaitSith2
 */
public class ShopPanel extends javax.swing.JPanel {

    private static final List<ShopPanel> shopPanels = new ArrayList<ShopPanel>();
    private static final Map<String,Set<String>> itemLocations = new HashMap<String, Set<String>>();
    public static ShopPanel knownLocationsPanel = null;
    
    private final String shopLocation;
    private final JLabel shopLabel;
    
    public ShopPanel() {
        initComponents();
        shopPanels.add(this);
        shopLocation = null;
        shopLabel = null;
        
        Component[] components = (Component[])getComponents();
        for (Component comp : components) {
            if (comp instanceof JCheckBox) {
                JCheckBox box = (JCheckBox) comp;
                itemLocations.put(box.getText(), new HashSet<String>());
            }
        }
        knownLocationsPanel = this;
    }
    
    /**
     * Creates new form ShopPanel
     * @param location - Specifies the Label whose text will be used for location purposes.
     */
    public ShopPanel(JLabel location) {
        initComponents();
        shopPanels.add(this);
        shopLocation = location.getText();
        shopLabel = location;
        
        Component[] components = (Component[])getComponents();
        for (Component comp : components) {
            if (comp instanceof JCheckBox) {
                JCheckBox box = (JCheckBox) comp;
                itemLocations.put(box.getText(), new HashSet<String>());
            }
        }
    }
    
    public String getShopName() {
        return shopLocation;
    }
    
    public void showShop() {
        if(shopLabel == null)
            return;
        MaikaTracker.tracker.showShopPanel(shopLabel);
    }
    
    private static List<JCheckBox> getCheckBoxes(ShopPanel panel) {
        return Arrays.stream(panel.getComponents()).map(c -> (Component) c)
                        .filter(c -> c instanceof JCheckBox)
                        .map(c -> (JCheckBox) c).collect(Collectors.toList());
    }
    
    public static void UpdateFlags()
    {
        MaikaTracker tracker = MaikaTracker.tracker;
        FlagSet flagset = tracker.flagset;
        final Boolean cabinsOnly = flagset != null && flagset.contains("Sc");
        final Boolean emptyShop = flagset != null && flagset.contains("Sx");
        final Boolean vanillaShop = !tracker.flagsetContainsAny("S2", "S3", "S4", "Sc", "Sx");
        final Boolean jItems = tracker.flagsetContains("Ji") && !vanillaShop;
        final Boolean sirens = flagset == null || (!tracker.flagsetContains("-nosirens") && jItems);
        final Boolean rarejItems = tracker.flagsetContainsAny("S3", "S4") && jItems;
        final Boolean wildShops = tracker.flagsetContains("S4");
        final Boolean apples = flagset == null || (!tracker.flagsetContains("-noapples") && jItems && wildShops);
        final Boolean pass = tracker.flagsetContains("Ps");
       
        shopPanels.forEach(panel -> {            
            for (JCheckBox box : getCheckBoxes(panel)) {
                switch(box.getName())
                {
                    case "j item":                        
                        box.setVisible(jItems);
                        break;
                    case "siren":
                        box.setVisible(sirens);
                        break;
                    case "apples":
                        box.setVisible(apples);
                        break;
                        
                    case "rare j item":
                        box.setVisible(rarejItems);
                        break;

                    case "pass":
                        box.setVisible(pass);
                        break;

                    case "cabin":
                        box.setVisible(!emptyShop);
                        break;

                    case "default":
                        box.setVisible(!emptyShop && !cabinsOnly);
                        break;
                }
            }
        });
    }
    
    public static void reset() {
        final MaikaTracker tracker = MaikaTracker.tracker;
        final Boolean vanillaShop = !tracker.flagsetContainsAny("S1", "S2", "S3", "S4", "Sc", "Sx");
        final Boolean pass = tracker.flagsetContains("Ps");
        final Boolean jItems = tracker.flagsetContains("Ji");
        tracker.getPanelForKeyItem(KeyItemMetadata.PASS).setLocationInShop(null);
        shopPanels.forEach(panel -> {
            getCheckBoxes(panel).forEach((box) -> {
                box.setSelected(false);
            });
            
            if(vanillaShop) {
                switch(panel.shopLocation) {
                    case "Dwarf Castle":
                    case "Feymarch":
                    case "Tomara":
                    case "Mysidia":
                        panel.cabin.setSelected(true);
                        panel.cure2.setSelected(true);
                    case "Agart":
                    case "Baron":
                    case "Fabul":
                    case "Kaipo":
                    case "Troia [Item]":
                        panel.cure1.setSelected(true);
                        panel.life.setSelected(true);
                        panel.tent.setSelected(true);
                        panel.ether1.setSelected(!jItems);                    
                        break;
                        
                    case "Eblan Cave":
                        panel.cure1.setSelected(!jItems);
                        panel.cure2.setSelected(!jItems);
                        panel.tent.setSelected(!jItems);
                        panel.cabin.setSelected(!jItems);
                        panel.life.setSelected(!jItems);
                        panel.ether1.setSelected(!jItems);
                        break;
                        
                    case "Silvera":
                        break;
                        
                    case "Troia [Pub]":
                        panel.pass.setSelected(pass);
                        MaikaTracker.tracker.getPanelForKeyItem(KeyItemMetadata.PASS).setLocationInShop(panel);
                        break;
                        
                    case "Hummingway":
                        panel.cure2.setSelected(true);
                        panel.life.setSelected(true);
                        panel.ether1.setSelected(true);
                        panel.ether2.setSelected(true);
                        panel.elixir.setSelected(true);
                        panel.cabin.setSelected(true);
                        break;
                }
            }
            
            getCheckBoxes(panel).forEach((box) -> {
                if(box.isSelected())
                    itemLocations.get(box.getText()).add(panel.shopLocation);
                else
                    itemLocations.get(box.getText()).remove(panel.shopLocation);

                if (knownLocationsPanel != null) {
                    getCheckBoxes(knownLocationsPanel).stream()
                        .filter(c -> c.getText().equals(box.getText()))
                        .collect(Collectors.toList()).forEach((b) -> {
                            b.setSelected(!itemLocations.get(box.getText()).isEmpty());
                    });
                }                    
            });
            
        });
        UpdateToolTips();
    }
    
    public static void setTextColor(Color color) {
        shopPanels.forEach(panel -> {
            for (JCheckBox box : getCheckBoxes(panel)) {
                box.setForeground(color);
            }
        });
    }
    
    public static void setBackgroundColor(Color color) {
        shopPanels.forEach(panel -> {
            panel.setBackground(color);
            for (JCheckBox box : getCheckBoxes(panel)) {
                box.setBackground(color);
            }
        });
    }
    
    private static void UpdateToolTips() {
        shopPanels.forEach(panel -> {
            for (JCheckBox box : getCheckBoxes(panel)) {
                List<String> locations = itemLocations.get(box.getText()).stream().sorted().collect(Collectors.toList());
                if(locations.isEmpty())
                    box.setToolTipText(null);
                else
                    box.setToolTipText(String.join(", ", locations));
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        life = new javax.swing.JCheckBox();
        pass = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        cure1 = new javax.swing.JCheckBox();
        cure2 = new javax.swing.JCheckBox();
        cure3 = new javax.swing.JCheckBox();
        ether1 = new javax.swing.JCheckBox();
        ether2 = new javax.swing.JCheckBox();
        elixir = new javax.swing.JCheckBox();
        tent = new javax.swing.JCheckBox();
        cabin = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        coffin = new javax.swing.JCheckBox();
        siren = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        bacchus = new javax.swing.JCheckBox();
        illusion = new javax.swing.JCheckBox();
        silkweb = new javax.swing.JCheckBox();
        starveil = new javax.swing.JCheckBox();
        moonveil = new javax.swing.JCheckBox();
        exit = new javax.swing.JCheckBox();
        hourglass1 = new javax.swing.JCheckBox();
        hourglass2 = new javax.swing.JCheckBox();
        hourglass3 = new javax.swing.JCheckBox();
        grimoire = new javax.swing.JCheckBox();
        gaiadrum = new javax.swing.JCheckBox();
        stardust = new javax.swing.JCheckBox();
        agapple = new javax.swing.JCheckBox();
        auapple = new javax.swing.JCheckBox();
        stardust1 = new javax.swing.JCheckBox();
        mutebell = new javax.swing.JCheckBox();
        kamikaze = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();

        life.setText("Life");
        life.setName("default"); // NOI18N
        life.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        pass.setText("Pass");
        pass.setName("pass"); // NOI18N
        pass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        cure1.setText("Cure 1");
        cure1.setName("default"); // NOI18N
        cure1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        cure2.setText("Cure 2");
        cure2.setName("default"); // NOI18N
        cure2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        cure3.setText("Cure 3");
        cure3.setName("default"); // NOI18N
        cure3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        ether1.setText("Ether 1");
        ether1.setName("default"); // NOI18N
        ether1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        ether2.setText("Ether 2");
        ether2.setName("default"); // NOI18N
        ether2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        elixir.setText("Elixir");
        elixir.setName("default"); // NOI18N
        elixir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        tent.setText("Tent");
        tent.setName("default"); // NOI18N
        tent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        cabin.setText("Cabin");
        cabin.setName("cabin"); // NOI18N
        cabin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        coffin.setText("Coffin");
        coffin.setName("j item"); // NOI18N
        coffin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        siren.setText("Siren");
        siren.setName("siren"); // NOI18N
        siren.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        bacchus.setText("Bacchus");
        bacchus.setName("j item"); // NOI18N
        bacchus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        illusion.setText("Illusion");
        illusion.setName("j item"); // NOI18N
        illusion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        silkweb.setText("Silkweb");
        silkweb.setName("j item"); // NOI18N
        silkweb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        starveil.setText("Starveil");
        starveil.setName("j item"); // NOI18N
        starveil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        moonveil.setText("Moonveil");
        moonveil.setName("rare j item"); // NOI18N
        moonveil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        exit.setText("Exit");
        exit.setName("j item"); // NOI18N
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        hourglass1.setText("Hourglass 1");
        hourglass1.setName("j item"); // NOI18N
        hourglass1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        hourglass2.setText("Hourglass 2");
        hourglass2.setName("j item"); // NOI18N
        hourglass2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        hourglass3.setText("Hourglass 3");
        hourglass3.setName("j item"); // NOI18N
        hourglass3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        grimoire.setText("Grimoire");
        grimoire.setName("rare j item"); // NOI18N
        grimoire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        gaiadrum.setText("GaiaDrum");
        gaiadrum.setName("rare j item"); // NOI18N
        gaiadrum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        stardust.setText("Stardust");
        stardust.setName("rare j item"); // NOI18N
        stardust.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        agapple.setText("Ag Apple");
        agapple.setName("apples"); // NOI18N
        agapple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        auapple.setText("Au Apple");
        auapple.setName("apples"); // NOI18N
        auapple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        stardust1.setText("Soma Drop");
        stardust1.setName("apples"); // NOI18N
        stardust1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        mutebell.setText("MuteBell");
        mutebell.setName("j item"); // NOI18N
        mutebell.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        kamikaze.setText("Kamikaze");
        kamikaze.setName("j item"); // NOI18N
        kamikaze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(life, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(cure1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(cure2, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(cure3, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(ether1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(ether2, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(elixir, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(tent, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(cabin, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(coffin, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(siren, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(bacchus, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(illusion, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(silkweb, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(starveil, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(moonveil, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(exit, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(hourglass1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(hourglass2, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(hourglass3, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(grimoire, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(gaiadrum, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(stardust, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(agapple, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(auapple, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(stardust1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(mutebell, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(kamikaze, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(life, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cure1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cure2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cure3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ether1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ether2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(elixir, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tent, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cabin, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(coffin, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(siren, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bacchus, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(illusion, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(silkweb, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(starveil, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moonveil, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exit, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hourglass1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hourglass2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hourglass3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(grimoire, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gaiadrum, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stardust, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(agapple, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(auapple, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stardust1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mutebell, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(kamikaze, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void checkboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxActionPerformed
        final MaikaTracker tracker = MaikaTracker.tracker;
        final Boolean vanillaShop = !tracker.flagsetContainsAny("S1", "S2", "S3", "S4", "Sc", "Sx");
        
        if(evt.getSource() instanceof JCheckBox) {
            JCheckBox box = (JCheckBox) evt.getSource();
            if(this == knownLocationsPanel) {
                box.setSelected(!itemLocations.get(box.getText()).isEmpty());
            }
            else {                
                if(vanillaShop)
                    box.setSelected(!box.isSelected());         
                
                if(box.isSelected())
                    itemLocations.get(box.getText()).add(shopLocation);
                else
                    itemLocations.get(box.getText()).remove(shopLocation);
                
                if(knownLocationsPanel == null)
                    return; 
                
                getCheckBoxes(knownLocationsPanel).stream()
                        .filter(c -> c.getText().equals(box.getText()))
                        .collect(Collectors.toList()).forEach((b) -> {
                            b.setSelected(!itemLocations.get(box.getText()).isEmpty());
                });
                
                if(box.getText().equals("Pass")) {
                    KeyItemPanel passPanel = MaikaTracker.tracker.getPanelForKeyItem(KeyItemMetadata.PASS);
                    passPanel.setLocationInShop(box.isSelected() ? this : null);
                } 
            }       
        }
        
        UpdateToolTips();
    }//GEN-LAST:event_checkboxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox agapple;
    public javax.swing.JCheckBox auapple;
    public javax.swing.JCheckBox bacchus;
    public javax.swing.JCheckBox cabin;
    public javax.swing.JCheckBox coffin;
    public javax.swing.JCheckBox cure1;
    public javax.swing.JCheckBox cure2;
    public javax.swing.JCheckBox cure3;
    public javax.swing.JCheckBox elixir;
    public javax.swing.JCheckBox ether1;
    public javax.swing.JCheckBox ether2;
    public javax.swing.JCheckBox exit;
    public javax.swing.JCheckBox gaiadrum;
    public javax.swing.JCheckBox grimoire;
    public javax.swing.JCheckBox hourglass1;
    public javax.swing.JCheckBox hourglass2;
    public javax.swing.JCheckBox hourglass3;
    public javax.swing.JCheckBox illusion;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JLabel jLabel4;
    public javax.swing.JLabel jLabel5;
    public javax.swing.JLabel jLabel6;
    public javax.swing.JLabel jLabel7;
    public javax.swing.JCheckBox kamikaze;
    public javax.swing.JCheckBox life;
    public javax.swing.JCheckBox moonveil;
    public javax.swing.JCheckBox mutebell;
    public javax.swing.JCheckBox pass;
    public javax.swing.JCheckBox silkweb;
    public javax.swing.JCheckBox siren;
    public javax.swing.JCheckBox stardust;
    public javax.swing.JCheckBox stardust1;
    public javax.swing.JCheckBox starveil;
    public javax.swing.JCheckBox tent;
    // End of variables declaration//GEN-END:variables
}
