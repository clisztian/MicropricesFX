package hyperdimension.examples;

abstract class Pet {
    String name;
    double age;

    public Pet(String name, double age) {
        this.name = name;
        this.age = age;
    }
}

class Cat extends Pet {
    public Cat(String name, double age) {
        super(name, age);
    }

    @Override
    public String toString() {
        return "Cat{name='" + name + "', age=" + age + '}';
    }
}

class Dog extends Pet {
    public Dog(String name, double age) {
        super(name, age);
    }

    @Override
    public String toString() {
        return "Dog{name='" + name + "', age=" + age + '}';
    }
}