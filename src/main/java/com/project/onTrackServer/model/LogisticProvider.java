package com.project.onTrackServer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * LogisticProvider entity to represent shipping/logistics providers
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logistic_provider")
public class LogisticProvider extends AuditBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "provider_name", nullable = false)
    private String providerName;
    
    @Column(name = "provider_rating")
    private Double providerRating;
    
    @OneToMany(mappedBy = "logisticProvider", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders;
}
