package org.specs2
package reporter

import java.lang.annotation.Annotation

import org.junit.runner.Description
import fp.*
import Tree.*
import data.Trees
import data.Trees.*
import control.Exceptions.*
import specification.*
import core.*
import process.*
import control.*
import concurrent.ExecutionEnv
import specification.core.{Fragment, NoText}
import specification.create.DefaultFragmentFactory

/** Create Description objects from the specification fragments and arrange them as a tree
  */
trait JUnitDescriptions extends ExecutionOrigin:

  def createDescription(spec: SpecStructure)(ee: ExecutionEnv): Description =
    createDescription(createTreeLoc(spec)(ee))

  def createDescription(treeLoc: TreeLoc[Description]): Description =
    treeLoc.toTree.bottomUp { (description: Description, children: LazyList[Description]) =>
      children.foreach { child =>
        description.addChild(child)
      }
      description
    }.rootLabel

  def createTreeLoc(spec: SpecStructure)(ee: ExecutionEnv) =
    createDescriptionTree(spec)(ee).map(_._2)

  def createDescriptionTree(spec: SpecStructure)(ee: ExecutionEnv): TreeLoc[(Fragment, Description)] =
    val className = spec.specClassName
    val annotations =
      tryOrElse(getClass.getClassLoader.loadClass(spec.specClassName).getAnnotations)(Array[Annotation]())
    val rootFragment = DefaultFragmentFactory.text(spec.header.simpleName)

    Levels
      .treeLocMap(spec.fragments)(keep)(ee)
      .getOrElse(Leaf(rootFragment).loc)
      .root
      .setLabel(rootFragment)
      .cojoin
      .map { (current: TreeLoc[Fragment]) =>
        val description =
          current.getLabel match
            case f @ Fragment(d, e, _) if !e.isExecutable =>
              createDescription(className, suiteName = testName(d.show), annotations = annotations)
            case f @ Fragment(NoText, e, _) if e.mustJoin =>
              createDescription(className, label = current.size.toString, annotations = annotations)
            case f @ Fragment(NoText, e, _) =>
              createDescription(
                className,
                label = current.size.toString,
                suiteName = "action",
                annotations = annotations
              )
            case f @ Fragment(d, e, _) =>
              createDescription(
                className,
                label = current.size.toString,
                id = f.hashCode.toString,
                testName = testName(d.show, parentPath(current.parents.map(_._2))),
                annotations = annotations
              )
        (current.getLabel, description)
      }

  /** description for the beginning of the specification */
  def specDescription(spec: SpecStructure) =
    val annotations =
      tryOrElse(getClass.getClassLoader.loadClass(spec.specClassName).getAnnotations)(Array[Annotation]())
    createDescription(spec.specClassName, suiteName = testName(spec.name), annotations = annotations)

  /** Map of each fragment to its description */
  def fragmentDescriptions(spec: SpecStructure)(ee: ExecutionEnv): Map[Fragment, Description] =
    createDescriptionTree(spec)(ee).root.toTree.flattenLeft.toMap

  /** filter out the fragments which don't need to be represented in the JUnit descriptions */
  def keep: Levels.Mapper =
    case f @ Fragment(Text(t), e, _) if t.trim.isEmpty => None
    case f if Fragment.isFormatting(f)                 => None
    case f                                             => Some(f)

  def createDescription(
      className: String,
      suiteName: String = "",
      testName: String = "",
      label: String = "",
      id: String = "",
      annotations: Array[Annotation] = Array()
  ): Description =
    val origin =
      if isExecutedFromAnIDE && !label.isEmpty then label
      else className

    val description =
      if testName.isEmpty then (if suiteName.isEmpty then className else suiteName)
      else sanitize(testName) + "(" + origin + ")"

    if id.nonEmpty then Description.createSuiteDescription(description, id, annotations*)
    else Description.createSuiteDescription(description, annotations*)

  import text.Trim.*

  /** @return a seq containing the path of an example without the root name */
  def parentPath(parentNodes: Seq[Fragment]) =
    parentNodes.reverse.drop(1).map(_.description.show)

  /** @return a test name with no newlines */
  def testName(s: String, parentNodes: Seq[String] = Seq()): String =
    (if parentNodes.isEmpty || isExecutedFromAnIDE then ""
     else parentNodes.map(_.replace("\n", "")).mkString("", "::", "::")) +
      (if isExecutedFromAnIDE then s.removeNewLines else s.trimNewLines)

  /** @return replace () with [] because it cause display issues in JUnit plugins */
  private def sanitize(s: String) =
    val trimmed = s.trimReplace("(" -> "[", ")" -> "]")
    if trimmed.isEmpty then " "
    else trimmed

object JUnitDescriptions extends JUnitDescriptions

case class JUnitDescriptionsTree(spec: SpecStructure, ee: ExecutionEnv):
  lazy val descriptionTree = JUnitDescriptions.createDescriptionTree(spec)(ee)
  lazy val descriptions = descriptionTree.root.toTree.flattenLeft.toMap
  lazy val description = JUnitDescriptions.createDescription(descriptionTree.map(_._2))
