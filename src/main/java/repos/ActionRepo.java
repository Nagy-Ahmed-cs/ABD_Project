package repos;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import database.MongoDBConnection;
import models.Action;
import models.Case;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActionRepo {


     // hell
    private MongoCollection<Document> collection;

    public ActionRepo() {
        MongoDatabase db = MongoDBConnection.getConnection();
        collection = db.getCollection("actions");
    }

    // Fetch all actions
    public List<Action> findAll() {
        List<Action> actions = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Action a = new Action();
                a.setId(doc.getObjectId("_id"));
                a.setType(doc.getString("action_type"));
                a.setDescription(doc.getString("description"));
                if (doc.getDate("taken_at") != null) {
                    a.setTakenAt(doc.getDate("taken_at").toInstant());
                }
                a.setCaseId(doc.getObjectId("caseId"));
                actions.add(a);
            }
        }
        return actions;
    }
    public List<Action> findByEmployeeId(ObjectId employeeId) {
        List<Action> actions = new ArrayList<>();
        CaseRepo caseRepo = new CaseRepo();

        // Get all cases assigned to this employee
        List<Case> employeeCases = caseRepo.getEmployeeCases(employeeId);

        // For each case, get actions
        for (Case c : employeeCases) {
            actions.addAll(findByCaseId(c.getId()));
        }

        return actions;
    }

    // Insert action
    public void save(Action a) {
        Document doc = new Document("_id", new ObjectId())
                .append("action_type", a.getType())
                .append("description", a.getDescription())
                .append("taken_at", Date.from(a.getTakenAt()))
                .append("caseId", a.getCaseId());
        collection.insertOne(doc);
    }

    // Fetch actions by caseId
    public List<Action> findByCaseId(ObjectId caseId) {
        List<Action> actions = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(new Document("caseId", caseId)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Action a = new Action();
                a.setId(doc.getObjectId("_id"));
                a.setType(doc.getString("action_type"));
                a.setDescription(doc.getString("description"));
                if (doc.getDate("taken_at") != null) {
                    a.setTakenAt(doc.getDate("taken_at").toInstant());
                }
                a.setCaseId(caseId);
                actions.add(a);
            }
        }
        return actions;
    }
}
