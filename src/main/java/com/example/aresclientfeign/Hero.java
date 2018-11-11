package com.example.aresclientfeign;

import lombok.Data;

@Data
public class Hero {

    private String firstName;
    private String lastName;
    private String codeName;
    private String email;
    private String team;

    public Hero() {}

    public Hero(String firstName, String lastName, String codeName, String email, String team) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.codeName = codeName;
        this.email = email;
        this.team = team;
    }

}