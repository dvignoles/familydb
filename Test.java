// java -cp /usr/share/java/postgresql-jdbc4.jar Test.java

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class Test {

    public static void main(String args[]) throws SQLException
    { 
        SQLFamily.resetDatabase();
        System.out.println("...Adding persons to person table...\n");

        Person dan = new Person("Dan","1995-11-03","M");
        dan.setPerson();

        Person charlotte = new Person("Charlotte","2000-02-18","F");
        charlotte.setPerson();

        Person michael = new Person("Michael","1998-10-23","M");
        michael.setPerson();

        Person navida = new Person("Navida","1996-05-16","F");
        navida.setPerson();

        Person rafia = new Person("Rafia","1997-01-01","F");
        rafia.setPerson();

        Person rafi = new Person("Rafi","1992-01-01","M");
        rafi.setPerson();

        Person cara = new Person("Cara","1997-02-02","F");
        cara.setPerson();

        Person tiffany = new Person("Tiffany","1998-03-03","F");
        tiffany.setPerson();

        // gen 2
        Person claudia = new Person("Claudia","1962-01-06","F");
        claudia.setPerson();

        Person peter = new Person("Peter","1970-07-05","M");
        peter.setPerson();

        // gen 3
        Person john = new Person("John","1939-01-01","M");
        john.setPerson();

        Person joanna = new Person("Joanna","1941-02-02","F");
        joanna.setPerson();

        Person francis = new Person("Francis","1934-01-01","F");
        francis.setPerson();

        Person douglass = new Person("Douglass", "1931-02-02","M");
        douglass.setPerson();

        // Relationships

        System.out.println("\n...Setting parental relationships in family table...\n");
        dan.setFamily(peter, claudia);
        michael.setFamily(peter,claudia);
        charlotte.setFamily(peter, claudia);
        peter.setFamily(john,joanna);
        claudia.setFamily(douglass,francis);

        System.out.println("\n...Setting brother relationships in brother table...\n");
        dan.setBrother(michael);
        dan.setBrother(charlotte);
        michael.setBrother(dan);
        michael.setBrother(charlotte);
        rafi.setBrother(navida);
        rafi.setBrother(rafia);

        System.out.println("\n...Setting sister relationships in sister table...\n");
        charlotte.setSister(dan);
        charlotte.setSister(michael);
        navida.setSister(rafi);
        navida.setSister(rafia);
        rafia.setSister(navida);
        rafia.setSister(rafi);

        System.out.println("\n...Setting spouse relationships in husband-wife table\n");
        navida.setHusbandWife(dan);
        rafi.setHusbandWife(cara);
        michael.setHusbandWife(tiffany);
        peter.setHusbandWife(claudia);
        john.setHusbandWife(joanna);
        douglass.setHusbandWife(francis);

        // Child Test
        System.out.println("\n...Testing Children class...\n");
        Child peter_child = new Child(peter);
        peter_child.print();

        Child claudia_child = new Child(claudia);
        claudia_child.print();

        Child john_child = new Child(john);
        john_child.print();

        Child joanna_child = new Child(joanna);
        joanna_child.print();

        Child francis_child = new Child(francis);
        francis_child.print();

        Child douglass_child = new Child(douglass);
        douglass_child.print();

        // Grandparents Test
        System.out.println("...Testing Grandparent class...\n");

        Grandparent dan_gp = new Grandparent(dan);
        dan_gp.print();

        Grandparent michael_gp = new Grandparent(michael);
        michael_gp.print();

        Grandparent charlotte_gp = new Grandparent(charlotte);
        charlotte_gp.print();

        // SisterInLaw Test
        System.out.println("...Testing SisterInLaw class...\n");

        SisterInLaw dan_sil = new SisterInLaw(dan);
        dan_sil.print();

        SisterInLaw michael_sil = new SisterInLaw(michael);
        michael_sil.print();

        SisterInLaw charlotte_sil = new SisterInLaw(charlotte);
        charlotte_sil.print();

        SisterInLaw navida_sil = new SisterInLaw(navida);
        navida_sil.print();
    }
    
}

interface SQLFamily {

    public static Connection myConnection(){
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                .getConnection("jdbc:postgresql://host:port/danielv",
                "user","password");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        return c;
    }

    // Truncate all tables
    public static void resetDatabase() throws SQLException{
        Connection c = myConnection();
        Statement stmt = c.createStatement();
        String sql = "TRUNCATE TABLE person RESTART IDENTITY CASCADE;";
        stmt.executeUpdate(sql);
    }

    // Determine what next serial id will be in DB
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

    ResultSet getPerson() throws SQLException;
    void setPerson() throws SQLException;
    void setFamily(Person father,Person mother) throws SQLException;
    void setBrother(Person brother_of) throws SQLException;
    void setSister(Person sister_of) throws SQLException;
    void setHusbandWife(Person spouse) throws SQLException;
}

class Person implements SQLFamily{
    private int id;
    private String name;
    private String dob;
    private String gender;
    private static Connection c = SQLFamily.myConnection();

    // public static void printResultSet(ResultSet resultSet) throws SQLException{
    //     ResultSetMetaData rsmd = resultSet.getMetaData();
    //     int columnsNumber = rsmd.getColumnCount();
    //     while (resultSet.next()) {
    //         for (int i = 1; i <= columnsNumber; i++) {
    //             if (i > 1) System.out.print(",  ");
    //             String columnValue = resultSet.getString(i);
    //             System.out.print(columnValue + " " + rsmd.getColumnName(i));
    //         }
    //         System.out.println("");
    //     }
    //     System.out.println("");
    // }

    public static void printResultSet(ResultSet resultSet) throws SQLException{
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (resultSet.next()) {
            System.out.print("(");
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",");
                String columnValue = resultSet.getString(i);
                System.out.print("'" + columnValue + "'");
            }
            System.out.print(")");
            System.out.println("");
        }
        System.out.println("");
    }

    public Person(String name, String dob, String gender) throws SQLException
    {   
        this.id = SQLFamily.nextId(c);
        this.name = name;
        this.dob = dob;
        this.gender = gender;
    }

    // when person already exists in db, use known id
    public Person(int id, String name, String dob, String gender) throws SQLException
    {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
    }

    public int getidPerson(){
        return this.id;
    }

    public void setidPerson(int id){
        this.id = id;
    }

    public String getnamePerson(){
        return this.name;
    }

    public void setnamePerson(String name){
        this.name = name;
    }

    public String getdobPerson(){
        return this.dob;
    }

    public void setdobPerson(String dob){
        this.dob = dob;
    }

    public String getgenderPerson(){
        return this.gender;
    }

    public void setgenderPerson(String gender){
        this.gender = gender;
    }

    public Connection getconnectionPerson(){
        return this.c;
    }

    public void setconnectionPerson(Connection c){
        this.c = c;
    }

    public String toString(){
        return "('"+ this.id+"','"+this.name+"','"+this.dob+"','"+this.gender+"')";
    }

    public ResultSet getPerson() throws SQLException{
        ResultSet rs = null;
        Statement stmt = this.c.createStatement();
        String sql = "SELECT * FROM person WHERE id = " + this.id + ";";
        rs = stmt.executeQuery(sql);
        return rs;
    }

    public void print() throws SQLException{
        ResultSet rs = this.getPerson();
        printResultSet(rs);
    }

    public void setPerson() throws SQLException{
        Statement stmnt = this.c.createStatement();
        String sql = String.format("INSERT INTO person (name,date_of_birth,gender) VALUES('%s','%s','%s');",this.name,this.dob,this.gender);
        stmnt.executeUpdate(sql);
        System.out.println(this.toString());
    }

    public void setFamily(Person father, Person mother) throws SQLException{

        Statement stmnt = this.c.createStatement();
        String sql = String.format("INSERT INTO FAMILY VALUES(%s,%s,%s);",this.id,father.getidPerson(),mother.getidPerson());    
        stmnt.executeUpdate(sql);

        String p = String.format("%s,%s father,mother of %s",father.getnamePerson(),mother.getnamePerson(),this.getnamePerson());
        System.out.println(p);
    }

    public void setBrother(Person brother_of) throws SQLException{
        Statement stmnt = this.c.createStatement();
        String sql = String.format("INSERT INTO brother VALUES(%s,%s);",this.id,brother_of.getidPerson());
        stmnt.executeUpdate(sql);

        String p = String.format("%s brother of %s",this.getnamePerson(),brother_of.getnamePerson());
        System.out.println(p);
    }
    public void setSister(Person sister_of) throws SQLException{
        Statement stmnt = this.c.createStatement();
        String sql = String.format("INSERT INTO sister VALUES(%s,%s);",this.id,sister_of.getidPerson());
        stmnt.executeUpdate(sql);

        String p = String.format("%s sister of %s",this.getnamePerson(),sister_of.getnamePerson());
        System.out.println(p);
    }
    public void setHusbandWife(Person spouse) throws SQLException{
        Statement stmnt = this.c.createStatement();
        if(this.gender == "M"){
            String sql = String.format("INSERT INTO husband_wife VALUES(%s,%s);",this.id,spouse.getidPerson());
            stmnt.executeUpdate(sql);
            System.out.println(""+spouse.getnamePerson() + " wife of " + this.getnamePerson());

        }else{
            String sql = String.format("INSERT INTO husband_wife VALUES(%s,%s);",spouse.getidPerson(),this.id);
            stmnt.executeUpdate(sql);
            System.out.println(""+spouse.getnamePerson() + " husband of " + this.getnamePerson());
        }
    }
}

// Inheritance requirements
// "appropriate constructors and methods"
// executeQuery -- return list of people that are a given ___ of a person
// print list of people that are a given ___ of person (result of executeQuery)

interface FamilyRelationship {
    ResultSet executeQuery() throws SQLException;
    void print() throws SQLException;
}

class Child extends Person implements FamilyRelationship{

    public Child(String name, String dob, String gender) throws SQLException{
        super(name, dob, gender);
    }

    public Child(Person p) throws SQLException{
        super(p.getidPerson(),p.getnamePerson(),p.getdobPerson(),p.getgenderPerson());
    }

    public ResultSet executeQuery() throws SQLException{
        Statement stmt = this.getconnectionPerson().createStatement();
        int personid = this.getidPerson();
        String sql = String.format("SELECT * FROM person where id in (SELECT person FROM family where father = %d or mother = %d);",personid,personid);
        ResultSet rs = stmt.executeQuery(sql);
        return rs;
    }

    public void print() throws SQLException{
        ResultSet rs = this.executeQuery();

        String p = String.format("Children of %s",this.getnamePerson());
        System.out.println(p);
        Person.printResultSet(rs);
    }
}

class Grandparent extends Person implements FamilyRelationship{

    public Grandparent(String name, String dob, String gender) throws SQLException{
        super(name, dob, gender);
    }

    public Grandparent(Person p) throws SQLException{
        super(p.getidPerson(),p.getnamePerson(),p.getdobPerson(),p.getgenderPerson());
    }


    public ResultSet executeQuery() throws SQLException{
        Statement stmt = this.getconnectionPerson().createStatement();
        int personid = this.getidPerson();
        String sql = String.format("SELECT * FROM person where id in" +
                                    "(SELECT unnest(ARRAY[father,mother]) FROM family where person in" +
                                    "(SELECT unnest(ARRAY[father,mother]::INT[]) FROM family where person = %d));"
                                    ,personid);
        ResultSet rs = stmt.executeQuery(sql);
        return rs;
    }

    public void print() throws SQLException{
        ResultSet rs = this.executeQuery();

        String p = String.format("Grandparents of %s",this.getnamePerson());
        System.out.println(p);
        Person.printResultSet(rs);
    }

}

class SisterInLaw extends Person implements FamilyRelationship{

    public SisterInLaw(String name, String dob, String gender) throws SQLException{
        super(name, dob, gender);
    }

    public SisterInLaw(Person p) throws SQLException{
        super(p.getidPerson(),p.getnamePerson(),p.getdobPerson(),p.getgenderPerson());
    }


    public ResultSet executeQuery() throws SQLException{

        Statement stmt = this.getconnectionPerson().createStatement();
        int personid = this.getidPerson();
        String sql = String.format("WITH all_sis_law as  (" +
        "SELECT * FROM person where id in (SELECT wife from husband_wife where husband in (SELECT id FROM person where id in (SELECT person from brother where brother_of = %d))) " +
        "UNION ALL " +
        "SELECT * FROM person where id in (SELECT person FROM sister where sister_of = get_spouse(%d)) " +
        "UNION ALL " +
        "SELECT * FROM person where id in " +
                                   "(SELECT wife FROM husband_wife where husband in " +
                                                                        "(SELECT id FROM person where id in " +
                                                                                                     "(SELECT person FROM brother where brother_of = get_spouse(%d))))) " +
        "SELECT DISTINCT * FROM all_sis_law;",personid,personid,personid);

        ResultSet rs = stmt.executeQuery(sql);
        return rs;
    }

    public void print() throws SQLException{
        ResultSet rs = this.executeQuery();

        String p = String.format("Sisters in Law of %s",this.getnamePerson());
        System.out.println(p);
        Person.printResultSet(rs);
    }
}


