import java.io.File
import org.specs2.mutable._
import se.radley.ember._

case class JSContext(
  firstname: String
)

class EmberCompilerSpec extends Specification {

  "The Ember compiler" should {

    "compile hello template" in {
      val js = EmberCompiler.compile(new File("src/test/resources/hello.handlebars"))
      js must contain("Hello")
    }

    "compile hello template again" in {
      val js = EmberCompiler.compile(new File("src/test/resources/hello.handlebars"))
      js must contain("Hello")
    }

    "compile if template" in {
      val js = EmberCompiler.compile(new File("src/test/resources/if.handlebars"))
      js must contain("rock")
    }

  }
}