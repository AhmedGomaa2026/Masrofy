package com.masrofy.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {
    private val green = Color.rgb(8, 127, 110)
    private val dark = Color.rgb(7, 90, 80)
    private val bg = Color.rgb(245, 250, 248)
    private val text = Color.rgb(23, 59, 55)
    private val muted = Color.rgb(100, 116, 113)
    private val danger = Color.rgb(210, 67, 91)
    private val income = Color.rgb(20, 145, 111)
    private val gold = Color.rgb(205, 151, 48)
    private val white = Color.WHITE
    private val prefs by lazy { getSharedPreferences("masrofy", Context.MODE_PRIVATE) }
    private val money = java.text.NumberFormat.getNumberInstance(Locale("ar", "EG"))
    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale("ar", "EG"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun base(title: String): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        val bar = LinearLayout(this).apply {
            setBackgroundColor(dark)
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(8, 4, 8, 4)
        }
        bar.addView(tv(title, 21f, white, true), LinearLayout.LayoutParams(0, 62, 1f))
        if (title != "مصروفي") {
            val back = Button(this).apply {
                text = "رجوع"
                textSize = 12f
                setTextColor(white)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { showHome() }
            }
            bar.addView(back, LinearLayout.LayoutParams(74, 58))
        }
        root.addView(bar)
        return root
    }

    private fun content(root: LinearLayout): LinearLayout {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 18)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        scroll.addView(box)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return box
    }

    private fun tv(s: String, size: Float, color: Int = text, bold: Boolean = false): TextView = TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        setPadding(12, 10, 12, 10)
        layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    private fun button(s: String, on: () -> Unit): Button = Button(this).apply {
        text = s
        setTextColor(white)
        textSize = 14f
        setBackgroundColor(green)
        setOnClickListener { on() }
        layoutParams = LinearLayout.LayoutParams(-1, 54).apply { setMargins(0, 7, 0, 7) }
    }

    private fun outlineButton(s: String, on: () -> Unit): Button = Button(this).apply {
        text = s
        setTextColor(green)
        textSize = 14f
        setBackgroundColor(white)
        setOnClickListener { on() }
        layoutParams = LinearLayout.LayoutParams(-1, 54).apply { setMargins(0, 7, 0, 7) }
    }

    private fun section(box: LinearLayout, title: String) {
        box.addView(tv(title, 19f, text, true).apply { setPadding(4, 14, 4, 6) })
    }

    private fun card(label: String, value: String, color: Int): TextView = tv("$label\n$value", 16f, color, true).apply {
        setBackgroundColor(white)
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(0, 96, 1f).apply { setMargins(4, 4, 4, 4) }
    }

    private fun data(key: String): JSONArray = try {
        JSONArray(prefs.getString(key, "[]"))
    } catch (_: Exception) { JSONArray() }

    private fun save(key: String, array: JSONArray) {
        prefs.edit().putString(key, array.toString()).apply()
    }

    private fun tx(): JSONArray = data("tx")
    private fun goals(): JSONArray = data("goals")
    private fun debts(): JSONArray = data("debts")
    private fun budgets(): JSONArray = data("budgets")

    private fun totalsCurrentMonth(): Triple<Double, Double, Double> {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        var inc = 0.0
        var exp = 0.0
        val a = tx()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            val c = Calendar.getInstance().apply { timeInMillis = o.optLong("time", 0L) }
            if (c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year) {
                if (o.optString("type") == "income") inc += o.optDouble("amount", 0.0)
                else exp += o.optDouble("amount", 0.0)
            }
        }
        return Triple(inc, exp, inc - exp)
    }

    private fun addTx(type: String, amount: Double, category: String, note: String) {
        val a = tx()
        a.put(JSONObject().apply {
            put("type", type)
            put("amount", amount)
            put("category", category)
            put("note", note)
            put("time", System.currentTimeMillis())
        })
        save("tx", a)
    }

    private fun showHome() {
        val root = base("مصروفي")
        val box = content(root)
        val t = totalsCurrentMonth()
        box.addView(tv("مساعدك المالي الذكي", 16f, muted, false))
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutDirection = View.LAYOUT_DIRECTION_RTL }
        row.addView(card("الدخل هذا الشهر", money.format(t.first) + " ج.م", income))
        row.addView(card("المصروف", money.format(t.second) + " ج.م", danger))
        row.addView(card("المتبقي", money.format(t.third) + " ج.م", green))
        box.addView(row)

        val percent = if (t.first > 0) min(100, (t.second / t.first * 100).toInt()) else 0
        box.addView(tv("نسبة الإنفاق من الدخل: $percent٪", 16f, green, true))
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100; this.progress = percent
        }
        box.addView(progress, LinearLayout.LayoutParams(-1, 18).apply { setMargins(4, 0, 4, 10) })

        section(box, "إجراءات سريعة")
        box.addView(button("＋ إضافة مصروف") { showAdd("expense") })
        box.addView(button("＋ إضافة دخل") { showAdd("income") })

        section(box, "إدارة أموالك")
        box.addView(outlineButton("📋 كل العمليات") { showTransactions() })
        box.addView(outlineButton("📊 التقارير والتحليل") { showReports() })
        box.addView(outlineButton("🎯 الأهداف المالية") { showGoals() })
        box.addView(outlineButton("💳 الديون والأقساط") { showDebts() })
        box.addView(outlineButton("💰 الميزانيات") { showBudgets() })
        box.addView(outlineButton("🤖 المساعد الذكي") { showAssistant() })

        section(box, "آخر العمليات")
        val a = tx()
        if (a.length == 0) box.addView(tv("لسه مفيش عمليات. أضف أول دخل أو مصروف.", 15f, muted))
        else {
            val start = max(0, a.length - 5)
            for (i in (start until a.length).reversed()) box.addView(txRow(a.getJSONObject(i), null))
        }
        setContentView(root)
    }

    private fun input(hint: String, number: Boolean = false): EditText = EditText(this).apply {
        this.hint = hint
        setPadding(16, 6, 16, 6)
        if (number) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layoutParams = LinearLayout.LayoutParams(-1, 56).apply { setMargins(0, 5, 0, 5) }
    }

    private fun showAdd(type: String) {
        val root = base(if (type == "income") "إضافة دخل" else "إضافة مصروف")
        val box = content(root)
        box.addView(tv(if (type == "income") "سجّل مصدر الدخل" else "سجّل المصروف", 20f, green, true))
        val amount = input("المبلغ بالجنيه", true)
        val cat = input(if (type == "income") "المصدر (مرتب، عمل إضافي...)" else "الفئة (أكل، مواصلات، فواتير...)")
        val note = input("ملاحظة اختيارية")
        box.addView(amount); box.addView(cat); box.addView(note)
        box.addView(button("حفظ العملية") {
            val v = amount.text.toString().replace(",", ".").toDoubleOrNull()
            if (v == null || v <= 0) {
                Toast.makeText(this, "اكتب مبلغ صحيح", Toast.LENGTH_SHORT).show(); return@button
            }
            addTx(type, v, if (cat.text.isBlank()) "عام" else cat.text.toString().trim(), note.text.toString().trim())
            Toast.makeText(this, "تم حفظ العملية", Toast.LENGTH_SHORT).show()
            showHome()
        })
        box.addView(outlineButton("إلغاء") { showHome() })
        setContentView(root)
    }

    private fun txRow(o: JSONObject, parent: LinearLayout?): LinearLayout {
        val isIncome = o.optString("type") == "income"
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(white)
            setPadding(8, 5, 8, 5)
            layoutParams = LinearLayout.LayoutParams(-1, 70).apply { setMargins(0, 3, 0, 3) }
        }
        val details = tv("${o.optString("category")}\n${o.optString("note")}\n${dateFmt.format(Date(o.optLong("time")))}", 13f, text, true)
        row.addView(details, LinearLayout.LayoutParams(0, -1, 1f))
        row.addView(tv((if (isIncome) "+" else "-") + " " + money.format(o.optDouble("amount")) + " ج.م", 15f, if (isIncome) income else danger, true), LinearLayout.LayoutParams(135, -1))
        val del = TextView(this).apply {
            text = "×"; textSize = 24f; gravity = Gravity.CENTER; setTextColor(danger)
            setOnClickListener {
                if (parent != null) {
                    val a = tx(); for (i in a.length() - 1 downTo 0) if (a.getJSONObject(i).optLong("time") == o.optLong("time")) { a.remove(i); break }
                    save("tx", a); parent.removeView(row)
                    Toast.makeText(this@MainActivity, "تم حذف العملية", Toast.LENGTH_SHORT).show()
                }
            }
        }
        row.addView(del, LinearLayout.LayoutParams(38, -1))
        return row
    }

    private fun showTransactions() {
        val root = base("كل العمليات")
        val box = content(root)
        val a = tx()
        if (a.length == 0) box.addView(tv("لا توجد عمليات حتى الآن.", 16f, muted))
        else for (i in (0 until a.length).reversed()) box.addView(txRow(a.getJSONObject(i), box))
        box.addView(button("＋ إضافة مصروف") { showAdd("expense") })
        box.addView(button("＋ إضافة دخل") { showAdd("income") })
        setContentView(root)
    }

    private fun showReports() {
        val root = base("التقارير والتحليل")
        val box = content(root)
        val t = totalsCurrentMonth()
        box.addView(tv("هذا الشهر", 21f, green, true))
        box.addView(tv("الدخل: ${money.format(t.first)} ج.م", 17f, income, true))
        box.addView(tv("المصروفات: ${money.format(t.second)} ج.م", 17f, danger, true))
        box.addView(tv("المتبقي: ${money.format(t.third)} ج.م", 17f, green, true))
        section(box, "أكثر فئات المصروفات")
        val map = mutableMapOf<String, Double>()
        val cal = Calendar.getInstance(); val month = cal.get(Calendar.MONTH); val year = cal.get(Calendar.YEAR)
        val a = tx()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            val d = Calendar.getInstance().apply { timeInMillis = o.optLong("time") }
            if (o.optString("type") == "expense" && d.get(Calendar.MONTH) == month && d.get(Calendar.YEAR) == year) {
                val c = o.optString("category", "عام"); map[c] = (map[c] ?: 0.0) + o.optDouble("amount")
            }
        }
        if (map.isEmpty()) box.addView(tv("أضف مصروفات علشان يظهر التحليل.", 15f, muted))
        else {
            val maxValue = map.values.maxOrNull() ?: 1.0
            map.entries.sortedByDescending { it.value }.forEach {
                val p = (it.value / maxValue * 100).toInt()
                box.addView(tv("${it.key}: ${money.format(it.value)} ج.م — $p٪ من أعلى فئة", 15f, text, true))
            }
        }
        box.addView(outlineButton("رجوع") { showHome() })
        setContentView(root)
    }

    private fun showGoals() {
        val root = base("الأهداف المالية")
        val box = content(root)
        box.addView(tv("حدد هدفًا وراقب تقدّمك", 18f, green, true))
        val a = goals()
        if (a.length == 0) box.addView(tv("مفيش أهداف لسه.", 15f, muted))
        for (i in 0 until a.length) {
            val o = a.getJSONObject(i)
            val target = o.optDouble("target"); val saved = o.optDouble("saved")
            val pct = if (target > 0) min(100, (saved / target * 100).toInt()) else 0
            box.addView(tv("${o.optString("name")}\n${money.format(saved)} من ${money.format(target)} ج.م — $pct٪", 16f, text, true))
            val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100; progress = pct }
            box.addView(pb, LinearLayout.LayoutParams(-1, 18))
            box.addView(outlineButton("إضافة مبلغ للهدف") { addToGoal(i) })
        }
        box.addView(button("＋ هدف جديد") { addGoal() })
        setContentView(root)
    }

    private fun addGoal() {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 10, 24, 10) }
        val name = input("اسم الهدف"); val target = input("المبلغ المستهدف", true)
        layout.addView(name); layout.addView(target)
        AlertDialog.Builder(this).setTitle("هدف جديد").setView(layout).setPositiveButton("حفظ") { _, _ ->
            val v = target.text.toString().toDoubleOrNull()
            if (name.text.isNotBlank() && v != null && v > 0) {
                val a = goals(); a.put(JSONObject().apply { put("name", name.text.toString().trim()); put("target", v); put("saved", 0.0) }); save("goals", a); showGoals()
            }
        }.setNegativeButton("إلغاء", null).show()
    }

    private fun addToGoal(index: Int) {
        val e = EditText(this).apply { hint = "المبلغ"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setPadding(20, 10, 20, 10) }
        AlertDialog.Builder(this).setTitle("إضافة للهدف").setView(e).setPositiveButton("إضافة") { _, _ ->
            val v = e.text.toString().toDoubleOrNull(); if (v != null && v > 0) { val a = goals(); val o = a.getJSONObject(index); o.put("saved", o.optDouble("saved") + v); save("goals", a); showGoals() }
        }.setNegativeButton("إلغاء", null).show()
    }

    private fun showDebts() {
        val root = base("الديون والأقساط")
        val box = content(root)
        val a = debts()
        if (a.length == 0) box.addView(tv("سجّل الديون والأقساط علشان تتابعها.", 15f, muted))
        for (i in 0 until a.length) {
            val o = a.getJSONObject(i)
            val total = o.optDouble("total"); val paid = o.optDouble("paid"); val remain = max(0.0, total - paid)
            box.addView(tv("${o.optString("name")}\nإجمالي: ${money.format(total)} ج.م — مدفوع: ${money.format(paid)} — متبقي: ${money.format(remain)} ج.م", 15f, text, true))
            box.addView(outlineButton("تسجيل دفعة") { payDebt(i) })
        }
        box.addView(button("＋ إضافة دين / قسط") { addDebt() })
        setContentView(root)
    }

    private fun addDebt() {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 10, 24, 10) }
        val name = input("اسم الدين أو القسط"); val total = input("إجمالي المبلغ", true)
        layout.addView(name); layout.addView(total)
        AlertDialog.Builder(this).setTitle("إضافة دين").setView(layout).setPositiveButton("حفظ") { _, _ ->
            val v = total.text.toString().toDoubleOrNull(); if (name.text.isNotBlank() && v != null && v > 0) { val a = debts(); a.put(JSONObject().apply { put("name", name.text.toString().trim()); put("total", v); put("paid", 0.0) }); save("debts", a); showDebts() }
        }.setNegativeButton("إلغاء", null).show()
    }

    private fun payDebt(index: Int) {
        val e = EditText(this).apply { hint = "قيمة الدفعة"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL; setPadding(20, 10, 20, 10) }
        AlertDialog.Builder(this).setTitle("تسجيل دفعة").setView(e).setPositiveButton("حفظ") { _, _ ->
            val v = e.text.toString().toDoubleOrNull(); if (v != null && v > 0) { val a = debts(); val o = a.getJSONObject(index); o.put("paid", min(o.optDouble("total"), o.optDouble("paid") + v)); save("debts", a); showDebts() }
        }.setNegativeButton("إلغاء", null).show()
    }

    private fun showBudgets() {
        val root = base("الميزانيات")
        val box = content(root)
        box.addView(tv("حدد سقفًا لكل فئة من مصروفاتك", 17f, green, true))
        val a = budgets(); val t = tx(); val spent = mutableMapOf<String, Double>()
        for (i in 0 until t.length()) { val o=t.getJSONObject(i); if(o.optString("type")=="expense") { val c=o.optString("category"); spent[c]=(spent[c]?:0.0)+o.optDouble("amount") } }
        if (a.length == 0) box.addView(tv("مفيش ميزانيات محددة.", 15f, muted))
        for (i in 0 until a.length) {
            val o=a.getJSONObject(i); val cat=o.optString("category"); val limit=o.optDouble("limit"); val s=spent[cat]?:0.0; val pct=if(limit>0) min(100,(s/limit*100).toInt()) else 0
            box.addView(tv("$cat\nمصروف: ${money.format(s)} / ${money.format(limit)} ج.م — $pct٪",15f,if(s>limit) danger else text,true))
        }
        box.addView(button("＋ إضافة ميزانية") { addBudget() })
        setContentView(root)
    }

    private fun addBudget() {
        val layout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,10,24,10)}
        val cat=input("الفئة (أكل، مواصلات...)"); val limit=input("الحد الشهري",true); layout.addView(cat);layout.addView(limit)
        AlertDialog.Builder(this).setTitle("ميزانية جديدة").setView(layout).setPositiveButton("حفظ"){_,_-> val v=limit.text.toString().toDoubleOrNull();if(cat.text.isNotBlank()&&v!=null&&v>0){val a=budgets();a.put(JSONObject().apply{put("category",cat.text.toString().trim());put("limit",v)});save("budgets",a);showBudgets()}}.setNegativeButton("إلغاء",null).show()
    }

    private fun showAssistant() {
        val root=base("المساعد الذكي"); val box=content(root)
        box.addView(tv("🤖 اكتب العملية بطريقتك",20f,green,true))
        box.addView(tv("مثال: قبضت 12000 وصرفت 3500 إيجار و800 مواصلات",15f,muted))
        val e=input("اكتب هنا..."); box.addView(e)
        box.addView(button("تحليل النص") {
            val s=e.text.toString(); val nums=Regex("\\d+(?:[.,]\\d+)?").findAll(s).map{it.value.replace(",",".").toDoubleOrNull()?:0.0}.toList()
            val answer=when {
                nums.size>=2 && (s.contains("قبض")||s.contains("دخل")) -> "لقيت أرقام في النص. في النسخة الحالية هسجّل العمليات يدويًا للتأكد من صحتها."
                nums.isNotEmpty() -> "لقيت مبلغ ${money.format(nums.first())} ج.م. اختار إضافة دخل أو مصروف من الصفحة الرئيسية."
                else -> "اكتب مبلغ واضح، مثل: صرفت 250 أكل."
            }
            box.addView(tv(answer,16f,green,true))
        })
        box.addView(tv("ملاحظة: الذكاء الاصطناعي الحقيقي وربطه بخدمة سحابية هيتم في مرحلة لاحقة، بعد إضافة الحسابات وحماية البيانات.",14f,muted))
        setContentView(root)
    }
}
