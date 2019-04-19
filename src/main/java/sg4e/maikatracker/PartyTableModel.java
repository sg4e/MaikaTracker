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

import com.google.common.collect.Range;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import sg4e.ff4stats.party.PartyMember;
import sg4e.ff4stats.party.Stats;

/**
 *
 * @author sg4e
 */
public class PartyTableModel extends DefaultTableModel {
    
    private final Class[] types = new Class [] {
        java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
    };
    private final boolean[] canEdit = new boolean [] {
        false, true, true, false, false, false, false, false, false, false, false, false
    };
    
    private List<PartyMember> members = new ArrayList<>();
    private static final int STARTING_LEVEL_COLUMN = 1;
    private static final int STARTING_XP_COLUMN = 2;
    
    public PartyTableModel() {
        super(new Object [][] {
            {null, null, null, null, null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null, null, null, null, null}
        },
        new String [] {
            "Party", "Start Lvl", "Start XP", "Level", "XP", "HP", "MP", "Str", "Agi", "Vit", "Will", "Wis"
        });
//        addTableModelListener((tableEvent) -> {
//            final int col = tableEvent.getColumn();
//            if(col == TableModelEvent.ALL_COLUMNS || col == 0 || col == 2)
//                updateStartingLevelAndXp();
//        });
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
    }
    
    public void setPartyMembers(Collection<PartyMember> party) {
        members = new ArrayList<>(party);
        members.stream().filter(m -> !m.hasPropertyChangeListener(statChangeListener)).forEach(m -> m.addPropertyChangeListener(statChangeListener));
        populateTable();
    }
    
    private void clearTable() {
        //clear table
        for(int i = 0; i < getRowCount(); i++) {
            for(int j = 0; j < getColumnCount(); j++) {
                if(j == STARTING_LEVEL_COLUMN || j == STARTING_XP_COLUMN)
                    continue;
                setValueAt(null, i, j);
            }
        }
    }
    
    private void populateTable() {
        clearTable();
        for(int i = 0; i < members.size(); i++) {
            PartyMember m = members.get(i);
            Range<Integer> hpRange = m.getData().getHpRangeAtLevel(m.getLevel());
            Range<Integer> mpRange = m.getData().getMpRangeAtLevel(m.getLevel());
            setValueAt(m.getData().toString(), i, 0);
            setValueAt(m.getLevel(), i, 3);
            setValueAt(m.getXp(), i, 4);
            setValueAt(hpRange.lowerEndpoint() + "-" + hpRange.upperEndpoint(), i, 5);
            setValueAt(mpRange.lowerEndpoint() + "-" + mpRange.upperEndpoint(), i, 6);
            Stats s = m.getStats();
            setValueAt(s.getStrength(), i, 6);
            setValueAt(s.getAgility(), i, 7);
            setValueAt(s.getVitality(), i, 8);
            setValueAt(s.getWillpower(), i, 9);
            setValueAt(s.getWisdom(), i, 10);
        }
    }
    
    public int getStartingLevel(PartyMember member) {
        int index = members.indexOf(member);
        Object value = getValueAt(index, STARTING_LEVEL_COLUMN);
        if(value == null || Integer.valueOf(0).equals(value)) {
            Object xpValue = getValueAt(index, STARTING_XP_COLUMN);
            return member.getData().getLevelForTotalExperience(xpValue == null ? 0 : (Integer) xpValue);
        }
        else {
            return (Integer) value;
        }
    }
    
    public int getStartingXp(PartyMember member) {
        int index = members.indexOf(member);
        Object value = getValueAt(index, STARTING_XP_COLUMN);
        if(value == null || Integer.valueOf(0).equals(value)) {
            Object levelValue = getValueAt(index, STARTING_LEVEL_COLUMN);
            return levelValue == null || ((Integer)levelValue) < member.getData().getStartingLevel() ?  0 : member.getData().getMinimumXpForLevel((Integer) levelValue);
        }
        else {
            return (Integer) value;
        }
    }
    
//    private void updateStartingLevelAndXp() {
//        final int rows = getRowCount();
//        final int startingLevelColumn = 1;
//        final int startingXpColumn = 2;
//        for(int i = 0; i < rows && i < members.size(); i++) {
//            Object levelValue = getValueAt(i, startingLevelColumn);
//            if(levelValue != null && !new Integer(0).equals(levelValue)) {
//                int level = (Integer) levelValue;
//                setValueAt(members.get(i).getData().getMinimumXpForLevel(level), i, startingXpColumn);
//            }
//            else {
//                Object xpValue = getValueAt(i, startingXpColumn);
//                if(xpValue != null && !new Integer(0).equals(xpValue)) {
//                    int xp = (Integer) xpValue;
//                    setValueAt(members.get(i).getData().getLevelForTotalExperience(xp), i, startingLevelColumn);
//                }
//            }
//        }
//    }
    
    private final PropertyChangeListener statChangeListener = (pce) -> {
        populateTable();
    };
    
}
