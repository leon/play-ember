import java.io.File
import org.specs2.mutable._
import se.radley.ember._

case class JSContext(
  firstname: String
)

class EmberCompilerSpec extends Specification {

  "The Ember compiler" should {
    "render" in {
      val template = new File("src/test/resources/hello.handlebars")
      val context = JSContext("leon")
      val output = EmberCompiler.render(template, context)
      output must contain("leon")
    }

    "compile hello template" in {
      val template = new File("src/test/resources/hello.handlebars")
      val js = EmberCompiler.compile(template)
      println(js)
      js must contain("Hello")
    }

    "compile if template" in {
      val template = new File("src/test/resources/if.handlebars")
      val js = EmberCompiler.compile(template)
      println(js)
      js must contain("If")
    }

  }
}