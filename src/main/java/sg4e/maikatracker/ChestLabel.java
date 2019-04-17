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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author sg4e
 */
public class ChestLabel extends JLabel {
    
    private static final Color COLOR_UNKNOWN = Color.RED;
    private static final String TEXT_UNKNOWN = "?";
    private static final Color COLOR_KNOWN = Color.GREEN;
    private static final String TEXT_KNOWN = "✓";
    private static final ImageIcon checked, unchecked;
    
    private State state;
    private TreasureChest chest;
    private KeyItemMetadata keyItemContents;
    private ImageIcon active, deactive;
    
    static {
        String uncheckedUrl = "maps/unchecked.png";
        String checkedUrl = "maps/checked.png";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream uncheckedStream = classLoader.getResourceAsStream(uncheckedUrl);
        InputStream checkedStream = classLoader.getResourceAsStream(checkedUrl);
        
        ImageIcon c, uc;
        
        try {
            uc = new ImageIcon(ImageIO.read(uncheckedStream).getScaledInstance(
                TreasureChest.PIXELS_PER_SQUARE, TreasureChest.PIXELS_PER_SQUARE, java.awt.Image.SCALE_SMOOTH));
            c = new ImageIcon(ImageIO.read(checkedStream).getScaledInstance(
                TreasureChest.PIXELS_PER_SQUARE, TreasureChest.PIXELS_PER_SQUARE, java.awt.Image.SCALE_SMOOTH));
        }
        catch(IOException ex) {
            LogManager.getLogger().error("Error loading Key Item icons", ex);
            uc = null;
            c = null;
        }
        unchecked = uc;
        checked = c;
    }
    
    public ChestLabel() {
        setOpaque(false);
        setPreferredSize(new Dimension(TreasureChest.PIXELS_PER_SQUARE, TreasureChest.PIXELS_PER_SQUARE));
        setHorizontalAlignment(SwingConstants.CENTER);
        setAlignmentX(JLabel.CENTER_ALIGNMENT);
        active = checked;
        deactive = unchecked;
    }
    
    public void activate(TreasureChest chest) {
        this.chest = chest;
        setOpaque(true);
        setUnchecked();
        setToolTipText(chest.getId());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    if(state == State.UNCHECKED) {
                        setChecked();
                    }
                    else {
                        setUnchecked();
                    }
                }
                else if(SwingUtilities.isRightMouseButton(e)) {
                    MaikaTracker tracker = MaikaTracker.getTrackerFromChild(ChestLabel.this);

                    JPopupMenu menu;
                    if(keyItemContents == null || !tracker.isResetOnly())
                    {
                        menu = tracker.getUnknownKeyItemMenu(ki -> {
                            if(keyItemContents != null)
                                tracker.resetKeyItemLocation(keyItemContents, chest.getId());
                            tracker.updateKeyItemLocation(ki, chest.getId());
                            keyItemContents = ki;
                        });
                    }
                    else {
                        menu = new JPopupMenu();
                    }
                    if (keyItemContents != null) {
                        if(!tracker.isResetOnly())
                            menu.add(new JSeparator(), 0);
                        JMenuItem resetItem = new JMenuItem("Reset");
                        resetItem.addActionListener((ae) -> tracker.resetKeyItemLocation(keyItemContents, chest.getId()));
                        menu.add(resetItem, 0);
                    }
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    private void setUnchecked() {
        setBackground(COLOR_UNKNOWN);
        if(deactive == null)
            setText(TEXT_UNKNOWN);
        else
            setIcon(deactive);
        setForeground(Color.WHITE);
        state = State.UNCHECKED;
    }
    
    private void setChecked() {
        setBackground(COLOR_KNOWN);        
        if(active == null)
            setText(TEXT_KNOWN);
        else
            setIcon(active);
        setForeground(Color.BLACK);
        state = State.CHECKED;
    }
    
    public void setKeyItem(KeyItemMetadata ki) {
        keyItemContents = ki;
        setText(null);
        setToolTipText(ki.getEnum().toString());
        active = new ImageIcon(ki.getColorIcon().getImage().getScaledInstance(
                TreasureChest.PIXELS_PER_SQUARE, TreasureChest.PIXELS_PER_SQUARE, java.awt.Image.SCALE_SMOOTH));
        deactive = new ImageIcon(ki.getGrayIcon().getImage().getScaledInstance(
                TreasureChest.PIXELS_PER_SQUARE, TreasureChest.PIXELS_PER_SQUARE, java.awt.Image.SCALE_SMOOTH));
        if(state == State.UNCHECKED)
            setUnchecked();
        else
            setChecked();
    }
    
    public void clearKeyItem() {
        setToolTipText(null);
        active = checked;
        deactive = unchecked;
        keyItemContents = null;
        if(state == State.UNCHECKED)
            setUnchecked();
        else
            setChecked();
    }
    
    public void reset() {
        setUnchecked();
        clearKeyItem();
    }
    
    private static enum State {
        UNCHECKED, CHECKED;
    }
    
}
