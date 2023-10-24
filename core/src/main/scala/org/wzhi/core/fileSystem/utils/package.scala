package org.wzhi.core.fileSystem

import java.nio.file.Path

package object utils {
  def folderOfFilePath(path: String): String =
    Path.of(path).getParent.toString
}
