package hyperdimension.examples;

import hyperdimension.encoders.IntervalEmbedding;
import hyperdimension.encoders.VanillaBHV;
import hyperdimension.encoders.VanillaEmbedding;

import java.util.Arrays;

public class EncoderDecoder {
    private static final VanillaBHV PersonHV = VanillaBHV.randVector();
    private static final VanillaBHV CatHV = VanillaBHV.randVector();
    private static final VanillaBHV DogHV = VanillaBHV.randVector();
    private static final VanillaEmbedding nameEmbed = new VanillaEmbedding();
    private static final IntervalEmbedding ageEmbed = new IntervalEmbedding(0, 100, 200);

    public static VanillaBHV encodePerson(Person p) {
        VanillaBHV nameHV = nameEmbed.forward(p.name).permute(1);
        VanillaBHV ageHV = ageEmbed.forward(p.age).permute(1);

        VanillaBHV petHV;
        if (p.pet instanceof Cat) {
            Cat pet = (Cat) p.pet;
            VanillaBHV catNameHV = nameEmbed.forward(pet.name).permute(2);
            VanillaBHV catAgeHV = ageEmbed.forward(pet.age).permute(2);
            petHV = VanillaBHV.logic_majority(Arrays.asList(CatHV, catNameHV, catAgeHV)).permute(1);
        } else {
            Dog pet = (Dog) p.pet;
            VanillaBHV dogNameHV = nameEmbed.forward(pet.name).permute(3);
            VanillaBHV dogAgeHV = ageEmbed.forward(pet.age).permute(3);
            petHV = VanillaBHV.logic_majority(Arrays.asList(DogHV, dogNameHV, dogAgeHV)).permute(1);
        }

        return VanillaBHV.logic_majority(Arrays.asList(PersonHV, nameHV, ageHV, petHV));
    }

    public static Person decodePerson(VanillaBHV hv) {
        if (!hv.related(PersonHV)) {
            throw new IllegalArgumentException("Hypervector is not related to a person");
        }
        VanillaBHV personHV = hv.permute(-1);
        String name = nameEmbed.back(personHV);
        double age = ageEmbed.back(personHV);

        System.out.println(name + " " + age);
        int distanceCat = personHV.hammingDistance(CatHV);
        int distanceDog = personHV.hammingDistance(DogHV);

        if (distanceCat < distanceDog) {
            VanillaBHV catHV = personHV.permute(-2);
            String catName = nameEmbed.back(catHV);
            double catAge = ageEmbed.back(catHV);
            return new Person(name, age, new Cat(catName, catAge));
        } else if (distanceDog < distanceCat) {
            VanillaBHV dogHV = personHV.permute(-3);
            String dogName = nameEmbed.back(dogHV);
            double dogAge = ageEmbed.back(dogHV);
            return new Person(name, age, new Dog(dogName, dogAge));
        } else {
            throw new IllegalArgumentException("Hypervector is not related to a valid pet");
        }
    }

    public static void main(String[] args) {
        Person joe = new Person("Joe", 16.5, new Dog("Blacky", 1));
        Person mia = new Person("Mia", 61, new Cat("Lucy", 9));

        VanillaBHV joeHV = EncoderDecoder.encodePerson(joe);
        VanillaBHV miaHV = EncoderDecoder.encodePerson(mia);

        Person decodedJoe = EncoderDecoder.decodePerson(joeHV);
        Person decodedMia = EncoderDecoder.decodePerson(miaHV);

        System.out.println(decodedJoe);
        System.out.println(decodedMia);
    }
}