/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.runtime.java8

@FunctionalInterface trait JFunction2$mcVJI$sp extends Function2[Any, Any, Any] with Serializable {
  def apply$mcVJI$sp(v1: Long, v2: Int): Unit
  override def apply(v1: Any, v2: Any): Any = {
    apply$mcVJI$sp(scala.runtime.BoxesRunTime.unboxToLong(v1), scala.runtime.BoxesRunTime.unboxToInt(v2))
    scala.runtime.BoxedUnit.UNIT
  }
}
