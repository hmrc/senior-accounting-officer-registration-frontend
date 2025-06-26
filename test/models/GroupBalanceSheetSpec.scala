package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class GroupBalanceSheetSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "GroupBalanceSheet" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(GroupBalanceSheet.values.toSeq)

      forAll(gen) {
        groupBalanceSheet =>

          JsString(groupBalanceSheet.toString).validate[GroupBalanceSheet].asOpt.value mustEqual groupBalanceSheet
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!GroupBalanceSheet.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[GroupBalanceSheet] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(GroupBalanceSheet.values.toSeq)

      forAll(gen) {
        groupBalanceSheet =>

          Json.toJson(groupBalanceSheet) mustEqual JsString(groupBalanceSheet.toString)
      }
    }
  }
}
