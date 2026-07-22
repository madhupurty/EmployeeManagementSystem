package com.ems.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * DEPARTMENT ENTITY - Organizational Unit
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * Represents a department/division within the organization.
 * Departments group employees and have a manager.
 * 
 * RELATIONSHIPS:
 * --------------
 * - Department has ONE manager (Employee)
 * - Department has MANY employees
 * - Employee belongs to ONE department
 * 
 * EXAMPLES:
 * ---------
 * - IT Department (Manager: John Doe)
 * - HR Department (Manager: Jane Smith)
 * - Finance Department (Manager: Bob Wilson)
 * 
 * =============================================================================
 */
@Entity
@Table(
    name = "departments",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_department_name"),
        @UniqueConstraint(columnNames = "code", name = "uk_department_code")
    },
    indexes = {
        @Index(columnList = "name", name = "idx_department_name"),
        @Index(columnList = "code", name = "idx_department_code"),
        @Index(columnList = "active", name = "idx_department_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique department code (e.g., "IT", "HR", "FIN")
     */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    /**
     * Full department name
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Department description
     */
    @Column(length = 500)
    private String description;

    /**
     * Department head/manager.
     * 
     * @ManyToOne: Many departments can reference employees, but each department
     *             has at most one manager.
     * 
     * Note: Using ManyToOne because the manager is an Employee, and one Employee
     * could potentially manage multiple departments (though uncommon).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "id")
    private Employee manager;

    /**
     * Employees belonging to this department.
     * 
     * @OneToMany: One department has many employees
     * mappedBy: Refers to 'departmentEntity' field in Employee entity
     * 
     * CascadeType.ALL would be dangerous here - deleting department
     * would delete all employees! We handle this in service layer.
     */
    @OneToMany(mappedBy = "departmentEntity", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    /**
     * Location/office of the department
     */
    @Column(length = 200)
    private String location;

    /**
     * Contact email for the department
     */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    /**
     * Contact phone for the department
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * Budget allocated to the department
     */
    @Column(name = "budget")
    private Double budget;

    /**
     * Whether the department is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    /**
     * Get the count of employees in this department.
     */
    public int getEmployeeCount() {
        return employees != null ? employees.size() : 0;
    }

    /**
     * Get manager's full name.
     */
    public String getManagerName() {
        return manager != null ? manager.getFullName() : "Not Assigned";
    }

    /**
     * Add an employee to this department.
     */
    public void addEmployee(Employee employee) {
        if (employees == null) {
            employees = new ArrayList<>();
        }
        employees.add(employee);
        employee.setDepartmentEntity(this);
    }

    /**
     * Remove an employee from this department.
     */
    public void removeEmployee(Employee employee) {
        if (employees != null) {
            employees.remove(employee);
            employee.setDepartmentEntity(null);
        }
    }
}
