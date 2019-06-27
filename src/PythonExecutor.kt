import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TextArea
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class PythonExecutor(private val file: File, private val env: String? = null) {
    private val builder = ProcessBuilder()
        .redirectErrorStream(true)

    val output = SimpleStringProperty()

    private lateinit var process: Process
    var done = false

    private var dataPath: String? = null

    fun start() {
        builder.command(env?.let { "$it/venv/Scripts/python" } ?: "python", "-u", file.absolutePath)
        process = builder.start()
        thread {
            val sc = Scanner(process.inputStream)
            while (sc.hasNextLine()) {
                val nextLine = sc.nextLine()
                if (nextLine.startsWith("!!")) {
                    dataPath = nextLine.substring(2)
                }
                output.set(output.get() + nextLine)
            }
            done = true
        }
    }

    val ta get() = TextArea().apply {
        isEditable = false
        textProperty().bind(output)
    }

    fun getData(): String {
        if (!done) {
            return ""
        }
        val env = env ?: return ""
        return dataPath?.let {
            File(it).readText()
        } ?: File(env, "outQueue").listFiles()?.maxBy { it.lastModified() }?.readText() ?: ""
    }
}