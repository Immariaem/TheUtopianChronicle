package study.coco.project_code

// shared base for all handlers, gives access to state and world
abstract class BaseHandler (
    protected val state: GameState,
    protected val world: World
)

