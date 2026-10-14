package model;

public class Passenger {

    private int passengerId;
    private Integer userId; // Optional link to User account
    private String name;
    private int age;
    private String gender; // MALE, FEMALE, OTHERS
    private String phoneNumber;
    private boolean hasDisability;
    private String disabilityIdNo;

    public Passenger() {}

    public Passenger(int passengerId, String name, int age, String gender, String phoneNumber) {
        this(passengerId, null, name, age, gender, phoneNumber, false, null);
    }

    public Passenger(int passengerId, Integer userId, String name, int age, String gender, String phoneNumber, boolean hasDisability, String disabilityIdNo) {
        this.passengerId = passengerId;
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.hasDisability = hasDisability;
        this.disabilityIdNo = disabilityIdNo;
    }

    public int getPassengerId() { return passengerId; }
    public void setPassengerId(int passengerId) { this.passengerId = passengerId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isHasDisability() { return hasDisability; }
    public void setHasDisability(boolean hasDisability) { this.hasDisability = hasDisability; }

    public String getDisabilityIdNo() { return disabilityIdNo; }
    public void setDisabilityIdNo(String disabilityIdNo) { this.disabilityIdNo = disabilityIdNo; }

    @Override
    public String toString() {
        String disc = hasDisability ? " [Disability Concession Eligible]" : "";
        return passengerId + " - " + name + " (" + gender + ", " + age + "y)" + disc;
    }
}