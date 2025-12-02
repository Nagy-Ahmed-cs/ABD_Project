package models;

import org.bson.types.ObjectId;

import java.util.List;

public class Employee {
    private ObjectId id; // MongoDB unique ID
    private String name;
    private String email;
    private String position;

    private String department;

    // Many-to-Many with Cases: store case IDs
    private List<ObjectId> caseIds;

    public Employee() {}

    public Employee(String name, String email, String position, List<ObjectId> caseIds,String department) {
        this.name = name;
        this.email = email;
        this.position = position;
        this.caseIds = caseIds;
        this.department=department;
    }


    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public List<ObjectId> getCaseIds() { return caseIds; }
    public void setCaseIds(List<ObjectId> caseIds) { this.caseIds = caseIds; }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
