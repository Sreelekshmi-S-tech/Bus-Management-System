package model;

public class Passenger {

    // Private variables (Encapsulation)
    private int passengerId;
    private String name;
    private int age;
    private String gender;
    private String phoneNumber;


    // Constructor
    public Passenger(int passengerId, String name, int age, String gender, String phoneNumber) {

        this.passengerId = passengerId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }


    // Getter methods
    public int getPassengerId() {
        return passengerId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    // Setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    // Display passenger details
    public String toString() {

        return "ID: " + passengerId +
                "\nName: " + name +
                "\nAge: " + age +
                "\nGender: " + gender +
                "\nPhone: " + phoneNumber;
    }
}