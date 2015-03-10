name := """juicy"""

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.6" % "test"

scalaSource in Compile := baseDirectory.value / "src"

scalaSource in Test := baseDirectory.value / "test"

testOptions in Test += Tests.Argument("-oI")



fork in run := true