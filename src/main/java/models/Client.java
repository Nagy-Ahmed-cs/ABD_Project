package models;

import org.bson.types.ObjectId;

import java.util.List;

public class Client {
    private ObjectId id;
    private String name;
    private String email;
    private String phone;
    private String address;

    // One-to-Many with Cases: store case IDs
    private List<ObjectId> caseIds;

    public Client() {}

    public Client(String name, String email, String phone, String address, List<ObjectId> caseIds) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.caseIds = caseIds;
    }


    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<ObjectId> getCaseIds() { return caseIds; }
    public void setCaseIds(List<ObjectId> caseIds) { this.caseIds = caseIds; }
}
