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

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
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
    private final JLabel itemImage;
    private final KeyItemMetadata metadata;
    private final JLabel locationLabel;
    private KeyItemLocation location = null;
    
    private static final String UNKNOWN_LOCATION = "?";
    
    public KeyItemPanel(KeyItemMetadata meta) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        metadata = meta;
        itemImage = new StativeLabel(metadata.getGrayIcon(), metadata.getColorIcon());
        itemImage.setToolTipText(metadata.getEnum().toString());
        itemImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu locationMenu;
                    MaikaTracker tracker = MaikaTracker.getTrackerFromChild(KeyItemPanel.this);
                    if(isKnown()) {
                        locationMenu = new JPopupMenu("Locations");
                        locationMenu.add("Reset").addActionListener((ae) -> {
                            if(location == null)
                                tracker.resetKeyItemLocation(metadata, locationLabel.getText());
                            else
                                reset();
                        });
                        if(isInChest()) {
                            locationMenu.add(new JSeparator(), 0);
                            JMenuItem goToChest = new JMenuItem("Show chest"/*slmLewd*/);
                            goToChest.addActionListener((ae) -> tracker.getAtlas().showChest(locationLabel.getText()));
                            locationMenu.add(goToChest, 0);
                        }
                    }
                    else {
                        locationMenu = ((MaikaTracker)SwingUtilities.getWindowAncestor(KeyItemPanel.this))
                                .getAvailableLocationsMenu(loc -> {
                                    location = loc;
                                    locationLabel.setText(loc.getAbbreviatedLocation());
                                    locationLabel.setToolTipText(loc.getLocation());
                                });
                        locationMenu.add(new JSeparator(), 0);
                        JMenuItem custom = new JMenuItem("Chest location");
                        custom.addActionListener((ae) -> {
                            String customOption = JOptionPane.showInputDialog("Enter chest location");
                            String chestId = customOption.toUpperCase();
                            if(tracker.getAtlas().hasChestId(chestId)) {
                                locationLabel.setText(chestId);
                                tracker.updateKeyItemLocation(metadata, chestId);
                            }
                            else {
                                JOptionPane.showMessageDialog(tracker, "Not a valid chest id", "Invalid id", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        locationMenu.add(custom, 0);
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
    
    public KeyItemLocation getItemLocation() {
        return location;
    }
    
    public boolean isAcquired() {
        return itemImage.getIcon() != metadata.getGrayIcon();
    }
    
    public boolean isKnown() {
        return location != null || !UNKNOWN_LOCATION.equals(locationLabel.getText());
    }
    
    public boolean isInChest() {
        return location == null && !UNKNOWN_LOCATION.equals(locationLabel.getText());
    }
    
    public KeyItemMetadata getKeyItem() {
        return metadata;
    }
    
    public void setLocationInChest(String chestId) {
        locationLabel.setText(chestId);
    }
    
    public void reset() {
        locationLabel.setText(UNKNOWN_LOCATION);
        locationLabel.setToolTipText(null);
        location = null;
    }
    
}
