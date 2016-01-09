import felix._
import moorka._

object MoorkaTodoMVC extends FelixApplication {

  override def start = {
    // Inject Data
    val todos = Buffer.fromSeq(
      0 to 5 map (x => Todo(s"$x todo", Active))
    )

    // The values represents display state of this component.
    // They are depends on domain data
    val numCompleted = todos.foldLeft(0) {
      case (num, Todo(_, Completed)) => num + 1
      case (num, _) => num
    }

    val areAllCompleted = for {
      todosLength ← todos.rxLength
      numCompleted ← numCompleted
    } yield todosLength > 0 && numCompleted == todosLength

    val numActive = for {
      todosLength ← todos.rxLength
      n ← numCompleted
    } yield todosLength - n

    val nowEditing = Var(false)
    val activeFilter = Var[Filter](All)

    // Internal component. It has own state but depends on
    // display state of main component
    class TodoComponent(state: Rx[Todo])(implicit val system: FelixSystem) extends Component {
      override val start = {
        val fieldText = Var("")
        'li (
          useClass("completed") when state.map(_.status == Completed),
          useClass("editing") when state.map(_.status == Editing),
          useClass("hidden") when {
            (state zip activeFilter) map {
              case (_, All) => false
              case (x, Completed) => x.status != Completed
              case (x, Active) => x.status == Completed
            }
          },
          'div ('className := "view",
            'dblclick listen {
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
            'input (
              'className := "toggle",
              'type := "checkbox",
              'style /= "cursor: pointer",
              'click listen {
                state once { todo ⇒
                  val newStatus = todo.status match {
                    case Active => Completed
                    case Completed => Active
                    case Editing => Active
                  }
                  todos.updateElement(todo, todo.copy(status = newStatus))
                }
              },
              'checked := state.map(_.status == Completed)
            ),
            'label ('textContent := state.map(_.txt)),
            'button (
              'className := "destroy",
              'style /= "cursor: pointer",
              'click listen {
                state once { todo ⇒
                  todos -= todo
                }
              }
            )
          ),
          'form (
            'submit listen {
              fieldText once { fieldText ⇒
                state once { todo ⇒
                  nowEditing modOnce { _ ⇒
                    todos.updateElement(todo, Todo(
                      txt = fieldText,
                      status = Active
                    ))
                    Val(false)
                  }
                }
              }
            },
            'input ('className := "edit", 'value =:= fieldText)
          )
        )
      }
    }
    // Tree of component
    'div (
      'section ('className := "todoapp",
        'header ('className := "header",
          'h1 ("todos"),
          'block {
            val inputText = Var("")
            'form (
              'input (
                'className := "new-todo",
                'placeholder := "What needs to be done?",
                'autofocus := true,
                'value =:= inputText
              ),
              'submit listen {
                inputText modOnce { inputText ⇒
                  val s = inputText.trim()
                  if (s != "") {
                    todos += Todo(s, Active)
                  }
                  Val("")
                }
              }
            )
          }
        ),
        'section ('className := "main",
          'input (
            'className := "toggle-all",
            'type := "checkbox",
            'style /= "cursor: pointer",
            'checked := areAllCompleted,
            'click listen {
              areAllCompleted once {
                case true ⇒ todos.updateAll(_.copy(status = Active))
                case false ⇒ todos.updateAll(_.copy(status = Completed))
              }
            }
          ),
          'label ('htmlFor := "toggle-all", "Mark all as complete"),
          'ul ('className := "todo-list",
            DataRepeat[Todo](
              dataProvider = todos, //.filter(_ == filter.unsafeGet),
              x => new TodoComponent(x)
            )
          ),
          'footer ('className := "footer",
            'span ('className := "todo-count",
              'strong ('textContent := numActive.map(_.toString)),
              'span (" item left")
            ),
            'ul ('className := "filters",
              Seq(All, Active, Completed).map { x =>
                'li (
                  'a (
                    'href := "#",
                    useClass("selected") when activeFilter.map(_ == x),
                    'click listen {
                      activeFilter pull Val(x)
                    },
                    x.toString
                  )
                )
              }
            ),
            'button ('className := "clear-completed",
              useClass("hidden") when numCompleted.map(_ == 0),
              'click listen {
                todos.remove(_.status != Completed)
              },
              'textContent := numCompleted.map(s ⇒ s"Clear completed ($s)")
            )
          )
        )
      ),
      'footer ('className := "info",
        'p ("Double-click to edit a todo")
      )
    )
  }

  println("MoorkaTodoMVC constructed")
}
