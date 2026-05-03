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
    var showResult by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) }
    var selectedProfile by remember { mutableIntStateOf(0) }
    var presetName by remember { mutableStateOf("") }
    var secretVisible by remember { mutableStateOf(false) }
    var dismissedSecretFor by remember { mutableStateOf("") }
    val presets = remember { mutableStateListOf<Formula>().apply { addAll(loadPresets(context)) } }
    val expression = expressionValue.text

    LaunchedEffect(expression) {
        if (expression == "114514" && dismissedSecretFor != expression) {
            secretVisible = true
        } else if (expression != "114514") {
            secretVisible = false
            dismissedSecretFor = ""
        }
    }

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
                    showResult = showResult
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
                        onKey = { key ->
                            showResult = key == "="
                            handleKey(
                                key = key,
                                expressionValue = expressionValue,
                                result = result,
                                onExpressionValue = { expressionValue = it },
                                onResult = { result = it }
                            )
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
                    dismissedSecretFor = expression
                }
            )
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
    showResult: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var cursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(expressionValue.text, expressionValue.selection) {
        cursorVisible = true
        while (true) {
            delay(500)
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
                Text(
                    text = result,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd),
                    color = Color(0xFF061006),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
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
            text = expression,
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
                text = fraction.numerator.ifBlank { " " },
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
                text = fraction.denominator.ifBlank { " " },
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

private fun String.toFractionParts(): FractionParts? {
    val slashIndex = indexOfTopLevelSlash()
    if (slashIndex == -1) return null
    val numerator = substring(0, slashIndex).trim().trimOuterParentheses()
    val denominator = substring(slashIndex + 1).trim().trimOuterParentheses()
    val width = maxOf(numerator.length, denominator.length, 2) * 16 + 8
    return FractionParts(numerator = numerator, denominator = denominator, lineWidthDp = width.coerceIn(40, 280))
}

private fun String.indexOfTopLevelSlash(): Int {
    var depth = 0
    forEachIndexed { index, char ->
        when (char) {
            '(' -> depth++
            ')' -> depth = maxOf(0, depth - 1)
            '/' -> if (depth == 0) return index
        }
    }
    return indexOf('/')
}

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
private fun CalculatorKeypad(modifier: Modifier = Modifier, onKey: (String) -> Unit) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        style = when {
                            key == "SHIFT" -> KeyStyle.Shift
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
    val cellHeight = 36.dp
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
                    style = KeyStyle.Cursor,
                    onClick = { onKey(key) }
                )
            }
        }
    }
}

private enum class KeyStyle { Standard, Function, Shift, Alpha, Cursor, Operator, Danger, Equals }

@Composable
private fun CalcButton(
    key: String,
    modifier: Modifier = Modifier,
    compact: Boolean,
    style: KeyStyle,
    onClick: () -> Unit
) {
    val colors = when (style) {
        KeyStyle.Standard -> ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3640), contentColor = Color.White)
        KeyStyle.Function -> ButtonDefaults.buttonColors(containerColor = Color(0xFF5F6B75), contentColor = Color.White)
        KeyStyle.Shift -> ButtonDefaults.buttonColors(containerColor = Color(0xFFC28A2E), contentColor = Color(0xFF17130B))
        KeyStyle.Alpha -> ButtonDefaults.buttonColors(containerColor = Color(0xFF9E4D67), contentColor = Color.White)
        KeyStyle.Cursor -> ButtonDefaults.buttonColors(containerColor = Color(0xFF44515D), contentColor = Color.White)
        KeyStyle.Operator -> ButtonDefaults.buttonColors(containerColor = Color(0xFF234D6F), contentColor = Color.White)
        KeyStyle.Danger -> ButtonDefaults.buttonColors(containerColor = Color(0xFF8E433E), contentColor = Color.White)
        KeyStyle.Equals -> ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6F55), contentColor = Color.White)
    }
    val mainLabel = keyMainLabel(key)
    val subLabel = keySubLabel(key)
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(if (compact) 39.dp else 50.dp),
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
            if (subLabel != null && !compact) {
                Text(
                    text = subLabel,
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
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

private fun keyMainLabel(key: String): String = when (key) {
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

private fun keySubLabel(key: String): String? = when (key) {
    "7", "8", "9", "4", "5", "6", "1", "2", "3", "0", "." -> null
    "DEL" -> "消去"
    "AC" -> "全消去"
    "=" -> "実行"
    "×", "÷", "+", "-" -> null
    "Ans" -> "前回値"
    "a/b" -> "横棒"
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
    onExpressionValue: (TextFieldValue) -> Unit,
    onResult: (String) -> Unit
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

    when (key) {
        "AC", "ON" -> {
            update("", 0)
            onResult("0")
        }

        "DEL" -> {
            val start = minOf(expressionValue.selection.start, expressionValue.selection.end).coerceIn(0, expression.length)
            val end = maxOf(expressionValue.selection.start, expressionValue.selection.end).coerceIn(0, expression.length)
            when {
                start != end -> update(expression.removeRange(start, end), start)
                start > 0 -> update(expression.removeRange(start - 1, start), start - 1)
            }
        }

        "=" -> onResult(runCatching { ExpressionParser(expression).parse().formatAnswer() }.getOrElse { "Error" })
        "←" -> moveCursor(expressionValue.selection.start - 1)
        "→" -> moveCursor(expressionValue.selection.end + 1)
        "↑" -> moveCursor(expression.numeratorCursor())
        "↓" -> moveCursor(expression.denominatorCursor())
        "a/b" -> insert("/", 0)
        "Ans" -> insert(result.takeIf { it != "Error" }.orEmpty())
        "×" -> insert("*")
        "÷" -> insert("/")
        "sqrt" -> insert("sqrt(")
        "sin", "cos", "tan", "log", "ln" -> insert("$key(")
        "x^2" -> insert("^2")
        "x^-1" -> insert("^-1")
        "EXP" -> insert("E")
        "(-)" -> insert("-")
        "SHIFT", "ALPHA", "MODE", "SETUP", "°′″", "hyp", "RCL", "ENG", "S⇔D", "M+" -> onResult("Ready")
        else -> insert(key)
    }
}

private fun String.asExpressionValue(): TextFieldValue = TextFieldValue(this, TextRange(length))

private fun String.numeratorCursor(): Int {
    val slashIndex = indexOfTopLevelSlash()
    return if (slashIndex == -1) 0 else 0
}

private fun String.denominatorCursor(): Int {
    val slashIndex = indexOfTopLevelSlash()
    return if (slashIndex == -1) length else slashIndex + 1
}

private fun Double.formatAnswer(): String {
    if (!isFinite()) return "Error"
    if (abs(this) < 0.0000000001) return "0"
    val rounded = if (abs(this - toLong()) < 0.0000000001) toLong().toString() else String.format(Locale.US, "%.10g", this)
    return rounded.removeSuffix(".0")
}

private class ExpressionParser(
    private val source: String,
    private val variables: Map<String, Double> = emptyMap()
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
                "sin" -> sin(value)
                "cos" -> cos(value)
                "tan" -> tan(value)
                "asin" -> asin(value)
                "acos" -> acos(value)
                "atan" -> atan(value)
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
