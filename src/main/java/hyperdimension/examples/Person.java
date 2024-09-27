package hyperdimension.examples;


public class Person {
    String name;
    double age;
    Pet pet;

    public Person(String name, double age, Pet pet) {
        this.name = name;
        this.age = age;
        this.pet = pet;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + ", pet=" + pet + '}';
    }
}
