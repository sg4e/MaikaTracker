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
import java.util.Arrays;
import javax.swing.JPanel;

/**
 *
 * @author sg4e
 */
public class TreasureMap extends JPanel {
    private static final int MAP_WIDTH = 512;
    private static final int MAP_HEIGHT = MAP_WIDTH;
    private static final Rectangle BOUNDS = new Rectangle(0, 0, MAP_WIDTH, MAP_HEIGHT);
    private static final Dimension MAP_DIMENSIONS = new Dimension(MAP_WIDTH, MAP_HEIGHT);
    private static final int ROWS = MAP_HEIGHT/TreasureChest.PIXELS_PER_SQUARE;
    private static final int COLUMNS = MAP_WIDTH/TreasureChest.PIXELS_PER_SQUARE;
    private final Image map;
    
    public TreasureMap(Image map, TreasureChest... chests) {
        this.map = map;
        setMinimumSize(MAP_DIMENSIONS);
        setMaximumSize(MAP_DIMENSIONS);
        setPreferredSize(MAP_DIMENSIONS);
        setLayout(new GridLayout(ROWS, COLUMNS));
        //initialize cells
        ChestLabel[][] cells = new ChestLabel[ROWS][COLUMNS];
        for(int m = 0; m < ROWS; m++) {
            for(int n = 0; n < COLUMNS; n++) {
               cells[m][n] = new ChestLabel();
               add(cells[m][n]);
            }
        }
        Arrays.stream(chests).forEach(ch -> {
            cells[ch.getX()][ch.getY()].activate();
        });
        //add(overlay, 0);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(map, 0, 0, null);
    }
}
