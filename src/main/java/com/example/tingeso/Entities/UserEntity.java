package com.example.tingeso.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;
    private String name;
    private String email;
    private String cellphone;
    private String rol;
    @Column(name = "keycloackid")
    private String keycloackID;

    private String nationality;
    @Column(name = "id_document", nullable = false)
    private String idDocument;
    private String statement;

}
