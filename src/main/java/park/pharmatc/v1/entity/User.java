package park.pharmatc.v1.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 약국 이름 (ex: 건강한약국)
    private String pharmacyName;

    // 대표자 이름 (ex: 홍길동)
    private String ownerName;

    // 생년월일 ("850203" 형식)
    private String birthDate;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private boolean emailVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}