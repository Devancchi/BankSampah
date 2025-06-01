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
    private int id;
    private String nama;
    private String level;

    public UserSession(int id, String nama, String level) {
        this.id = id;
        this.nama = nama;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getLevel() {
        return level;
    }

}
