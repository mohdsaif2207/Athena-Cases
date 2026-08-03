package com.athena.cases.identity.entity;

import com.athena.cases.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "case_types")
public class CaseTypeEntity extends AuditableEntity {

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "module_key", nullable = false, length = 64)
    private String moduleKey;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "case_type_initiating_teams",
            joinColumns = @JoinColumn(name = "case_type_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<TeamEntity> initiatingTeams = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "case_type_receiving_teams",
            joinColumns = @JoinColumn(name = "case_type_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<TeamEntity> receivingTeams = new HashSet<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<TeamEntity> getInitiatingTeams() {
        return initiatingTeams;
    }

    public void setInitiatingTeams(Set<TeamEntity> initiatingTeams) {
        this.initiatingTeams = initiatingTeams;
    }

    public Set<TeamEntity> getReceivingTeams() {
        return receivingTeams;
    }

    public void setReceivingTeams(Set<TeamEntity> receivingTeams) {
        this.receivingTeams = receivingTeams;
    }
}
