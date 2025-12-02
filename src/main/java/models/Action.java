package models;

import org.bson.types.ObjectId;

import java.time.Instant;

public class Action {
    private ObjectId id;
    private String type;
    private String description;
    private Instant takenAt;

    // Many-to-One: Each action belongs to a Case
    private ObjectId caseId;

    public Action() {}

    public Action(String type, String description, Instant takenAt, ObjectId caseId) {
        this.type = type;
        this.description = description;
        this.takenAt = takenAt;
        this.caseId = caseId;
    }


    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getTakenAt() { return takenAt; }
    public void setTakenAt(Instant takenAt) { this.takenAt = takenAt; }

    public ObjectId getCaseId() { return caseId; }
    public void setCaseId(ObjectId caseId) { this.caseId = caseId; }
}
