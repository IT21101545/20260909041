package com.example.trainingmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trainingmanagement.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
	Optional<Department> findByName(String name);
}
