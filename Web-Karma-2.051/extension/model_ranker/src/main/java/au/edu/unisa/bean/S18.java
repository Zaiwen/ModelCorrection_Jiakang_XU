package au.edu.unisa.bean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class S18 {

    private String title = "";
    private String artistName = "";
    private String nationality = "";
    private String birthDate = "";
    private String deathDate = "";


    public String getArtistName() {
        return artistName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getDeathDate() {
        return deathDate;
    }

    public String getNationality() {
        return nationality;
    }

    public String getTitle() {
        return title;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    /**ingest DS #18**/
    public static void ingestDS18 () {

        try {
            String title = "";
            String artist_name = "";
            String nationality = "";
            String birthDeath = "";
            String birthYear = "";
            String deathYear = "";

            File inputFile = new File ("/Users/hejie/Documents/Data_Multiple_Schema_Matching/Karma-data-set/RDF-Graph@5May19/" +
                    "museum-crm/sources/s18-s-indianapolis-artists.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("artist");
            List<S18> dataList = new ArrayList<>();
            System.out.println("--------------------------");

            for (int temp = 0 ; temp < nList.getLength(); temp++ ) {

                Node nNode = nList.item(temp);
                System.out.println("\nCurrent Element : " + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement =  (Element) nNode;
                    System.out.println("title of art is: " + eElement.getAttribute("title"));
                    title = eElement.getAttribute("title");

                    if (eElement.getElementsByTagName("name").item(0) != null) {
                        System.out.println("name : " + eElement.getElementsByTagName("name").item(0).getTextContent());
                        artist_name = eElement.getElementsByTagName("name").item(0).getTextContent();
                    } else {
                        System.out.println("name : ");
                        artist_name = "";
                    }

                    if (eElement.getElementsByTagName("nationality").item(0) != null) {
                        System.out.println("nationality : " + eElement.getElementsByTagName("nationality").item(0).getTextContent());
                        nationality = eElement.getElementsByTagName("nationality").item(0).getTextContent();
                    } else {
                        System.out.println("nationality : ");
                        nationality = "";
                    }

                    if (eElement.getElementsByTagName("birthDeath").item(0) != null) {
                        System.out.println("birthDeath : " + eElement.getElementsByTagName("birthDeath").item(0).getTextContent());
                        birthDeath = eElement.getElementsByTagName("birthDeath").item(0).getTextContent();
                        birthYear = birthDeath.substring(0,4);
                        if (birthDeath.length() == 9) {
                            deathYear = birthDeath.substring(5,9);
                        } else {
                            deathYear = "";
                        }

                        System.out.println("birth Year is: " + birthYear);
                        System.out.println("death year is: " + deathYear);
                    } else {
                        System.out.println("birthDeath : ");
                        birthDeath = "";
                        birthYear = "";
                        deathYear = "";
                        System.out.println("birth Year is: " + birthYear);
                        System.out.println("death year is: " + deathYear);
                    }

                    /**build java bean**/
                    S18 s18 = new S18();
                    s18.setTitle(title);
                    s18.setArtistName(artist_name);
                    s18.setNationality(nationality);
                    s18.setBirthDate(birthYear);
                    s18.setDeathDate(deathYear);
                    dataList.add(s18);
                }
            }

            System.out.println("# arts is: " + nList.getLength());


            /**Now begin to generate a unique person id for every person **/
            List<E21_Person> personList = new ArrayList<>();

            int person_id = 0;
            Iterator<S18> it = dataList.iterator();
            while (it.hasNext()) {
                S18 s18 = it.next();

                String artistName = s18.getArtistName();
                String artistBirthDate = s18.getBirthDate();
                String artistDeathDate = s18.getDeathDate();
                String nationalityOfArtist = s18.getNationality();

                /**search for all of the elements in the current 'personList'**/
                boolean isExist = false; //true - 'personList' has already contained this element. 'false' - hasn't contained this element yet
                for (E21_Person person : personList) {
                    String artistName$ = person.getName();
                    String artistBirthDate$ = person.getBirthDate();

                    if (artistName.equals(artistName$) && artistBirthDate.equals(artistBirthDate$)) {
                        isExist = true;
                        break;   /**it means that there exist same artist already**/
                    } else if (artistName.equals(artistName$) && artistBirthDate.equals("")) {
                        isExist = true;
                        break;    /**it means that there exist the same artist already**/
                    }
                }

                if (isExist) {
                    continue;
                } else {
                    /**assign a new identifier**/
                    E21_Person newArtist = new E21_Person();
                    newArtist.setPerson_id(person_id);
                    person_id++;
                    newArtist.setBirthDate(artistBirthDate);
                    newArtist.setDeathDate(artistDeathDate);
                    newArtist.setNationality(nationalityOfArtist);
                    newArtist.setName(artistName);
                    personList.add(newArtist);
                }
            }

            /**remove the empty object of 'personList'**/
            Iterator<E21_Person> iterator2 = personList.iterator();
            while (iterator2.hasNext()) {
                E21_Person e21_person = iterator2.next();
                if (e21_person.getName().equals("")) {
                    iterator2.remove();
                }
            }

            System.out.println("there are " + personList.size() + " artist in DS18");

            /**begin to ingest into MySQL. Create a mysql database connection**/
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = "jdbc:mysql:// localhost:3306/";
            Class.forName(myDriver);
            String dbName = "Museum?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            String dbUserName = "root";
            String dbPassword = "123456Whu";
            Connection conn = DriverManager.getConnection(myUrl + dbName, dbUserName, dbPassword);


            PreparedStatement st = conn.prepareStatement("delete from s18");//firstly, delete all the existing rows
            st.executeUpdate();


            int id = 1; //id--primary key

            Iterator<S18> iterator = dataList.iterator();
            while (iterator.hasNext()) {
                S18 insertedData = iterator.next();
                String title1 = insertedData.getTitle();
                String artistName1 = insertedData.getArtistName();
                String nationality1 = insertedData.getNationality();
                String birthDate1 = insertedData.getBirthDate();
                String deathDate1 = insertedData.getDeathDate();

                /**the mysql insert statement**/
                String query = "insert into s18 (id, title, artist_name, birthDate, deathDate, nationality)" +
                        " values (?, ?, ?, ?, ?, ?)";

                /**create mysql insert prepared statement**/
                PreparedStatement preparedStatement = conn.prepareStatement(query);
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, title1);
                preparedStatement.setString(3, artistName1);
                preparedStatement.setString(4, birthDate1);
                preparedStatement.setString(5, deathDate1);
                preparedStatement.setString(6, nationality1);

                /**execute the prepared statement**/
                preparedStatement.execute();

                id++;
            }

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    public static void main (String args []) {

        ingestDS18();


    }


}
