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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author sg4e
 */
public class TreasureMap extends JPanel {
    private static final int MAP_WIDTH = 512;
    private static final int MAP_HEIGHT = MAP_WIDTH;
    private static final Rectangle BOUNDS = new Rectangle(0, 0, MAP_WIDTH, MAP_HEIGHT);
    public static final Dimension MAP_DIMENSIONS = new Dimension(MAP_WIDTH, MAP_HEIGHT);
    private static final int ROWS = MAP_HEIGHT/TreasureChest.PIXELS_PER_SQUARE;
    private static final int COLUMNS = MAP_WIDTH/TreasureChest.PIXELS_PER_SQUARE;
    private static final boolean MAP_DEV_MODE = false;
    private final Image map;
    private final List<TreasureChest> chests;
    private final ChestLabel[][] cells;
    private final String dungeon, floor;
    
    public TreasureMap(String dungeon, String floor, Image map, TreasureChest... chests) {
        this.dungeon = dungeon;
        this.floor = floor;
        this.map = map;
        setMinimumSize(MAP_DIMENSIONS);
        setMaximumSize(MAP_DIMENSIONS);
        setPreferredSize(MAP_DIMENSIONS);
        setLayout(new GridLayout(ROWS, COLUMNS));
        //initialize cells
        cells = new ChestLabel[ROWS][COLUMNS];
        for(int m = 0; m < ROWS; m++) {
            for(int n = 0; n < COLUMNS; n++) {
               cells[m][n] = new ChestLabel();
               add(cells[m][n]);
            }
        }
        this.chests = Arrays.asList(chests);
        this.chests.forEach(ch -> {
            cells[ch.getX()][ch.getY()].activate(ch);
        });
        if(MAP_DEV_MODE) {
            addMouseListener(new MouseAdapter() {
                int count = 1;
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println(String.format("new TreasureChest(\"A%d\", %d, %d),", count++, 
                            e.getY()/TreasureChest.PIXELS_PER_SQUARE, e.getX()/TreasureChest.PIXELS_PER_SQUARE));
                }
            });
        }
    }
    
    public List<TreasureChest> getChests() {
        return new ArrayList<>(chests);
    }
    
    public ChestLabel setChestContents(String chestId, KeyItemMetadata keyItem) {
        TreasureChest chest = getChest(chestId);
        return cells[chest.getX()][chest.getY()].setKeyItem(keyItem);
    }
    
    public ChestLabel getChestLabel(String chestId) {
        TreasureChest chest = getChest(chestId);
        return cells[chest.getX()][chest.getY()];
    }
    
    public void clearChestContents(String chestId) {
        TreasureChest chest = getChest(chestId);
        cells[chest.getX()][chest.getY()].clearKeyItem();
    }
    
    private TreasureChest getChest(String chestId) {
        return chests.stream().filter(ch -> chestId.equals(ch.getId()))
                .findAny()
                .get();
    }
    
    public void reset() {
        for (TreasureChest ch : chests) {
            cells[ch.getX()][ch.getY()].reset();
        }
    }

    public String getDungeon() {
        return dungeon;
    }

    public String getFloor() {
        return floor;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(map, 0, 0, null);
    }
    
}
