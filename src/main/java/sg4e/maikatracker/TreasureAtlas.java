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

import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author sg4e
 */
public class TreasureAtlas extends JPanel {
    
    private final CardLayout cards;
    private final Map<String,Map<String,TreasureMap>> dungeonToFloors = new LinkedHashMap<>();
    private final Map<String,Page> chestIdToPage = new LinkedHashMap<>();
    
    private static final Logger LOG = LogManager.getLogger();
    
    public TreasureAtlas() {
        cards = new CardLayout();
        setLayout(cards);
        setMinimumSize(TreasureMap.MAP_DIMENSIONS);
        setMaximumSize(TreasureMap.MAP_DIMENSIONS);
        setPreferredSize(TreasureMap.MAP_DIMENSIONS);
    }
    
    public void add(TreasureMap map) {
        String dungeon = map.getDungeon();
        String floor = map.getFloor();
        Map<String,TreasureMap> floors = dungeonToFloors.get(dungeon);
        if(floors == null) {
            floors = new HashMap<>();
            dungeonToFloors.put(dungeon, floors);
        }
        floors.put(floor, map);
        Page page = new Page(dungeon, floor);
        add(map, page.toString());
        map.getChests().stream().map(TreasureChest::getId).forEach(id -> chestIdToPage.put(id, page));
    }
    
    public void setChestContents(String chestId, KeyItemMetadata ki) {
        Page page = chestIdToPage.get(chestId);
        getTreasureMap(page).setChestContents(chestId, ki);
    }
    
    public void clearChestContents(String chestId) {
        getTreasureMap(chestIdToPage.get(chestId)).clearChestContents(chestId);
    }
    
    public void showFloor(String dungeon, String floor) {
        MaikaTracker.getTrackerFromChild(this).setMapComboBoxes(dungeon, floor);
        cards.show(this, new Page(dungeon, floor).toString());
    }
    
    public void showChest(String chestId) {
        if(!hasChestId(chestId)) {
            LOG.error("Unknown chestId: {}", chestId);
        }
        else {
            Page p = chestIdToPage.get(chestId);
            showFloor(p.getDungeon(), p.getFloor());
        }
    }
    
    public boolean hasChestId(String chestId) {
        return chestIdToPage.containsKey(chestId);
    }
    
    public List<String> getAllDungeons() {
        return new ArrayList<>(dungeonToFloors.keySet());
    }
    
    public List<String> getFloorsInDungeon(String dungeon) {
        return new ArrayList<>(dungeonToFloors.get(dungeon).keySet());
    }
    
    private TreasureMap getTreasureMap(Page page) {
        return dungeonToFloors.get(page.getDungeon()).get(page.getFloor());
    }
    
    private static class Page {
        private final String dungeon, floor;
        
        public Page(String dungeon, String floor) {
            this.dungeon = dungeon;
            this.floor = floor;
        }

        public String getDungeon() {
            return dungeon;
        }

        public String getFloor() {
            return floor;
        }
        
        @Override
        public String toString() {
            return new StringBuilder(dungeon).append("@").append(floor).toString();
        }
        
    }
    
}
