package com.example

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import example.{HttpClient, Note}
import org.scalajs.dom.ext.KeyCode

import scala.concurrent.ExecutionContext

object WebPage:
  given ExecutionContext = ExecutionContext.global
  val service = new HttpClient()
  import example._

  private val onEnterPress = onKeyPress.filter(_.keyCode == KeyCode.Enter)

  //case class Note(id: Int, title: String, content: String)

  sealed abstract class Filter(val name: String, val passes: Note => Boolean)

  object ShowAll extends Filter("All", _ => true)


  val filters: List[Filter] = ShowAll :: Nil


  sealed trait Command

  case class Create(title: String, body: String) extends Command

  //case class UpdateText(itemId: Int, text: String) extends Command

 // case class UpdateCompleted(itemId: Int, completed: Boolean) extends Command

  //case class Delete(itemId: Int) extends Command

  private val itemsVar = Var(List[Note]())

  private val filterVar = Var[Filter](ShowAll)


  private var lastId = 1 // just for auto-incrementing IDs

  val titleValue = Var("title")
  val bodyValue = Var("body")



  private val commandObserver = Observer[Command] {
    case Create(titleText, bodyText) =>
      //lastId += 1
      //val newNote = Note(id = lastId, title = titleText, content = bodyText)
      //println("new note " + newNote)
      //itemsVar.update(_ :+ newNote)
      service.createNote(titleText, bodyText).map(addNote)
  }

  def addNote(newNote: Note): Unit = {
    itemsVar.update(_ :+ newNote)
  }


  val titleInput = input(typ := "text", onInput.mapToValue --> titleValue)

  val contentTextArea = textArea(onInput.mapToValue --> bodyValue)

  val saveButton = button("Create Note", onClick.mapTo(Create(titleInput.ref.value, contentTextArea.ref.value)) --> commandObserver)

  val form: Div = div(cls("note-form"),
    titleInput,
    contentTextArea,
    saveButton
  )


  lazy val container = dom.document.body
  lazy val appElement: Div = div(
    h1("My Notepad"),
    form,
    children <-- itemsVar.signal.split(_.id)(renderNote)
  )

  private def renderNote(itemId: String, initialTodo: Note, $item: Signal[Note]): HtmlElement =
    div(cls("note"),
      h2(child.text <-- $item.map(_.title)),
      p(child.text <-- $item.map(_.content))
    )

  def main(args: Array[String]): Unit = {
    renderOnDomContentLoaded(container, appElement)

    for notes <- service.getAllNotes(); note <- notes do {
      println("add Note " + note)
      addNote(note)
    }
  }

