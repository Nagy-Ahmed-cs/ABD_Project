package repos;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import database.MongoDBConnection;
import models.Case;
import models.Client;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class ClientRepository {

    private MongoCollection<Document> collection;
    private MongoCollection<Document> caseCollection;

    public ClientRepository() {
        MongoDatabase db = MongoDBConnection.getConnection();
        collection = db.getCollection("clients");
        caseCollection = db.getCollection("cases");
    }

    // Fetch all clients
    public List<Client> findAll() {
        List<Client> clients = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Client c = new Client();
                c.setId(doc.getObjectId("_id"));
                c.setName(doc.getString("name"));
                c.setEmail(doc.getString("email"));
                c.setPhone(doc.getString("phone"));
                c.setAddress(( doc.getString("address")));
                clients.add(c);
            }
        }
        return clients;
    }
    public Client findByEmail(String email) {
        Document doc = collection.find(new Document("email", email)).first();
        if (doc == null) return null;

        Client client = new Client();
        client.setId(doc.getObjectId("_id"));
        client.setName(doc.getString("name"));
        client.setEmail(doc.getString("email"));
        client.setPhone(doc.getString("phone"));
        client.setAddress(doc.getString("address"));
        return client;
    }

//    // Fetch client by ID
//    public Client findById(ObjectId id) {
//        Document doc = collection.find(new Document("_id", id)).first();
//        if (doc == null) return null;
//
//        Client c = new Client();
//        c.setId(doc.getObjectId("_id"));
//        c.setName(doc.getString("name"));
//        c.setEmail(doc.getString("email"));
//        c.setPhone(doc.getString("phone"));
//        c.setAddress(doc.get("address", Document.class).toJson());
//        return c;
//    }

    // Insert new client
    public void save(Client client) {
        Document doc = new Document("_id", new ObjectId())
                .append("name", client.getName())
                .append("email", client.getEmail())
                .append("phone", client.getPhone())
                .append("address", client.getAddress());
        collection.insertOne(doc);
    }

    public List<Case> getClientCases(ObjectId clientId) {
        List<Case> cases = new ArrayList<>();
        try (MongoCursor<Document> cursor = caseCollection.find(new Document("clientId", clientId)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Case c = new Case();
                c.setId(doc.getObjectId("_id"));
                c.setTitle(doc.getString("title"));
                c.setStatus(doc.getString("status"));
                c.setPriority(doc.getString("priority"));

                // fix field names
                if(doc.getDate("createAt") != null) {
                    c.setCreateAt(doc.getDate("createAt").toInstant());
                }
                if(doc.getDate("updateAt") != null) {
                    c.setUpdateAt(doc.getDate("updateAt").toInstant());
                }

                // map employeeIds if needed
                cases.add(c);
            }
        }
        return cases;
    }
    public void update(Client client) {
        if (client.getId() == null) {
            throw new IllegalArgumentException("Client ID cannot be null for update.");
        }

        Document updatedDoc = new Document()
                .append("name", client.getName())
                .append("email", client.getEmail())
                .append("phone", client.getPhone())
                .append("address", client.getAddress());

        collection.updateOne(
                new Document("_id", client.getId()),   // Filter by _id
                new Document("$set", updatedDoc)       // Set updated fields
        );
    }
    // Delete a client by ID
    public void delete(ObjectId clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID cannot be null for deletion.");
        }

        collection.deleteOne(new Document("_id", clientId));
    }



}
