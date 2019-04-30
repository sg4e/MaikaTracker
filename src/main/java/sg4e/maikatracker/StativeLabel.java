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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author sg4e
 */
public class StativeLabel extends JLabel {
    
    private ImageIcon active, deactive;
    
    public static final ImageIcon UNKNOWN_ICON = new ImageIcon(MaikaTracker.loadImageResource("characters/unknown.png"));
    
    public StativeLabel() {
        this(UNKNOWN_ICON, UNKNOWN_ICON);
    }
    
    public StativeLabel(ImageIcon off, ImageIcon on) {
        setNewIconState(off, on);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    if(getIcon() == deactive) {
                        setIcon(active);
                    }
                    else {
                        setIcon(deactive);
                    }
                }
            }
        });
    }
    
    protected void clearLabel() {
        setIcon(active = deactive = UNKNOWN_ICON);
    }
    
    public void reset() {
        setIcon(deactive);
    }
    
    public void setActive(boolean on) {
        setIcon(on ? active : deactive);
    }
    
    public boolean isActive() {
        return getIcon() == active;
    }
    
    public boolean isCleared() {
        return getIcon() == UNKNOWN_ICON;
    }
    
    public void setNewIconState(ImageIcon off, ImageIcon on) {
        active = on;
        deactive = off;
        setIcon(off);
    }
    
}
