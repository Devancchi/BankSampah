/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package component;

/**
 *
 * @author devan
 */
public class UserSession {
    private String nama;
    private String level;

    public UserSession(String nama, String level) {
        this.nama = nama;
        this.level = level;
    }

    public String getNama() {
        return nama;
    }

    public String getLevel() {
        return level;
    }
}

