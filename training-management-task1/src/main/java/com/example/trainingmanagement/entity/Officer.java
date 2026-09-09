package com.example.trainingmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "officers")
public class Officer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String designation;

    private Integer yearsOfService;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    public Officer() {}

    public Officer(String name, String email, Department department) {
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public Officer(String name, String email, Department department,
                   String designation, Integer yearsOfService) {
        this(name, email, department);
        this.designation = designation;
        this.yearsOfService = yearsOfService;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Department getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public Integer getYearsOfService() { return yearsOfService; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setDepartment(Department department) { this.department = department; }
    public void setDesignation(String designation) { this.designation = designation; }
    public void setYearsOfService(Integer yearsOfService) { this.yearsOfService = yearsOfService; }
}
