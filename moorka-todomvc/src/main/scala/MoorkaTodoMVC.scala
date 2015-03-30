
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

import moorka.rx._
import moorka.ui._
import moorka.ui.components.html._
import moorka.ui.components.base._
import scala.scalajs.js
import js.Dynamic.global

object MoorkaTodoMVC extends Application {
  println("MoorkaTodoMVC constructed")
  def start() = {
    // Inject Data
    val todos = Buffer.fromSeq(
      0 to 20 map(x => Todo(s"$x todo", Active))
    )
    // The values represents display state of this component.
    // They are depends on domain data
    val numCompleted = todos.foldLeft(0) {
      case (num, Todo(_, Completed)) => num + 1
      case (num, _) => num
    }
    val numActive = Bind { todos.length() - numCompleted() }
    val nowEditing = Var(false)
    val filter = Var[Filter](All)
    // Internal component. It has own state but depends on
    // display state of main component
    class TodoComponent(state: State[Todo]) extends Component {
      val el = {
        val fieldText = Var("")
        state.observe(fieldText() = state().txt)
        li(
          useClassName("completed") := state.map(_.status == Completed),
          useClassName("editing") := state.map(_.status == Editing),
          `hide` := (state zip filter) map {
            case (_, All) => false
            case (x, Completed) => x.status != Completed
            case (x, Active) => x.status == Completed
          },
          div(`className` := "view",
            on.`double-click` listen {
              val todo = state()
              if (todo.status == Active && !nowEditing()) {
                todos.updateElement(todo, todo.copy(status = Editing))
                nowEditing() = true
              }
            },
            input(
              `className` := "toggle",
              `type` := "checkbox",
              `style` := "cursor: pointer",
              on.click listen {
                val todo = state()
                val newStatus = todo.status match {
                  case Active => Completed
                  case Completed => Active
                  case Editing => Active
                }
                todos.updateElement(todo, todo.copy(status = newStatus))
              },
              `checked` := state.map(_.status == Completed)
            ),
            label(state.map(_.txt)),
            button(
              `className` := "destroy",
              `style` := "cursor: pointer",
              on.click listen {
                todos -= state()
              }
            )
          ),
          form(
            on.submit listen {
              val todo = state()
              todos.updateElement(todo, Todo(txt = fieldText(), status = Active))
              nowEditing() = false
            },
            input(`className` := "edit", `value` =:= fieldText)
          )
        )
      }
    }
    // Tree of component
    div(
      section(`className` := "todoapp",
        header(`className` := "header",
          h1("todos"),
            block {
              val inputText = Var("")
              form(
                input(
                  `className` := "new-todo",
                  `placeholder` := "What needs to be done?",
                  `autofocus` := true,
                  `value` =:= inputText
                ),
                on.submit listen {
                  val s = inputText().trim()
                  if (s != "") {
                    todos += Todo(s, Active)
                    inputText() = ""
                  }
                }
              )
            }
        ),
        section(`className` := "main",
          input(
            `className` := "toggle-all",
            `type` := "checkbox",
            `style` := "cursor: pointer",
            `checked` := Bind { todos.length() > 0 && numCompleted() == todos.length() },
            on.click subscribe { event =>
              `checked` from event.target onSuccess {
                case true =>
                  todos.updateAll(_.copy(status = Completed))
                case false =>
                  todos.updateAll(_.copy(status = Active))
              }
            }
          ),
          label(`for`:= "toggle-all", "Mark all as complete"),
          ul(`className` := "todo-list",
            DataRepeat[Todo](
              dataProvider = todos,
              x => new TodoComponent(x)
            )
          ),
          footer(`className` := "footer",
            span(`className` := "todo-count",
              strong( numActive.map(_.toString)),
              span(" item left")
            ),
            ul(`className` := "filters",
              Seq(All, Active, Completed).map { x =>
                li(
                  a(`href`:="#",
                    useClassName("selected") := Bind { filter() == x },
                    on.click listen {
                      filter() = x
                    },
                    x match {
                      case All => "All"
                      case Active => "Active"
                      case Completed => "Completed"
                    }
                  )
                )
              }
            ),
            button(
              `className` := "clear-completed",
              `show` := numCompleted.map(_ > 0),
              on.click listen {
                todos.remove(_.status != Completed)
              },
              numCompleted.map(s ⇒ s"Clear completed ($s)")
            )
          )
        )
      ),
      footer(`className` := "info",
        p("Double-click to edit a todo")
      )
    )
  }
}
