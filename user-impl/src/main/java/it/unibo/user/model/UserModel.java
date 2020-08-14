package it.unibo.user.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class UserModel {

    @Id
    private String id;
    private String username;
}
