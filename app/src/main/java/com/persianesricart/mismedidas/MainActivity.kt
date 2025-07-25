package com.persianesricart.mismedidas

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.persianesricart.mismedidas.data.ajustes.AjustesDatabase
import com.persianesricart.mismedidas.ui.ajustes.AjustesScreen
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModel
import com.persianesricart.mismedidas.viewmodel.ajustes.AjustesViewModelFactory
import java.io.File
import java.io.IOException

private lateinit var exportLauncher: ActivityResultLauncher<Intent>
private lateinit var importLauncher: ActivityResultLauncher<Intent>


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                MainViewModel(AppDatabase.getInstance(this).notaDao()).handleExportResult(this, uri)
            }
        }

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
            composable("main?reload={reload}",
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

            //val ajustesViewModel: AjustesViewModel = viewModel(
            //    factory = object: ViewModelProvider.Factory {
            //        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            //            return AjustesViewModel.create(LocalContext.current) as T
            //        }
            //    }
            //)

            composable("settings") {
                val context = LocalContext.current
                // Creamos el ViewModel con su factory
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
                    ajustesViewModel = ajustesViewModel
                )
            }


            // Crear nueva nota
            composable("note") {
                val context = LocalContext.current
                val dao = AppDatabase.getInstance(context).notaDao()
                val factory = NoteViewModelFactory(dao)
                val noteViewModel: NoteViewModel = viewModel(factory = factory)

                NoteScreen(navController, noteViewModel)
            }

            // Editar nota existente
            composable("note/{notaId}", arguments = listOf(
                navArgument("notaId") { type = NavType.IntType }
            )) { backStackEntry ->
                val notaId = backStackEntry.arguments?.getInt("notaId") ?: 0
                val context = LocalContext.current
                val dao = AppDatabase.getInstance(context).notaDao()
                val factory = NoteViewModelFactory(dao)
                val noteViewModel: NoteViewModel = viewModel(factory = factory)

                LaunchedEffect(notaId) {
                    noteViewModel.loadNota(notaId)
                }

                NoteScreen(navController, noteViewModel)
            }



        }
    }
}


/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("MainActivity cargada")
        setContent {
            MisMedidasApp()
        }
    }
}

@Composable
fun MisMedidasApp() {
    println("Composición de MisMedidasApp")
    val navController = rememberNavController()
    MaterialTheme {
        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                println("Composición de MainScreen")
                val mainViewModel: MainViewModel = viewModel()
                MainScreen(navController, mainViewModel)
            }
            composable("note") {
                println("Composición de NoteScreen")
                val noteViewModel: NoteViewModel = viewModel()
                NoteScreen(navController, noteViewModel)
            }
        }
    }
}
 */