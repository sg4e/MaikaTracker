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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import sg4e.ff4stats.fe.FlagSet;
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
    
    public static final List<PartyLabel> PartyMembers = new ArrayList<PartyLabel>();
    
    public static boolean MtOrdealsComplete;
    public static boolean DwarfCastleComplete;
    public static FlagSet flagset;
    
    public PartyLabel(PropertyChangeListener onLevelUp) {
        this.pcl = onLevelUp;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu menu = new JPopupMenu();
                    int count = 0;
                    int baseCount = 0;
                    if(!isCleared()) {
                        JMenuItem reset = new JMenuItem("Reset");
                        reset.addActionListener((ae) -> clearLabel());
                        menu.add(reset);
                        if(isDupeAllowed(data)) {
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
                            baseCount++;
                        }
                        menu.add(new JSeparator());
                        baseCount += 2;
                    }
                    for(LevelData data : LevelData.values())
                    {
                        Boolean addMember;
                        int position = count++;
                        switch(data) {
                            case DARK_KNIGHT_CECIL:
                                addMember = memberAllowed("cecil");
                                addMember &= !MtOrdealsComplete;
                                break;
                            case PALADIN_CECIL:
                                addMember = memberAllowed("cecil");
                                addMember &= MtOrdealsComplete;
                                position = 0;
                                break;
                            case CID:
                                addMember = memberAllowed("cid");
                                break;                                        
                            case EDGE:
                                addMember = memberAllowed("edge");
                                break;
                            case EDWARD:
                                addMember = memberAllowed("edward");
                                break;
                            case FUSOYA:
                                addMember = memberAllowed("fusoya");
                                break;
                            case KAIN:
                                addMember = memberAllowed("kain");
                                break;
                            case PALOM:
                                addMember = memberAllowed("palom");
                                break;
                            case POROM:
                                addMember = memberAllowed("porom");
                                break;
                            case ROSA:
                                addMember = memberAllowed("rosa");
                                break;
                            case RYDIA:
                                addMember = memberAllowed("rydia");
                                break;
                            case TELLAH:
                                addMember = memberAllowed("tellah");
                                break;
                            case YANG:
                                addMember = memberAllowed("yang");
                                break;
                            default:
                                addMember = false;
                                break;
                        }                        
                        if(addMember && isDupeAllowed(data)) {
                            JMenuItem member = new JMenuItem(data.toString());
                            member.addActionListener((ae) -> setPartyMember(data));
                            menu.add(member, position + baseCount);
                        }
                        else {
                            count--;
                        }
                    }
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    private Boolean memberAllowed(String member) {
        return flagset == null || flagset.contains("-start" + member) || !flagset.contains("-no" + member);
    }
    
    private Boolean isDupeAllowed(LevelData member)
    {
        if(member == null || flagset == null) return true;
        
        Boolean notAllowed = flagset.contains("-nodupes");

        switch(member)
        {
            case CID:
                notAllowed |= flagset.contains("-nocid");
                break;
            case DARK_KNIGHT_CECIL:
            case PALADIN_CECIL:
                notAllowed |= flagset.contains("-nocecil");
                break;
            case EDGE:
                notAllowed |= flagset.contains("-noedge");
                break;
            case EDWARD:
                notAllowed |= flagset.contains("-noedward");
                break;
            case FUSOYA:
                notAllowed |= flagset.contains("-nofusoya");
                break;
            case KAIN:
                notAllowed |= flagset.contains("-nokain");
                break;
            case PALOM:
                notAllowed |= flagset.contains("-nopalom");
                break;
            case POROM:
                notAllowed |= flagset.contains("-noporom");
                break;
            case ROSA:
                notAllowed |= flagset.contains("-norosa");
                break;
            case RYDIA:
                notAllowed |= flagset.contains("-norydia");
                break;
            case TELLAH:
                notAllowed |= flagset.contains("-notellah");
                break;
            case YANG:
                notAllowed |= flagset.contains("-noyang");
                break;
            default:
                return false;
        }
        
        return !notAllowed || PartyMembers.stream().noneMatch((label) -> (label.data == member));
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
        if (member.equals(LevelData.PALADIN_CECIL) && !MtOrdealsComplete)
            member = LevelData.DARK_KNIGHT_CECIL;

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
            setNewIconState(icon);
            if(onChangeAction != null)
                onChangeAction.run();
        }
        else if(member.equals(LevelData.RYDIA) && DwarfCastleComplete)
            setNewIconState(ADULT_RYDIA_ICON, ADULT_RYDIA_ICON);
        else if (member.equals(LevelData.RYDIA) && !DwarfCastleComplete)
            setNewIconState(CHARACTER_ICONS.get(member), CHARACTER_ICONS.get(member));

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
