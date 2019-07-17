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

import com.google.common.base.Objects;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import sg4e.ff4stats.fe.KeyItemLocation;

/**
 *
 * @author sg4e
 */
public class KeyItemPanel extends JPanel {
    private final MaikaTracker tracker;
    private final StativeLabel itemImage;
    private final KeyItemMetadata metadata;
    private final JLabel locationLabel;
    private KeyItemLocation location = null;
    private ChestLabel chestLabel = null;
    private ShopPanel shopPanel = null;
    private boolean useCheckedIcons;
    
    private static final String UNKNOWN_LOCATION = "?";
    
    private boolean resetting;
    
    public KeyItemPanel(KeyItemMetadata meta) {
        tracker = MaikaTracker.tracker;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        metadata = meta;
        ImageIcon checkedIcon = metadata.getCheckedIcon();
        
        if(checkedIcon != null)
            itemImage = new StativeLabel(metadata.getGrayIcon(), metadata.getColorIcon(), metadata.getCheckedIcon());
        else
            itemImage = new StativeLabel(metadata.getGrayIcon(), metadata.getColorIcon());
        
        itemImage.setToolTipText(metadata.getEnum().toString());
        itemImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    updateKeyItem();
                }
                if(SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu locationMenu = new JPopupMenu("Locations");
                    if(isKnown()) {                        
                        if(isInChest())
                            locationMenu.add("Show chest").addActionListener((ae) -> tracker.getAtlas().showChest(locationLabel.getText()));
                        if(isInShop())
                            locationMenu.add("Show shop").addActionListener((ae) -> shopPanel.showShop());                        
                        if(allowLocation() || allowChest() || allowShop()) {
                            if(isInChest() || isInShop())
                                locationMenu.add(new JSeparator());
                            locationMenu.add("Reset").addActionListener((ae) -> reset());
                            if(!tracker.isResetOnly())
                                locationMenu.add(new JSeparator());
                        }
                    }
                    if(!isKnown() || !tracker.isResetOnly()) {                        
                        if(allowChest()) {                            
                            locationMenu.add("Chest location").addActionListener((ae) -> {
                                String customOption = JOptionPane.showInputDialog("Enter chest location");
                                if(customOption != null) {
                                    String chestId = customOption.toUpperCase();
                                    if(tracker.getAtlas().hasChestId(chestId)) {
                                        final List<KeyItemPanel> panels = tracker.getKeyItemPanels();
                                        panels.forEach(panel -> {
                                            if(panel.locationLabel.getText().equals(chestId)) {
                                                panel.reset();
                                            }
                                        });
                                        if(isKnown())
                                            reset();
                                        locationLabel.setText(chestId);
                                        tracker.updateKeyItemLocation(metadata, chestId);
                                    }
                                    else {
                                        JOptionPane.showMessageDialog(tracker, "Not a valid chest id", "Invalid id", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                        }
                        if(allowShop()) {
                            ShopPanel.getAvailableShopsMenu(shop -> {
                                if(!shop.pass.isSelected())
                                    shop.pass.doClick();
                                setLocationInShop(shop);
                            }, locationMenu);
                        }
                        if(allowLocation())
                            tracker.getAvailableLocationsMenu(loc -> setLocation(loc), locationMenu);
                    }
                    locationMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        itemImage.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        add(itemImage, BorderLayout.CENTER);
        
        locationLabel = new JLabel(UNKNOWN_LOCATION, JLabel.CENTER);
        locationLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);        
        add(locationLabel, BorderLayout.CENTER);
    }
    
    public void setCheckedKeyItem(boolean checked) {
        useCheckedIcons = checked;
        if(metadata.getCheckedIcon() == null)
            return;
        boolean on = itemImage.isActive();
        if(checked)
            itemImage.setNewIconState(metadata.getGrayIcon(), metadata.getColorIcon(), metadata.getCheckedIcon());
        else
            itemImage.setNewIconState(metadata.getGrayIcon(), metadata.getColorIcon());
        itemImage.setActive(on);
    }
    
    public void updateKeyItem() {
        tracker.updateKeyItemCountLabel();
        tracker.handleLogic(location, isAcquired());
        if(location != null) {
            if (isAcquired())
                tracker.locationsVisited.add(location);
            else
                tracker.locationsVisited.remove(location);
        }
        else if (isInChest()) {
            chestLabel.setChecked(isAcquired());
        }
        tracker.updateLogic();
    }
    
    public boolean allowLocation() {
        switch(metadata) {
            case PASS:
                return tracker.flagsetContainsAll("K", "Pk");
            case CRYSTAL:
                return tracker.flagsetContains("K") && !tracker.flagsetContains(false, "V1");
            default:
                return tracker.flagsetContains("K");
        }
    }
    
    public boolean allowChest() {
        return tracker.isItemAllowedInChest(metadata);
    }
    
    public boolean allowShop() {
        return metadata.equals(KeyItemMetadata.PASS) && tracker.flagsetContains("Ps") &&
                tracker.flagsetContainsAny("S1", "S2", "S3", "S4", "Sc", "Sx");
    }
    
    public void setActive(boolean on) {
        if(on == itemImage.isActive())
            return;
        itemImage.setActive(on);
        updateKeyItem();
    }
    
    public void setLocation(KeyItemLocation loc) {
        if(isKnown())
            reset();
        location = loc;
        locationLabel.setText(loc.getAbbreviatedLocation());
        locationLabel.setToolTipText(loc.getLocation());
        tracker.handleLogic(loc, isAcquired());
        if(isAcquired())
            tracker.locationsVisited.add(loc);
        else
            tracker.locationsVisited.remove(loc);
        tracker.updateLogic();
    }
    
    public void setTextColor(Color color) {
        locationLabel.setForeground(color);
    }
    
    public void setBackgroundColor(Color color) {
        setBackground(color);
        itemImage.setBackground(color);
    }
    
    public KeyItemLocation getItemLocation() {
        return location;
    }
    
    public boolean isAcquired() {
        return itemImage.getIcon() != metadata.getGrayIcon();
    }
    
    public boolean isKnown() {
        return location != null || chestLabel != null || shopPanel != null;
    }
    
    public boolean isInChest() {
        return location == null && chestLabel != null;
    }
    
    public boolean isInShop() {
        return location == null && shopPanel != null;
    }
    
    public KeyItemMetadata getKeyItem() {
        return metadata;
    }
    
    public void setLocationInChest(ChestLabel label) {
        chestLabel = label;
        locationLabel.setText(label.getId());        
    }
    
    public void setLocationInShop(ShopPanel panel) {
        if(metadata != KeyItemMetadata.PASS || Objects.equal(shopPanel, panel))
            return;
        
        if(isKnown())
            reset();
        
        if(panel != null && panel.pass.isSelected()) {
            shopPanel = panel;
            locationLabel.setText("Shop");
            locationLabel.setToolTipText(panel.getShopName());
        }
        else {
            shopPanel = null;
        }
    }
    
    public void setDarkness(float darkness) {
        metadata.setDarkness(darkness);
        int state = itemImage.getState();
        setCheckedKeyItem(useCheckedIcons);
        itemImage.setState(state);
    }
    
    public void reset() {
        reset(false);
    }
    
    public void reset(boolean resetIcon) {
        if(resetting) return;
        resetting = true;
        
        if (isInChest()) {
            chestLabel.reset();
            chestLabel = null;
        }
        
        if (isInShop()) {
            if(shopPanel.pass.isSelected())
                shopPanel.pass.doClick();
            shopPanel = null;
        }
        
        if (location != null) {
            tracker.handleLogic(location, false);
            tracker.locationsVisited.remove(location);
        }

        locationLabel.setText(UNKNOWN_LOCATION);
        locationLabel.setToolTipText(null);
        location = null;
        if(resetIcon)
            ((StativeLabel)itemImage).reset();
        
        if(tracker.flagset == null) {
            resetting = false;
            return;
        }
        
        if(!tracker.flagsetContainsAny("Pk", "Ps", "Pt") && metadata.equals(KeyItemMetadata.PASS)) {
            locationLabel.setText("None");
            locationLabel.setToolTipText("No Pass in this flagset :(");
        }            
        
        if (!tracker.flagsetContains("K")) {
            switch(metadata) {
                case ADAMANT:
                    setLocation(KeyItemLocation.RAT_TAIL);
                    break;
                case BARON_KEY:
                    setLocation(KeyItemLocation.BARON_INN);
                    break;
                case CRYSTAL:
                    setLocation(tracker.flagsetContains("V1") ? KeyItemLocation.KOKKOL : KeyItemLocation.ZEROMUS);
                    break;
                case DARKNESS:
                    setLocation(KeyItemLocation.SEALED_CAVE);
                    break;
                case EARTH:
                    setLocation(KeyItemLocation.DARK_ELF);
                    break;
                case HOOK:
                    setLocation(KeyItemLocation.LOW_BABIL);
                    break;
                case LEGEND:
                    setLocation(KeyItemLocation.ORDEALS);
                    break;
                case LUCA_KEY:
                    setLocation(KeyItemLocation.DWARF_CASTLE);
                    break;
                case MAGMA_KEY:
                    setLocation(KeyItemLocation.ZOT);
                    break;
                case PACKAGE:
                    setLocation(KeyItemLocation.START);
                    break;
                case PAN:
                    setLocation(KeyItemLocation.SHEILA_PANLESS);
                    break;
                case PASS:
                    if(tracker.flagsetContains("Pk"))
                        setLocation(KeyItemLocation.BARON_CASTLE);
                    break;
                case PINK_TAIL:                    
                    break;
                case RAT_TAIL:
                    setLocation(KeyItemLocation.SUMMONED_MONSTERS_CHEST);
                    break;
                case SAND_RUBY:
                    setLocation(KeyItemLocation.ANTLION);
                    break;
                case SPOON:
                    setLocation(KeyItemLocation.SHEILA_PAN);
                    break;
                case TOWER_KEY:
                    setLocation(KeyItemLocation.TOP_BABIL);
                    break;
                case TWIN_HARP:
                    if(tracker.flagset == null)
                        break;
                    setLocation(tracker.flagsetContains("Nk") ? KeyItemLocation.MIST : KeyItemLocation.TOROIA);
                    break;
            }
        }
        else if(tracker.flagsetContains("V1") && metadata.equals(KeyItemMetadata.CRYSTAL))
            setLocation(KeyItemLocation.KOKKOL);
        tracker.updateLogic();
        
        resetting = false;
    }
    
}
