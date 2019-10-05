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

import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg4e.ff4stats.fe.KeyItem;

/**
 *
 * @author sg4e
 */
public enum KeyItemMetadata {
    CRYSTAL(KeyItem.CRYSTAL, "1THE", "[KEY] (crystal) Crystal"),
    PASS(KeyItem.PASS, "2", "Pass"),
    HOOK(KeyItem.HOOK, "3", "[KEY] Hook"),
    DARKNESS(KeyItem.DARKNESS, "4", "[KEY] (crystal) Darkness"),
    EARTH(KeyItem.EARTH, "5", "[KEY] (crystal) Earth"),
    TWIN_HARP(KeyItem.TWIN_HARP, "6", "[KEY] (harp) TwinHarp"),
    PACKAGE(KeyItem.PACKAGE, "7", "[KEY] Package"),
    SAND_RUBY(KeyItem.SAND_RUBY, "8", "[KEY] SandRuby"),
    BARON_KEY(KeyItem.BARON_KEY, "9", "[KEY] (key) Baron"),
    MAGMA_KEY(KeyItem.MAGMA_KEY, "10", "[KEY] (key) Magma"),
    TOWER_KEY(KeyItem.TOWER_KEY, "11", "[KEY] (key) Tower"),
    LUCA_KEY(KeyItem.LUCA_KEY, "12", "[KEY] (key) Luca"),
    ADAMANT(KeyItem.ADAMANT, "13", "[KEY] Adamant"),
    LEGEND(KeyItem.LEGEND, "14", "[KEY] (holy sword) Legend"),
    PAN(KeyItem.PAN, "15", "[KEY] Pan"),
    SPOON(KeyItem.SPOON, "16", "[KEY] (dagger) Spoon"),
    RAT_TAIL(KeyItem.RAT_TAIL, "17", "[KEY] (tail) Rat"),
    PINK_TAIL(KeyItem.PINK_TAIL, "18", "[KEY] (tail) Pink");
    
    private final KeyItem ki;
    private ImageIcon gray, color, checked;
    private String imageName;
    private String spoilerEntry;
    
    private static final Logger LOG = LogManager.getLogger();
    
    private KeyItemMetadata(KeyItem ki, String imageId, String spoiler) {
        this.ki = ki;
        spoilerEntry = spoiler;
        imageName = imageId + ki.toString().replaceAll(" ", "");
        String baseUrl = "key-items/%s/FFIVFE-Icons-" + imageName + "-";
        String grayUrl = String.format(baseUrl, "grayscale") + "Gray.png";
        String colorUrl = String.format(baseUrl, "color") + "Color.png";
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            gray = new ImageIcon(ImageIO.read(classLoader.getResourceAsStream(grayUrl)));
            color = new ImageIcon(ImageIO.read(classLoader.getResourceAsStream(colorUrl)));
            checked = BossLabel.CheckMarkIcon(color, 0.25f);
        }
        catch(IOException | IllegalArgumentException ex) {
            LogManager.getLogger().error("Error loading Key Item icons: " + ki.toString(), ex);
        }
    }

    public KeyItem getEnum() {
        return ki;
    }

    public ImageIcon getGrayIcon() {
        return gray;
    }

    public ImageIcon getColorIcon() {
        return color;
    }
    
    public ImageIcon getCheckedIcon() {
        return checked;
    }
    
    public void setDarkness(float darkness) {
        checked = BossLabel.CheckMarkIcon(color, darkness);
    }
    
    public String getImageName() {
        return imageName;
    }
    
    public String getSpoiler() {
        return spoilerEntry;
    }
}
