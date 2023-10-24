package org.wzhi.core.fileSystem.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class packageTest extends AnyFlatSpec {
  "folderOfFilePath" should "get parent path of file path" in {
    folderOfFilePath("folder/fsdfsd.csv") shouldBe "folder"
  }
}
