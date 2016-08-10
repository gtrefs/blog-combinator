package de.gtrefs.util.combinator;

import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.gtrefs.util.combinator.ValidatorWithResultTypeShould.UserValidation.*;
import static de.gtrefs.util.combinator.ValidatorWithResultTypeShould.ValidationResult.invalid;
import static de.gtrefs.util.combinator.ValidatorWithResultTypeShould.ValidationResult.valid;
import static de.gtrefs.util.combinator.ValidatorWithResultTypeShould.WebValidation.all;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ValidatorWithResultTypeShould {
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

    interface UserValidation extends Function<User, ValidationResult> {
        static UserValidation nameIsNotEmpty() {
            return holds(user -> !user.name.trim().isEmpty(), "Name is empty.");
        }

        static UserValidation eMailContainsAtSign() {
            return holds(user -> user.email.contains("@"), "Missing @-sign in E-Mail.");
        }

        static UserValidation holds(Predicate<User> p, String message){
            return user -> p.test(user) ? valid() : invalid(message);
        }

        default UserValidation and(UserValidation other) {
            return user -> {
                final ValidationResult result = this.apply(user);
                return result.isValid() ? other.apply(user) : result;
            };
        }
    }

    interface WebValidation {
        static UserValidation all(UserValidation... validations){
            return user -> {
                String reasons = Arrays.stream(validations)
                      .map(v -> v.apply(user))
                      .filter(r -> !r.isValid())
                      .map(r -> r.getReason().get())
                      .collect(Collectors.joining("\n"));
                return reasons.isEmpty()?valid():invalid(reasons);
            };
        }
    }

    interface ValidationResult{
        static ValidationResult valid(){
            return ValidationSupport.valid();
        }
        static ValidationResult invalid(String reason){
            return new Invalid(reason);
        }
        boolean isValid();
        Optional<String> getReason();
    }

    private final static class Invalid implements ValidationResult {

        private final String reason;

        Invalid(String reason){
            this.reason = reason;
        }

        public boolean isValid(){
            return false;
        }

        public Optional<String> getReason(){
            return Optional.of(reason);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Invalid invalid = (Invalid) o;
            return Objects.equals(reason, invalid.reason);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reason);
        }
        @Override
        public String toString() {
            return "Invalid[" +
                    "reason='" + reason + '\'' +
                    ']';
        }
    }

    private static final class ValidationSupport {
        private static final ValidationResult valid = new ValidationResult(){
            public boolean isValid(){ return true; }
            public Optional<String> getReason(){ return Optional.empty(); }
        };

        static ValidationResult valid(){
            return valid;
        }
    }

    @Test
    public void yield_valid_for_user_with_non_empty_name_and_mail_with_at_sign(){
        final UserValidation validation = nameIsNotEmpty().and(eMailContainsAtSign());
        final User gregor = new User("Gregor", 30, "mail@mailinator.com");

        assertThat(validation.apply(gregor), is(valid()));
    }

    @Test
    public void yield_invalid_for_user_with_empty_name(){
        final UserValidation validation = nameIsNotEmpty().and(eMailContainsAtSign());
        final User gregor = new User("", 30, "mail@mailinator.com");

        final ValidationResult result = validation.apply(gregor);
        assertThat(result, is(invalid("Name is empty.")));
        result.getReason().ifPresent(System.out::println);
    }

    @Test
    public void yield_invalid_for_user_with_mail_missing_at_sign(){
        final UserValidation validation = nameIsNotEmpty().and(eMailContainsAtSign());
        final User gregor = new User("Gregor", 30, "mailmailinator.com");

        assertThat(validation.apply(gregor), is(invalid("Missing @-sign in E-Mail.")));
    }

    @Test
    public void yield_invalid_with_two_reasons(){
        final UserValidation validation = all(nameIsNotEmpty(), eMailContainsAtSign());
        final User gregor = new User("", 30, "mailmailinator.com");

        assertThat(validation.apply(gregor), is(invalid("Name is empty.\nMissing @-sign in E-Mail.")));
    }
}
