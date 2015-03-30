name := """juicy"""

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "org.apache.commons" % "commons-lang3" % "3.0"
)

scalaSource in Compile := baseDirectory.value / "src"

scalaSource in Test := baseDirectory.value / "test"

testOptions in Test += Tests.Argument("-oI")

parallelExecution in Test := false

fork in run := true
