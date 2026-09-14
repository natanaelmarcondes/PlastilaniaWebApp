package com.example.plastilaniaapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

object ApiConfigManager {
    private const val PREFS_NAME = "plastilania_prefs"
    private const val KEY_API_IP = "api_ip"
    private const val KEY_API_PORTA = "api_porta"

    fun getApiIp(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_IP, "192.168.1.48") ?: "192.168.1.48"
    }

    fun setApiIp(context: Context, ip: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_IP, ip).apply()
    }

    fun getApiPorta(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_PORTA, "5555") ?: "5555"
    }

    fun setApiPorta(context: Context, porta: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_PORTA, porta).apply()
    }

    fun getBaseUrl(context: Context): String {
        var ip = getApiIp(context).trim()
        val porta = getApiPorta(context).trim()

        if (ip.isBlank()) ip = "192.168.1.48"

        if (!ip.startsWith("http://") && !ip.startsWith("https://")) {
            ip = "http://$ip"
        }
        ip = ip.trimEnd('/')

        return if (porta.isNotBlank()) {
            "$ip:$porta/"
        } else {
            "$ip/"
        }
    }
}

object PasswordManager {
    private const val PREFS_NAME = "plastilania_prefs"
    private const val KEY_SENHA_PRODUCAO = "senha_producao"
    private const val KEY_SENHA_TRANSFERENCIA = "senha_transferencia"

    fun getSenhaProducao(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SENHA_PRODUCAO, "4321") ?: "4321"
    }

    fun setSenhaProducao(context: Context, novaSenha: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SENHA_PRODUCAO, novaSenha).apply()
    }

    fun getSenhaTransferencia(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SENHA_TRANSFERENCIA, "1234") ?: "1234"
    }

    fun setSenhaTransferencia(context: Context, novaSenha: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SENHA_TRANSFERENCIA, novaSenha).apply()
    }
}

enum class ConfigTarget { SENHAS, API }

enum class OperacaoMode(
    val title: String,
    val primaryColor: Color,
    val lightColor: Color,
    val icon: ImageVector
) {
    PRODUCAO(
        title = "PRODUÇÃO",
        primaryColor = Color(0xFF2E7D32),
        lightColor = Color(0xFFE8F5E9),
        icon = Icons.Default.Factory
    ),
    TRANSFERENCIA(
        title = "TRANSFERÊNCIA",
        primaryColor = Color(0xFF1565C0),
        lightColor = Color(0xFFE3F2FD),
        icon = Icons.Default.SwapHoriz
    )
}

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
    var isEditingQuantity by remember { mutableStateOf(false) }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSplash by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    // Modo Selecionado (Produção ou Transferência)
    var selectedMode by remember { mutableStateOf<OperacaoMode?>(null) }
    var invalidScanMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    // Estados
    var grupo by remember { mutableStateOf("90 - PRODUTO ACABADO") }
    var produto by remember { mutableStateOf("AGUARDANDO LEITURA") }
    var prxCodigo by remember { mutableStateOf("") }
    var etiqueta by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("0") }
    var maxQuantidadeLida by remember { mutableStateOf(0) }
    var localOrigem by remember { mutableStateOf("01 - MATRIZ") }
    var localDestino by remember { mutableStateOf("02 - FILIAL") }

    fun resetFormState() {
        grupo = "90 - PRODUTO ACABADO"
        produto = "AGUARDANDO LEITURA"
        prxCodigo = ""
        etiqueta = ""
        quantidade = "0"
        maxQuantidadeLida = 0
        if (selectedMode == OperacaoMode.PRODUCAO) {
            localOrigem = ""
            localDestino = "01 - MATRIZ"
        } else {
            localOrigem = "01 - MATRIZ"
            localDestino = "02 - FILIAL"
        }
    }

    if (errorMessage != null) {
        ErrorScreen(
            message = errorMessage!!,
            selectedMode = selectedMode,
            onChangeMode = {
                errorMessage = null
                isConfirming = false
                selectedMode = null
                resetFormState()
            },
            onBack = {
                errorMessage = null
                isConfirming = false
                resetFormState()
            }
        )
        return
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    if (selectedMode == null) {
        ModeSelectionScreen(
            onSelectMode = { mode ->
                selectedMode = mode
                if (mode == OperacaoMode.PRODUCAO) {
                    localOrigem = ""
                    localDestino = "01 - MATRIZ"
                } else {
                    localOrigem = "01 - MATRIZ"
                    localDestino = "02 - FILIAL"
                }
                grupo = "90 - PRODUTO ACABADO"
                produto = "AGUARDANDO LEITURA"
                prxCodigo = ""
                etiqueta = ""
                quantidade = "0"
                maxQuantidadeLida = 0
            }
        )
        return
    }

    if (isEditingQuantity) {
        EditQuantityScreen(
            produto = produto,
            initialQuantity = quantidade,
            maxQuantity = maxQuantidadeLida,
            selectedMode = selectedMode,
            onChangeMode = {
                isEditingQuantity = false
                selectedMode = null
                resetFormState()
            },
            onConfirm = {
                quantidade = it
                isEditingQuantity = false
            },
            onBack = { isEditingQuantity = false }
        )
        return
    }

    if (isConfirming) {
        ConfirmationScreen(
            produto = produto,
            quantidade = quantidade,
            selectedMode = selectedMode,
            isLoading = isLoading,
            onChangeMode = {
                if (!isLoading) {
                    isConfirming = false
                    selectedMode = null
                    resetFormState()
                }
            },
            onBack = { if (!isLoading) isConfirming = false },
            onConfirm = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val api = ApiService.create(context)
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

                        if (selectedMode == OperacaoMode.TRANSFERENCIA) {
                            // Chamada 1: toaCodigo 60 (Saída da Matriz)
                            val resp1 = api.registrarEntrada(baseRequest.copy(toaCodigo = 60, locCodigo = 1))
                            // Chamada 2: toaCodigo 10 (Entrada na Filial)
                            val resp2 = api.registrarEntrada(baseRequest.copy(toaCodigo = 10, locCodigo = 2))
                            
                            if (resp1.isSuccessful && resp2.isSuccessful) {
                                isConfirming = false
                                resetFormState()
                                successMessage = "Movimentação de transferência realizada com sucesso!"
                            } else {
                                errorMessage = "Erro no servidor ao registrar a transferência. Verifique a conexão e tente novamente."
                            }
                        } else {
                            // Produção: Chamada Única: toaCodigo 10 (Entrada na Matriz)
                            val resp = api.registrarEntrada(baseRequest.copy(toaCodigo = 10, locCodigo = 1))
                            if (resp.isSuccessful) {
                                isConfirming = false
                                resetFormState()
                                successMessage = "Movimentação de produção realizada com sucesso!"
                            } else {
                                errorMessage = "Erro no servidor (Código: ${resp.code()}). Verifique a conexão ou tente novamente."
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Erro ao chamar API", e)
                        errorMessage = "Falha na conexão: ${e.message ?: e.toString()}"
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
        return
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
                    val cleanResult = result.trim()
                    if (cleanResult.startsWith("KEY|", ignoreCase = true)) {
                        val parts = cleanResult.split("|")
                        if (parts.size >= 4) {
                            val codPro = parts[1].trim()
                            val descPro = parts[2].trim()
                            val qtdStr = parts[3].trim()
                            val qtdVal = qtdStr.toIntOrNull() ?: 0

                            playSuccessBeep()

                            etiqueta = cleanResult
                            prxCodigo = codPro
                            produto = "$codPro - $descPro"
                            quantidade = qtdStr
                            maxQuantidadeLida = qtdVal
                            isScanning = false
                        } else {
                            invalidScanMessage = "A etiqueta lida possui formato inválido."
                            isScanning = false
                        }
                    } else {
                        invalidScanMessage = "Etiqueta Inválida."
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
        if (invalidScanMessage != null) {
            InvalidScanDialog(
                message = invalidScanMessage!!,
                onDismiss = { invalidScanMessage = null }
            )
        }
        if (successMessage != null) {
            SuccessDialog(
                message = successMessage!!,
                onDismiss = { successMessage = null }
            )
        }
        InventoryFormScreen(
            grupo = grupo,
            produto = produto,
            etiqueta = etiqueta,
            quantidade = quantidade,
            localOrigem = localOrigem,
            localDestino = localDestino,
            selectedMode = selectedMode,
            onChangeMode = {
                selectedMode = null
                resetFormState()
            },
            onScanClick = {
                if (hasCameraPermission) isScanning = true else launcher.launch(Manifest.permission.CAMERA)
            },
            onEtiquetaChange = { etiqueta = it },
            onQuantityClick = {
                if (produto != "AGUARDANDO LEITURA") {
                    isEditingQuantity = true
                } else {
                    invalidScanMessage = "Por favor, leia uma etiqueta primeiro antes de editar a quantidade."
                }
            },
            onDestinoChange = { novoDestino ->
                localDestino = novoDestino
            },
            onCancel = {
                resetFormState()
            },
            onConfirm = {
                val qty = quantidade.toIntOrNull() ?: 0
                if (produto == "AGUARDANDO LEITURA") {
                    invalidScanMessage = "Por favor, leia uma etiqueta primeiro antes de confirmar."
                } else if (qty <= 0) {
                    invalidScanMessage = "A quantidade deve ser maior que zero."
                } else {
                    isConfirming = true
                }
            }
        )
    }
}

@Composable
fun ModeSelectionScreen(
    onSelectMode: (OperacaoMode) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    var pendingMode by remember { mutableStateOf<OperacaoMode?>(null) }
    var configTarget by remember { mutableStateOf(ConfigTarget.SENHAS) }
    var showMasterDialog by remember { mutableStateOf(false) }
    var showConfigSenhasDialog by remember { mutableStateOf(false) }
    var showConfigApiDialog by remember { mutableStateOf(false) }

    if (pendingMode != null) {
        PasswordDialog(
            mode = pendingMode!!,
            onDismiss = { pendingMode = null },
            onSuccess = {
                val mode = pendingMode!!
                pendingMode = null
                onSelectMode(mode)
            }
        )
    }

    if (showMasterDialog) {
        MasterPasswordDialog(
            onDismiss = { showMasterDialog = false },
            onSuccess = {
                showMasterDialog = false
                if (configTarget == ConfigTarget.SENHAS) {
                    showConfigSenhasDialog = true
                } else {
                    showConfigApiDialog = true
                }
            }
        )
    }

    if (showConfigSenhasDialog) {
        ConfigurarSenhasDialog(
            onDismiss = { showConfigSenhasDialog = false }
        )
    }

    if (showConfigApiDialog) {
        ConfigurarApiDialog(
            onDismiss = { showConfigApiDialog = false }
        )
    }

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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Selecione a Operação",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0D47A1)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Escolha a modalidade de movimentação:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // Primeiro: PRODUÇÃO
            ModeOptionCard(
                title = "PRODUÇÃO",
                description = "Entrada de produção na Matriz",
                tagline = "Destino: Matriz",
                primaryColor = OperacaoMode.PRODUCAO.primaryColor,
                lightColor = OperacaoMode.PRODUCAO.lightColor,
                icon = OperacaoMode.PRODUCAO.icon,
                onClick = { pendingMode = OperacaoMode.PRODUCAO }
            )

            Spacer(Modifier.height(18.dp))

            // Segundo: TRANSFERÊNCIA
            ModeOptionCard(
                title = "TRANSFERÊNCIA",
                description = "Transferir produtos da Matriz para Filial",
                tagline = "Origem: Matriz  ➜  Destino: Filial",
                primaryColor = OperacaoMode.TRANSFERENCIA.primaryColor,
                lightColor = OperacaoMode.TRANSFERENCIA.lightColor,
                icon = OperacaoMode.TRANSFERENCIA.icon,
                onClick = { pendingMode = OperacaoMode.TRANSFERENCIA }
            )

            Spacer(Modifier.height(28.dp))

            // Botões de Configuração (Senhas e API)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        configTarget = ConfigTarget.SENHAS
                        showMasterDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE3F2FD),
                        contentColor = Color(0xFF0D47A1)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFBBDEFB))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "CONFIGURAR SENHAS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = {
                        configTarget = ConfigTarget.API
                        showMasterDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE3F2FD),
                        contentColor = Color(0xFF0D47A1)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFBBDEFB))
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "CONFIGURAR API",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botão Sair do Sistema
            Button(
                onClick = { activity?.finish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sair do Sistema",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "SAIR DO SISTEMA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ModeOptionCard(
    title: String,
    description: String,
    tagline: String,
    primaryColor: Color,
    lightColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(lightColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(lightColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tagline,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordDialog(
    mode: OperacaoMode,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val expectedPassword = if (mode == OperacaoMode.PRODUCAO) {
        PasswordManager.getSenhaProducao(context)
    } else {
        PasswordManager.getSenhaTransferencia(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = mode.primaryColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Senha - ${mode.title}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = mode.primaryColor
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Digite a senha para acessar o modo ${mode.title.lowercase()}:",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isError = false
                    },
                    label = { Text("Senha") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Ocultar senha" else "Mostrar senha"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                if (isError) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Senha incorreta! Tente novamente.",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password == expectedPassword) {
                        onSuccess()
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = mode.primaryColor)
            ) {
                Text("ACESSAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun MasterPasswordDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Acesso Restrito",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D47A1)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Digite a senha Master Keysystems para configurar as senhas:",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isError = false
                    },
                    label = { Text("Senha Master") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Ocultar senha" else "Mostrar senha"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                if (isError) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Senha Master incorreta!",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password == "5420") {
                        onSuccess()
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
            ) {
                Text("AVANÇAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ConfigurarSenhasDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var senhaProducao by remember { mutableStateOf(PasswordManager.getSenhaProducao(context)) }
    var senhaTransferencia by remember { mutableStateOf(PasswordManager.getSenhaTransferencia(context)) }
    var isProdVisible by remember { mutableStateOf(false) }
    var isTransfVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Configurar Senhas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D47A1)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Defina as novas senhas de acesso:",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                
                OutlinedTextField(
                    value = senhaProducao,
                    onValueChange = { senhaProducao = it },
                    label = { Text("Senha - PRODUÇÃO") },
                    singleLine = true,
                    visualTransformation = if (isProdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { isProdVisible = !isProdVisible }) {
                            Icon(
                                imageVector = if (isProdVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = senhaTransferencia,
                    onValueChange = { senhaTransferencia = it },
                    label = { Text("Senha - TRANSFERÊNCIA") },
                    singleLine = true,
                    visualTransformation = if (isTransfVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { isTransfVisible = !isTransfVisible }) {
                            Icon(
                                imageVector = if (isTransfVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (senhaProducao.isBlank() || senhaTransferencia.isBlank()) {
                        Toast.makeText(context, "As senhas não podem ser vazias!", Toast.LENGTH_SHORT).show()
                    } else {
                        PasswordManager.setSenhaProducao(context, senhaProducao.trim())
                        PasswordManager.setSenhaTransferencia(context, senhaTransferencia.trim())
                        Toast.makeText(context, "Senhas alteradas com sucesso!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("SALVAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ConfigurarApiDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var ip by remember { mutableStateOf(ApiConfigManager.getApiIp(context)) }
    var porta by remember { mutableStateOf(ApiConfigManager.getApiPorta(context)) }

    val formattedPreview = remember(ip, porta) {
        var tempIp = ip.trim()
        if (tempIp.isBlank()) tempIp = "192.168.1.48"
        if (!tempIp.startsWith("http://") && !tempIp.startsWith("https://")) {
            tempIp = "http://$tempIp"
        }
        tempIp = tempIp.trimEnd('/')
        val tempPorta = porta.trim()
        if (tempPorta.isNotBlank()) "$tempIp:$tempPorta/" else "$tempIp/"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Configurar API / Servidor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D47A1)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Defina o IP e a Porta do servidor de API:",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("Endereço IP ou Servidor") },
                    placeholder = { Text("Ex: 192.168.1.48") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = porta,
                    onValueChange = { porta = it },
                    label = { Text("Porta") },
                    placeholder = { Text("Ex: 5555") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "URL Resultante:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = formattedPreview,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0D47A1)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ip.isBlank()) {
                        Toast.makeText(context, "O IP do servidor não pode ser vazio!", Toast.LENGTH_SHORT).show()
                    } else {
                        ApiConfigManager.setApiIp(context, ip.trim())
                        ApiConfigManager.setApiPorta(context, porta.trim())
                        Toast.makeText(context, "Configurações de API salvas!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("SALVAR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun InvalidScanDialog(
    title: String = "Atenção!",
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(52.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFB71C1C),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(52.dp)
            )
        },
        title = {
            Text(
                text = "Sucesso!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun ConfirmationScreen(
    produto: String,
    quantidade: String,
    selectedMode: OperacaoMode? = null,
    isLoading: Boolean = false,
    onChangeMode: (() -> Unit)? = null,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F7FF), Color(0xFFDDEBFF))))
            .systemBarsPadding()
    ) {
        HeaderSection(selectedMode = selectedMode, onChangeMode = onChangeMode)

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
                    CircularProgressIndicator(
                        color = Color(0xFF00BFFF),
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 6.dp
                    )
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
fun ErrorScreen(
    message: String,
    selectedMode: OperacaoMode? = null,
    onChangeMode: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F7FF), Color(0xFFDDEBFF))))
            .systemBarsPadding()
    ) {
        HeaderSection(selectedMode = selectedMode, onChangeMode = onChangeMode)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Ops! Algo deu errado",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFB71C1C)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF5F5), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "DETALHE TÉCNICO PARA O SUPORTE:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF424242),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Close, null)
                Spacer(Modifier.width(8.dp))
                Text("VOLTAR AO INÍCIO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditQuantityScreen(
    produto: String,
    initialQuantity: String,
    maxQuantity: Int,
    selectedMode: OperacaoMode? = null,
    onChangeMode: (() -> Unit)? = null,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialQuantity,
                selection = TextRange(initialQuantity.length)
            )
        )
    }
    
    val currentEnteredQty = textFieldValue.text.toIntOrNull() ?: 0
    val isExceedingMax = maxQuantity > 0 && currentEnteredQty > maxQuantity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF0F7FF), Color(0xFFDDEBFF))))
            .systemBarsPadding()
    ) {
        HeaderSection(selectedMode = selectedMode, onChangeMode = onChangeMode)

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
                    Text(
                        "Editar Quantidade",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0D47A1)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        produto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (maxQuantity > 0) {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "MÁXIMO PERMITIDO (ETIQUETA): $maxQuantity",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Display e Input Manual
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            if (newValue.text.all { char -> char.isDigit() }) {
                                val valInt = newValue.text.toIntOrNull() ?: 0
                                if (maxQuantity <= 0 || valInt <= maxQuantity) {
                                    textFieldValue = newValue
                                } else {
                                    textFieldValue = TextFieldValue(
                                        text = maxQuantity.toString(),
                                        selection = TextRange(maxQuantity.toString().length)
                                    )
                                    Toast.makeText(
                                        context,
                                        "Quantidade máxima permitida é $maxQuantity",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = isExceedingMax,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isExceedingMax) Color(0xFFD32F2F) else Color(0xFF0D47A1),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (isExceedingMax) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "A quantidade não pode ser maior que a quantidade lida ($maxQuantity)",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Quick Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickQuantityButton(text = "-10", onClick = { 
                            val current = textFieldValue.text.toIntOrNull() ?: 0
                            val newText = (current - 10).coerceAtLeast(0).toString()
                            textFieldValue = TextFieldValue(newText, TextRange(newText.length))
                        }, modifier = Modifier.weight(1f))
                        QuickQuantityButton(text = "-1", onClick = { 
                            val current = textFieldValue.text.toIntOrNull() ?: 0
                            val newText = (current - 1).coerceAtLeast(0).toString()
                            textFieldValue = TextFieldValue(newText, TextRange(newText.length))
                        }, modifier = Modifier.weight(1f))
                        QuickQuantityButton(text = "+1", onClick = { 
                            val current = textFieldValue.text.toIntOrNull() ?: 0
                            val target = current + 1
                            val capped = if (maxQuantity > 0) target.coerceAtMost(maxQuantity) else target
                            if (target > maxQuantity && maxQuantity > 0) {
                                Toast.makeText(context, "Limite máximo da etiqueta é $maxQuantity", Toast.LENGTH_SHORT).show()
                            }
                            val newText = capped.toString()
                            textFieldValue = TextFieldValue(newText, TextRange(newText.length))
                        }, modifier = Modifier.weight(1f))
                        QuickQuantityButton(text = "+10", onClick = { 
                            val current = textFieldValue.text.toIntOrNull() ?: 0
                            val target = current + 10
                            val capped = if (maxQuantity > 0) target.coerceAtMost(maxQuantity) else target
                            if (target > maxQuantity && maxQuantity > 0) {
                                Toast.makeText(context, "Limite máximo da etiqueta é $maxQuantity", Toast.LENGTH_SHORT).show()
                            }
                            val newText = capped.toString()
                            textFieldValue = TextFieldValue(newText, TextRange(newText.length))
                        }, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { 
                    val qty = textFieldValue.text.toIntOrNull() ?: 0
                    if (qty <= 0) {
                        Toast.makeText(context, "A quantidade deve ser maior que zero!", Toast.LENGTH_SHORT).show()
                    } else if (maxQuantity > 0 && qty > maxQuantity) {
                        Toast.makeText(context, "A quantidade não pode ser maior que $maxQuantity!", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirm(textFieldValue.text)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null)
                Spacer(Modifier.width(8.dp))
                Text("CONFIRMAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Close, null)
                Spacer(Modifier.width(8.dp))
                Text("CANCELAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickQuantityButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF0D47A1)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun InventoryFormScreen(
    grupo: String,
    produto: String,
    etiqueta: String,
    quantidade: String,
    localOrigem: String,
    localDestino: String,
    selectedMode: OperacaoMode? = null,
    onChangeMode: (() -> Unit)? = null,
    onScanClick: () -> Unit,
    onEtiquetaChange: (String) -> Unit,
    onQuantityClick: () -> Unit,
    onDestinoChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDestinoMenu by remember { mutableStateOf(false) }
    val isTransferencia = selectedMode == OperacaoMode.TRANSFERENCIA

    val modePrimaryColor = selectedMode?.primaryColor ?: Color(0xFF1976D2)
    val modeLightColor = selectedMode?.lightColor ?: Color(0xFFE3F2FD)
    val bgGradient = if (selectedMode == OperacaoMode.PRODUCAO) {
        listOf(Color(0xFFF4FBF5), Color(0xFFE2F4E5))
    } else {
        listOf(Color(0xFFF0F7FF), Color(0xFFDDEBFF))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient))
            .systemBarsPadding()
    ) {
        HeaderSection(selectedMode = selectedMode, onChangeMode = onChangeMode)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(2.dp, modePrimaryColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (selectedMode != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(modeLightColor, RoundedCornerShape(8.dp))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = selectedMode.icon,
                                contentDescription = null,
                                tint = modePrimaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "OPERAÇÃO: ${selectedMode.title}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = modePrimaryColor
                            )
                        }
                    }
                }

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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = modePrimaryColor,
                            contentColor = Color.White
                        ),
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
                    IconBoxCompact(Icons.AutoMirrored.Filled.Sort, "Quantidade")
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
                        Icon(Icons.Default.Edit, null, tint = modePrimaryColor, modifier = Modifier.size(20.dp))
                    }
                }

                FormFieldRowCompact(Icons.Default.NorthEast, "Origem", localOrigem, false)
                
                // Destino (com Menu apenas se Transferência)
                Box {
                    FormFieldRowCompact(
                        icon = Icons.Default.SouthEast,
                        label = "Destino",
                        value = localDestino,
                        isDropdown = isTransferencia,
                        onClick = if (isTransferencia) { { showDestinoMenu = true } } else null
                    )
                    if (isTransferencia) {
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
                colors = ButtonDefaults.buttonColors(containerColor = modePrimaryColor, contentColor = Color.White),
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
fun HeaderSection(
    selectedMode: OperacaoMode? = null,
    onChangeMode: (() -> Unit)? = null,
    onExitApp: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_key),
                contentDescription = "Logo KeySystems",
                modifier = Modifier.size(38.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(6.dp))
            Text("Keysystems Informática", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1))
            Text(" | PLASTILANIA", fontSize = 11.sp, color = Color(0xFF1976D2))
        }

        val onButtonClick = onChangeMode ?: onExitApp
        if (onButtonClick != null) {
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sair",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Sair",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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

private fun playSuccessBeep() {
    try {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    } catch (e: Exception) {
        Log.e("BEEP_ERROR", "Erro ao emitir som de bip", e)
    }
}
