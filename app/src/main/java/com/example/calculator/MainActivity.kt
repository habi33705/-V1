package com.example.calculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.CalculatorTheme
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme(dynamicColor = false) {
                EngineeringCalculatorApp()
            }
        }
    }
}

private data class Formula(
    val name: String,
    val expression: String,
    val variables: List<String>,
    val category: String
)

private data class FormulaProfile(
    val name: String,
    val formulas: List<Formula>
)

private enum class AngleMode(val label: String) {
    Rad("RAD"),
    Deg("DEG")
}

private enum class DisplayMode(val label: String) {
    Normal("Norm"),
    Fix2("Fix 2"),
    Fix4("Fix 4")
}

private enum class CursorBlinkSpeed(val label: String, val intervalMillis: Long) {
    Fast("速い", 300),
    Normal("普通", 500),
    Slow("遅い", 800)
}

private data class CalculationHistory(
    val expression: String,
    val result: String,
    val numericResult: Double?
)

private val engineeringProfiles = listOf(
    FormulaProfile(
        name = "機械",
        formulas = listOf(
            Formula("応力 σ = F/A", "F/A", listOf("F", "A"), "材料力学"),
            Formula("ひずみ ε = ΔL/L", "dL/L", listOf("dL", "L"), "材料力学"),
            Formula("軸伸び ΔL = FL/AE", "F*L/(A*E)", listOf("F", "L", "A", "E"), "材料力学"),
            Formula("トルク T = F r", "F*r", listOf("F", "r"), "機械要素"),
            Formula("動力 P = 2πNT/60", "2*pi*N*T/60", listOf("N", "T"), "回転機械")
        )
    ),
    FormulaProfile(
        name = "電気",
        formulas = listOf(
            Formula("オーム V = I R", "I*R", listOf("I", "R"), "基礎"),
            Formula("電力 P = V I", "V*I", listOf("V", "I"), "基礎"),
            Formula("合成抵抗 並列", "1/(1/R1+1/R2)", listOf("R1", "R2"), "回路"),
            Formula("容量リアクタンス Xc", "1/(2*pi*f*C)", listOf("f", "C"), "交流"),
            Formula("インダクタンスリアクタンス XL", "2*pi*f*L", listOf("f", "L"), "交流")
        )
    ),
    FormulaProfile(
        name = "土木・熱",
        formulas = listOf(
            Formula("圧力 p = F/A", "F/A", listOf("F", "A"), "流体"),
            Formula("流量 Q = A v", "A*v", listOf("A", "v"), "流体"),
            Formula("熱量 Q = m c ΔT", "m*c*dT", listOf("m", "c", "dT"), "熱"),
            Formula("熱伝導 q = kAΔT/L", "k*A*dT/L", listOf("k", "A", "dT", "L"), "熱"),
            Formula("梁曲げ応力 σ = Mc/I", "M*c/I", listOf("M", "c", "I"), "構造")
        )
    )
)

private val buttonRows = listOf(
    listOf("SHIFT", "ALPHA", "MODE", "SETUP", "ON"),
    listOf("a/b", "x^-1", "x^2", "sqrt", "^", "log", "ln"),
    listOf("(-)", "°′″", "hyp", "sin", "cos", "tan"),
    listOf("RCL", "ENG", "(", ")", "S⇔D", "M+"),
    listOf("7", "8", "9", "DEL", "AC"),
    listOf("4", "5", "6", "×", "÷"),
    listOf("1", "2", "3", "+", "-"),
    listOf("0", ".", "EXP", "Ans", "=")
)

@Composable
private fun EngineeringCalculatorApp() {
    val context = LocalContext.current
    var expressionValue by remember { mutableStateOf(TextFieldValue("")) }
    var result by remember { mutableStateOf("0") }
    var lastNumericResult by remember { mutableStateOf<Double?>(null) }
    var resultAsFraction by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) }
    var selectedProfile by remember { mutableIntStateOf(0) }
    var presetName by remember { mutableStateOf("") }
    var secretVisible by remember { mutableStateOf(false) }
    var modePanelVisible by remember { mutableStateOf(false) }
    var setupPanelVisible by remember { mutableStateOf(false) }
    var angleMode by remember { mutableStateOf(AngleMode.Rad) }
    var displayMode by remember { mutableStateOf(DisplayMode.Normal) }
    var cursorBlinkSpeed by remember { mutableStateOf(CursorBlinkSpeed.Normal) }
    var shiftActive by remember { mutableStateOf(false) }
    var alphaActive by remember { mutableStateOf(false) }
    var historyIndex by remember { mutableIntStateOf(-1) }
    val presets = remember { mutableStateListOf<Formula>().apply { addAll(loadPresets(context)) } }
    val calculationHistory = remember { mutableStateListOf<CalculationHistory>() }
    val expression = expressionValue.text

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFFE8ECEF)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorDisplay(
                    expressionValue = expressionValue,
                    onExpressionValueChange = {
                        expressionValue = it
                        showResult = false
                    },
                    result = result,
                    showResult = showResult,
                    angleMode = angleMode,
                    displayMode = displayMode,
                    cursorBlinkSpeed = cursorBlinkSpeed
                )
                PrimaryTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFFD9DEE3),
                    contentColor = Color(0xFF10202D)
                ) {
                    listOf("計算", "公式", "プリセット").forEachIndexed { index, title ->
                        Tab(selected = activeTab == index, onClick = { activeTab = index }, text = { Text(title) })
                    }
                }

                when (activeTab) {
                    0 -> CalculatorKeypad(
                        modifier = Modifier.weight(1f),
                        shiftActive = shiftActive,
                        alphaActive = alphaActive,
                        onKey = { key ->
                            when (key) {
                                "SHIFT" -> {
                                    shiftActive = !shiftActive
                                    alphaActive = false
                                    showResult = false
                                    result = if (shiftActive) "SHIFT" else "0"
                                }

                                "ALPHA" -> {
                                    alphaActive = !alphaActive
                                    shiftActive = false
                                    showResult = false
                                    result = if (alphaActive) "ALPHA" else "0"
                                }

                                "MODE" -> {
                                    modePanelVisible = true
                                    shiftActive = false
                                    alphaActive = false
                                    showResult = false
                                }

                                "SETUP" -> {
                                    setupPanelVisible = true
                                    shiftActive = false
                                    alphaActive = false
                                    showResult = false
                                }

                                "ON" -> {
                                    expressionValue = TextFieldValue("")
                                    result = "0"
                                    lastNumericResult = null
                                    resultAsFraction = false
                                    showResult = false
                                    calculationHistory.clear()
                                    historyIndex = -1
                                    shiftActive = false
                                    alphaActive = false
                                }

                                "↑", "↓" -> {
                                    val nextIndex = nextHistoryIndex(
                                        key = key,
                                        currentIndex = historyIndex,
                                        historySize = calculationHistory.size
                                    )
                                    if (nextIndex != -1) {
                                        val item = calculationHistory[nextIndex]
                                        historyIndex = nextIndex
                                        expressionValue = item.expression.asExpressionValue()
                                        result = item.result
                                        lastNumericResult = item.numericResult
                                        resultAsFraction = false
                                        showResult = true
                                    }
                                }

                                else -> {
                                    showResult = key == "=" || key == "S⇔D"
                                    if (key == "=" && expression == "114514") {
                                        secretVisible = true
                                    }
                                    handleKey(
                                        key = key,
                                        expressionValue = expressionValue,
                                        result = result,
                                        lastNumericResult = lastNumericResult,
                                        resultAsFraction = resultAsFraction,
                                        angleMode = angleMode,
                                        displayMode = displayMode,
                                        shiftActive = shiftActive,
                                        alphaActive = alphaActive,
                                        onExpressionValue = { expressionValue = it },
                                        onResult = { result = it },
                                        onNumericResult = {
                                            lastNumericResult = it
                                            resultAsFraction = false
                                        },
                                        onResultAsFraction = { resultAsFraction = it },
                                        onHistoryAdd = { item ->
                                            calculationHistory.add(item)
                                            historyIndex = calculationHistory.lastIndex
                                        }
                                    )
                                    if (key.consumesModifier()) {
                                        shiftActive = false
                                        alphaActive = false
                                    }
                                }
                            }
                        }
                    )

                    1 -> FormulaProfiles(
                        modifier = Modifier.weight(1f),
                        selectedProfile = selectedProfile,
                        onProfileSelected = { selectedProfile = it },
                        onEvaluate = { formula ->
                            expressionValue = formula.expression.asExpressionValue()
                            showResult = false
                            activeTab = 0
                        }
                    )

                    else -> PresetEditor(
                        modifier = Modifier.weight(1f),
                        presets = presets,
                        presetName = presetName,
                        expression = expression,
                        onNameChange = { presetName = it },
                        onExpressionChange = {
                            expressionValue = it.asExpressionValue()
                            showResult = false
                        },
                        onInsert = {
                            expressionValue = it.expression.asExpressionValue()
                            showResult = false
                            activeTab = 0
                        },
                        onDelete = { formula ->
                            presets.remove(formula)
                            savePresets(context, presets)
                        },
                        onSave = {
        val cleanName = presetName.trim().ifBlank { "ユーザー公式 ${presets.size + 1}" }
                        if (expression.isNotBlank()) {
                                presets.add(
                                    Formula(
                                        name = cleanName,
                                        expression = expression,
                                        variables = extractVariables(expression),
                                        category = "ユーザー"
                                    )
                                )
                                savePresets(context, presets)
                                presetName = ""
                            }
                        }
                    )
                }
            }
        }
        if (secretVisible) {
            SecretImageOverlay(
                onDismiss = {
                    secretVisible = false
                }
            )
        }
        if (modePanelVisible) {
            ModePanel(
                angleMode = angleMode,
                onSelectAngleMode = {
                    angleMode = it
                    result = it.label
                    modePanelVisible = false
                },
                onDismiss = { modePanelVisible = false }
            )
        }
        if (setupPanelVisible) {
            SetupPanel(
                displayMode = displayMode,
                cursorBlinkSpeed = cursorBlinkSpeed,
                onSelectDisplayMode = {
                    displayMode = it
                    result = it.label
                },
                onSelectCursorBlinkSpeed = {
                    cursorBlinkSpeed = it
                    result = "CUR ${it.label}"
                },
                onDismiss = { setupPanelVisible = false }
            )
        }
    }
}

@Composable
private fun SetupPanel(
    displayMode: DisplayMode,
    cursorBlinkSpeed: CursorBlinkSpeed,
    onSelectDisplayMode: (DisplayMode) -> Unit,
    onSelectCursorBlinkSpeed: (CursorBlinkSpeed) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFFF6F8FA),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("SETUP", color = Color(0xFF10202D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("結果表示", color = Color(0xFF55606A), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DisplayMode.values().forEach { mode ->
                        SetupChoiceButton(
                            label = mode.label,
                            selected = displayMode == mode,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectDisplayMode(mode) }
                        )
                    }
                }
                Text("カーソル点滅", color = Color(0xFF55606A), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CursorBlinkSpeed.values().forEach { speed ->
                        SetupChoiceButton(
                            label = speed.label,
                            selected = cursorBlinkSpeed == speed,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectCursorBlinkSpeed(speed) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupChoiceButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(7.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF234D6F) else Color(0xFFE7EDF2),
            contentColor = if (selected) Color.White else Color(0xFF10202D)
        )
    ) {
        Text(label, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ModePanel(
    angleMode: AngleMode,
    onSelectAngleMode: (AngleMode) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFFF6F8FA),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("MODE", color = Color(0xFF10202D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("角度単位", color = Color(0xFF55606A), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AngleMode.values().forEach { mode ->
                        ElevatedButton(
                            onClick = { onSelectAngleMode(mode) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(7.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (angleMode == mode) Color(0xFF234D6F) else Color(0xFFE7EDF2),
                                contentColor = if (angleMode == mode) Color.White else Color(0xFF10202D)
                            )
                        ) {
                            Text(mode.label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecretImageOverlay(onDismiss: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.secret_114514),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onDismiss),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun CalculatorDisplay(
    expressionValue: TextFieldValue,
    onExpressionValueChange: (TextFieldValue) -> Unit,
    result: String,
    showResult: Boolean,
    angleMode: AngleMode,
    displayMode: DisplayMode,
    cursorBlinkSpeed: CursorBlinkSpeed
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(expressionValue.text, expressionValue.selection) {
        cursorVisible = true
        while (true) {
            delay(cursorBlinkSpeed.intervalMillis)
            cursorVisible = !cursorVisible
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp),
        color = Color(0xFFB9C8B4),
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            val visualExpression = expressionValue.toCursorText(cursorVisible)
            ExpressionPreview(
                expression = visualExpression,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            )
            BasicTextField(
                value = expressionValue,
                onValueChange = onExpressionValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .onFocusChanged { if (it.isFocused) keyboardController?.hide() },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Transparent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                ),
                cursorBrush = SolidColor(Color.Transparent),
                maxLines = 2,
                decorationBox = { innerTextField -> innerTextField() }
            )
            if (showResult) {
                ResultPreview(
                    result = result,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd)
                )
            }
            Text(
                text = "${angleMode.label}  ${displayMode.label}",
                modifier = Modifier.align(Alignment.BottomStart),
                color = Color(0xFF314631),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ResultPreview(result: String, modifier: Modifier = Modifier) {
    val fraction = result.toResultFractionParts()
    if (fraction == null) {
        Text(
            text = result,
            modifier = modifier,
            color = Color(0xFF061006),
            fontFamily = FontFamily.Monospace,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (fraction.whole.isNotBlank()) {
                Text(
                    text = fraction.whole,
                    color = Color(0xFF061006),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = fraction.numerator,
                    color = Color(0xFF061006),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
                Box(
                    modifier = Modifier
                        .width(fraction.lineWidthDp.dp)
                        .height(2.dp)
                        .background(Color(0xFF061006))
                )
                Text(
                    text = fraction.denominator,
                    color = Color(0xFF061006),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ExpressionPreview(expression: String, modifier: Modifier = Modifier) {
    val fraction = expression.toFractionParts()
    if (fraction == null) {
        Text(
            text = expression.toDisplayExpression(),
            modifier = modifier,
            color = Color(0xFF132113),
            fontFamily = FontFamily.Monospace,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start
        )
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = fraction.numerator.toDisplayExpression().ifBlank { " " },
                color = Color(0xFF132113),
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .width(fraction.lineWidthDp.dp)
                    .height(2.dp)
                    .background(Color(0xFF132113))
            )
            Text(
                text = fraction.denominator.toDisplayExpression().ifBlank { " " },
                color = Color(0xFF132113),
                fontFamily = FontFamily.Monospace,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun TextFieldValue.toCursorText(cursorVisible: Boolean): String {
    val cursorMark = if (cursorVisible) "|" else " "
    if (text.isBlank()) return cursorMark
    val cursorIndex = selection.end.coerceIn(0, text.length)
    return text.substring(0, cursorIndex) + cursorMark + text.substring(cursorIndex)
}

private data class FractionParts(
    val numerator: String,
    val denominator: String,
    val lineWidthDp: Int
)

private data class ResultFractionParts(
    val whole: String,
    val numerator: String,
    val denominator: String,
    val lineWidthDp: Int
)

private fun String.toResultFractionParts(): ResultFractionParts? {
    if (!contains('/')) return null
    val tokens = trim().split(Regex("\\s+"))
    val whole = if (tokens.size == 2) tokens[0] else ""
    val fractionText = if (tokens.size == 2) tokens[1] else tokens.firstOrNull().orEmpty()
    val parts = fractionText.split('/')
    if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) return null
    val width = maxOf(parts[0].length, parts[1].length, 1) * 14 + 12
    return ResultFractionParts(
        whole = whole,
        numerator = parts[0],
        denominator = parts[1],
        lineWidthDp = width.coerceIn(28, 160)
    )
}

private fun String.toFractionParts(): FractionParts? {
    val slashIndex = indexOfTopLevelFractionSlash()
    if (slashIndex == -1) return null
    val numerator = substring(0, slashIndex).trim().trimOuterParentheses()
    val denominator = substring(slashIndex + 1).trim().trimOuterParentheses()
    val width = maxOf(numerator.length, denominator.length, 2) * 16 + 8
    return FractionParts(numerator = numerator, denominator = denominator, lineWidthDp = width.coerceIn(40, 280))
}

private fun String.indexOfTopLevelFractionSlash(): Int {
    var depth = 0
    forEachIndexed { index, char ->
        when (char) {
            '(' -> depth++
            ')' -> depth = maxOf(0, depth - 1)
            '⁄' -> if (depth == 0) return index
        }
    }
    return indexOf('⁄')
}

private fun String.toDisplayExpression(): String = replace("/", "÷").replace("⁄", "÷")

private fun String.toParserExpression(): String = replace('⁄', '/')

private fun String.trimOuterParentheses(): String {
    if (length < 2 || first() != '(' || last() != ')') return this
    var depth = 0
    for (index in indices) {
        when (this[index]) {
            '(' -> depth++
            ')' -> depth--
        }
        if (depth == 0 && index != lastIndex) return this
    }
    return substring(1, lastIndex)
}


@Composable
private fun CalculatorKeypad(
    modifier: Modifier = Modifier,
    shiftActive: Boolean,
    alphaActive: Boolean,
    onKey: (String) -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        buttonRows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    CalcButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        compact = rowIndex < 4,
                        shiftActive = shiftActive,
                        alphaActive = alphaActive,
                        style = when {
                            key == "SHIFT" && shiftActive -> KeyStyle.ShiftActive
                            key == "SHIFT" -> KeyStyle.Shift
                            key == "ALPHA" && alphaActive -> KeyStyle.AlphaActive
                            key == "ALPHA" -> KeyStyle.Alpha
                            rowIndex == 0 -> KeyStyle.Function
                            key in listOf("DEL", "AC") -> KeyStyle.Danger
                            key == "=" -> KeyStyle.Equals
                            key in listOf("+", "-", "×", "÷") -> KeyStyle.Operator
                            else -> KeyStyle.Standard
                        },
                        onClick = { onKey(key) }
                    )
                }
            }
            if (rowIndex == 3) {
                CursorPad(onKey = onKey)
            }
        }
    }
}

@Composable
private fun CursorPad(onKey: (String) -> Unit) {
    val cellWidth = 54.dp
    val cellHeight = 38.dp
    val gap = 6.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Row(
            modifier = Modifier.requiredWidth(cellWidth * 3 + gap * 2),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            Spacer(modifier = Modifier.width(cellWidth).height(cellHeight))
            CalcButton(
                key = "↑",
                modifier = Modifier.width(cellWidth),
                compact = true,
                shiftActive = false,
                alphaActive = false,
                style = KeyStyle.Cursor,
                onClick = { onKey("↑") }
            )
            Spacer(modifier = Modifier.width(cellWidth).height(cellHeight))
        }
        Row(
            modifier = Modifier.requiredWidth(cellWidth * 3 + gap * 2),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            listOf("←", "↓", "→").forEach { key ->
                CalcButton(
                    key = key,
                    modifier = Modifier.width(cellWidth),
                    compact = true,
                    shiftActive = false,
                    alphaActive = false,
                    style = KeyStyle.Cursor,
                    onClick = { onKey(key) }
                )
            }
        }
    }
}

private enum class KeyStyle { Standard, Function, Shift, ShiftActive, Alpha, AlphaActive, Cursor, Operator, Danger, Equals }

@Composable
private fun CalcButton(
    key: String,
    modifier: Modifier = Modifier,
    compact: Boolean,
    shiftActive: Boolean,
    alphaActive: Boolean,
    style: KeyStyle,
    onClick: () -> Unit
) {
    val colors = when (style) {
        KeyStyle.Standard -> ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3640), contentColor = Color.White)
        KeyStyle.Function -> ButtonDefaults.buttonColors(containerColor = Color(0xFF5F6B75), contentColor = Color.White)
        KeyStyle.Shift -> ButtonDefaults.buttonColors(containerColor = Color(0xFFC28A2E), contentColor = Color(0xFF17130B))
        KeyStyle.ShiftActive -> ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC857), contentColor = Color(0xFF17130B))
        KeyStyle.Alpha -> ButtonDefaults.buttonColors(containerColor = Color(0xFF9E4D67), contentColor = Color.White)
        KeyStyle.AlphaActive -> ButtonDefaults.buttonColors(containerColor = Color(0xFFE66B98), contentColor = Color.White)
        KeyStyle.Cursor -> ButtonDefaults.buttonColors(containerColor = Color(0xFF44515D), contentColor = Color.White)
        KeyStyle.Operator -> ButtonDefaults.buttonColors(containerColor = Color(0xFF234D6F), contentColor = Color.White)
        KeyStyle.Danger -> ButtonDefaults.buttonColors(containerColor = Color(0xFF8E433E), contentColor = Color.White)
        KeyStyle.Equals -> ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6F55), contentColor = Color.White)
    }
    val mainLabel = keyMainLabel(key, shiftActive, alphaActive)
    val subLabel = keySubLabel(key, shiftActive, alphaActive)
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(if (compact) 41.dp else 58.dp),
        shape = RoundedCornerShape(7.dp),
        colors = colors,
        contentPadding = PaddingValues(
            start = 2.dp,
            top = 2.dp,
            end = 2.dp,
            bottom = 2.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    fontSize = if (compact) 8.sp else 9.sp,
                    lineHeight = if (compact) 8.sp else 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = mainLabel,
                fontSize = when {
                    compact && mainLabel.length >= 5 -> 10.sp
                    compact -> 12.sp
                    mainLabel.length >= 4 -> 15.sp
                    else -> 20.sp
                },
                lineHeight = if (compact) 13.sp else 21.sp,
                fontWeight = if (compact) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun keyMainLabel(key: String, shiftActive: Boolean, alphaActive: Boolean): String = when {
    alphaActive -> alphaLabel(key) ?: keyBaseLabel(key)
    shiftActive -> shiftLabel(key) ?: keyBaseLabel(key)
    else -> keyBaseLabel(key)
}

private fun keyBaseLabel(key: String): String = when (key) {
    "a/b" -> "分数"
    "x^-1" -> "x⁻¹"
    "x^2" -> "x²"
    "sqrt" -> "√"
    "^" -> "xʸ"
    "(-)" -> "±"
    "°′″" -> "DMS"
    "S⇔D" -> "S↔D"
    "EXP" -> "×10ˣ"
    "←" -> "◀"
    "↑" -> "▲"
    "↓" -> "▼"
    "→" -> "▶"
    else -> key
}

private fun keySubLabel(key: String, shiftActive: Boolean, alphaActive: Boolean): String? = when {
    alphaActive && alphaLabel(key) != null -> "ALPHA"
    shiftActive && shiftLabel(key) != null -> "SHIFT"
    key in listOf("7", "8", "9", "4", "5", "6", "1", "2", "3", "0", ".") -> null
    key == "DEL" -> "消去"
    key == "AC" -> "全消去"
    key == "=" -> "実行"
    key in listOf("×", "÷", "+", "-") -> null
    key == "Ans" -> "前回値"
    key == "a/b" -> "横棒"
    else -> null
}

private fun shiftLabel(key: String): String? = when (key) {
    "sin" -> "asin"
    "cos" -> "acos"
    "tan" -> "atan"
    "log" -> "10ˣ"
    "ln" -> "eˣ"
    "x^2" -> "x³"
    "sqrt" -> "³√"
    "EXP" -> "π"
    "Ans" -> "e"
    "AC" -> "OFF"
    else -> null
}

private fun alphaLabel(key: String): String? = when (key) {
    "7" -> "A"
    "8" -> "B"
    "9" -> "C"
    "4" -> "D"
    "5" -> "E"
    "6" -> "F"
    "1" -> "x"
    "2" -> "y"
    "3" -> "z"
    "0" -> "π"
    "." -> "e"
    "Ans" -> "M"
    else -> null
}

@Composable
private fun FormulaProfiles(
    modifier: Modifier,
    selectedProfile: Int,
    onProfileSelected: (Int) -> Unit,
    onEvaluate: (Formula) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            engineeringProfiles.forEachIndexed { index, profile ->
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onProfileSelected(index) },
                    shape = RoundedCornerShape(7.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (index == selectedProfile) Color(0xFF10202D) else Color.Transparent,
                        contentColor = if (index == selectedProfile) Color.White else Color(0xFF10202D)
                    )
                ) {
                    Text(profile.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        FormulaList(
            formulas = engineeringProfiles[selectedProfile].formulas,
            onEvaluate = onEvaluate,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FormulaList(
    formulas: List<Formula>,
    onEvaluate: (Formula) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(formulas) { formula ->
            FormulaRow(
                formula = formula,
                onEvaluate = { onEvaluate(formula) }
            )
        }
    }
}

@Composable
private fun FormulaRow(
    formula: Formula,
    onEvaluate: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(formula.name, fontWeight = FontWeight.Bold, color = Color(0xFF10202D))
                    Text(
                        "${formula.category}  ${formula.expression}",
                        color = Color(0xFF55606A),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Text("変数: ${formula.variables.joinToString(", ")}", color = Color(0xFF6F7880), fontSize = 12.sp)
                }
                Spacer(Modifier.width(6.dp))
                SmallAction("計算", onEvaluate)
            }
        }
    }
}

@Composable
private fun SmallAction(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE7EDF2))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color(0xFF10202D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PresetEditor(
    modifier: Modifier,
    presets: List<Formula>,
    presetName: String,
    expression: String,
    onNameChange: (String) -> Unit,
    onExpressionChange: (String) -> Unit,
    onInsert: (Formula) -> Unit,
    onDelete: (Formula) -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = presetName,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("プリセット名") }
        )
        OutlinedTextField(
            value = expression,
            onValueChange = onExpressionChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("保存する公式 / 式") }
        )
        ElevatedButton(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6F55), contentColor = Color.White)
        ) {
            Text("現在の式をプリセット保存")
        }
        Text("保存済みプリセット", color = Color(0xFF10202D), fontWeight = FontWeight.Bold)
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { formula ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(formula.name, fontWeight = FontWeight.Bold)
                            Text(formula.expression, fontFamily = FontFamily.Monospace, color = Color(0xFF55606A))
                        }
                        SmallAction("挿入") { onInsert(formula) }
                        Spacer(Modifier.width(4.dp))
                        SmallAction("削除") { onDelete(formula) }
                    }
                }
            }
        }
    }
}

private fun handleKey(
    key: String,
    expressionValue: TextFieldValue,
    result: String,
    lastNumericResult: Double?,
    resultAsFraction: Boolean,
    angleMode: AngleMode,
    displayMode: DisplayMode,
    shiftActive: Boolean,
    alphaActive: Boolean,
    onExpressionValue: (TextFieldValue) -> Unit,
    onResult: (String) -> Unit,
    onNumericResult: (Double?) -> Unit,
    onResultAsFraction: (Boolean) -> Unit,
    onHistoryAdd: (CalculationHistory) -> Unit
) {
    val expression = expressionValue.text
    fun update(text: String, cursor: Int = text.length) {
        onExpressionValue(TextFieldValue(text, TextRange(cursor.coerceIn(0, text.length))))
    }
    fun insert(text: String, cursorOffset: Int = text.length) {
        val start = minOf(expressionValue.selection.start, expressionValue.selection.end).coerceIn(0, expression.length)
        val end = maxOf(expressionValue.selection.start, expressionValue.selection.end).coerceIn(0, expression.length)
        val next = expression.replaceRange(start, end, text)
        update(next, start + cursorOffset)
    }
    fun moveCursor(cursor: Int) {
        onExpressionValue(expressionValue.copy(selection = TextRange(cursor.coerceIn(0, expression.length))))
    }

    if (alphaActive && handleAlphaKey(key, { text, cursor -> insert(text, cursor) }, onResult)) return
    if (shiftActive && handleShiftKey(key, { text, cursor -> insert(text, cursor) }, { text, cursor -> update(text, cursor) }, onResult)) return

    when (key) {
        "AC" -> {
            update("", 0)
            onResult("0")
            onNumericResult(null)
            onResultAsFraction(false)
        }

        "DEL" -> {
            val start = minOf(expressionValue.selection.start, expressionValue.selection.end).coerceIn(0, expression.length)
            val end = maxOf(expressionValue.selection.start, expressionValue.selection.end).coerceIn(0, expression.length)
            when {
                start != end -> update(expression.removeRange(start, end), start)
                start > 0 -> update(expression.removeRange(start - 1, start), start - 1)
            }
        }

        "=" -> {
            val value = runCatching { ExpressionParser(expression.toParserExpression(), angleMode = angleMode).parse() }.getOrNull()
            if (value == null) {
                onResult("Error")
                onNumericResult(null)
                onResultAsFraction(false)
            } else {
                val formattedResult = value.formatAnswer(displayMode)
                onResult(formattedResult)
                onNumericResult(value)
                onHistoryAdd(
                    CalculationHistory(
                        expression = expression,
                        result = formattedResult,
                        numericResult = value
                    )
                )
            }
        }
        "←" -> moveCursor(expressionValue.selection.start - 1)
        "→" -> moveCursor(expressionValue.selection.end + 1)
        "↑" -> moveCursor(expression.numeratorCursor())
        "↓" -> moveCursor(expression.denominatorCursor())
        "a/b" -> insert("⁄", 0)
        "Ans" -> insert(result.takeIf { it != "Error" }.orEmpty())
        "×" -> insert("*")
        "÷" -> insert("/")
        "sqrt" -> insert("sqrt(")
        "sin", "cos", "tan", "log", "ln" -> insert("$key(")
        "x^2" -> insert("^2")
        "x^-1" -> insert("^-1")
        "EXP" -> insert("E")
        "(-)" -> insert("-")
        "S⇔D" -> {
            if (lastNumericResult == null) {
                onResult("Ready")
            } else if (resultAsFraction) {
                onResult(lastNumericResult.formatAnswer(displayMode))
                onResultAsFraction(false)
            } else {
                onResult(lastNumericResult.toFractionString() ?: lastNumericResult.formatAnswer(displayMode))
                onResultAsFraction(true)
            }
        }
        "°′″", "hyp", "RCL", "ENG", "M+" -> onResult("Ready")
        else -> insert(key)
    }
}

private fun handleShiftKey(
    key: String,
    insert: (String, Int) -> Unit,
    update: (String, Int) -> Unit,
    onResult: (String) -> Unit
): Boolean {
    when (key) {
        "sin" -> insert("asin(", 5)
        "cos" -> insert("acos(", 5)
        "tan" -> insert("atan(", 5)
        "log" -> insert("10^(", 4)
        "ln" -> insert("e^(", 3)
        "x^2" -> insert("^3", 2)
        "sqrt" -> insert("^(1/3)", 6)
        "EXP" -> insert("pi", 2)
        "Ans" -> insert("e", 1)
        "AC" -> {
            update("", 0)
            onResult("OFF")
        }
        else -> return false
    }
    return true
}

private fun handleAlphaKey(
    key: String,
    insert: (String, Int) -> Unit,
    onResult: (String) -> Unit
): Boolean {
    val value = when (key) {
        "7" -> "A"
        "8" -> "B"
        "9" -> "C"
        "4" -> "D"
        "5" -> "E"
        "6" -> "F"
        "1" -> "x"
        "2" -> "y"
        "3" -> "z"
        "0" -> "pi"
        "." -> "e"
        "Ans" -> "M"
        else -> return false
    }
    insert(value, value.length)
    onResult("ALPHA")
    return true
}

private fun String.consumesModifier(): Boolean = this !in listOf("←", "→", "↑", "↓")

private fun nextHistoryIndex(key: String, currentIndex: Int, historySize: Int): Int {
    if (historySize == 0) return -1
    return when (key) {
        "↑" -> if (currentIndex == -1) historySize - 1 else (currentIndex - 1).coerceAtLeast(0)
        "↓" -> if (currentIndex == -1) historySize - 1 else (currentIndex + 1).coerceAtMost(historySize - 1)
        else -> -1
    }
}

private fun String.asExpressionValue(): TextFieldValue = TextFieldValue(this, TextRange(length))

private fun String.numeratorCursor(): Int {
    val slashIndex = indexOfTopLevelFractionSlash()
    return if (slashIndex == -1) 0 else 0
}

private fun String.denominatorCursor(): Int {
    val slashIndex = indexOfTopLevelFractionSlash()
    return if (slashIndex == -1) length else slashIndex + 1
}

private fun Double.formatAnswer(displayMode: DisplayMode = DisplayMode.Normal): String {
    if (!isFinite()) return "Error"
    if (abs(this) < 0.0000000001) return "0"
    if (displayMode == DisplayMode.Fix2) return String.format(Locale.US, "%.2f", this)
    if (displayMode == DisplayMode.Fix4) return String.format(Locale.US, "%.4f", this)
    val rounded = if (abs(this - toLong()) < 0.0000000001) toLong().toString() else String.format(Locale.US, "%.10g", this)
    return rounded.removeSuffix(".0")
}

private fun Double.toFractionString(maxDenominator: Int = 10000): String? {
    if (!isFinite()) return null
    val sign = if (this < 0) "-" else ""
    val target = abs(this)
    val integerPart = target.toLong()
    val fractional = target - integerPart
    if (fractional < 0.0000000001) return "${sign}${integerPart}"

    var bestNumerator = 0L
    var bestDenominator = 1L
    var bestError = Double.MAX_VALUE
    for (denominator in 1..maxDenominator) {
        val numerator = kotlin.math.round(fractional * denominator).toLong()
        val value = numerator.toDouble() / denominator
        val error = abs(value - fractional)
        if (error < bestError) {
            bestNumerator = numerator
            bestDenominator = denominator.toLong()
            bestError = error
        }
        if (error < 0.000000001) break
    }

    if (bestNumerator == 0L) return "${sign}${integerPart}"
    val gcd = gcd(bestNumerator, bestDenominator)
    val numerator = bestNumerator / gcd
    val denominator = bestDenominator / gcd
    return if (integerPart == 0L) {
        "$sign$numerator/$denominator"
    } else {
        "$sign$integerPart $numerator/$denominator"
    }
}

private tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a).coerceAtLeast(1L) else gcd(b, a % b)

private class ExpressionParser(
    private val source: String,
    private val variables: Map<String, Double> = emptyMap(),
    private val angleMode: AngleMode = AngleMode.Rad
) {
    private var pos = 0

    fun parse(): Double {
        val value = parseExpression()
        skipSpaces()
        require(pos == source.length)
        return value
    }

    private fun parseExpression(): Double {
        var value = parseTerm()
        while (true) {
            skipSpaces()
            value = when {
                match('+') -> value + parseTerm()
                match('-') -> value - parseTerm()
                else -> return value
            }
        }
    }

    private fun parseTerm(): Double {
        var value = parsePower()
        while (true) {
            skipSpaces()
            value = when {
                match('*') -> value * parsePower()
                match('/') -> value / parsePower()
                else -> return value
            }
        }
    }

    private fun parsePower(): Double {
        var value = parseUnary()
        skipSpaces()
        if (match('^')) value = value.pow(parsePower())
        return value
    }

    private fun parseUnary(): Double {
        skipSpaces()
        return when {
            match('+') -> parseUnary()
            match('-') -> -parseUnary()
            else -> parsePrimary()
        }
    }

    private fun parsePrimary(): Double {
        skipSpaces()
        if (match('(')) {
            val value = parseExpression()
            require(match(')'))
            return value
        }
        if (peek()?.isDigit() == true || peek() == '.') return parseNumber()
        if (peek()?.isLetter() == true || peek() == 'π') return parseIdentifier()
        error("Unexpected token")
    }

    private fun parseNumber(): Double {
        val start = pos
        while (peek()?.isDigit() == true || peek() == '.') pos++
        if (peek() == 'E' || peek() == 'e') {
            pos++
            if (peek() == '+' || peek() == '-') pos++
            while (peek()?.isDigit() == true) pos++
        }
        return source.substring(start, pos).toDouble()
    }

    private fun parseIdentifier(): Double {
        val start = pos
        while (peek()?.isLetterOrDigit() == true || peek() == '_' || peek() == 'π') pos++
        val name = source.substring(start, pos)
        skipSpaces()
        if (match('(')) {
            val value = parseExpression()
            require(match(')'))
            return when (name.lowercase(Locale.US)) {
                "sin" -> sin(value.toRadiansIfNeeded())
                "cos" -> cos(value.toRadiansIfNeeded())
                "tan" -> tan(value.toRadiansIfNeeded())
                "asin" -> asin(value).fromRadiansIfNeeded()
                "acos" -> acos(value).fromRadiansIfNeeded()
                "atan" -> atan(value).fromRadiansIfNeeded()
                "sqrt" -> sqrt(value)
                "log" -> log10(value)
                "ln" -> ln(value)
                else -> error("Unknown function")
            }
        }
        return when (name.lowercase(Locale.US)) {
            "pi", "π" -> PI
            "e" -> E
            else -> variables[name] ?: error("Unknown variable")
        }
    }

    private fun skipSpaces() {
        while (peek()?.isWhitespace() == true) pos++
    }

    private fun match(char: Char): Boolean {
        skipSpaces()
        if (peek() != char) return false
        pos++
        return true
    }

    private fun peek(): Char? = source.getOrNull(pos)

    private fun Double.toRadiansIfNeeded(): Double = when (angleMode) {
        AngleMode.Rad -> this
        AngleMode.Deg -> Math.toRadians(this)
    }

    private fun Double.fromRadiansIfNeeded(): Double = when (angleMode) {
        AngleMode.Rad -> this
        AngleMode.Deg -> Math.toDegrees(this)
    }
}

private fun extractVariables(expression: String): List<String> {
    val reserved = setOf("sin", "cos", "tan", "asin", "acos", "atan", "sqrt", "log", "ln", "pi", "e", "E")
    return Regex("[A-Za-z_][A-Za-z0-9_]*")
        .findAll(expression)
        .map { it.value }
        .filter { it !in reserved }
        .distinct()
        .toList()
}

private fun loadPresets(context: Context): List<Formula> {
    val raw = context.getSharedPreferences("formula_presets", Context.MODE_PRIVATE).getString("items", "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return raw.split('\n').mapNotNull { line ->
        val parts = line.split('|')
        if (parts.size < 3) return@mapNotNull null
        Formula(
            name = parts[0].unescapePreset(),
            expression = parts[1].unescapePreset(),
            variables = parts[2].split(',').filter { it.isNotBlank() },
            category = "ユーザー"
        )
    }
}

private fun savePresets(context: Context, presets: List<Formula>) {
    val raw = presets.joinToString("\n") { formula ->
        listOf(
            formula.name.escapePreset(),
            formula.expression.escapePreset(),
            formula.variables.joinToString(",").escapePreset()
        ).joinToString("|")
    }
    context.getSharedPreferences("formula_presets", Context.MODE_PRIVATE)
        .edit()
        .putString("items", raw)
        .apply()
}

private fun String.escapePreset(): String = replace("\\", "\\\\").replace("|", "\\p").replace("\n", "\\n")

private fun String.unescapePreset(): String {
    val builder = StringBuilder()
    var escaped = false
    for (char in this) {
        if (escaped) {
            builder.append(
                when (char) {
                    'p' -> '|'
                    'n' -> '\n'
                    '\\' -> '\\'
                    else -> char
                }
            )
            escaped = false
        } else if (char == '\\') {
            escaped = true
        } else {
            builder.append(char)
        }
    }
    if (escaped) builder.append('\\')
    return builder.toString()
}

@Preview(showBackground = true)
@Composable
private fun CalculatorPreview() {
    CalculatorTheme(dynamicColor = false) {
        EngineeringCalculatorApp()
    }
}
