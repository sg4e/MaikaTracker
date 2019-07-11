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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author sg4e
 */
public class StativeLabel extends JLabel {
    
    private List<ImageIcon> imageIcons = new ArrayList<>();
    private int iconIndex = 0;
    
    public static final ImageIcon UNKNOWN_ICON = new ImageIcon(MaikaTracker.loadImageResource("characters/unknown.png"));
    
    public StativeLabel(ImageIcon ... icons) {
        setNewIconState(icons);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    iconIndex++;
                    if(iconIndex >= imageIcons.size())
                        iconIndex = 0;
                    setIcon(imageIcons.get(iconIndex));
                }
            }
        });
    }
    
    protected void clearLabel() {
        setNewIconState(UNKNOWN_ICON);
    }
    
    public void reset() {
        iconIndex = 0;
        setIcon(imageIcons.get(iconIndex));
    }
    
    public void setActive(boolean on) {
        iconIndex = on ? 1 : 0;
        if(iconIndex >= imageIcons.size())
            iconIndex = 0;
        setIcon(imageIcons.get(iconIndex));
    }
    
    public boolean isActive() {
        return iconIndex > 0;
    }
    
    public boolean isCleared() {
        return getIcon() == UNKNOWN_ICON && imageIcons.size() == 1;
    }
    
    public void setNewIconState(ImageIcon ... icons) {
        imageIcons.clear();
        if(icons.length > 0)
            Stream.of(icons).forEach(icon -> {imageIcons.add(icon == null ? UNKNOWN_ICON : icon);});
        else
            imageIcons.add(UNKNOWN_ICON);
        iconIndex = 0;
        setIcon(imageIcons.get(iconIndex));
    }
    
}
