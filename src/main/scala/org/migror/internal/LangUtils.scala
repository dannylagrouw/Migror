package org.migror.internal

object LangUtils {
  
  def withDo[A <: Any, B](a: A)(f: A => B): B = {
    f(a)
  }

  def using[Closeable <: {def close(): Unit}, B](closeable: Closeable)(f: Closeable => B): B =
    try {
      f(closeable)
    } finally {
      closeable.close()
    }
  
  def catchToBoolean(f: => Unit): Boolean = {
    try {
      f
      true
    } catch {
      case e: Exception => println(e); false
    }
  }

  def nullOr[A <: AnyRef](o: A, defaultValue: A): A = if (o == null) defaultValue else o

  def addShutdownHook(block: => Unit) {
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run {
        block
      }
    })
  }

}