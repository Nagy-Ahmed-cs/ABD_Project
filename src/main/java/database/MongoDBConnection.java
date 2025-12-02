package database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
public class MongoDBConnection {

    private static final String URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "case_management_system";

    private static MongoClient client = null;

    public static MongoDatabase getDatabase() {
        if (client == null) {
            client = MongoClients.create(URI);
        }
        return client.getDatabase(DB_NAME);
    }

    public static MongoDatabase getConnection() {
        if (client == null) {
            client = MongoClients.create("mongodb://localhost:27017");
        }
        return client.getDatabase(DB_NAME);
    }
}
