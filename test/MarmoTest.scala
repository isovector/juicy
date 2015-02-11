import java.io.File
import java.nio.file.{Files,Paths}
import java.security.Permission
import juicy.source._
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class MarmoTest extends FreeSpec with ShouldMatchers {

  def recurseList(f: File): Seq[File] = {
    val mine = f.listFiles
    mine ++ mine.filter(_.isDirectory).flatMap(recurseList)
  }
  def allFilenames(f: File) = {
    recurseList(f)
      .filter(!_.isDirectory)
      .filterNot(_.getName startsWith ".")
      .map(_.toString)
  }

  val stdlib = allFilenames(new File("./stdlib"))

  CompilerTerminate.debug = true
  def failTestDirectory(directory: File) = {
    val dirName = directory.toString
    s"compiler should fail on input from directory $dirName" in {
      try {
        CompilerMain.main(allFilenames(directory).toArray ++ stdlib)
      } catch {
        case CompilerExit(st) => st should be === 42
      }
    }
  }
  def failTestFile(f: File) = {
    val fname = f.toString
    s"compiler should fail on input from file $fname" in {
      try {
        CompilerMain.main(Array(fname) ++ stdlib)
      } catch {
        case CompilerExit(st) => st should be === 42
      }
    }
  }
  def succeedTestDirectory(directory: File) = {
    val dirName = directory.toString
    s"compiler should not fail on input from directory $dirName" in {
      try {
        CompilerMain.main(allFilenames(directory).toArray ++ stdlib)
      } catch {
        case CompilerExit(st) => st should be === 0
      }
    }
  }
  def succeedTestFile(f: File) = {
    val fname = f.toString
    s"compiler should not fail on input from file $fname" in {
      try {
        CompilerMain.main(Array(fname) ++ stdlib)
      } catch {
        case CompilerExit(st) => st should be === 0
      }
    }
  }
  val failPrefix = "Je"
  val successPrefixes = Seq("J1", "J2")

  def shouldSucceed (fname: String) = {
    successPrefixes.map(s => fname.startsWith(s)).exists(b => b)
  }
  def shouldFail (fname: String) = {
    fname startsWith failPrefix
  }

  def recurseTest(f: File): Any = {
    if (f.isDirectory) {
      if (shouldSucceed(f.getName)) {
        succeedTestDirectory(f)
      } else if (shouldFail(f.getName)) {
        failTestDirectory(f)
      } else {
        f.listFiles.foreach(recurseTest)
      }
    } else if (shouldSucceed(f.getName)) {
      succeedTestFile(f)
    } else if (shouldFail(f.getName)) {
      failTestFile(f)
    }
  }
  "MarmoTest" - {
    recurseTest(new File("./marmotest"))
  }
}
