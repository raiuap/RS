package org.example.restlye1.Domain;

import jakarta.persistence.Entity;

@Entity
public class User {
    private String id;
    private String name;
    private String email;
    private String password;
}
