package de.gtrefs.util.combinator;

import org.junit.Test;

import java.util.function.Function;
import static de.gtrefs.util.combinator.ValidatorWithBooleanShould.UserValidation.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ValidatorWithBooleanShould {
    class User{
        final String name;
        final int age;
        final String email;

        User(String name, int age, String email){
            this.name = name;
            this.age = age;
            this.email = email;
        }
    }

    interface UserValidation extends Function<User, Boolean> {
        static UserValidation nameIsNotEmpty() {
            return user -> !user.name.trim().isEmpty();
        }

        static UserValidation eMailContainsAtSign() {
            return user -> user.email.contains("@");
        }

        default UserValidation and(UserValidation other) {
            return user -> this.apply(user) && other.apply(user);
        }
    }

    @Test
    public void yield_valid_for_combination_with_at_sign_and_user_name(){
        final UserValidation validation = nameIsNotEmpty().and(eMailContainsAtSign());
        final User gregor = new User("Gregor", 30, "mail@mailinator.com");

        assertThat(validation.apply(gregor), is(true));
    }
}
