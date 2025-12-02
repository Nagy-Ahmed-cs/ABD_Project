package models;

import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

public class Case {
    private ObjectId id;
    private String title;
    private String status;
    private String priority;
    private Instant createAt;
    private Instant updateAt;

    // Many-to-Many with Employees
    private List<ObjectId> employeeIds;

    // One-to-Many with Actions
    private List<ObjectId> actionIds;

    // Many-to-One with Client
    private ObjectId clientId;

    public Case() {}

    public Case(String title, String status, String priority, Instant createAt, Instant updateAt,
                List<ObjectId> employeeIds, List<ObjectId> actionIds, ObjectId clientId) {
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.employeeIds = employeeIds;
        this.actionIds = actionIds;
        this.clientId = clientId;
    }


    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Instant getCreateAt() { return createAt; }
    public void setCreateAt(Instant createAt) { this.createAt = createAt; }

    public Instant getUpdateAt() { return updateAt; }
    public void setUpdateAt(Instant updateAt) { this.updateAt = updateAt; }

    public List<ObjectId> getEmployeeIds() { return employeeIds; }
    public void setEmployeeIds(List<ObjectId> employeeIds) { this.employeeIds = employeeIds; }

    public List<ObjectId> getActionIds() { return actionIds; }
    public void setActionIds(List<ObjectId> actionIds) { this.actionIds = actionIds; }

    public ObjectId getClientId() { return clientId; }
    public void setClientId(ObjectId clientId) { this.clientId = clientId; }
}
