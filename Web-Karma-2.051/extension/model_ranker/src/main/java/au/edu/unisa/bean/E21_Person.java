package au.edu.unisa.bean;

public class E21_Person {

    private int person_id; //primary key
    private String birthDate = "";
    private String deathDate = "";
    private String name = "";
    private String nationality = "";

    public int getPerson_id() {
        return person_id;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getDeathDate() {
        return deathDate;
    }

    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setPerson_id(int person_id) {
        this.person_id = person_id;
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

    public void setName(String name) {
        this.name = name;
    }
}
