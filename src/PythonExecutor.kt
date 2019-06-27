import javafx.beans.property.SimpleStringProperty
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class PythonExecutor(file: File, private val env: String? = null) {
    private val builder = ProcessBuilder()
        .command("$env/venv/Scripts/python" ?: "python", "-u", file.absolutePath)
        .redirectErrorStream(true)

    val output = SimpleStringProperty()
    private lateinit var process: Process
    var done = false

    fun start() {
        process = builder.start()
        thread {
            val sc = Scanner(process.inputStream)
            while (sc.hasNextLine()) {
                output.set(output.get() + sc.nextLine())
            }
            done = true
        }
    }

    fun getData(): String {
        if (!done) {
            return ""
        }
        val env = env ?: return ""
        return File(env, "outQueue").listFiles()?.last()?.readText() ?: ""
    }
}