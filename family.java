import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
:import java.sql.ResultSetMetaData;
import java.sql.SQLException;


public class Test {

    public static void printResultSet(ResultSet resultSet) throws SQLException{
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (resultSet.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = resultSet.getString(i);
                System.out.print(columnValue + " " + rsmd.getColumnName(i));
            }
            System.out.println("");
        }
    }

    public static void resetDatabase(Connection c) throws SQLException{
        Statement stmt = c.createStatement();
        String sql = "TRUNCATE TABLE person RESTART IDENTITY CASCADE;";
        stmt.executeUpdate(sql);
    }

    public static void main(String args[]) throws SQLException
    { 
        myConnection myc = new myConnection();
        Connection c = myc.get();
        resetDatabase(c);

        // gen 1
        Person dan = new Person("Dan","1995-11-03","M",c);
        dan.setPerson();

        Person charlotte = new Person("Charlotte","2000-02-18","F",c);
        charlotte.setPerson();

        Person michael = new Person("Michael","1998-10-23","M",c);
        michael.setPerson();

        Person navida = new Person("Navida","1996-05-16","F",c)
        navida.setPerson();
        navida.setHusbandWife(dan);

        dan.setBrother(michael);
        dan.setBrother(charlotte);

        michael.setBrother(dan);
        michael.setBrother(charlotte);

        charlotte.setSister(dan);
        charlotte.setSister(michael);

        // gen 2
        Person claudia = new Person("Claudia","1962-01-06","F",c);
        claudia.setPerson();

        Person peter = new Person("Peter","1970-07-05","M",c);
        peter.setPerson();

        peter.setHusbandWife(claudia);

        dan.setFamily(peter, claudia);
        michael.setFamily(peter,claudia);
        charlotte.setFamily(peter, claudia);
    }                         
}

public class myConnection {
    
    public Connection c;

    public myConnection(){
        this.c = null;
        try {
            Class.forName("org.postgresql.Driver");
            this.c = DriverManager
                .getConnection("jdbc:postgresql://host:port/danielv",
                "username","password");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public Connection get(){
        return this.c;
    }
}

interface SQLFamily {
    ResultSet getPerson() throws SQLException;
    void setPerson() throws SQLException;
    void setFamily(Person father,Person mother) throws SQLException;
    void setBrother(Person brother_of) throws SQLException;
    void setSister(Person sister_of) throws SQLException;
    void setHusbandWife(Person spouse) throws SQLException;
}

public class Person implements SQLFamily{
    private int id;
    private String name;
    private String dob;
    private String gender;
    private Connection c;

    // Set proper id based on autoincrement id field from database
    public static int nextId(Connection c) throws SQLException{
        Statement stmnt = c.createStatement();
        String sql = "SELECT max(id) as id FROM person;";
        ResultSet rs = stmnt.executeQuery(sql);
        int next_id = 0;
        while (rs.next()) {
            String max_id = rs.getString("id");
            if (max_id != null){
                next_id = Integer.parseInt(max_id) + 1;
            }
            else{
                next_id = 1;
            }
        }
        return next_id;
    }

    public Person(String name, String dob, String gender, Connection c) throws SQLException
    {
        this.id = this.nextId(c);
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.c = c;
    }

    public int getidPerson(){
        return this.id;
    }

    public String getnamePerson(){
        return this.name;
    }

    public String getdobPerson(){
        return this.dob;
    }

    public String getgenderPerson(){
        return this.gender;
    }

    public String toString(){
        return "('"+ name+"','"+dob+"','"+gender+"')";
    }

    public ResultSet getPerson() throws SQLException{
        ResultSet rs = null;
        Statement stmt = this.c.createStatement();
        String sql = "SELECT * FROM person WHERE id = " + this.id + ";";
        rs = stmt.executeQuery(sql);
        return rs;

    }

    public void setPerson() throws SQLException{
        Statement stmnt = this.c.createStatement();
        String sql = String.format("INSERT INTO person (name,date_of_birth,gender) VALUES('%s','%s','%s');",this.name,this.dob,this.gender);
        stmnt.executeUpdate(sql);
    }

    public void setFamily(Person father, Person mother) throws SQLException{

        Statement stmnt = this.c.createStatement();
        String sql = String.format("INSERT INTO FAMILY VALUES(%s,%s,%s);",this.id,father.getidPerson(),mother.getidPerson());    
        stmnt.executeUpdate(sql);
    }

    public void setBrother(Person brother_of) throws SQLException{
        Statement stmnt = this.c.createStatement();
        String sql = String.format("INSERT INTO brother VALUES(%s,%s);",this.id,brother_of.getidPerson());
        stmnt.executeUpdate(sql);
    }
    public void setSister(Person sister_of) throws SQLException{
        Statement stmnt = this.c.createStatement();
        String sql = String.format("INSERT INTO sister VALUES(%s,%s);",this.id,sister_of.getidPerson());
        stmnt.executeUpdate(sql);
    }
    public void setHusbandWife(Person spouse) throws SQLException{
        Statement stmnt = this.c.createStatement();
        if(this.gender == "M"){
            String sql = String.format("INSERT INTO husband_wife VALUES(%s,%s);",this.id,spouse.getidPerson());
            stmnt.executeUpdate(sql);
        }else{
            String sql = String.format("INSERT INTO husband_wife VALUES(%s,%s);",spouse.getidPerson(),this.id);
            stmnt.executeUpdate(sql);
        }
    }
}

// Inheritance requirements
// "appropriate constructors and methods"
// executeQuery -- return list of people that are a given ___ of a person
// print list of people that are a given ___ of person (result of executeQuery)

interface FamilyRelationship {
    List<Person> executeQuery() throws SQLException;
    void print() throws SQLException;
}

public class Child extends Person implements FamilyRelationship{

    public Child(Person person){

    }

    public List<Person> executeQuery() throws SQLException{

    }

    public void print() throws SQLException{

    }
}

public class Grandparent extends Person implements FamilyRelationship{

    public Grandparent(Person person){

    }

    public Grandparent(String name, String dob, String gender, Connection c){
        
    }


    public List<Person> executeQuery() throws SQLException{

    }

    public void print() throws SQLException{
        
    }

}

public class SisterInLaw extends Person implements FamilyRelationship{
    public SisterInLaw(Person person){

    }

    public SisterInLaw(String name, String dob, String gender, Connection c){
        
    }


    public List<Person> executeQuery() throws SQLException{

    }

    public void print() throws SQLException{
        
    }
}


