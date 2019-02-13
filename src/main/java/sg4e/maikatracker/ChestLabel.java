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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 *
 * @author sg4e
 */
public class ChestLabel extends JLabel {
    
    private static final Color COLOR_UNKNOWN = Color.RED;
    private static final String TEXT_UNKNOWN = "?";
    private static final Color COLOR_KNOWN = Color.GREEN;
    private static final String TEXT_KNOWN = "✓";
    
    private State state;
    
    public ChestLabel() {
        setOpaque(false);
        setPreferredSize(new Dimension(TreasureChest.PIXELS_PER_SQUARE, TreasureChest.PIXELS_PER_SQUARE));
        setHorizontalAlignment(SwingConstants.CENTER);
        setAlignmentX(JLabel.CENTER_ALIGNMENT);
    }
    
    public void activate() {
        setOpaque(true);
        setUnchecked();
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
                    ((MaikaTracker)SwingUtilities.getWindowAncestor(ChestLabel.this))
                            .getUnknownKeyItemMenu(ki -> {
                                setText(null);
                                setToolTipText(ki.getEnum().toString());
                                setIcon(new ImageIcon(ki.getColorIcon().getImage().getScaledInstance(
                                        TreasureChest.PIXELS_PER_SQUARE, TreasureChest.PIXELS_PER_SQUARE, java.awt.Image.SCALE_SMOOTH)));
                            }).show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    private void setUnchecked() {
        setBackground(COLOR_UNKNOWN);
        if(getIcon() == null)
            setText(TEXT_UNKNOWN);
        setForeground(Color.WHITE);
        state = State.UNCHECKED;
    }
    
    private void setChecked() {
        setBackground(COLOR_KNOWN);
        if(getIcon() == null)
            setText(TEXT_KNOWN);
        setForeground(Color.BLACK);
        state = State.CHECKED;
    }
    
    private static enum State {
        UNCHECKED, CHECKED;
    }
    
}
