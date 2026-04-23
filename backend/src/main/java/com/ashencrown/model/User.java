package com.ashencrown.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "correo", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "tiene_partida_guardada")
    private boolean hasSavedGame = false;

    @Column(name = "idioma", length = 5)
    private String language = "es";

    @Column(name = "fecha_creacion")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public Long getId()                    { return id; }
    public String getUsername()            { return username; }
    public void setUsername(String u)      { this.username = u; }
    public String getEmail()               { return email; }
    public void setEmail(String e)         { this.email = e; }
    public String getPasswordHash()        { return passwordHash; }
    public void setPasswordHash(String p)  { this.passwordHash = p; }
    public boolean isHasSavedGame()        { return hasSavedGame; }
    public void setHasSavedGame(boolean b) { this.hasSavedGame = b; }
    public String getLanguage()            { return language; }
    public void setLanguage(String l)      { this.language = l; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
}
