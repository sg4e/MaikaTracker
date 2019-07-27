/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg4e.maikatracker;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CaitSith2
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackerState {
    
    public static class KeyItemState {
        public String name;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String location;
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public boolean collected;
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public boolean used;
    }
    
    public static class BossState {
        public String name;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String location;
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public boolean seen;
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public boolean defeated;
    }
    
    public String version = "1.0";
    public String text_flags;
    public String binary_flags;
    public String seed;
    
    public List<KeyItemState> keyItems = new ArrayList<>();
    public List<String> locationsVisited = new ArrayList<>();
    public List<BossState> bosses = new ArrayList<>();
    public List<String> characters = new ArrayList<>();
    public List<String> openedChests = MaikaTracker.tracker.getAtlas().getOpenedChests();
    public Map<String, List<String>> shopItems = ShopPanel.getCheckedItemsMap();
    
    
    public void addKeyItem(KeyItemPanel panel) {
        KeyItemState state = new KeyItemState();
        state.collected = panel.getState() > 0;
        state.used = panel.getState() > 1;
        state.name = panel.getKeyItem().name();
        state.location = panel.getLocationString();
        keyItems.add(state);
    }
    
    
    
    public void addBoss(BossLabel label, boolean defeated) {
        BossState state = new BossState();
        if(label.getState() > 0) {
            state.seen = true;
            state.defeated = label.getState() > 1 || defeated;
        }
        state.name = label.name();
        BossLabel location = label.getBossLocation();
        if(location != null)
            state.location = location.name();
        bosses.add(state);
    }
}