package repos;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import database.MongoDBConnection;
import models.Employee;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository {

    private MongoCollection<Document> collection;

    public EmployeeRepository() {
        MongoDatabase db = MongoDBConnection.getConnection();
        collection = db.getCollection("employees");
    }

    // Fetch all employees
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Employee e = new Employee();
                e.setId(doc.getObjectId("_id"));
                e.setName(doc.getString("employee_name"));
                e.setEmail(doc.getString("employee_email"));
                e.setPosition(doc.getString("role"));
                e.setDepartment(doc.getString("department"));
                employees.add(e);
            }
        }
        return employees;
    }

    // Fetch employee by ID
    public Employee findById(ObjectId id) {
        Document doc = collection.find(new Document("_id", id)).first();
        if (doc == null) return null;

        Employee e = new Employee();
        e.setId(doc.getObjectId("_id"));
        e.setName(doc.getString("employee_name"));
        e.setEmail(doc.getString("employee_email"));
        e.setPosition(doc.getString("role"));
        e.setDepartment(doc.getString("department"));
        return e;
    }

    // Fetch employee by email
    public Employee findByEmail(String email) {
        Document doc = collection.find(new Document("employee_email", email)).first();
        if (doc == null) return null;

        Employee e = new Employee();
        e.setId(doc.getObjectId("_id"));
        e.setName(doc.getString("employee_name"));
        e.setEmail(doc.getString("employee_email"));
        e.setPosition(doc.getString("role"));
        e.setDepartment(doc.getString("department"));
        return e;
    }


    // Insert a new employee
    public void save(Employee e) {
        Document doc = new Document("_id", new ObjectId())
                .append("employee_name", e.getName())
                .append("employee_email", e.getEmail())
                .append("role", e.getPosition())
                .append("department", e.getDepartment());
        collection.insertOne(doc);
    }

    // Update employee
    public void update(Employee e) {
        Document updateDoc = new Document("$set", new Document()
                .append("employee_name", e.getName())
                .append("employee_email", e.getEmail())
                .append("role", e.getPosition())
                .append("department", e.getDepartment())
        );
        collection.updateOne(new Document("_id", e.getId()), updateDoc);
    }

    // Delete employee by ID
    public void delete(ObjectId id) {
        collection.deleteOne(new Document("_id", id));
    }

    // Update employee info by ID
    public void updateEmployeeInfo(Employee employee) {
        Document updateDoc = new Document("$set", new Document()
                .append("employee_name", employee.getName())
                .append("employee_email", employee.getEmail())
                .append("role", employee.getPosition())
                .append("department", employee.getDepartment())
        );
        collection.updateOne(new Document("_id", employee.getId()), updateDoc);
    }

}