package br.com.project.music.business.dtos;

import lombok.Data;

@Data
public class GoogleUserInfo {
    private final String id;
    private final String email;
    private final String name;
    private final String picture;

    public GoogleUserInfo(String id, String email, String name, String picture) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
    }
}