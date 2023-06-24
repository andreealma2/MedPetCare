package com.example.petcareapp.model;

public class Pet {
    private String name;
    private String type;
    private String petId;
    private String breed;
    private String dateOfBirth;
    private String weight;
    private String sterilized;

    // Firebase Realtime Database are nevoie de constructor gol
    public Pet() { }

    public Pet(String name, String type, String breed, String dateOfBirth, String weight, String sterilized) {
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.dateOfBirth = dateOfBirth;
        this.weight = weight;
        this.sterilized = sterilized;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setType(String type){this.type = type;}

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getName() {
        return name;
    }
    public String getType() {return type;}

    public String getBreed() {
        return breed;
    }

    @Override
    public String toString() {
        return name + " - " + type + " - " + breed;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getSterilized() {
        return sterilized;
    }

    public void setSterilized(String sterilized) {
        this.sterilized = sterilized;
    }
}
