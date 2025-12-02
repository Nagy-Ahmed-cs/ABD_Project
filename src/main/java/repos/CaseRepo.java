package repos;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import database.MongoDBConnection;
import models.Case;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CaseRepo {

    private final MongoCollection<Document> collection;

    public CaseRepo() {
        MongoDatabase db = MongoDBConnection.getConnection();
        collection = db.getCollection("cases");
    }

    // Fetch all cases
    public List<Case> findAll() {
        List<Case> cases = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                cases.add(documentToCase(doc));
            }
        }
        return cases;
    }
    // Fetch cases for a specific client
    public List<Case> findByClientId(ObjectId clientId) {
        List<Case> cases = new ArrayList<>();

        // Query documents where clientId matches
        Document query = new Document("clientId", clientId);

        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                cases.add(documentToCase(doc));
            }
        }

        return cases;
    }


    // Fetch cases for a specific employee
    public List<Case> getEmployeeCases(ObjectId employeeId) {
        List<Case> cases = new ArrayList<>();

        // Query for documents where employeeIds array contains this employeeId
        Document query = new Document("employeeIds", employeeId); // MongoDB automatically checks arrays

        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                cases.add(documentToCase(doc));
            }
        }

        return cases;
    }


    // Insert case
    public void save(Case c) {
        Document doc = new Document("_id", new ObjectId())
                .append("title", c.getTitle())
                .append("status", c.getStatus())
                .append("priority", c.getPriority())
                .append("createAt", Date.from(c.getCreateAt()))
                .append("updateAt", Date.from(c.getUpdateAt()))
                .append("employeeIds", c.getEmployeeIds())
                .append("clientId", c.getClientId());
        collection.insertOne(doc);
    }

    // Update an existing case
    public void updateCase(Case c) {
        Document updateDoc = new Document("$set", new Document()
                .append("title", c.getTitle())
                .append("status", c.getStatus())
                .append("priority", c.getPriority())
                .append("updateAt", Date.from(c.getUpdateAt()))
                .append("employeeIds", c.getEmployeeIds())
        );

        collection.updateOne(new Document("_id", c.getId()), updateDoc);
    }

    // Helper method to convert Document to Case
    private Case documentToCase(Document doc) {
        Case c = new Case();
        c.setId(doc.getObjectId("_id"));
        c.setTitle(doc.getString("title"));
        c.setStatus(doc.getString("status"));
        c.setPriority(doc.getString("priority"));
        c.setCreateAt(doc.getDate("createAt").toInstant());
        c.setUpdateAt(doc.getDate("updateAt").toInstant());
        c.setEmployeeIds((List<ObjectId>) doc.get("employeeIds"));
        c.setClientId(doc.getObjectId("clientId"));
        return c;
    }
}
