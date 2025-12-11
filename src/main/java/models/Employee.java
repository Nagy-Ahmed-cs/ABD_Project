package models;

import org.bson.types.ObjectId;

import java.util.List;

public class Employee {
    private ObjectId id; // MongoDB unique ID
    private String employee_name;
    private String employee_email;
    private String role;

    private String department;

    // Many-to-Many with Cases: store case IDs
    private List<ObjectId> caseIds;

    public Employee() {}

    public Employee(String name, String email, String position, List<ObjectId> caseIds,String department) {
        this.employee_name = name;
        this.employee_email = email;
        this.role = position;
        this.caseIds = caseIds;
        this.department=department;
    }


    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getName() { return employee_name; }
    public void setName(String name) { this.employee_name = name; }

    public String getEmail() { return employee_email; }
    public void setEmail(String email) { this.employee_email = email; }

    public String getPosition() { return role; }
    public void setPosition(String position) { this.role = position; }

    public List<ObjectId> getCaseIds() { return caseIds; }
    public void setCaseIds(List<ObjectId> caseIds) { this.caseIds = caseIds; }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}