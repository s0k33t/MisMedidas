package com.persianesricart.mismedidas

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.intSetOf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.platform.LocalContext
import com.persianesricart.mismedidas.viewmodel.MainViewModelFactory
import com.persianesricart.mismedidas.viewmodel.NoteViewModelFactory
import com.persianesricart.mismedidas.data.AppDatabase


import com.persianesricart.mismedidas.ui.MainScreen
import com.persianesricart.mismedidas.ui.NoteScreen
import com.persianesricart.mismedidas.viewmodel.MainViewModel
import com.persianesricart.mismedidas.viewmodel.NoteViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope

import com.persianesricart.mismedidas.data.ajustes.AjustesDatabase
import com.persianesricart.mismedidas.ui.ImportExportScreen
import com.persianesricart.mismedidas.ui.ajustes.AjustesScreen
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModelFactory
import java.io.File
import java.io.IOException


import androidx.navigation.navArgument
import com.persianesricart.mismedidas.data.dao.MedidaDao
import com.persianesricart.mismedidas.viewmodel.CroquisViewModel
import com.persianesricart.mismedidas.viewmodel.CroquisViewModelFactory
import com.persianesricart.mismedidas.ui.croquis.CroquisDrawScreen
import kotlinx.coroutines.launch


private lateinit var exportLauncher: ActivityResultLauncher<Intent>
private lateinit var importLauncher: ActivityResultLauncher<Intent>
private lateinit var exportAjustesLauncher: ActivityResultLauncher<Intent>
private lateinit var importAjustesLauncher: ActivityResultLauncher<Intent>


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        actionBar?.hide()
        exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                MainViewModel(AppDatabase.getInstance(this).notaDao()).handleExportResult(this, uri)
            }
        }
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@MainActivity)
            backfillMedidaUuids(db.medidaDao())
        }
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                try {
                    // Grant persistente si el proveedor lo soporta
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Exception) {}

                    val dbName = "mis_medidas.db"
                    val dbFile = getDatabasePath(dbName)
                    val dbDir  = dbFile.parentFile!!

                    // 1) Cerrar Room
                    com.persianesricart.mismedidas.data.AppDatabase.getInstance(this).close()

                    // 2) Limpiar WAL/SHM
                    runCatching { File(dbDir, "$dbName-wal").delete() }
                    runCatching { File(dbDir, "$dbName-shm").delete() }

                    // 3) Copiar
                    contentResolver.openInputStream(uri)?.use { input ->
                        dbFile.outputStream().use { out -> input.copyTo(out) }
                    }

                    // 4) Limpiar por si el archivo importado traía journaling extra
                    runCatching { File(dbDir, "$dbName-wal").delete() }
                    runCatching { File(dbDir, "$dbName-shm").delete() }

                    // 5) Mostrar tamaño/fecha tras copiar (debug)
                    val size = dbFile.length()
                    val mod = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(java.util.Date(dbFile.lastModified()))
                    android.widget.Toast
                        .makeText(this, "Import OK (${size} bytes, $mod). Reiniciando…", android.widget.Toast.LENGTH_LONG)
                        .show()

                    // 6) Reinicio duro del proceso
                    finishAffinity()
                    startActivity(intent)
                    Runtime.getRuntime().exit(0)

                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "Error al importar: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
        /*
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult

                // (opcional) persist permission
                runCatching {
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                // Usa el doctor
                val report = com.persianesricart.mismedidas.data.DbDoctor.importNotasWithRepair(this, uri)
                if (report.ok) {
                    Toast.makeText(this, "Importación correcta. Reiniciando…", Toast.LENGTH_LONG).show()
                    finishAffinity()
                    startActivity(intent)
                    Runtime.getRuntime().exit(0)
                } else {
                    Toast.makeText(this, "Importación fallida: ${report.message}", Toast.LENGTH_LONG).show()
                    android.util.Log.e("DbDoctor", "Detalles:\n${report.details}")
                }
            }
        }
        */
        /*
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                try {
                    // Asegura permiso de lectura persistente del SAF
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { /* en algunos OEM no hace falta */ }

                try {
                    val dbName = "mis_medidas.db" // ¡asegúrate que es exactamente el que usas en AppDatabase!
                    val dbFile = getDatabasePath(dbName)
                    val dbDir  = dbFile.parentFile!!

                    // 1) Cerrar Room antes de tocar archivos
                    com.persianesricart.mismedidas.data.AppDatabase.getInstance(this).close()

                    // 2) Borrar WAL/SHM si existieran
                    kotlin.runCatching { File(dbDir, "$dbName-wal").delete() }
                    kotlin.runCatching { File(dbDir, "$dbName-shm").delete() }

                    // 3) Copiar el archivo importado a la ruta de Room
                    contentResolver.openInputStream(uri)?.use { input ->
                        dbFile.outputStream().use { out ->
                            input.copyTo(out)
                        }
                    }

                    // 4) Parchea el archivo (crea tabla Croquis, añade columnas recientes, fija user_version=3)
                    patchImportedNotasDb(dbFile)

                    // 5) Seguridad extra: vuelve a borrar cualquier WAL/SHM que pueda haber quedado
                    kotlin.runCatching { File(dbDir, "$dbName-wal").delete() }
                    kotlin.runCatching { File(dbDir, "$dbName-shm").delete() }

                    Toast.makeText(this, "Importación completada. Reiniciando…", Toast.LENGTH_LONG).show()

                    // 6) Reinicia el proceso para reabrir Room desde 0 con el nuevo archivo
                    finishAffinity()
                    startActivity(intent)
                    Runtime.getRuntime().exit(0)

                } catch (e: Exception) {
                    Toast.makeText(this, "Error al importar: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
        */
        /*
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        val dbName = "mis_medidas.db"
                        val dbDir = getDatabasePath(dbName).parentFile!!
                        val dbFile = File(dbDir, dbName)

                        // ✅ Cerrar base de datos activa
                        AppDatabase.getInstance(this).close()

                        // ✅ Eliminar WAL y SHM
                        File(dbDir, "$dbName-shm").delete()
                        File(dbDir, "$dbName-wal").delete()

                        // ✅ Copiar nuevo archivo
                        contentResolver.openInputStream(uri)?.use { input ->
                            dbFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        Toast.makeText(this, "Importación completada", Toast.LENGTH_SHORT).show()

                        // ✅ Reiniciar para recargar base
                        finishAffinity()
                        startActivity(intent)

                    } catch (e: Exception) {
                        Toast.makeText(this, "Error al importar: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }
        }
        */
        exportAjustesLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result.data?.data?.let { uri ->
                AjustesViewModel.create(this)
                    .handleExportAjustesResult(this, uri)
            }
        }
        importAjustesLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // reiniciar la app tras importar
                    val restart = {
                        finishAffinity()
                        startActivity(intent)
                    }
                    AjustesViewModel.create(this)
                        .handleImportAjustesResult(this, uri, restart)
                }
            }
        }

        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1001
                )
            }
        }

        setContent {
            val insetsController =   WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;
            insetsController.hide(WindowInsetsCompat.Type.statusBars());
            insetsController.hide(WindowInsetsCompat.Type.navigationBars());
            insetsController.hide(WindowInsetsCompat.Type.captionBar());
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
            insetsController.hide(WindowInsetsCompat.Type.tappableElement());
            MisMedidasApp()
        }

        //supportActionBar?.hide()
    }

    suspend fun backfillMedidaUuids(dao: MedidaDao) {
        val sinUuid = dao.getAllWithoutUuid()
        for (m in sinUuid) {
            val fixed = m.copy(uuid = java.util.UUID.randomUUID().toString())
            dao.update(fixed)
        }
    }
}

// Parchea el archivo de notas importado para que cuadre con el esquema actual (v3)
private fun patchImportedNotasDb(dbFile: File) {
    val path = dbFile.absolutePath
    val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)

    try {
        // seguridad
        db.execSQL("PRAGMA foreign_keys=OFF;")

        // === Tablas mínimas ===
        // Croquis (por si el backup viene de una versión sin croquis)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS Croquis (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                medidaId INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                filePath TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY(medidaId) REFERENCES Medida(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // === Columnas que pudieron añadirse en el tiempo ===
        // Nota: email, referencia (si tuviste estas ampliaciones)
        runCatching { db.execSQL("ALTER TABLE Nota ADD COLUMN email TEXT") }
        runCatching { db.execSQL("ALTER TABLE Nota ADD COLUMN referencia TEXT") }

        // Medida: campos de luz/cargo y motor/acabado si no estaban
        runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN luz INTEGER NOT NULL DEFAULT 0") }
        runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN cargoAncho TEXT") }
        runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN cargoAlto TEXT") }
        runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN motor TEXT") }
        runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN acabado TEXT") }

        // Medida: uuid (tu migración 2→3) + índice único
        runCatching { db.execSQL("ALTER TABLE Medida ADD COLUMN uuid TEXT") }
        runCatching {
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_Medida_uuid ON Medida(uuid)")
        }

        // Sube la versión del archivo al valor que espera Room
        db.execSQL("PRAGMA user_version=3;")
    } finally {
        db.close()
    }
}


@Preview(showBackground = true)
@Composable
fun SimpleComposablePreview() {
    MisMedidasApp()
}

@Composable
fun MisMedidasApp() {
    val navController: NavHostController = rememberNavController()

    MaterialTheme {
        NavHost(navController = navController, startDestination = "main") {

            // Pantalla principal
            composable(
                "main?reload={reload}",
                arguments = listOf(navArgument("reload") {
                    defaultValue = "false"
                })
            ) { backStackEntry ->
                val reload = backStackEntry.arguments?.getString("reload") == "true"
                val context = LocalContext.current
                val dao = AppDatabase.getInstance(context).notaDao()
                val factory = MainViewModelFactory(dao)
                val mainViewModel: MainViewModel = viewModel(factory = factory)

                //val backStackEntry = navController.currentBackStackEntryAsState()
                //LaunchedEffect(backStackEntry.value) {
                //    mainViewModel.cargarNotas()
                //}
                LaunchedEffect(reload) {
                    mainViewModel.cargarNotas()
                }


                MainScreen(
                    navController = navController,
                    viewModel = mainViewModel,
                    exportLauncher = exportLauncher,
                    importLauncher = importLauncher
                )
            }

            composable("import_export") {
                val context = LocalContext.current

                // MainViewModel
                val notaDao = AppDatabase.getInstance(context).notaDao()
                val mainFactory = MainViewModelFactory(notaDao)
                val mainViewModel: MainViewModel = viewModel(factory = mainFactory)

                // AjustesViewModel
                val ajustesDb = AjustesDatabase.getInstance(context)
                val ajustesFactory = AjustesViewModelFactory(
                    ajustesDb.tipoDao(),
                    ajustesDb.modeloDao(),
                    ajustesDb.acabadoDao(),
                    ajustesDb.colorDao()
                )
                val ajustesViewModel: AjustesViewModel = viewModel(factory = ajustesFactory)

                ImportExportScreen(
                    navController             = navController,
                    mainViewModel             = mainViewModel,
                    ajustesViewModel          = ajustesViewModel,
                    exportNotasLauncher       = exportLauncher,
                    importNotasLauncher       = importLauncher,
                    exportAjustesLauncher     = exportAjustesLauncher,
                    importAjustesLauncher     = importAjustesLauncher
                )
            }

            composable("settings") {
                val context = LocalContext.current
                val db = AjustesDatabase.getInstance(context)
                val factory = AjustesViewModelFactory(
                    db.tipoDao(),
                    db.modeloDao(),
                    db.acabadoDao(),
                    db.colorDao()
                )
                val ajustesViewModel: AjustesViewModel = viewModel(factory = factory)

                AjustesScreen(
                    navController = navController,
                    ajustesViewModel = ajustesViewModel,
                    exportAjustesLauncher   = exportAjustesLauncher,
                    importAjustesLauncher   = importAjustesLauncher
                )
            }


            // Crear nueva nota
            composable("note") {
                val context = LocalContext.current
                val noteDb = AppDatabase.getInstance(context).notaDao()
                val noteFactory = NoteViewModelFactory(noteDb)
                val noteViewModel: NoteViewModel = viewModel(factory = noteFactory)

                val ajustesDb = AjustesDatabase.getInstance(context)
                val ajustesFactory = AjustesViewModelFactory(
                    ajustesDb.tipoDao(),
                    ajustesDb.modeloDao(),
                    ajustesDb.acabadoDao(),
                    ajustesDb.colorDao()
                )
                val ajustesViewModel: AjustesViewModel = viewModel(factory = ajustesFactory)

                NoteScreen(
                    navController = navController,
                    viewModel = noteViewModel,
                    ajustesViewModel = ajustesViewModel
                )
            }

            composable("note/{notaId") {
                val context = LocalContext.current
                val noteDb = AppDatabase.getInstance(context).notaDao()
                val noteFactory = NoteViewModelFactory(noteDb)
                val noteViewModel: NoteViewModel = viewModel(factory = noteFactory)

                val ajustesDb = AjustesDatabase.getInstance(context)
                val ajustesFactory = AjustesViewModelFactory(
                    ajustesDb.tipoDao(),
                    ajustesDb.modeloDao(),
                    ajustesDb.acabadoDao(),
                    ajustesDb.colorDao()
                )
                val ajustesViewModel: AjustesViewModel = viewModel(factory = ajustesFactory)

                NoteScreen(navController, noteViewModel, ajustesViewModel = ajustesViewModel)
            }

            // Editar nota existente
            composable(
                "note/{notaId}",
                arguments = listOf(navArgument("notaId") {
                    type = NavType.IntType
                })
            ) { backStack ->
                val notaId = backStack.arguments?.getInt("notaId") ?: 0
                val context = LocalContext.current

                // NoteViewModel
                val notaDao = AppDatabase.getInstance(context).notaDao()
                val noteVmFactory = NoteViewModelFactory(notaDao)
                val noteViewModel: NoteViewModel = viewModel(factory = noteVmFactory)
                // carga la nota con todas sus medidas
                LaunchedEffect(notaId) {
                    noteViewModel.loadNota(notaId)
                }

                // AjustesViewModel (igual que antes)
                val ajustesDb = AjustesDatabase.getInstance(context)
                val ajustesVmFactory = AjustesViewModelFactory(
                    ajustesDb.tipoDao(),
                    ajustesDb.modeloDao(),
                    ajustesDb.acabadoDao(),
                    ajustesDb.colorDao()
                )
                val ajustesViewModel: AjustesViewModel = viewModel(factory = ajustesVmFactory)

                NoteScreen(
                    navController = navController,
                    viewModel = noteViewModel,
                    ajustesViewModel = ajustesViewModel
                )
            }

            composable(
                route = "croquis/{medidaId}",
                arguments = listOf(navArgument("medidaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val medidaId = backStackEntry.arguments?.getInt("medidaId") ?: 0
                val context = LocalContext.current
                val dao = AppDatabase.getInstance(context).croquisDao()
                val factory = CroquisViewModelFactory(dao)
                val croquisVM: CroquisViewModel = viewModel(factory = factory)

                CroquisDrawScreen(
                    navController = navController,
                    medidaId = medidaId,
                    croquisId = null,
                    viewModel = croquisVM
                )
            }

            composable(
                route = "croquis/{medidaId}/{croquisId}",
                arguments = listOf(
                    navArgument("medidaId") { type = NavType.IntType },
                    navArgument("croquisId") { type = NavType.IntType },
                )
            ) { backStackEntry ->
                val medidaId = backStackEntry.arguments?.getInt("medidaId") ?: 0
                val croquisId = backStackEntry.arguments?.getInt("croquisId")
                val context = LocalContext.current
                val dao = AppDatabase.getInstance(context).croquisDao()
                val factory = CroquisViewModelFactory(dao)
                val croquisVM: CroquisViewModel = viewModel(factory = factory)

                CroquisDrawScreen(
                    navController = navController,
                    medidaId = medidaId,
                    croquisId = croquisId,
                    viewModel = croquisVM
                )
            }
        }
    }
}