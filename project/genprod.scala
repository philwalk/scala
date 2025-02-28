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

/**
 * This program generates the ProductN, TupleN, FunctionN, and AbstractFunctionN,
 * where 0 <= N <= MaxArity. Usage: sbt generateSources
 */
object genprod {
  final val MaxArity = 22
  def arities = (1 to MaxArity).toList

  class Group(val name: String) {
    def className(i: Int) = name + i
    def fileName(i: Int) = className(i) + ".scala"
  }

  def productFiles  = arities map Product.make
  def tupleFiles    = arities map Tuple.make
  def functionFiles = (0 :: arities) map Function.make
  def absFunctionFiles = (0 :: arities) map AbstractFunction.make
  def allfiles      = productFiles ::: tupleFiles ::: functionFiles ::: absFunctionFiles

  trait Arity extends Group {
    def i: Int    // arity

    def typeArgsString(xs: Seq[String]) = xs.mkString("[", ", ", "]")
    def typeArgsToTupleSyntacticSugarString(xs: Seq[String]) = xs.mkString("(", ", ", ")")

    def to              = (1 to i).toList
    def s               = if (i == 1) "" else "s"
    def className       = name + i
    def classAnnotation = ""
    def fileName        = className + ".scala"
    def targs           = to map ("T" + _)
    def vdefs           = to map ("v" + _)
    def xdefs           = to map ("x" + _)
    def mdefs           = to map ("_" + _)
    def invariantArgs   = typeArgsString(targs)
    def covariantArgs   = typeArgsString(targs map (covariantSpecs + "+" + _))
    def covariantSpecs  = ""
    def contravariantSpecs = ""
    def contraCoArgs    = typeArgsString((targs map (contravariantSpecs + "-" + _)) ::: List(covariantSpecs + "+R"))
    def constructorArgs = (targs).map( _.toLowerCase ) mkString ", "
    def fields          = (mdefs, targs).zipped.map(_ + ": " + _) mkString ", "
    def funArgs         = (vdefs, targs).zipped.map(_ + ": " + _) mkString ", "

    def genprodString       = " See scala.Function0 for timestamp."
    def moreMethods         = ""
    def companionObject     = ""
    def packageDef          = "scala"
    def imports             = ""

    def header = """/*
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

// GENERATED CODE: DO NOT EDIT.%s

package %s
%s
""".trim.format(genprodString, packageDef, imports)
  }

  def run(outDir: java.io.File): Unit = {
    val out = outDir.getAbsolutePath
    def writeFile(node: scala.xml.Node): Unit = {
      import scala.tools.nsc.io._
      val f = Path(out) / node.attributes("name").toString
      f.parent.createDirectory(force = true)
      f.toFile writeAll node.text
    }
    allfiles foreach writeFile
  }
}

import genprod._


/* zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz
                             F U N C T I O N
zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz */

object FunctionZero extends Function(0) {
  override def genprodString  = "\n// genprod generated these sources at: " + java.time.Instant.now()
  override def covariantSpecs = "@specialized(Specializable.Primitives) "
  override def descriptiveComment = "  " + functionNTemplate.format("greeting", "anonfun0",
raw"""
 *    val name = "world"
 *    val greeting = () => s"hello, $$name"
 *
 *    val anonfun0 = new Function0[String] {
 *      def apply(): String = s"hello, $$name"
 *    }
 *    assert(greeting() == anonfun0())
 * """)
  override def moreMethods = ""
}

object FunctionOne extends Function(1) {
  override def classAnnotation    = "@annotation.implicitNotFound(msg = \"No implicit view available from ${T1} => ${R}.\")\n"
  override def contravariantSpecs = "@specialized(Specializable.Arg) "
  override def covariantSpecs     = "@specialized(Specializable.Return) "

  override def descriptiveComment = "  " + functionNTemplate.format("succ", "anonfun1",
"""
 *    val succ = (x: Int) => x + 1
 *    val anonfun1 = new Function1[Int, Int] {
 *      def apply(x: Int): Int = x + 1
 *    }
 *    assert(succ(0) == anonfun1(0))
 * """) + """
 *
 *  Note that the difference between `Function1` and [[scala.PartialFunction]]
 *  is that the latter can specify inputs which it will not handle."""

  override def moreMethods = """
  /** Composes two instances of Function1 in a new Function1, with this function applied last.
   *
   *  @tparam   A   the type to which function `g` can be applied
   *  @param    g   a function A => T1
   *  @return       a new function `f` such that `f(x) == apply(g(x))`
   */
  @annotation.unspecialized def compose[A](g: A => T1): A => R = { x => apply(g(x)) }

  /** Composes two instances of Function1 in a new Function1, with this function applied first.
   *
   *  @tparam   A   the result type of function `g`
   *  @param    g   a function R => A
   *  @return       a new function `f` such that `f(x) == g(apply(x))`
   */
  @annotation.unspecialized def andThen[A](g: R => A): T1 => A = { x => g(apply(x)) }
"""
  override def companionObject =
"""
object Function1 {

  implicit final class UnliftOps[A, B] private[Function1](private val f: A => Option[B]) extends AnyVal {
    /** Converts an optional function to a partial function.
      *
      * @example Unlike [[Function.unlift]], this [[UnliftOps.unlift]] method can be used in extractors.
      *          {{{
      *          val of: Int => Option[String] = { i =>
      *            if (i == 2) {
      *              Some("matched by an optional function")
      *            } else {
      *              None
      *            }
      *          }
      *
      *          util.Random.nextInt(4) match {
      *            case of.unlift(m) => // Convert an optional function to a pattern
      *              println(m)
      *            case _ =>
      *              println("Not matched")
      *          }
      *          }}}
      */
    def unlift: PartialFunction[A, B] = Function.unlift(f)
  }

}
"""
}

object FunctionTwo extends Function(2) {
  override def contravariantSpecs = "@specialized(Specializable.Args) "
  override def covariantSpecs = "@specialized(Specializable.Return) "

  override def descriptiveComment = "  " + functionNTemplate.format("max", "anonfun2",
"""
 *    val max = (x: Int, y: Int) => if (x < y) y else x
 *
 *    val anonfun2 = new Function2[Int, Int, Int] {
 *      def apply(x: Int, y: Int): Int = if (x < y) y else x
 *    }
 *    assert(max(0, 1) == anonfun2(0, 1))
 * """)
}

object Function {
  def make(i: Int) = apply(i)()
  def apply(i: Int) = i match {
    case 0    => FunctionZero
    case 1    => FunctionOne
    case 2    => FunctionTwo
    case _    => new Function(i)
  }
}

class Function(val i: Int) extends Group("Function") with Arity {
  def descriptiveComment  = ""
  def functionNTemplate =
"""
 *  In the following example, the definition of `%s` is
 *  shorthand, conceptually, for the anonymous class definition
 *  `%s`, although the implementation details of how the
 *  function value is constructed may differ:
 *
 *  {{{
 *  object Main extends App {%s}
 *  }}}"""

  def toStr = "\"" + ("<function%d>" format i) + "\""
  def apply() = {
<file name={fileName}>{header}
{companionObject}
/** A function of {i} parameter{s}.
 *{descriptiveComment}
 */
{classAnnotation}trait {className}{contraCoArgs} extends AnyRef {{ self =>
  /** Apply the body of this function to the argument{s}.
   *  @return   the result of function application.
   */
  def apply({funArgs}): R
{moreMethods}
  override def toString(): String = {toStr}
}}
</file>
}

  private def commaXs = xdefs.mkString("(", ", ", ")")

  // (x1: T1) => (x2: T2) => (x3: T3) => (x4: T4) => apply(x1,x2,x3,x4)
  def shortCurry = {
    val body = "apply" + commaXs
    (xdefs, targs).zipped.map("(%s: %s) => ".format(_, _)).mkString("", "", body)
  }

  // (x1: T1) => ((x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7) => self.apply(x1,x2,x3,x4,x5,x6,x7)).curried
  def longCurry = ((xdefs, targs).zipped.map(_ + ": " + _) drop 1).mkString(
    "(x1: T1) => ((",
    ", ",
    ") => self.apply%s).curried".format(commaXs)
  )

  // f(x1,x2,x3,x4,x5,x6)  == (f.curried)(x1)(x2)(x3)(x4)(x5)(x6)
  def curryComment = {
"""  /** Creates a curried version of this function.
   *
   *  @return   a function `f` such that `f%s == apply%s`
   */""".format(xdefs.map("(" + _ + ")").mkString, commaXs)
  }

  def tupleMethod = {
    def comment =
"""  /** Creates a tupled version of this function: instead of %d arguments,
   *  it accepts a single [[scala.Tuple%d]] argument.
   *
   *  @return   a function `f` such that `f(%s) == f(Tuple%d%s) == apply%s`
   */
""".format(i, i, commaXs, i, commaXs, commaXs)
    def body = "case (%s) => apply%s".format(commaXs, commaXs)

    comment + "\n  @annotation.unspecialized def tupled: (%s) => R = {\n    %s\n  }".format(
      typeArgsToTupleSyntacticSugarString(targs), body)
  }

  def curryMethod = {
    val body = if (i < 5) shortCurry else longCurry

    curryComment +
    "\n  @annotation.unspecialized def curried: %s => R = {\n    %s\n  }\n".format(
      targs mkString " => ", body
    )
  }

  override def moreMethods = curryMethod + tupleMethod
} // object Function


/* zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz
                                     T U P L E
zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz */

object Tuple {
  val zipImports = ""

  def make(i: Int) = apply(i)()
  def apply(i: Int) = i match {
    case 1  => TupleOne
    case 2  => TupleTwo
    case 3  => TupleThree
    case _  => new Tuple(i)
  }
}

object TupleOne extends Tuple(1)
{
  override def covariantSpecs = "@specialized(Int, Long, Double) "
}

object TupleTwo extends Tuple(2)
{
  override def imports = Tuple.zipImports
  override def covariantSpecs = "@specialized(Int, Long, Double, Char, Boolean/*, AnyRef*/) "
  override def moreMethods = """
  /** Swaps the elements of this `Tuple`.
   * @return a new Tuple where the first element is the second element of this Tuple and the
   * second element is the first element of this Tuple.
   */
  def swap: Tuple2[T2,T1] = Tuple2(_2, _1)
"""
}

object TupleThree extends Tuple(3) {
  override def imports = Tuple.zipImports
}

class Tuple(val i: Int) extends Group("Tuple") with Arity {
  private def idiomatic =
    if (i < 2) ""
    else " Note that it is more idiomatic to create a %s via `(%s)`".format(className, constructorArgs)

  private def params = (
    1 to i map (x => " *  @param  _%d   Element %d of this Tuple%d".format(x, x, i))
  ) mkString "\n"

  // prettifies it a little if it's overlong
  def mkToString() = {
  def str(xs: List[String]) = xs.mkString(""" + "," + """)
    if (i <= MaxArity / 2) str(mdefs)
    else {
      val s1 = str(mdefs take (i / 2))
      val s2 = str(mdefs drop (i / 2))
      s1 + " +\n    \",\" + " + s2
    }
  }

  def apply() = {
<file name={fileName}>{header}

/** A tuple of {i} elements; the canonical representation of a [[scala.{Product.className(i)}]].
 *
 *  @constructor  Create a new tuple with {i} elements.{idiomatic}
{params}
 */
final case class {className}{covariantArgs}({fields})
  extends {Product.className(i)}{invariantArgs}
{{
  override def toString(): String = "(" + {mkToString} + ")"
  {moreMethods}
}}
</file>}
} // object Tuple


/* zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz
                                  P R O D U C T
zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz */

object Product extends Group("Product")
{
  def make(i: Int) = apply(i)()
  def apply(i: Int) = i match {
    case 1  => ProductOne
    case 2  => ProductTwo
    case _ => new Product(i)
  }
}

object ProductOne extends Product(1)
{
  override def covariantSpecs = "@specialized(Int, Long, Double) "
}

object ProductTwo extends Product(2)
{
  override def covariantSpecs = "@specialized(Int, Long, Double) "
}

class Product(val i: Int) extends Group("Product") with Arity {
  val productElementComment = s"""
  /** Returns the n-th projection of this product if 0 <= n < productArity,
   *  otherwise throws an `IndexOutOfBoundsException`.
   *
   *  @param n number of the projection to be returned
   *  @return  same as `._(n+1)`, for example `productElement(0)` is the same as `._1`.
   *  @throws  IndexOutOfBoundsException if the `n` is out of range(n < 0 || n >= $i).
   */
"""

  def cases = {
    val xs = for ((x, i) <- mdefs.zipWithIndex) yield "case %d => %s".format(i, x)
    val default = "case _ => throw new IndexOutOfBoundsException(s\"$n is out of bounds (min 0, max " + (i-1) +")\")"
    "\n" + ((xs ::: List(default)).map("    " + _ + "\n").mkString)
  }
  def proj = {
    (mdefs,targs).zipped.map( (_,_) ).zipWithIndex.map { case ((method,typeName),index) =>
      """|  /** A projection of element %d of this Product.
         |   *  @return   A projection of element %d.
         |   */
         |  def %s: %s
         |""".stripMargin.format(index + 1, index + 1, method, typeName)
    }.mkString
  }

  def apply() = {
<file name={fileName}>{header}
object {className} {{
  def unapply{invariantArgs}(x: {className}{invariantArgs}): Option[{className}{invariantArgs}] =
    Some(x)
}}

/** {className} is a Cartesian product of {i} component{s}.
 */
trait {className}{covariantArgs} extends Any with Product {{
  /** The arity of this product.
   *  @return {i}
   */
  override def productArity: Int = {i}

  {productElementComment}
  @throws(classOf[IndexOutOfBoundsException])
  override def productElement(n: Int): Any = n match {{ {cases} }}

{proj}
{moreMethods}
}}
</file>}

}

/** Abstract functions **/

object AbstractFunctionZero extends AbstractFunction(0) {
  override def covariantSpecs = FunctionZero.covariantSpecs
}

object AbstractFunctionOne extends AbstractFunction(1) {
  override def covariantSpecs = FunctionOne.covariantSpecs
  override def contravariantSpecs = FunctionOne.contravariantSpecs
}

object AbstractFunctionTwo extends AbstractFunction(2) {
  override def covariantSpecs = FunctionTwo.covariantSpecs
  override def contravariantSpecs = FunctionTwo.contravariantSpecs
}

class AbstractFunction(val i: Int) extends Group("AbstractFunction") with Arity
{
  override def packageDef = "scala.runtime"

  val superTypeArgs = typeArgsString(targs ::: List("R"))

  def apply() = {
<file name={"runtime/" + fileName}>{header}
abstract class {className}{contraCoArgs} extends Function{i}{superTypeArgs} {{
{moreMethods}
}}
</file>}

}
object AbstractFunction
{
  def make(i: Int) = apply(i)()
  def apply(i: Int) = i match {
    case 0    => AbstractFunctionZero
    case 1    => AbstractFunctionOne
    case 2    => AbstractFunctionTwo
    case _    => new AbstractFunction(i)
  }
}
