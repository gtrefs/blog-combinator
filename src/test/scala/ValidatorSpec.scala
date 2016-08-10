import org.scalatest.{FlatSpec, Matchers}

class ValidatorSpec extends FlatSpec with Matchers {
  import UserValidator._

  case class User(name: String, age: Integer, eMail: String)

  case class UserValidator(v: Validator) {
    def and(other: UserValidator): UserValidator = UserValidator(t => v(t) && other.v(t))
    def or(other: UserValidator): UserValidator = UserValidator(t => v(t) || other.v(t))
    def validate(u: User): Boolean = apply(u)
    def apply(u: User): Boolean = v(u)
  }

  object UserValidator{
    type Validator = User => Boolean

    def nameIsNotEmpty: UserValidator = UserValidator(!_.name.isEmpty)
    def mailContainsAtSign: UserValidator = UserValidator(_.eMail contains "@")
  }

  "Name" should "not be empty" in {
    val validator = nameIsNotEmpty and mailContainsAtSign
    validator validate User("", 30, "test@test.de") should be(false)
  }


  "Mail" should "contain @ sign" in {
    val validator = nameIsNotEmpty and mailContainsAtSign
    validator validate User("Gregor", 30, "testtest.de") should be(false)
  }

  "User with name and mail with @ sign" should "be valid" in {
    val validator = nameIsNotEmpty and mailContainsAtSign
    validator validate User("Gregor", 30, "test@test.de") should be(true)
  }

  "User" should "have no empty name or mail without @ sign" in {
    val validator = nameIsNotEmpty or mailContainsAtSign
    validator validate User("Gregor", 30, "") should be(true)
  }
}
