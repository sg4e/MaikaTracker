/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg4e.maikatracker;

import javax.swing.ImageIcon;

/**
 *
 * @author CaitSith2
 */
public final class DemoLabel extends StativeLabel {
    private final ImageIcon greyBoss;
    private final ImageIcon colorBoss;
    private ImageIcon checkedBoss;
    
    public DemoLabel(String imageName, int type, int state) {
        super();
        removeListener();
        if(type == 0) {
            greyBoss = new ImageIcon(MaikaTracker.loadImageResource("bosses/grayscale/FFIVFE-Bosses-" + imageName + "-Gray.png"));
            colorBoss = new ImageIcon(MaikaTracker.loadImageResource("bosses/color/FFIVFE-Bosses-" + imageName + "-Color.png"));
            checkedBoss = BossLabel.CheckMarkIcon(colorBoss, 0.25f);
        }
        else {
            greyBoss = new ImageIcon(MaikaTracker.loadImageResource("key-items/grayscale/FFIVFE-Icons-" + imageName + "-Gray.png"));
            colorBoss = new ImageIcon(MaikaTracker.loadImageResource("key-items/color/FFIVFE-Icons-" + imageName + "-Color.png"));
            checkedBoss = BossLabel.CheckMarkIcon(colorBoss, 0.25f);
        }
        setNewIconState(greyBoss, colorBoss, checkedBoss);
        setState(state);
    }
    
    public void setDarkness(float darkness) {
        int state = getState();
        checkedBoss = BossLabel.CheckMarkIcon(colorBoss, darkness);
        setNewIconState(greyBoss, colorBoss, checkedBoss);
        setState(state);
    }
}
