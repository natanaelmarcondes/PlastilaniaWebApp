package com.example.plastilaniaapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.plastilaniaapp.ui.theme.PlastilaniaAppTheme
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            PlastilaniaAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var showQuantityDialog by remember { mutableStateOf(false) }
    var isConfirming by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    // Estados
    var grupo by remember { mutableStateOf("90 - PRODUTO ACABADO") }
    var produto by remember { mutableStateOf("AGUARDANDO LEITURA") }
    var prxCodigo by remember { mutableStateOf("") }
    var etiqueta by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("0") }
    var localOrigem by remember { mutableStateOf("01 - MATRIZ") }
    var localDestino by remember { mutableStateOf("02 - FILIAL") }

    if (isConfirming) {
        ConfirmationScreen(
            produto = produto,
            quantidade = quantidade,
            isLoading = isLoading,
            onBack = { if (!isLoading) isConfirming = false },
            onConfirm = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val api = ApiService.create()
                        val dataAtual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        
                        val baseRequest = MovimentacaoRequest(
                            empCodigo = "01",
                            mrcCodigo = "01",
                            dataOcorrencia = dataAtual,
                            numeroDocumento = 1,
                            quantidade = quantidade.toIntOrNull() ?: 0,
                            servidorB = true,
                            toaCodigo = 10,
                            gpxCodigo = 81,
                            prxCodigo = prxCodigo,
                            locCodigo = 2,
                            endereco = "",
                            cliFor = 0
                        )

                        if (localOrigem == "01 - MATRIZ" && localDestino == "02 - FILIAL") {
                            // Chamada 1: toaCodigo 60 (Saída da Matriz)
                            val resp1 = api.registrarEntrada(baseRequest.copy(toaCodigo = 60, locCodigo = 1))
                            // Chamada 2: toaCodigo 10 (Entrada na Filial)
                            val resp2 = api.registrarEntrada(baseRequest.copy(toaCodigo = 10, locCodigo = 2))
                            
                            if (resp1.isSuccessful && resp2.isSuccessful) {
                                Toast.makeText(context, "Movimentação de estoque realizada com sucesso!", Toast.LENGTH_SHORT).show()
                                isConfirming = false
                                // Reset
                                produto = "AGUARDANDO LEITURA"
                                prxCodigo = ""
                                etiqueta = ""
                                quantidade = "0"
                            } else {
                                Toast.makeText(context, "Erro em uma das chamadas", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            // Chamada Única: toaCodigo 10 (Entrada na Matriz)
                            val resp = api.registrarEntrada(baseRequest.copy(toaCodigo = 10, locCodigo = 1))
                            if (resp.isSuccessful) {
                                Toast.makeText(context, "Movimentação realizada com sucesso!", Toast.LENGTH_SHORT).show()
                                isConfirming = false
                                // Reset
                                produto = "AGUARDANDO LEITURA"
                                prxCodigo = ""
                                etiqueta = ""
                                quantidade = "0"
                            } else {
                                Toast.makeText(context, "Erro: ${resp.code()}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Erro ao chamar API", e)
                        Toast.makeText(context, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
        return
    }

    if (showQuantityDialog) {
        QuantityDialog(
            initialValue = quantidade,
            onConfirm = {
                quantidade = it
                showQuantityDialog = false
            },
            onDismiss = { showQuantityDialog = false }
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) isScanning = true else Toast.makeText(context, "Permissão necessária", Toast.LENGTH_SHORT).show()
    }

    if (isScanning && hasCameraPermission) {
        BackHandler { isScanning = false }
        Box(modifier = Modifier.fillMaxSize()) {
            CameraScanner(
                onResult = { result ->
                    val parts = result.split("|")
                    if (parts.size >= 3) {
                        etiqueta = parts[0]
                        prxCodigo = parts[0]
                        produto = "${parts[0]} - ${parts[1]}"
                        quantidade = parts[2]
                        isScanning = false
                    } else {
                        etiqueta = result
                        prxCodigo = result
                        produto = result
                        isScanning = false
                    }
                }
            )
            IconButton(
                onClick = { isScanning = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    } else {
        InventoryFormScreen(
            grupo = grupo,
            produto = produto,
            etiqueta = etiqueta,
            quantidade = quantidade,
            localOrigem = localOrigem,
            localDestino = localDestino,
            onScanClick = {
                if (hasCameraPermission) isScanning = true else launcher.launch(Manifest.permission.CAMERA)
            },
            onEtiquetaChange = { etiqueta = it },
            onQuantityClick = {
                if (produto != "AGUARDANDO LEITURA") {
                    showQuantityDialog = true
                } else {
                    Toast.makeText(context, "Leia um produto primeiro!", Toast.LENGTH_SHORT).show()
                }
            },
            onDestinoChange = { novoDestino ->
                localDestino = novoDestino
                if (novoDestino.contains("MATRIZ")) {
                    localOrigem = ""
                } else {
                    localOrigem = "01 - MATRIZ"
                }
            },
            onCancel = {
                grupo = "90 - MATERIAL ACABADO"
                produto = "AGUARDANDO LEITURA"
                prxCodigo = ""
                etiqueta = ""
                quantidade = "0"
                localOrigem = "01 - MATRIZ"
                localDestino = "02 - FILIAL"
            },
            onConfirm = {
                val qty = quantidade.toIntOrNull() ?: 0
                if (produto == "AGUARDANDO LEITURA") {
                    Toast.makeText(context, "Aguardando leitura do produto", Toast.LENGTH_SHORT).show()
                } else if (qty <= 1) {
                    Toast.makeText(context, "Quantidade deve ser maior que 1", Toast.LENGTH_SHORT).show()
                } else {
                    isConfirming = true
                }
            }
        )
    }
}

@Composable
fun ConfirmationScreen(
    produto: String,
    quantidade: String,
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F7FF), Color(0xFFDDEBFF))))
            .systemBarsPadding()
    ) {
        HeaderSection()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Confirmar Movimentação",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0D47A1)
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F8FE), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("PRODUTO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        Text(produto, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text("QUANTIDADE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        Text(quantidade, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("SIM, CONFIRMAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Close, null)
                Spacer(Modifier.width(8.dp))
                Text("NÃO, VOLTAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuantityDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(0, initialValue.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Quantidade", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Aceita apenas números inteiros
                    if (newValue.text.all { it.isDigit() }) {
                        textFieldValue = newValue
                    }
                },
                label = { Text("Nova Quantidade") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(textFieldValue.text) }) {
                Text("Confirmar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun InventoryFormScreen(
    grupo: String,
    produto: String,
    etiqueta: String,
    quantidade: String,
    localOrigem: String,
    localDestino: String,
    onScanClick: () -> Unit,
    onEtiquetaChange: (String) -> Unit,
    onQuantityClick: () -> Unit,
    onDestinoChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDestinoMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F7FF), Color(0xFFDDEBFF))))
            .systemBarsPadding()
    ) {
        HeaderSection()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FormFieldRowCompact(Icons.Default.Category, "Grupo", grupo, false)

                // Botão de Leitura (Etiqueta) - Movido para antes do Prod.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onScanClick,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("LER ETIQUETA", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                FormFieldRowCompact(Icons.Default.Inventory, "Produto", produto, false)
                
                // Qtd Row
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconBoxCompact(Icons.Default.Numbers, "Quantidade")
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(Color(0xFFF1F8FE), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFBBDEFB), RoundedCornerShape(6.dp))
                            .clickable { onQuantityClick() },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = quantidade,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1),
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = onQuantityClick,
                        modifier = Modifier.size(50.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                    }
                }

                FormFieldRowCompact(Icons.Default.NorthEast, "Origem", localOrigem, false)
                
                // Destino com Menu
                Box {
                    FormFieldRowCompact(
                        icon = Icons.Default.SouthEast,
                        label = "Destino",
                        value = localDestino,
                        isDropdown = true,
                        onClick = { showDestinoMenu = true }
                    )
                    DropdownMenu(
                        expanded = showDestinoMenu,
                        onDismissRequest = { showDestinoMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("01 - MATRIZ") },
                            onClick = {
                                onDestinoChange("01 - MATRIZ")
                                showDestinoMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("02 - FILIAL") },
                            onClick = {
                                onDestinoChange("02 - FILIAL")
                                showDestinoMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cancelar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Confirmar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_key),
                contentDescription = "KeySystems Logo",
                modifier = Modifier
                    .size(180.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Keysystems Informática",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1)
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_key),
            contentDescription = "Logo KeySystems",
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(10.dp))
        Text("ESTOQUE", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1))
        Text(" | PLASTILANIA", fontSize = 13.sp, color = Color(0xFF1976D2))
    }
}

@Composable
fun IconBoxCompact(icon: ImageVector, label: String) {
    Row(modifier = Modifier.width(110.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(28.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = label, 
            fontWeight = FontWeight.ExtraBold, 
            fontSize = 13.sp, 
            color = Color(0xFF000000),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FormFieldRowCompact(
    icon: ImageVector, 
    label: String, 
    value: String, 
    isDropdown: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val isAguardando = value == "AGUARDANDO LEITURA"
    val alpha by if (isAguardando) {
        val infiniteTransition = rememberInfiniteTransition(label = "blink")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBoxCompact(icon, label)
        Spacer(Modifier.width(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false, // Para permitir o clique no Row
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 50.dp),
            trailingIcon = if (isDropdown) {
                { Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(20.dp), tint = Color(0xFF0D47A1)) }
            } else null,
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = alpha)
            ),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color(0xFFBDBDBD),
                disabledTextColor = Color.Black.copy(alpha = alpha),
                disabledContainerColor = Color(0xFFF9F9F9)
            )
        )
    }
}

@Composable
fun CameraScanner(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val mainExecutor = ContextCompat.getMainExecutor(context)

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply { layoutParams = ViewGroup.LayoutParams(-1, -1) }
            val analyzerExecutor = Executors.newSingleThreadExecutor()
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val scanner = BarcodeScanning.getClient()
                val analyzer = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                analyzer.setAnalyzer(analyzerExecutor) { imageProxy ->
                    processImageProxy(scanner, imageProxy) { result -> mainExecutor.execute { onResult(result) } }
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)
                } catch (e: Exception) { Log.e("Scanner", "Erro", e) }
            }, mainExecutor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy, onResult: (String) -> Unit) {
    imageProxy.image?.let { mediaImage ->
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes -> barcodes.firstOrNull()?.rawValue?.let { onResult(it) } }
            .addOnCompleteListener { imageProxy.close() }
    } ?: imageProxy.close()
}
