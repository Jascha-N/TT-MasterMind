import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class MMHintSpec extends FlatSpec with ShouldMatchers {

  import Util._

  private def newAnswer(mode: Int = 6) = new MMAnswer(mode).getAnswer
  private def newHint(answer: Array[Char] = newAnswer(), hintsUsed: Int = 0) = new MMHint(answer, hintsUsed)

  private val HintPattern        = "^Hint: There is at least one ([roygbivx]) peg.$".r
  private val NoMoreHintsPattern = "^Hint: Sorry, you have already used all of your hints.$".r

  behavior of "MMHint"

  it should "show only valid hints" ignore {
    repeat(100) {
      val answer = newAnswer()
      val output = catchOutputLine(newHint(answer).getHint)
      val _match = "(.) peg.$".r.findFirstMatchIn(output)
      _match should be ('defined)
      val peg = _match.get.group(1).head
      answer should contain (peg)
    }
  }

//  it should "show all pegs eventually" in {
//    val answer = newAnswer()
//    val hint = newHint(answer)
//  }

  it should "show as many hints as there are pegs" ignore {
    val answer = newAnswer()
    val hint = newHint(answer)
    repeat(answer.length) {
      catchOutputLine(hint.getHint) should fullyMatch regex HintPattern
    }
    catchOutputLine(hint.getHint) should fullyMatch regex NoMoreHintsPattern
  }

  it should "show as many hints as there are pegs (reusing MMHint object)" ignore {
    val answer = newAnswer()

    def hint(n: Int) = newHint(answer, n).getHint

    catchOutputLine(hint(0                )) should fullyMatch regex HintPattern
    catchOutputLine(hint(answer.length - 1)) should fullyMatch regex HintPattern
    catchOutputLine(hint(answer.length    )) should fullyMatch regex NoMoreHintsPattern
  }

  it should "return the number of hints used" ignore {
    val answer = newAnswer()
    val hint = newHint(answer)
    for (hintsUsed <- 1 to answer.length) {
      discardOutput(hint.getHint) should equal (hintsUsed)
    }
  }

}
