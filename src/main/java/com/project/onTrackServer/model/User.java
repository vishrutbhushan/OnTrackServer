package com.project.onTrackServer.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends AuditBase {
    
    /**
     * Internal database ID - NEVER used in API communication.
     * Used only for database relationships.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * PRIMARY IDENTIFIER: The email address.
     * This is the ONLY identifier used in API communication.
     * Rule: userId ALWAYS = email address
     */
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "access_token", length = 2000)
    private String accessToken;
    
    @Column(name = "fcm_token", length = 1000)
    private String fcmToken;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private UserConfig userConfig;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Platform> platforms;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Category> categories;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders;

}

