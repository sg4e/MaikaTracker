/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg4e.maikatracker;

/**
 *
 * @author CaitSith2
 */
public final class Util{

    private Util() { }   

    public static boolean isNullOrEmpty(String s){
        return s==null || s.isEmpty();
    }

    public static boolean isNullOrWhiteSpace(String s){
        return s==null || s.trim().isEmpty();
    }
}
