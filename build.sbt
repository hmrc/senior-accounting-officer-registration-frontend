import play.sbt.routes.RoutesKeys
import sbt.Def
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "senior-accounting-officer-registration-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings) *)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models.*",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat.*",
      "uk.gov.hmrc.govukfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers.*",
      "uk.gov.hmrc.hmrcfrontend.views.config.*",
      "views.ViewUtils.*",
      "models.Mode",
      "controllers.routes.*",
      "viewmodels.govuk.all.*",
      "viewmodels.*"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:cat=deprecation:ws,cat=feature:ws,cat=optimizer:ws,src=target/.*:s"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged          := true,
    pipelineStages           := Seq(digest),
    Assets / pipelineStages  := Seq(concat),
    PlayKeys.playDefaultPort := 10057
  )
  .settings(CodeCoverageSettings.settings *)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")
