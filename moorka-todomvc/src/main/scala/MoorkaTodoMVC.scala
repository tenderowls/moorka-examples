
import moorka.rx._
import moorka.ui._
import moorka.ui.components.base._
import moorka.ui.components.html._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

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
    val numActive = for { 
      todosLength ← todos.rxLength
      n ← numCompleted 
    }
    yield {
      todosLength - n
    } 
    val nowEditing = Var(false)
    val filter = Var[Filter](All)
    // Internal component. It has own state but depends on
    // display state of main component
    class TodoComponent(state: Rx[Todo]) extends Component {
      val el = {
        val fieldText = Var("")
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
              nowEditing modOnce { nowEditing ⇒
                state map { todo ⇒
                  if (todo.status == Active && !nowEditing) {
                    todos.updateElement(todo, todo.copy(status = Editing))
                    true
                  }
                  else nowEditing
                }
              }
            },
            input(
              `className` := "toggle",
              `type` := "checkbox",
              `style` := "cursor: pointer",
              on.click listen {
                state once { todo ⇒
                  val newStatus = todo.status match {
                    case Active => Completed
                    case Completed => Active
                    case Editing => Active
                  }
                  todos.updateElement(todo, todo.copy(status = newStatus))
                }
              },
              `checked` := state.map(_.status == Completed)
            ),
            label(state.map(_.txt)),
            button(
              `className` := "destroy",
              `style` := "cursor: pointer",
              on.click listen {
                state once { todo ⇒
                  todos -= todo
                }
              }
            )
          ),
          form(
            on.submit listen {
              fieldText once { fieldText ⇒
                state once { todo ⇒
                  nowEditing modOnce { _ ⇒
                    todos.updateElement(todo, Todo(
                      txt = fieldText,
                      status = Active
                    ))
                    false
                  }
                }
              }
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
                  inputText modOnce { inputText ⇒
                    val s = inputText.trim()
                    if (s != "") {
                      todos += Todo(s, Active)
                    }
                    ""
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
            `checked` := {
              for {
                todosLength ← todos.rxLength
                numCompleted ← numCompleted
              } yield {
                todosLength > 0 && numCompleted == todosLength
              }
            },
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
                    useClassName("selected") := filter.map(_ == x),
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
