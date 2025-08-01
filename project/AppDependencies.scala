import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.18.0"
  private val hmrcMongoVersion = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"    % "12.7.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"            % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalacheck-1-18"         % "3.2.19.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
