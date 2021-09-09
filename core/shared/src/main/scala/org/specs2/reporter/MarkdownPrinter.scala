package org.specs2
package reporter

import fp.*, syntax.*
import control.*
import origami.*
import io.*
import FileName.*
import execute.*
import main.Arguments
import specification.core.*

/** This trait is not a full fledged markdown printer yet
  */
case class MarkdownPrinter(env: Env) extends Printer:

  def prepare(specifications: List[SpecStructure]): Action[Unit] =
    env.fileSystem.mkdirs(MarkdownOptions.create(env.arguments).outDir).toAction

  def finalize(specifications: List[SpecStructure]): Action[Unit] =
    Action.unit

  /** @return a Fold for the markdown output */
  def sink(spec: SpecStructure): AsyncSink[Fragment] =
    val env1 = env.setArguments(env.arguments.overrideWith(spec.arguments))
    val options = MarkdownOptions.create(env1.arguments)
    val path = options.outDir / FilePath.unsafe(spec.header.className + "." + options.extension)
    FoldIo.printToFilePath[Fragment](path)(f => fragmentToLine(options)(f))

  def fragmentToLine(options: MarkdownOptions)(fragment: Fragment): Action[String] =
    fragment match
      case t if Fragment.isText(t) => Action.protect(t.description.show)

      case e if Fragment.isExample(e) =>
        val description = e.description.show

        e.executionResult.map {
          case r: Success              => showDescription(description, r)
          case r @ Failure(m, _, _, _) => showDescription(description, r) + "\n  " + m
          case r @ Error(_, e1)        => showDescription(description, r) + "\n  " + e1
          case r: Skipped              => showDescription(description, r) + "\n  " + r.message
          case r: Pending              => showDescription(description, r) + " - " + r.message
          case r                       => description + "\n" + r.message
        }

      case f if Fragment.isStepOrAction(f) =>
        f.executionResult map {
          case Failure(m, _, _, _) => "Step failed " + m
          case Error(_, e)         => "Step error " + e
          case _                   => ""
        }

      case Fragment(ref: SpecificationRef, _, _) => Action.protect(toMarkdown(ref, options))
      case _                                     => Action.pure("")

  def showDescription(description: String, result: Result): String =
    if Seq("*", "-").exists(description.trim.startsWith) then description
    else result.status + " " + description

  def toMarkdown(ref: SpecificationRef, options: MarkdownOptions) =
    s"[${ref.linkText}](${options.outDir / FilePath.unsafe(ref.url)})"

object MarkdownPrinter:
  val default = MarkdownPrinter(Env())

case class MarkdownOptions(
    outDir: DirectoryPath,
    extension: String
)

object MarkdownOptions:

  /** create markdown options from arguments */
  def create(arguments: Arguments): MarkdownOptions =
    MarkdownOptions(
      outDir = arguments.commandLine.directoryOr("markdown.outdir", outDir),
      extension = arguments.commandLine.valueOr("markdown.ext", extension)
    )

  val outDir: DirectoryPath = "target" / "specs2-reports"
  val extension = "md"
