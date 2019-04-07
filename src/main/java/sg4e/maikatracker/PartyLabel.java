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
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import sg4e.ff4stats.party.LevelData;
import sg4e.ff4stats.party.PartyMember;

/**
 *
 * @author sg4e
 */
public class PartyLabel extends StativeLabel {
    
    private static final Map<LevelData,ImageIcon> CHARACTER_ICONS = Collections.unmodifiableMap(
            Arrays.stream(LevelData.values()).collect(Collectors.toMap((data) -> data, 
                    (data) -> new ImageIcon(MaikaTracker.loadImageResource("characters/" + data.toString().toLowerCase().replaceAll(" ", "") + ".png")))));
    private static final ImageIcon ADULT_RYDIA_ICON = new ImageIcon(MaikaTracker.loadImageResource("characters/adultrydia.png"));
    
    private LevelData data = null;
    private PartyMember character;
    private final PropertyChangeListener pcl;
    private Runnable onChangeAction = null;
    
    public static boolean MtOrdealsComplete;
    public static boolean DwarfCastleComplete;
    
    public PartyLabel(PropertyChangeListener onLevelUp) {
        this.pcl = onLevelUp;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu menu = new JPopupMenu();
                    if(!isCleared()) {
                        JMenuItem reset = new JMenuItem("Reset");
                        reset.addActionListener((ae) -> clearLabel());
                        menu.add(reset);
                        JMenuItem duplicate = new JMenuItem("Replace with Duplicate");
                        duplicate.addActionListener((ae) -> {
                            if(character != null && pcl != null)
                                character.removePropertyChangeListener(pcl);
                            character = new PartyMember(data);
                            if(pcl != null)
                                character.addPropertyChangeListener(pcl);
                            if(onChangeAction != null)
                                onChangeAction.run();
                        });
                        menu.add(duplicate);
                        menu.add(new JSeparator());
                    }
                    Arrays.stream(LevelData.values()).forEach((data) -> {                        
                        if(!(data.equals(LevelData.DARK_KNIGHT_CECIL) && MtOrdealsComplete) &&
                           !(data.equals(LevelData.PALADIN_CECIL) && !MtOrdealsComplete)) {
                            if (data.equals(LevelData.PALADIN_CECIL) && MtOrdealsComplete) {
                                JMenuItem cecil = new JMenuItem(data.toString());
                                cecil.addActionListener((ae) -> setPartyMember(data));
                                menu.add(cecil, isCleared() ? 0 : 3);
                            }
                            else
                                menu.add(data.toString()).addActionListener((ae) -> setPartyMember(data));
                        }
                            
                    });
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    public LevelData getData() {
        return data;
    }
    
    public PartyMember getPartyMember() {
        return character;
    }
    
    public boolean hasPartyMember() {
        return data != null;
    }
    
    public void setPartyMember(LevelData member) {
        if (member.equals(LevelData.DARK_KNIGHT_CECIL) && MtOrdealsComplete)
            member = LevelData.PALADIN_CECIL;

        if(data != member) {
            data = member;
            if(character != null && pcl != null)
                character.removePropertyChangeListener(pcl);
            character = new PartyMember(data);
            if(pcl != null)
                character.addPropertyChangeListener(pcl);
            ImageIcon icon = CHARACTER_ICONS.get(member);
            if(member.equals(LevelData.RYDIA) && DwarfCastleComplete)
                icon = ADULT_RYDIA_ICON;
            setNewIconState(icon, icon);
            if(onChangeAction != null)
                onChangeAction.run();
        }
        else if(member.equals(LevelData.RYDIA) && DwarfCastleComplete)
            setNewIconState(ADULT_RYDIA_ICON, ADULT_RYDIA_ICON);

    }
    
    public void setOnPartyChangeAction(Runnable action) {
        onChangeAction = action;
    }

    @Override
    protected void clearLabel() {
        super.clearLabel();
        data = null;
        if(character != null && pcl != null)
            character.removePropertyChangeListener(pcl);
        character = null;
        if(onChangeAction != null)
            onChangeAction.run();
    }
    
}
