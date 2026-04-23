package com.ashencrown.dto;

public class AuthResponseDto {

    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private boolean hasSavedGame;
    private String language;

    private AuthResponseDto() {}

    public static AuthResponseDto success(Long userId, String username,
                                           boolean hasSavedGame, String language,
                                           String message) {
        AuthResponseDto r = new AuthResponseDto();
        r.success      = true;
        r.message      = message;
        r.userId       = userId;
        r.username     = username;
        r.hasSavedGame = hasSavedGame;
        r.language     = language;
        return r;
    }

    public static AuthResponseDto error(String message) {
        AuthResponseDto r = new AuthResponseDto();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess()      { return success; }
    public String getMessage()      { return message; }
    public Long getUserId()         { return userId; }
    public String getUsername()     { return username; }
    public boolean isHasSavedGame() { return hasSavedGame; }
    public String getLanguage()     { return language; }
}
