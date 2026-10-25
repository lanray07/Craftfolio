package com.craftfolio.patterns

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource as s
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import android.app.Application

class CraftViewModel(application: Application) : AndroidViewModel(application) {
    val app = application as CraftfolioApp
    val repo = app.repository
    val patterns = repo.dao.patterns()
    val projects = repo.dao.projects()
    val counters = repo.dao.counters()
    val notes = repo.dao.notes()
    val errors = mutableStateOf<Throwable?>(null)
    private val actions = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private val positions = Channel<Unit>(Channel.CONFLATED)
    private val pendingPositions = linkedMapOf<String, suspend () -> Unit>()
    init {
        viewModelScope.launch { for (action in actions) runCatching { action() }.onFailure { errors.value = it } }
        viewModelScope.launch { for (signal in positions) {
            while (pendingPositions.isNotEmpty()) {
                val entry = pendingPositions.entries.first(); val save = entry.value; pendingPositions.remove(entry.key)
                runCatching { save() }.onFailure { errors.value = if (it is UserFacingFailure) it else UserFacingFailure(R.string.reading_position_failed) }
            }
        } }
    }
    fun act(action: suspend () -> Unit) { actions.trySend(action) }
    fun position(id: String, save: suspend () -> Unit) { pendingPositions[id] = save; positions.trySend(Unit) }
}
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) { super.attachBaseContext(AppLanguages.wrap(newBase)) }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { Craftfolio() } }
    override fun onResume() { super.onResume(); (application as CraftfolioApp).billing.refresh() }
}

@Composable
fun Craftfolio(vm: CraftViewModel = viewModel()) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) darkColorScheme(primary = Color(0xFFA8CDB9), background = Color(0xFF1D2520), surface = Color(0xFF27312A), surfaceContainerHighest = Color(0xFF354139))
        else lightColorScheme(primary = Color(0xFF315D50), background = Color(0xFFF8F5EF), surface = Color(0xFFEEE8DC), secondary = Color(0xFF80503D), secondaryContainer = Color(0xFFDCE6DA), onSecondaryContainer = Color(0xFF233D30), surfaceContainerHighest = Color(0xFFEEE8DC))
    MaterialTheme(colorScheme = colors) {
        val context = LocalContext.current
        val defaultCounterName = s(R.string.rows)
        val localizedCraftNames = mapOf("All" to s(R.string.all), "Knitting" to s(R.string.knitting), "Crochet" to s(R.string.crochet), "Sewing" to s(R.string.sewing), "Other" to s(R.string.other))
        var languageDialog by remember { mutableStateOf(false) }
        val patterns by vm.patterns.collectAsState(initial = emptyList())
        val projects by vm.projects.collectAsState(initial = emptyList())
        val counters by vm.counters.collectAsState(initial = emptyList())
        val notes by vm.notes.collectAsState(initial = emptyList())
        val premium by vm.app.billing.premium.collectAsState()
        var tab by rememberSaveable { mutableStateOf(0) }
        var selected by rememberSaveable { mutableStateOf<String?>(null) }
        var search by rememberSaveable { mutableStateOf("") }
        var craft by rememberSaveable { mutableStateOf("All") }
        var createPattern by remember { mutableStateOf<Pattern?>(null) }
        var preview by remember { mutableStateOf<Pattern?>(null) }
        var exportPattern by remember { mutableStateOf<Pattern?>(null) }
        var editing by remember { mutableStateOf<Project?>(null) }
        var newCounter by rememberSaveable { mutableStateOf(false) }
        var newNote by rememberSaveable { mutableStateOf(false) }
        var importCraft by rememberSaveable { mutableStateOf("Knitting") }
        androidx.activity.compose.BackHandler(enabled = selected != null) { selected = null }
        LaunchedEffect(selected) { selected?.let { id -> vm.act { vm.repo.dao.touch(id, System.currentTimeMillis()) } } }
        val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { vm.act { vm.repo.import(it, importCraft, premium) } } }
        val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            val pattern = exportPattern
            if (uri != null && pattern != null) vm.act { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                vm.app.contentResolver.openOutputStream(uri)?.use { out -> java.io.File(pattern.path).inputStream().use { it.copyTo(out) } } ?: throw UserFacingFailure(R.string.export_failed)
            } }
        }
        Surface(Modifier.fillMaxSize(), color = colors.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f).padding(end = 8.dp)) { Text(s(R.string.app_name), style = MaterialTheme.typography.headlineMedium); Text(s(R.string.tagline), style = MaterialTheme.typography.bodySmall) }
                    TextButton(onClick = { languageDialog = true }, modifier = Modifier.testTag("language-selector")) { Text(s(R.string.language)) }
                    TextButton(onClick = { tab = 2; selected = null }) { Text(s(if (premium) R.string.premium else R.string.free)) }
                }
                val project = projects.find { it.id == selected }
                if (project != null) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selected = null }) { Text(s(R.string.back)) }
                        TextButton(onClick = { editing = project }) { Text(s(R.string.edit_project)) }
                        TextButton(onClick = { patterns.find { it.id == project.patternId }?.let { exportPattern = it; exporter.launch(exportName(it)) } }) { Text(s(R.string.export_original)) }
                    }
                    Text(project.name, style = MaterialTheme.typography.headlineSmall)
                    val pattern = patterns.find { it.id == project.patternId }
                    BoxWithConstraints(Modifier.weight(1f)) {
                        val wide = maxWidth >= 900.dp * androidx.compose.ui.platform.LocalDensity.current.fontScale.coerceAtLeast(1f)
                        @Composable fun Controls(modifier: Modifier) {
                            LazyColumn(modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                item { Text(s(when(project.status) { "PAUSED" -> R.string.paused; "COMPLETED" -> R.string.completed; else -> R.string.active }), style = MaterialTheme.typography.labelLarge) }
                                items(counters.filter { it.projectId == project.id }, key = { it.id }) { c ->
                                    Card { Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                        Text(c.name, style = MaterialTheme.typography.titleMedium)
                                        Text(c.value.toString(), style = MaterialTheme.typography.displayMedium)
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedButton(onClick = { vm.act { vm.repo.change(c.id, -1) } }, modifier = Modifier.sizeIn(minWidth = 64.dp, minHeight = 56.dp)) { Text(s(R.string.minus)) }
                                            Button(onClick = { vm.act { vm.repo.change(c.id, 1) } }, modifier = Modifier.sizeIn(minWidth = 80.dp, minHeight = 56.dp)) { Text(s(R.string.plus)) }
                                        }
                                        TextButton(onClick = { vm.act { vm.repo.undo(c.id) } }) { Text(s(R.string.undo)) }
                                    } }
                                }
                                item { OutlinedButton(onClick = { newCounter = true }) { Text(s(R.string.add_counter)) } }
                                item { Text(s(R.string.materials), style = MaterialTheme.typography.titleMedium); Text(project.materials.ifBlank { s(R.string.no_materials) }) }
                                item { Row { Text(s(R.string.notes), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium); TextButton(onClick = { newNote = true }) { Text(s(R.string.add_note)) } } }
                                items(notes.filter { it.projectId == project.id }, key = { it.id }) { n -> Card { Text(n.text, Modifier.fillMaxWidth().padding(16.dp)) } }
                            }
                        }
                        if (wide) Row { if (pattern != null) PatternViewer(pattern, project, vm, Modifier.weight(1.4f).fillMaxHeight()); Controls(Modifier.weight(1f)) }
                        else Column { if (pattern != null) PatternViewer(pattern, project, vm, Modifier.fillMaxWidth().weight(1f)); Controls(Modifier.fillMaxWidth().weight(1f)) }
                    }
                } else {
                    TabRow(selectedTabIndex = tab) {
                        listOf(R.string.library, R.string.projects, R.string.premium).forEachIndexed { index, label -> Tab(selected = tab == index, onClick = { tab = index }, text = { Text(s(label)) }) }
                    }
                    when (tab) {
                        0 -> {
                            projects.firstOrNull { it.status == "ACTIVE" }?.let { p -> Card(onClick = { selected = p.id }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                Column(Modifier.padding(20.dp)) { Text(s(R.string.continue_project), style = MaterialTheme.typography.labelLarge); Text(p.name, style = MaterialTheme.typography.titleLarge) }
                            } }
                            OutlinedTextField(search, { search = it }, label = { Text(s(R.string.search)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All", "Knitting", "Crochet", "Sewing", "Other").forEach { value -> FilterChip(selected = craft == value, onClick = { craft = value }, label = { Text(craftLabel(value)) }) } }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { importCraft = when(importCraft) { "Knitting" -> "Crochet"; "Crochet" -> "Sewing"; "Sewing" -> "Other"; else -> "Knitting" } }) { Text(s(R.string.import_craft, craftLabel(importCraft))) }
                                Button(onClick = { importer.launch(arrayOf("application/pdf", "image/jpeg", "image/png", "image/webp")) }) { Text(s(R.string.import_pattern)) }
                            }
                            LazyVerticalGrid(columns = GridCells.Adaptive(340.dp), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (patterns.isEmpty()) item { Text(s(R.string.empty_library), Modifier.padding(vertical = 32.dp)) }
                                gridItems(patterns.filter { (craft == "All" || it.craft == craft) && (it.title.contains(search, true) || it.craft.contains(search, true) || localizedCraftNames[it.craft].orEmpty().contains(search, true)) }, key = { it.id }) { p ->
                                    Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) { PatternCover(p); Column(Modifier.weight(1f)) { Text(p.title, style = MaterialTheme.typography.titleLarge); Text("${craftLabel(p.craft)} · ${if(p.mime == "application/pdf") "PDF" else s(R.string.image)}")
                                        Button(onClick = { createPattern = p }) { Text(s(R.string.start_project)) }
                                        TextButton(onClick = {
                                            val existing = projects.firstOrNull { it.patternId == p.id }
                                            if (existing != null) selected = existing.id else preview = p
                                        }) { Text(s(R.string.open_pattern)) }
                                        TextButton(onClick = { exportPattern = p; exporter.launch(exportName(p)) }) { Text(s(R.string.export_original)) }
                                    } } }
                                }
                            }
                        }
                        1 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
                            if (projects.isEmpty()) item { Text(s(R.string.empty_projects)) }
                            items(projects, key = { it.id }) { p -> Card(onClick = { selected = p.id }, modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Text(p.name, style = MaterialTheme.typography.titleLarge); Text(s(when(p.status) { "PAUSED" -> R.string.paused; "COMPLETED" -> R.string.completed; else -> R.string.active })) } } }
                        }
                        2 -> PremiumScreen(vm)
                    }
                }
            }
        }
        createPattern?.let { pattern -> ProjectDialog(null, pattern.title, onDismiss = { createPattern = null }) { name, materials, _ -> vm.act { selected = vm.repo.create(pattern, name, materials, premium, defaultCounterName); createPattern = null } } }
        preview?.let { pattern -> androidx.compose.ui.window.Dialog(onDismissRequest = { preview = null }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize().padding(16.dp)) { Column { TextButton(onClick = { preview = null }) { Text(s(R.string.close)) }; PatternViewer(pattern, Project("", pattern.id, pattern.title, page = pattern.page, zoom = pattern.zoom, panX = pattern.panX, panY = pattern.panY), vm, Modifier.weight(1f)) } }
        } }
        editing?.let { p -> ProjectDialog(p, p.name, onDismiss = { editing = null }) { name, materials, status -> vm.act { vm.repo.edit(p.id, name, materials, status); editing = null } } }
        if (newCounter && selected != null) TextDialog(R.string.add_counter, { newCounter = false }) { value -> val id = selected!!; vm.act { vm.repo.addCounter(id, value, premium); newCounter = false } }
        if (newNote && selected != null) TextDialog(R.string.add_note, { newNote = false }) { value -> val id = selected!!; vm.act { vm.repo.note(id, value); newNote = false } }
        vm.errors.value?.let { error -> AlertDialog(onDismissRequest = { vm.errors.value = null }, title = { Text(s(R.string.action_failed)) }, text = { Text(context.userMessage(error)) }, confirmButton = { TextButton(onClick = { vm.errors.value = null }) { Text(s(R.string.close)) } }) }
        if (languageDialog) AlertDialog(onDismissRequest = { languageDialog = false }, title = { Text(s(R.string.language)) }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                (listOf("") + AppLanguages.supported).forEach { tag ->
                    TextButton(onClick = {
                        AppLanguages.save(context, tag)
                        languageDialog = false
                        (context as? android.app.Activity)?.recreate()
                    }, modifier = Modifier.fillMaxWidth().testTag("language-$tag")) {
                        Text(if (tag.isEmpty()) s(R.string.language_system) else AppLanguages.nativeName(tag))
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { languageDialog = false }) { Text(s(R.string.close)) } })
    }
}

private fun exportName(pattern: Pattern): String = pattern.title + when(pattern.mime) { "application/pdf" -> ".pdf"; "image/png" -> ".png"; "image/webp" -> ".webp"; else -> ".jpg" }

@Composable fun craftLabel(value: String): String = s(when(value) { "All" -> R.string.all; "Knitting" -> R.string.knitting; "Crochet" -> R.string.crochet; "Sewing" -> R.string.sewing; else -> R.string.other })
@Composable fun TextDialog(title: Int, dismiss: () -> Unit, save: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss, title = { Text(s(title)) }, text = { OutlinedTextField(text, { text = it }, label = { Text(s(R.string.text)) }) }, confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { save(text) }) { Text(s(R.string.save)) } }, dismissButton = { TextButton(onClick = dismiss) { Text(s(R.string.cancel)) } })
}
@Composable fun ProjectDialog(project: Project?, initial: String, onDismiss: () -> Unit, save: (String, String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }; var materials by rememberSaveable { mutableStateOf(project?.materials ?: "") }; var status by rememberSaveable { mutableStateOf(project?.status ?: "ACTIVE") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(s(if (project == null) R.string.start_project else R.string.edit_project)) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(name, { name = it }, label = { Text(s(R.string.project_name)) })
        OutlinedTextField(materials, { materials = it }, label = { Text(s(R.string.materials)) })
        if (project != null) listOf("ACTIVE" to R.string.active, "PAUSED" to R.string.paused, "COMPLETED" to R.string.completed).forEach { (value, label) -> FilterChip(status == value, { status = value }, label = { Text(s(label)) }) }
    } }, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { save(name, materials, status) }) { Text(s(R.string.save)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(s(R.string.cancel)) } })
}
@Composable fun PremiumScreen(vm: CraftViewModel) {
    val products by vm.app.billing.products.collectAsState(); val message by vm.app.billing.message.collectAsState()
    val purchaseReady by vm.app.billing.purchaseReady.collectAsState()
    Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(s(R.string.premium_title), style = MaterialTheme.typography.headlineMedium)
        Text(s(R.string.premium_description))
        Text(s(R.string.free_description))
        if (vm.app.billing.configured) {
            products.forEach { product ->
                val label = if (product.sku == BillingCatalog.MONTHLY) R.string.premium_monthly else R.string.premium_annual
                Button(enabled = purchaseReady, onClick = { vm.app.billing.purchase(product.sku) }) { Text("${s(label)} · ${product.price}") }
            }
            OutlinedButton(onClick = { vm.app.billing.refresh() }) { Text(s(R.string.restore)) }
            if (products.isEmpty()) Text(s(R.string.billing_failed))
        } else Text(s(R.string.billing_unconfigured))
        Text(message)
        Text(s(R.string.subscription_terms))
        Text(s(R.string.privacy))
    }
}
