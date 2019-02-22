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

import com.google.common.collect.Maps;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import sg4e.ff4stats.party.LevelData;

/**
 *
 * @author sg4e
 */
public class PartyLabel extends StativeLabel {
    
    private static final Map<LevelData,ImageIcon> CHARACTER_ICONS = Collections.unmodifiableMap(
            Arrays.stream(LevelData.values()).collect(Collectors.toMap((data) -> data, 
                    (data) -> new ImageIcon(MaikaTracker.loadImageResource("characters/" + data.toString().toLowerCase().replaceAll(" ", "") + ".png")))));
    
    private LevelData character = null;
    
    public PartyLabel() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu menu = new JPopupMenu();
                    if(!isCleared()) {
                        JMenuItem reset = new JMenuItem("Reset");
                        reset.addActionListener((ae) -> clearLabel());
                        menu.add(reset);
                        menu.add(new JSeparator());
                    }
                    Arrays.stream(LevelData.values()).forEach((data) -> menu.add(data.toString()).addActionListener((ae) -> setPartyMember(data)));
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    public LevelData getPartyMember() {
        return character;
    }
    
    public boolean hasPartyMember() {
        return character != null;
    }
    
    public void setPartyMember(LevelData member) {
        character = member;
        ImageIcon icon = CHARACTER_ICONS.get(member);
        setNewIconState(icon, icon);
    }

    @Override
    protected void clearLabel() {
        super.clearLabel();
        character = null;
    }
    
}
